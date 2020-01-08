package gov.healthit.chpl.scheduler.job;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import gov.healthit.chpl.auth.permission.GrantedPermission;
import gov.healthit.chpl.auth.user.JWTAuthenticatedUser;
import gov.healthit.chpl.dao.CertificationCriterionDAO;
import gov.healthit.chpl.dao.PendingCertifiedProductDAO;
import gov.healthit.chpl.dto.listing.pending.PendingCertificationResultDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertifiedProductDTO;
import gov.healthit.chpl.entity.CertificationCriterionEntity;
import gov.healthit.chpl.entity.listing.pending.PendingCertificationResultEntity;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.scheduler.job.extra.JobResponse;

public class AddCriteriaToSinglePendingListingJob extends QuartzJob {

    // Default logger
    private Logger logger = LogManager.getLogger("addCriteriaToListingsJobLogger");

    @Autowired
    private PendingCertifiedProductDAO pendingCertifiedProductDAO;

    @Autowired
    private CertificationCriterionDAO certCritDAO;

    @Override
    public void execute(JobExecutionContext jobContext) throws JobExecutionException {

        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        setLogger(jobContext);
        setSecurityContext();

        Long listingId = jobContext.getMergedJobDataMap().getLong("listing");
        ArrayList<String> criteria = new ArrayList<String>(Arrays.asList(jobContext.getMergedJobDataMap()
                .get("criteria").toString().split(";")));

        logger.info("********* Starting the Add Criteria to Single Pending Listing job. [" + listingId + "] *********");

        PendingCertifiedProductDTO pcp = getListing(listingId);
        JobResponse response;

        try {
            for (String criterion : criteria) {
                addCertToPendingListing(pcp, criterion);
            }
            String msg = "Completed Updating certified product {" + pcp.getUniqueId() + "}: "
                    + "-" + criteria.toString();
            response = new JobResponse(pcp.getId().toString()/*pcp.getChplProductNumber()*/, true, msg);

        } catch (Exception e) {
            String msg = "Unsuccessful Update certified product {" + pcp.getId() + "} " + criteria.toString()
            + ':' + e.getMessage();
            response =  new JobResponse(pcp.getId().toString()/*pcp.getChplProductNumber()*/, false, msg);
        }

        jobContext.setResult(response);

        logger.info(response.toString());
        logger.info("********* Completed the Add Criteria to Single Pending Listing job. [" + listingId + "] *********");
    }

    private void setLogger(JobExecutionContext jobContext) {
        if (jobContext.getMergedJobDataMap().containsKey("logger")) {
            if (jobContext.getMergedJobDataMap().get("logger") instanceof Logger) {
                logger = (Logger) jobContext.getMergedJobDataMap().get("logger");
            }
        }
    }

    private PendingCertifiedProductDTO getListing(Long cpId) {
        PendingCertifiedProductDTO pcp;
        try {
            pcp = pendingCertifiedProductDAO.findById(cpId, false);
        } catch (Exception e) {
            logger.error(e);
            return null;
        }
        return pcp;
    }

    private void addCertToPendingListing(
            PendingCertifiedProductDTO pcp, String criterionNumber) throws EntityCreationException {
        CertificationCriterionEntity criterion = certCritDAO.getEntityByName(criterionNumber);
        if (criterion == null || criterion.getId() == null) {
            throw new EntityCreationException(
                    "Cannot create certification result mapping for unknown criteria " + criterionNumber);
        }
        List<PendingCertificationResultDTO> certResults = pcp.getCertificationCriterion();
        for (PendingCertificationResultDTO certResult : certResults) {
            if (certResult.getCriterion().getNumber().equalsIgnoreCase(criterionNumber)) {
                throw new EntityCreationException(
                        "Cannot create duplicate certification result mapping for criteria " + criterionNumber);
            }
        }

        PendingCertificationResultEntity toCreate = new PendingCertificationResultEntity();
        toCreate.setPendingCertifiedProductId(pcp.getId());
        toCreate.setMappedCriterion(criterion);
        toCreate.setMeetsCriteria(false);
        pendingCertifiedProductDAO.createCertificationResult(pcp.getId(), toCreate);
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
