package gov.healthit.chpl.scheduler.job;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
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
import gov.healthit.chpl.certifiedproduct.CertifiedProductDetailsManager;
import gov.healthit.chpl.domain.CertificationStatus;
import gov.healthit.chpl.domain.CertificationStatusEvent;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.ListingUpdateRequest;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.manager.CertifiedProductManager;

public class UpdateListingStatusJob extends QuartzJob {
    private static final Logger LOGGER = LogManager.getLogger("updateListingStatusJobLogger");

    @Autowired
    private CertifiedProductDetailsManager certifiedProductDetailsManager;

    @Autowired
    private CertifiedProductManager certifiedProductManager;

    @Override
    public void execute(JobExecutionContext jobContext) throws JobExecutionException {
        LOGGER.info("********* Starting the Update Listing Status job. *********");

        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        setSecurityContext();

        List<Long> listings = getListingIds(jobContext);

        CertificationStatus certificationStatus = getCertificationStatus(jobContext);

        Date statusDate = getStatusEffectiveDate(jobContext);

        for (Long cpId : listings) {
            // This will get listing details, update the listing with the new status, and update listing
            CertifiedProductSearchDetails cpsd = getListing(cpId);
            updateListing(cpsd, certificationStatus, statusDate);
        }

        LOGGER.info("********* Completed the Update Listing Status job. *********");
    }

    private CertificationStatus getCertificationStatus(JobExecutionContext context) {
        @SuppressWarnings("unchecked") Map<String, Object> csMap = (Map<String, Object>) context.getMergedJobDataMap()
                .get("certificationStatus");

        CertificationStatus cs = new CertificationStatus();
        cs.setId(Long.parseLong(csMap.get("id").toString()));
        cs.setName(csMap.get("name").toString());
        return cs;
    }

    private CertificationStatusEvent getCertifiectionStatusEvent(CertificationStatus cs, Date effectiveDate) {
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

    private void updateListing(CertifiedProductSearchDetails cpd, CertificationStatus cs, Date effectiveDate) {
        cpd.getCertificationEvents().add(getCertifiectionStatusEvent(cs, effectiveDate));
        ListingUpdateRequest updateRequest = new ListingUpdateRequest();
        updateRequest.setListing(cpd);

        try {
            certifiedProductManager.update(updateRequest);
            LOGGER.info("Completed Updating certified product {" + updateRequest.getListing().getId() + "}: "
                    + updateRequest.getListing().getChplProductNumber());
        } catch (ValidationException e) {
            LOGGER.error("Error validating {" + cpd.getId() + "}: " + cpd.getChplProductNumber());
            e.getErrorMessages().stream()
                    .forEach(msg -> LOGGER.error(msg));
            LOGGER.info("Unsuccessful Update certified product {" + updateRequest.getListing().getId() + "}: "
                    + updateRequest.getListing().getChplProductNumber());
        } catch (Exception e) {
            LOGGER.error("An unexpected error occurred", e);
            LOGGER.info("Unsuccessful Update certified product {" + updateRequest.getListing().getId() + "}: "
                    + updateRequest.getListing().getChplProductNumber());
        }

    }

    private Date getStatusEffectiveDate(JobExecutionContext jobContext) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date statusDate;
        try {
            statusDate = sdf.parse(jobContext.getMergedJobDataMap().getString("effectiveDate").substring(0, 10));
        } catch (ParseException e) {
            LOGGER.error(
                    "Could not parse the effectiveDate" + jobContext.getMergedJobDataMap().getString("effectiveDate"),
                    e);
            throw new RuntimeException(e);
        }
        return statusDate;
    }

    private List<Long> getListingIds(JobExecutionContext jobContext) {
        String listingsCommaSeparated = jobContext.getMergedJobDataMap().getString("listings");

        return Stream.of(listingsCommaSeparated.split(","))
                .map(str -> str.trim())
                .map(Long::parseLong)
                .collect(Collectors.toList());
    }

    private void setSecurityContext() {
        JWTAuthenticatedUser adminUser = new JWTAuthenticatedUser();
        adminUser.setFullName("Administrator");
        adminUser.setId(-2L);
        adminUser.setFriendlyName("Admin");
        adminUser.setSubjectName("admin");
        adminUser.getPermissions().add(new GrantedPermission("ROLE_ADMIN"));

        SecurityContextHolder.getContext().setAuthentication(adminUser);
        SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_INHERITABLETHREADLOCAL);
    }
}
