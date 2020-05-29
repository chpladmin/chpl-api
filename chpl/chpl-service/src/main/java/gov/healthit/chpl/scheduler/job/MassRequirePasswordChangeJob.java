package gov.healthit.chpl.scheduler.job;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.InterruptableJob;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.UnableToInterruptJobException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AccountStatusException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import com.fasterxml.jackson.core.JsonProcessingException;

import gov.healthit.chpl.auth.authentication.Authenticator;
import gov.healthit.chpl.auth.authentication.JWTUserConverter;
import gov.healthit.chpl.auth.user.User;
import gov.healthit.chpl.domain.auth.LoginCredentials;
import gov.healthit.chpl.dto.auth.UserDTO;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.JWTCreationException;
import gov.healthit.chpl.exception.JWTValidationException;
import gov.healthit.chpl.exception.UserRetrievalException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.manager.auth.UserManager;

/**
 * Quartz job to require all non-deleted users, except for Admin, to change their password on next login.
 * 
 * @author alarned
 *
 */
public class MassRequirePasswordChangeJob extends QuartzJob implements InterruptableJob {
    private static final Logger LOGGER = LogManager.getLogger("massRequirePasswordChangeJobLogger");
    private boolean interrupted;

    @Autowired
    private UserManager userManager;

    @Autowired
    private Authenticator authenticator;

    @Autowired
    private JWTUserConverter userConverter;

    /**
     * Default constructor.
     */
    public MassRequirePasswordChangeJob() {
        interrupted = false;
    }

    @Override
    public void execute(final JobExecutionContext jobContext) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);

        try {
            UserDTO actor = authenticator.getUser(
                    new LoginCredentials(jobContext.getMergedJobDataMap().getString("username"),
                            jobContext.getMergedJobDataMap().getString("password")));

            String jwt = authenticator.getJWT(actor);
            User authenticatedUser = userConverter.getAuthenticatedUser(jwt);
            SecurityContextHolder.getContext().setAuthentication(authenticatedUser);
            List<UserDTO> allUsers = userManager.getAll();
            for (UserDTO user : allUsers) {
                if (interrupted) {
                    LOGGER.info("Interrupted while marking users as password change required");
                    break;
                }
                if (user.getId() > 0L && !user.isPasswordResetRequired()) {
                    user.setPasswordResetRequired(true);
                    try {
                        LOGGER.info("Marking user {} as requiring password change on next login", user.getUsername());
                        userManager.update(user);
                    } catch (UserRetrievalException | JsonProcessingException | EntityCreationException
                            | EntityRetrievalException | ValidationException e) {
                        LOGGER.debug("Unable to update user with username {} and message {}",
                                user.getUsername(), e.getMessage());
                    }
                } else {
                    LOGGER.info("Not requiring user {} to change their password: {}", user.getUsername(),
                            user.getId() <= 0L ? "id is negative"
                                    : user.isPasswordResetRequired() ? "already required" : "other");
                }
            }
            SecurityContextHolder.getContext().setAuthentication(null);
        } catch (BadCredentialsException | AccountStatusException | UserRetrievalException
                | JWTCreationException | JWTValidationException e) {
            LOGGER.debug("Unable to update users {}", e.getLocalizedMessage());
        }
    }

    @Override
    public void interrupt() throws UnableToInterruptJobException {
        this.interrupted = true;
    }
}
