package gov.healthit.chpl.scheduler.job;

import java.util.List;

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

import gov.healthit.chpl.auth.authentication.JWTUserConverter;
import gov.healthit.chpl.auth.user.User;
import gov.healthit.chpl.domain.auth.LoginCredentials;
import gov.healthit.chpl.dto.auth.UserDTO;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.JWTCreationException;
import gov.healthit.chpl.exception.JWTValidationException;
import gov.healthit.chpl.exception.MultipleUserAccountsException;
import gov.healthit.chpl.exception.UserAccountExistsException;
import gov.healthit.chpl.exception.UserRetrievalException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.manager.auth.AuthenticationManager;
import gov.healthit.chpl.manager.auth.UserManager;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class MassRequirePasswordChangeJob extends QuartzJob implements InterruptableJob {
    private boolean interrupted;

    @Autowired
    private UserManager userManager;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JWTUserConverter userConverter;

    /**
     * Default constructor.
     */
    public MassRequirePasswordChangeJob() {
        interrupted = false;
    }

    @Override
    public void execute(JobExecutionContext jobContext) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);

        try {
            UserDTO actor = authenticationManager.getUser(
                    new LoginCredentials(jobContext.getMergedJobDataMap().getString("username"),
                            jobContext.getMergedJobDataMap().getString("password")));

            String jwt = authenticationManager.getJWT(actor);
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
                | MultipleUserAccountsException | UserAccountExistsException
                | JWTCreationException | JWTValidationException e) {
            LOGGER.debug("Unable to update users {}", e.getLocalizedMessage());
        }
    }

    @Override
    public void interrupt() throws UnableToInterruptJobException {
        this.interrupted = true;
    }
}
