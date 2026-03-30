package gov.healthit.chpl.util;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.auth.UserDAO;
import gov.healthit.chpl.domain.auth.User;
import gov.healthit.chpl.user.cognito.CognitoApiWrapper;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class ChplUserToCognitoUserUtil {
    private UserDAO userDAO;
    private CognitoApiWrapper cognitoApiWrapper;
    private ServerEnvironment serverEnvironment;

    @Autowired
    public ChplUserToCognitoUserUtil(UserDAO userDAO, CognitoApiWrapper cognitoApiWrapper,
            @Value("${server.environment}") String serverEnvironment) {
        this.cognitoApiWrapper = cognitoApiWrapper;
        this.userDAO = userDAO;
        this.serverEnvironment = ServerEnvironment.getByName(serverEnvironment);
    }

    public User getUser(Long chplUserId, UUID cognitoUserId) {
        User currentUser = null;
        if (chplUserId != null) {
            try {
                currentUser = userDAO.getById(chplUserId, true);
            } catch (Exception e) {
                LOGGER.error("Could not retreive user with ID: {}", chplUserId, e);
            }
        } else if (cognitoUserId != null) {
            try {
                if (serverEnvironment.equals(ServerEnvironment.PRODUCTION)) {
                    currentUser = cognitoApiWrapper.getUserInfo(cognitoUserId);
                } else {
                    currentUser = cognitoApiWrapper.getUserInfoIfCached(cognitoUserId);
                }
            } catch (Exception e) {
                LOGGER.error("Could not retreive user with ID: {}", cognitoUserId, e);
            }
        }
        return currentUser;
    }
}
