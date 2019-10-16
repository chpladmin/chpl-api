package gov.healthit.chpl.scheduler.job;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
import gov.healthit.chpl.entity.CertificationStatusType;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.manager.CertifiedProductDetailsManager;
import gov.healthit.chpl.manager.CertifiedProductManager;

public class UpdateListingStatusJob extends QuartzJob {
    private static final Logger LOGGER = LogManager.getLogger("updateListingStatusJobLogger");

    @Autowired
    private CertifiedProductDetailsManager certifiedProductDetailsManager;

    @Autowired
    private CertifiedProductManager certifiedProductManager;

    /**
     * Default constructor.
     */
    public UpdateListingStatusJob() {
    }

    @Override
    public void execute(JobExecutionContext jobContext) throws JobExecutionException {
        LOGGER.info("********* Starting the Update Listing Status job. *********");

        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        setSecurityContext();

        String listingsCommaSeparated = jobContext.getMergedJobDataMap().getString("listings");
        // String listingsCommaSeparated =
        // jobContext.getMergedJobDataMap().getString("listings");

        List<Long> listingIds = Stream.of(listingsCommaSeparated.split(","))
                .map(str -> str.trim())
                .map(Long::parseLong)
                .collect(Collectors.toList());

        // TODO - Need to add some validation
        Long certificationStatusId = Long
                .parseLong(jobContext.getMergedJobDataMap().getString("certificationStatusId"));

        // TODO - Need to add some validation
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date statusDate;
        try {
            statusDate = sdf.parse(jobContext.getMergedJobDataMap().getString("effectiveDate").substring(0, 10));
        } catch (ParseException e) {
            LOGGER.error(
                    "Could not parse the effectiveDate" + jobContext.getMergedJobDataMap().getString("effectiveDate"),
                    e);
            return;
        }

        for (Long listingId : listingIds) {
            try {
                LOGGER.info("Getting certified product: " + listingId);
                CertifiedProductSearchDetails cpd = certifiedProductDetailsManager
                        .getCertifiedProductDetails(listingId, true);
                LOGGER.info("Completed Getting certified product: " + listingId);

                CertificationStatus cs = new CertificationStatus();
                cs.setId(certificationStatusId);
                cs.setName(CertificationStatusType.WithdrawnByDeveloper.toString());

                CertificationStatusEvent cse = new CertificationStatusEvent();
                cse.setStatus(cs);
                cse.setEventDate(statusDate.getTime());

                cpd.getCertificationEvents().add(cse);

                LOGGER.info("Updating certified product: " + listingId);
                ListingUpdateRequest updateRequest = new ListingUpdateRequest();
                updateRequest.setListing(cpd);
                certifiedProductManager.update(
                        Long.parseLong(cpd.getCertifyingBody().get("id").toString()), updateRequest);
                LOGGER.info("Completed Updating certified product: " + listingId);
            } catch (ValidationException e) {
                LOGGER.error(e);
                e.getErrorMessages().stream()
                        .forEach(msg -> LOGGER.error(msg));
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
        }

        LOGGER.info("********* Completed the Update Listing Status job. *********");
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
