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
import gov.healthit.chpl.scheduler.job.extra.JobResponse;

public class UpdateSingleListingStatusJob extends QuartzJob {

    // Default logger
    private Logger logger = LogManager.getLogger("updateListingStatusJobLogger");

    @Autowired
    private CertifiedProductDetailsManager certifiedProductDetailsManager;

    @Autowired
    private CertifiedProductManager certifiedProductManager;

    @Override
    public void execute(JobExecutionContext jobContext) throws JobExecutionException {

        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        setLogger(jobContext);
        setSecurityContext();

        Long listing = jobContext.getMergedJobDataMap().getLong("listing");
        CertificationStatus certificationStatus = (CertificationStatus) jobContext.getMergedJobDataMap()
                .get("certificationStatus");
        Date statusDate = (Date) jobContext.getMergedJobDataMap().get("statusDate");

        logger.info("********* Starting the Update Single Listing Status job. [" + listing + "] *********");

        // This will get listing details, update the listing with the new status, and update listing
        CertifiedProductSearchDetails cpsd = getListing(listing);
        JobResponse response = updateListing(cpsd, certificationStatus, statusDate);

        jobContext.setResult(response);
        logger.info("********* Completed the Update Listing Status job. [" + listing + "] *********");
    }

    private CertificationStatusEvent getCertificationStatusEvent(CertificationStatus cs, Date effectiveDate) {
        CertificationStatusEvent cse = new CertificationStatusEvent();
        cse.setStatus(cs);
        cse.setEventDate(effectiveDate.getTime());

        return cse;
    }

    private void setLogger(JobExecutionContext jobContext) {
        if (jobContext.getMergedJobDataMap().containsKey("logger")) {
            if (jobContext.getMergedJobDataMap().get("logger") instanceof Logger) {
                logger = (Logger) jobContext.getMergedJobDataMap().get("logger");
            }
        }
    }

    private CertifiedProductSearchDetails getListing(Long cpId) {
        try {
            CertifiedProductSearchDetails cpsd = certifiedProductDetailsManager.getCertifiedProductDetails(cpId);
            // LOGGER.info("Completed Retrieving certified product {" + cpsd.getId() + "}: "
            // + cpsd.getChplProductNumber());
            return cpsd;
        } catch (Exception e) {
            logger.error(e);
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

            String msg = "Completed Updating certified product {" + updateRequest.getListing().getId() + "}: "
                    + updateRequest.getListing().getChplProductNumber();
            return new JobResponse(cpd.getChplProductNumber(), true, msg);
        } catch (ValidationException e) {
            String msg = "Error validating {" + cpd.getId() + "}: " + cpd.getChplProductNumber() + "\n";
            msg = msg + String.join("\n", e.getErrorMessages());
            return new JobResponse(cpd.getChplProductNumber(), false, msg);
        } catch (Exception e) {
            String msg = "Unsuccessful Update certified product {" + updateRequest.getListing().getId() + "}: "
                    + updateRequest.getListing().getChplProductNumber() + "\n" + e.getMessage();
            return new JobResponse(cpd.getChplProductNumber(), false, msg);
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
}
