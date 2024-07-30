package gov.healthit.chpl.scheduler.job;

import java.util.List;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AccountStatusException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import gov.healthit.chpl.auth.ChplAccountEmailNotConfirmedException;
import gov.healthit.chpl.auth.authentication.JWTUserConverterFacade;
import gov.healthit.chpl.auth.user.JWTAuthenticatedUser;
import gov.healthit.chpl.domain.auth.LoginCredentials;
import gov.healthit.chpl.dto.auth.UserDTO;
import gov.healthit.chpl.exception.ActivityException;
import gov.healthit.chpl.exception.JWTCreationException;
import gov.healthit.chpl.exception.JWTValidationException;
import gov.healthit.chpl.exception.MultipleUserAccountsException;
import gov.healthit.chpl.exception.UserRetrievalException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.manager.auth.AuthenticationManager;
import gov.healthit.chpl.manager.auth.UserManager;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class MassRequirePasswordChangeJob extends QuartzJob {
    private boolean interrupted;

    @Autowired
    private UserManager userManager;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JWTUserConverterFacade userConverterFacade;

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
            JWTAuthenticatedUser authenticatedUser = userConverterFacade.getAuthenticatedUser(jwt);
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
                    } catch (UserRetrievalException | ValidationException | ActivityException e) {
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
                | MultipleUserAccountsException | JWTCreationException | ChplAccountEmailNotConfirmedException | JWTValidationException e) {
            LOGGER.debug("Unable to update users {}", e.getLocalizedMessage());
        }
    }
}
