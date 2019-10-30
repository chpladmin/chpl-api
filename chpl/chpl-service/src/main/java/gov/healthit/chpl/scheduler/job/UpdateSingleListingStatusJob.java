package gov.healthit.chpl.scheduler.job;

import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import gov.healthit.chpl.auth.permission.GrantedPermission;
import gov.healthit.chpl.auth.user.JWTAuthenticatedUser;
import gov.healthit.chpl.domain.CertificationStatus;
import gov.healthit.chpl.domain.CertificationStatusEvent;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.ListingUpdateRequest;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.manager.CertifiedProductDetailsManager;
import gov.healthit.chpl.manager.CertifiedProductManager;

public class UpdateSingleListingStatusJob extends QuartzJob {

    private static final Logger LOGGER = LogManager.getLogger("updateListingStatusJobLogger");

    @Autowired
    private CertifiedProductDetailsManager certifiedProductDetailsManager;

    @Autowired
    private CertifiedProductManager certifiedProductManager;

    @Override
    public void execute(JobExecutionContext jobContext) throws JobExecutionException {

        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        setSecurityContext();

        Long listing = jobContext.getMergedJobDataMap().getLong("listing");

        LOGGER.info("********* Starting the Update Single Listing Status job. [" + listing + "] *********");

        CertificationStatus certificationStatus = (CertificationStatus) jobContext.getMergedJobDataMap()
                .get("certificationStatus");

        Date statusDate = (Date) jobContext.getMergedJobDataMap().get("statusDate");

        // This will get listing details, update the listing with the new status, and update listing
        CertifiedProductSearchDetails cpsd = getListing(listing);
        JobResponse response = updateListing(cpsd, certificationStatus, statusDate);

        jobContext.getTrigger().getJobDataMap().put("success", response.completedSuccessfully);
        jobContext.getTrigger().getJobDataMap().put("message", response.getMessage());

        LOGGER.info("********* Completed the Update Listing Status job. [" + listing + "] *********");
    }

    private CertificationStatusEvent getCertificationStatusEvent(CertificationStatus cs, Date effectiveDate) {
        CertificationStatusEvent cse = new CertificationStatusEvent();
        cse.setStatus(cs);
        cse.setEventDate(effectiveDate.getTime());

        return cse;
    }

    private CertifiedProductSearchDetails getListing(Long cpId) {
        try {
            CertifiedProductSearchDetails cpsd = certifiedProductDetailsManager.getCertifiedProductDetails(cpId);
            LOGGER.info("Completed Retrieving certified product {" + cpsd.getId() + "}: "
                    + cpsd.getChplProductNumber());
            return cpsd;
        } catch (Exception e) {
            LOGGER.error(e);
            return null;
        }
    }

    private JobResponse updateListing(CertifiedProductSearchDetails cpd, CertificationStatus cs, Date effectiveDate) {
        cpd.getCertificationEvents().add(getCertificationStatusEvent(cs, effectiveDate));
        ListingUpdateRequest updateRequest = new ListingUpdateRequest();
        updateRequest.setListing(cpd);

        try {
            certifiedProductManager.update(
                    Long.parseLong(updateRequest.getListing().getCertifyingBody().get("id").toString()), updateRequest);

            // LOGGER.info("Completed Updating certified product {" + updateRequest.getListing().getId() + "}: "
            // + updateRequest.getListing().getChplProductNumber());
            String msg = "Completed Updating certified product {" + updateRequest.getListing().getId() + "}: "
                    + updateRequest.getListing().getChplProductNumber();
            return new JobResponse(true, msg);
        } catch (ValidationException e) {
            String msg = "Error validating {" + cpd.getId() + "}: " + cpd.getChplProductNumber() + "\n";
            msg = msg + String.join("\n", e.getErrorMessages());
            return new JobResponse(false, msg);
            // e.getErrorMessages().stream()
            // .forEach(msg -> LOGGER.error(msg));
            // LOGGER.info("Unsuccessful Update certified product {" + updateRequest.getListing().getId() + "}: "
            // + updateRequest.getListing().getChplProductNumber());
        } catch (Exception e) {
            // LOGGER.error("An unexpected error occurred", e);
            String msg = "Unsuccessful Update certified product {" + updateRequest.getListing().getId() + "}: "
                    + updateRequest.getListing().getChplProductNumber();
            return new JobResponse(false, msg);
        }
    }

    private void setSecurityContext() {
        JWTAuthenticatedUser adminUser = new JWTAuthenticatedUser();
        adminUser.setFullName("Administrator");
        adminUser.setId(-2L);
        adminUser.setFriendlyName("Admin");
        adminUser.setSubjectName("admin");
        adminUser.getPermissions().add(new GrantedPermission("ROLE_ADMIN"));

        SecurityContextHolder.getContext().setAuthentication(adminUser);
    }

    private class JobResponse {
        private String message = "";
        private boolean completedSuccessfully = false;

        public JobResponse(final boolean completedSuccessfully, final String message) {
            this.setMessage(message);
            this.setCompletedSuccessfully(completedSuccessfully);
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public boolean isCompletedSuccessfully() {
            return completedSuccessfully;
        }

        public void setCompletedSuccessfully(boolean completedSuccessfully) {
            this.completedSuccessfully = completedSuccessfully;
        }
    }
}
