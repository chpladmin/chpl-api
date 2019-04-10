package gov.healthit.chpl.manager.auth.impl;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
import gov.healthit.chpl.manager.auth.SecuredUserManager;
import gov.healthit.chpl.manager.auth.UserManager;

/**
 * Implementation of User Manager.
 */
@Service
public class UserManagerImpl implements UserManager {
    private static final Logger LOGGER = LogManager.getLogger(UserManagerImpl.class);

    @Autowired
    private Environment env;

    @Autowired
    private SecuredUserManager securedUserManager;

    @Autowired
    private UserDAO userDAO;

    @Autowired
    private UserResetTokenDAO userResetTokenDAO;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Override
    @Transactional
    public UserDTO create(final UserDTO userInfo) throws UserCreationException, UserRetrievalException {

        Strength strength = getPasswordStrength(userInfo, userInfo.getPassword());
        if (strength.getScore() < UserManager.MIN_PASSWORD_STRENGTH) {
            LOGGER.info("Strength results: [warning: {}] [suggestions: {}] [score: {}] [worst case crack time: {}]",
                    strength.getFeedback().getWarning(), strength.getFeedback().getSuggestions().toString(),
                    strength.getScore(), strength.getCrackTimesDisplay().getOfflineFastHashing1e10PerSecond());
            throw new UserCreationException("Password is not strong enough");
        }
        String encodedPassword = encodePassword(userInfo.getPassword());
        UserDTO createdUser = securedUserManager.create(userInfo, encodedPassword);
        return createdUser;
    }

    @Override
    @Transactional
    public UserDTO update(final UserDTO user) throws UserRetrievalException {
        return securedUserManager.update(user);
    }

    @Transactional
    private void updateContactInfo(final UserEntity user) {
        securedUserManager.updateContactInfo(user);
    }

    @Override
    @Transactional
    public void delete(final UserDTO user)
            throws UserRetrievalException, UserPermissionRetrievalException, UserManagementException {
        securedUserManager.delete(user);
    }

    @Override
    @Transactional
    public void delete(final String userName)
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

    @Override
    @Transactional
    public List<UserDTO> getUsersWithPermission(final String permissionName) {
        return securedUserManager.getUsersWithPermission(permissionName);
    }

    @Override
    @Transactional
    public UserDTO getById(final Long id) throws UserRetrievalException {
        return securedUserManager.getById(id);
    }

    @Override
    @Transactional
    public void updateFailedLoginCount(final UserDTO userToUpdate) throws UserRetrievalException {
        securedUserManager.updateFailedLoginCount(userToUpdate);
        String maxLoginsStr = env.getProperty("authMaximumLoginAttempts");
        int maxLogins = Integer.parseInt(maxLoginsStr);

        if (userToUpdate.getFailedLoginCount() >= maxLogins) {
            userToUpdate.setAccountLocked(true);
            securedUserManager.updateAccountLockedStatus(userToUpdate);
        }
    }

    @Override
    @Transactional
    public void updateUserPassword(final String userName, final String password) throws UserRetrievalException {
        String encodedPassword = encodePassword(password);
        UserDTO userToUpdate = securedUserManager.getBySubjectName(userName);
        securedUserManager.updatePassword(userToUpdate, encodedPassword);
    }

    @Override
    @Transactional
    public void updateUserPasswordUnsecured(final String userName, final String password)
            throws UserRetrievalException {
        String encodedPassword = encodePassword(password);
        userDAO.updatePassword(userName, encodedPassword);
    }

    // no auth needed. create a random string and create a new reset token row
    // for the user
    @Override
    @Transactional
    public UserResetTokenDTO createResetUserPasswordToken(final String username, final String email)
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

    @Override
    public String encodePassword(final String password) {
        String encodedPassword = bCryptPasswordEncoder.encode(password);
        return encodedPassword;
    }

    @Override
    public String getEncodedPassword(final UserDTO user) throws UserRetrievalException {
        return userDAO.getEncodedPassword(user);
    }

    @Override
    public UserDTO getByName(final String userName) throws UserRetrievalException {
        UserDTO dto = securedUserManager.getBySubjectName(userName);
        return dto;
    }

    @Override
    public UserDTO getByNameUnsecured(final String userName) throws UserRetrievalException {
        return userDAO.getByName(userName);
    }

    @Override
    public User getUserInfo(final String userName) throws UserRetrievalException {
        UserDTO user = securedUserManager.getBySubjectName(userName);
        return new User(user);
    }

    @Override
    public Strength getPasswordStrength(final UserDTO user, final String password) {
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
}
