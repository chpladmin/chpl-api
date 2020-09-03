package gov.healthit.chpl.manager.auth;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.acls.domain.BasePermission;
import org.springframework.security.acls.domain.ObjectIdentityImpl;
import org.springframework.security.acls.domain.PrincipalSid;
import org.springframework.security.acls.model.MutableAcl;
import org.springframework.security.acls.model.MutableAclService;
import org.springframework.security.acls.model.NotFoundException;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.security.acls.model.Permission;
import org.springframework.security.acls.model.Sid;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.nulabinc.zxcvbn.Strength;
import com.nulabinc.zxcvbn.Zxcvbn;

import gov.healthit.chpl.dao.auth.UserDAO;
import gov.healthit.chpl.dao.auth.UserResetTokenDAO;
import gov.healthit.chpl.domain.activity.ActivityConcept;
import gov.healthit.chpl.domain.auth.User;
import gov.healthit.chpl.dto.auth.UserDTO;
import gov.healthit.chpl.dto.auth.UserResetTokenDTO;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.UserCreationException;
import gov.healthit.chpl.exception.UserManagementException;
import gov.healthit.chpl.exception.UserPermissionRetrievalException;
import gov.healthit.chpl.exception.UserRetrievalException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.manager.ActivityManager;
import gov.healthit.chpl.manager.impl.SecuredManager;
import gov.healthit.chpl.util.EmailBuilder;
import gov.healthit.chpl.util.ErrorMessageUtil;
import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class UserManager extends SecuredManager {
    public static final int MIN_PASSWORD_STRENGTH = 3;

    private Environment env;
    private UserDAO userDAO;
    private UserResetTokenDAO userResetTokenDAO;
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    private MutableAclService mutableAclService;
    private ErrorMessageUtil errorMessageUtil;
    private ActivityManager activityManager;

    @Autowired
    public UserManager(Environment env, UserDAO userDAO,
            UserResetTokenDAO userResetTokenDAO, BCryptPasswordEncoder bCryptPasswordEncoder,
            MutableAclService mutableAclService, ErrorMessageUtil errorMessageUtil, ActivityManager activityManager) {
        this.env = env;
        this.userDAO = userDAO;
        this.userResetTokenDAO = userResetTokenDAO;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.mutableAclService = mutableAclService;
        this.errorMessageUtil = errorMessageUtil;
        this.activityManager = activityManager;
    }

    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).SECURED_USER, "
            + "T(gov.healthit.chpl.permissions.domains.SecuredUserDomainPermissions).CREATE)")
    public UserDTO create(UserDTO userDto, String password)
            throws UserCreationException {

        Strength strength = getPasswordStrength(userDto, password);
        if (strength.getScore() < UserManager.MIN_PASSWORD_STRENGTH) {
            LOGGER.info("Strength results: [warning: {}] [suggestions: {}] [score: {}] [worst case crack time: {}]",
                    strength.getFeedback().getWarning(), strength.getFeedback().getSuggestions().toString(),
                    strength.getScore(), strength.getCrackTimesDisplay().getOfflineFastHashing1e10PerSecond());
            throw new UserCreationException("Password is not strong enough");
        }
        UserDTO newUser = userDAO.create(userDto, encodePassword(password));

        // Grant the user administrative permission over itself.
        addAclPermission(newUser, new PrincipalSid(newUser.getSubjectName()), BasePermission.ADMINISTRATION);

        return newUser;
    }

    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).SECURED_USER, "
            + "T(gov.healthit.chpl.permissions.domains.SecuredUserDomainPermissions).UPDATE, #user)")
    public UserDTO update(User user)
            throws UserRetrievalException, JsonProcessingException, EntityCreationException, EntityRetrievalException,
            ValidationException {
        UserDTO before = getById(user.getUserId());
        UserDTO toUpdate = UserDTO.builder()
                .id(before.getId())
                .passwordResetRequired(user.getPasswordResetRequired())
                .accountEnabled(user.getAccountEnabled())
                .accountExpired(before.isAccountExpired())
                .accountLocked(user.getAccountLocked())
                .credentialsExpired(user.getCredentialsExpired())
                .email(user.getEmail())
                .failedLoginCount(before.getFailedLoginCount())
                .friendlyName(user.getFriendlyName())
                .fullName(user.getFullName())
                .passwordResetRequired(user.getPasswordResetRequired())
                .permission(before.getPermission())
                .phoneNumber(user.getPhoneNumber())
                .signatureDate(before.getSignatureDate())
                .subjectName(before.getSubjectName())
                .title(user.getTitle())
                .lastLoggedInDate(before.getLastLoggedInDate())
                .build();
        if (before.isAccountLocked() && !user.getAccountLocked()) { //unlocking locked users needs to reset failed login count
            toUpdate.setFailedLoginCount(0);
        }
        return update(toUpdate);
    }

    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).SECURED_USER, "
            + "T(gov.healthit.chpl.permissions.domains.SecuredUserDomainPermissions).UPDATE, #user)")
    public UserDTO update(UserDTO user)
            throws UserRetrievalException, JsonProcessingException, EntityCreationException, EntityRetrievalException,
            ValidationException {
        Optional<ValidationException> validationException = validateUser(user);
        if (validationException.isPresent()) {
            throw validationException.get();
        }

        UserDTO before = getById(user.getId());
        UserDTO updated = userDAO.update(user);

        String activityDescription = "User " + user.getSubjectName() + " was updated.";
        activityManager.addActivity(ActivityConcept.USER, before.getId(), activityDescription, before,
                updated);

        return updated;
    }

    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).SECURED_USER, "
            + "T(gov.healthit.chpl.permissions.domains.SecuredUserDomainPermissions).DELETE)")
    public void delete(UserDTO user)
            throws UserRetrievalException, UserPermissionRetrievalException, UserManagementException {
        // remove all ACLs for this user
        // should only be one - for themselves
        ObjectIdentity oid = new ObjectIdentityImpl(UserDTO.class, user.getId());
        mutableAclService.deleteAcl(oid, false);

        // now delete the user
        userDAO.delete(user.getId());
    }

    @Transactional
    @PostFilter("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).SECURED_USER, "
            + "T(gov.healthit.chpl.permissions.domains.SecuredUserDomainPermissions).GET_ALL, filterObject)")
    public List<UserDTO> getAll() {
        return userDAO.findAll();
    }

    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).SECURED_USER, "
            + "T(gov.healthit.chpl.permissions.domains.SecuredUserDomainPermissions).GET_BY_PERMISSION)")
    public List<UserDTO> getUsersWithPermission(String permissionName) {
        return userDAO.getUsersWithPermission(permissionName);
    }

    @PostAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).SECURED_USER, "
            + "T(gov.healthit.chpl.permissions.domains.SecuredUserDomainPermissions).GET_BY_ID, returnObject)")
    public UserDTO getById(Long id) throws UserRetrievalException {
        return userDAO.getById(id);
    }

    public void updateFailedLoginCount(UserDTO userToUpdate) throws UserRetrievalException {
        userDAO.updateFailedLoginCount(userToUpdate.getSubjectName(), userToUpdate.getFailedLoginCount());
        String maxLoginsStr = env.getProperty("authMaximumLoginAttempts");
        int maxLogins = Integer.parseInt(maxLoginsStr);

        if (userToUpdate.getFailedLoginCount() == maxLogins) {
            userToUpdate.setAccountLocked(true);
            try {
                userDAO.updateAccountLockedStatus(userToUpdate.getSubjectName(), userToUpdate.isAccountLocked());
                sendAccountLockedEmail(userToUpdate);
            } catch (Exception ex) {
                LOGGER.error("Unable to set account " + userToUpdate.getSubjectName() + " as locked.", ex);
            }
        }
    }

    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).SECURED_USER, "
            + "T(gov.healthit.chpl.permissions.domains.SecuredUserDomainPermissions).UPDATE_PASSWORD, #user)")
    public void updateUserPassword(UserDTO user, String password) throws UserRetrievalException {
        updateUserPasswordUnsecured(user.getSubjectName(), password);
    }

    @Transactional
    public void updateUserPasswordUnsecured(String userName, String password)
            throws UserRetrievalException {
        String encodedPassword = encodePassword(password);
        userDAO.updatePassword(userName, encodedPassword);
        userDAO.updateFailedLoginCount(userName, 0);
        userDAO.updateAccountLockedStatus(userName, false);
    }

    // no auth needed. create a random string and create a new reset token row
    // for the user
    @Transactional
    public UserResetTokenDTO createResetUserPasswordToken(String username, String email)
            throws UserRetrievalException {
        UserDTO foundUser = userDAO.findUserByNameAndEmail(username, email);
        if (foundUser == null) {
            throw new UserRetrievalException("Cannot find user with name " + username + " and email address " + email);
        }

        String password = UUID.randomUUID().toString();

        // delete all previous tokens from that user that are in the table
        userResetTokenDAO.deletePreviousUserTokens(foundUser.getId());

        // create new row in reset token table
        UserResetTokenDTO userResetToken = userResetTokenDAO.create(password, foundUser.getId());

        return userResetToken;
    }

    @Transactional
    public boolean authorizePasswordReset(String token) {
        UserResetTokenDTO userResetToken = userResetTokenDAO.findByAuthToken(token);
        if (userResetToken != null && isTokenValid(userResetToken)) {
            return true;
        }
        return false;
    }

    @Transactional
    public void deletePreviousTokens(String token) {
        UserResetTokenDTO userResetToken = userResetTokenDAO.findByAuthToken(token);
        userResetTokenDAO.deletePreviousUserTokens(userResetToken.getUser().getId());
    }

    public String getEncodedPassword(UserDTO user) throws UserRetrievalException {
        return userDAO.getEncodedPassword(user);
    }

    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).SECURED_USER, "
            + "T(gov.healthit.chpl.permissions.domains.SecuredUserDomainPermissions).GET_BY_USER_NAME)")
    @PostAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).SECURED_USER, "
            + "T(gov.healthit.chpl.permissions.domains.SecuredUserDomainPermissions).GET_BY_USER_NAME, returnObject)")
    public UserDTO getByName(String userName) throws UserRetrievalException {
        return getByNameUnsecured(userName);
    }

    public UserDTO getByNameUnsecured(String userName) throws UserRetrievalException {
        return userDAO.getByName(userName);
    }

    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).SECURED_USER, "
            + "T(gov.healthit.chpl.permissions.domains.SecuredUserDomainPermissions).GET_BY_USER_NAME)")
    @PostAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).SECURED_USER, "
            + "T(gov.healthit.chpl.permissions.domains.SecuredUserDomainPermissions).GET_BY_USER_NAME, returnObject)")
    public User getUserInfo(String userName) throws UserRetrievalException {
        UserDTO user = getByNameUnsecured(userName);
        return new User(user);
    }

    public Strength getPasswordStrength(UserDTO user, final String password) {
        ArrayList<String> badWords = new ArrayList<String>();
        badWords.add("chpl");
        badWords.add(user.getEmail());
        badWords.add(user.getFullName());
        badWords.add(user.getUsername());
        if (user.getFriendlyName() != null) {
            badWords.add(user.getFriendlyName());
        }

        Zxcvbn zxcvbn = new Zxcvbn();
        Strength strength = zxcvbn.measure(password, badWords);
        return strength;
    }

    @Transactional
    public void updateLastLoggedInDate(UserDTO user) throws UserRetrievalException {
        user.setLastLoggedInDate(new Date());
        userDAO.update(user);
    }

    private void sendAccountLockedEmail(UserDTO user) throws AddressException, MessagingException {
        String subject = "CHPL Account Locked";
        String htmlMessage = "<p>The account associated with " + user.getSubjectName()
                + " has exceeded the maximum number of failed login attempts and is locked.</p>";
        String[] toEmails = {
                user.getEmail()
        };

        EmailBuilder emailBuilder = new EmailBuilder(env);
        emailBuilder.recipients(new ArrayList<String>(Arrays.asList(toEmails)))
        .subject(subject)
        .htmlMessage(htmlMessage)
        .htmlFooter()
        .sendEmail();
    }

    private void addAclPermission(UserDTO user, Sid recipient, Permission permission) {
        MutableAcl acl;
        ObjectIdentity oid = new ObjectIdentityImpl(UserDTO.class, user.getId());
        try {
            acl = (MutableAcl) mutableAclService.readAclById(oid);
        } catch (NotFoundException nfe) {
            acl = mutableAclService.createAcl(oid);
        }
        acl.insertAce(acl.getEntries().size(), permission, recipient, true);
        mutableAclService.updateAcl(acl);
    }

    private Optional<ValidationException> validateUser(UserDTO user) {
        Set<String> errors = new HashSet<String>();

        if (StringUtils.isEmpty(user.getFullName())) {
            errors.add(errorMessageUtil.getMessage("user.fullName.required"));
        }
        if (StringUtils.isEmpty(user.getEmail())) {
            errors.add(errorMessageUtil.getMessage("user.email.required"));
        }
        if (StringUtils.isEmpty(user.getPhoneNumber())) {
            errors.add(errorMessageUtil.getMessage("user.phone.required"));
        }

        if (errors.size() > 0) {
            return Optional.of(new ValidationException(errors));
        } else {
            return Optional.empty();
        }
    }

    // checks that the token was made in the last x hours
    private boolean isTokenValid(UserResetTokenDTO userResetToken) {
        Date checkDate = userResetToken.getCreationDate();
        Instant now = Instant.now();
        return (!checkDate.toInstant().isBefore(
                now.minus(Integer.parseInt(env.getProperty("resetLinkExpirationTimeInHours")), ChronoUnit.HOURS)));
    }

    private String encodePassword(String password) {
        return bCryptPasswordEncoder.encode(password);
    }
}
