package gov.healthit.chpl.manager.auth;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nulabinc.zxcvbn.Strength;
import com.nulabinc.zxcvbn.Zxcvbn;

import gov.healthit.chpl.dao.auth.UserDAO;
import gov.healthit.chpl.dao.auth.UserResetTokenDAO;
import gov.healthit.chpl.domain.auth.User;
import gov.healthit.chpl.dto.auth.UserDTO;
import gov.healthit.chpl.dto.auth.UserResetTokenDTO;
import gov.healthit.chpl.entity.auth.UserEntity;
import gov.healthit.chpl.exception.UserCreationException;
import gov.healthit.chpl.exception.UserManagementException;
import gov.healthit.chpl.exception.UserPermissionRetrievalException;
import gov.healthit.chpl.exception.UserRetrievalException;
import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class UserManager {
    private static int MIN_PASSWORD_STRENGTH = 3;

    private Environment env;
    private SecuredUserManager securedUserManager;
    private UserDAO userDAO;
    private UserResetTokenDAO userResetTokenDAO;
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    public UserManager(SecuredUserManager securedUserManager, Environment env, UserDAO userDAO,
            UserResetTokenDAO userResetTokenDAO, BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.securedUserManager = securedUserManager;
        this.env = env;
        this.userDAO = userDAO;
        this.userResetTokenDAO = userResetTokenDAO;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    @Transactional
    public UserDTO create(UserDTO userDto, String password)
            throws UserCreationException, UserRetrievalException {

        Strength strength = getPasswordStrength(userDto, password);
        if (strength.getScore() < UserManager.MIN_PASSWORD_STRENGTH) {
            LOGGER.info("Strength results: [warning: {}] [suggestions: {}] [score: {}] [worst case crack time: {}]",
                    strength.getFeedback().getWarning(), strength.getFeedback().getSuggestions().toString(),
                    strength.getScore(), strength.getCrackTimesDisplay().getOfflineFastHashing1e10PerSecond());
            throw new UserCreationException("Password is not strong enough");
        }
        String encodedPassword = encodePassword(password);
        UserDTO createdUser = securedUserManager.create(userDto, encodedPassword);
        return createdUser;
    }

    @Transactional
    public UserDTO update(UserDTO user) throws UserRetrievalException {
        return securedUserManager.update(user);
    }

    @Transactional
    private void updateContactInfo(UserEntity user) {
        securedUserManager.updateContactInfo(user);
    }

    @Transactional
    public void delete(UserDTO user)
            throws UserRetrievalException, UserPermissionRetrievalException, UserManagementException {
        securedUserManager.delete(user);
    }

    @Transactional
    public void delete(String userName)
            throws UserRetrievalException, UserPermissionRetrievalException, UserManagementException {

        UserDTO user = securedUserManager.getBySubjectName(userName);
        if (user == null) {
            throw new UserRetrievalException("User not found");
        } else {
            delete(user);
        }
    }

    @Transactional
    public List<UserDTO> getAll() {
        return securedUserManager.getAll();
    }

    @Transactional
    public List<UserDTO> getUsersWithPermission(String permissionName) {
        return securedUserManager.getUsersWithPermission(permissionName);
    }

    @Transactional
    public UserDTO getById(Long id) throws UserRetrievalException {
        return securedUserManager.getById(id);
    }

    @Transactional
    public void updateFailedLoginCount(UserDTO userToUpdate) throws UserRetrievalException {
        securedUserManager.updateFailedLoginCount(userToUpdate);
        String maxLoginsStr = env.getProperty("authMaximumLoginAttempts");
        int maxLogins = Integer.parseInt(maxLoginsStr);

        if (userToUpdate.getFailedLoginCount() >= maxLogins) {
            userToUpdate.setAccountLocked(true);
            securedUserManager.updateAccountLockedStatus(userToUpdate);
        }
    }

    @Transactional
    public void updateUserPassword(String userName, String password) throws UserRetrievalException {
        String encodedPassword = encodePassword(password);
        UserDTO userToUpdate = securedUserManager.getBySubjectName(userName);
        securedUserManager.updatePassword(userToUpdate, encodedPassword);
    }

    @Transactional
    public void updateUserPasswordUnsecured(String userName, String password)
            throws UserRetrievalException {
        String encodedPassword = encodePassword(password);
        userDAO.updatePassword(userName, encodedPassword);
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

    // checks that the token was made in the last x hours
    private boolean isTokenValid(UserResetTokenDTO userResetToken) {
        Date checkDate = userResetToken.getCreationDate();
        Instant now = Instant.now();
        return (!checkDate.toInstant().isBefore(
                now.minus(Integer.parseInt(env.getProperty("resetLinkExpirationTimeInHours")), ChronoUnit.HOURS)));
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

    public String encodePassword(String password) {
        String encodedPassword = bCryptPasswordEncoder.encode(password);
        return encodedPassword;
    }

    public String getEncodedPassword(UserDTO user) throws UserRetrievalException {
        return userDAO.getEncodedPassword(user);
    }

    public UserDTO getByName(String userName) throws UserRetrievalException {
        UserDTO dto = securedUserManager.getBySubjectName(userName);
        return dto;
    }

    public UserDTO getByNameUnsecured(String userName) throws UserRetrievalException {
        return userDAO.getByName(userName);
    }

    public User getUserInfo(String userName) throws UserRetrievalException {
        UserDTO user = securedUserManager.getBySubjectName(userName);
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
}
