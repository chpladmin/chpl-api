package gov.healthit.chpl.scheduler.job;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import gov.healthit.chpl.auth.permission.GrantedPermission;
import gov.healthit.chpl.auth.user.JWTAuthenticatedUser;
import gov.healthit.chpl.dao.CertificationCriterionDAO;
import gov.healthit.chpl.dao.PendingCertifiedProductDAO;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.PendingCertifiedProductDetails;
import gov.healthit.chpl.entity.CertificationCriterionEntity;
import gov.healthit.chpl.entity.listing.pending.PendingCertificationResultEntity;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.manager.PendingCertifiedProductManager;
import gov.healthit.chpl.scheduler.job.extra.JobResponse;

public class AddCriteriaToSinglePendingListingJob extends QuartzJob {
    private static final long ADMIN_ID = -2L;

    // Default logger
    private Logger logger = LogManager.getLogger("addCriteriaToListingsJobLogger");

    @Autowired
    private PendingCertifiedProductDAO pendingCertifiedProductDAO;

    @Autowired
    private PendingCertifiedProductManager pcpManager;

    @Autowired
    private CertificationCriterionDAO criterionDAO;

    @Autowired
    private JpaTransactionManager txManager;

    @Override
    public void execute(JobExecutionContext jobContext) throws JobExecutionException {

        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        setLogger(jobContext);
        setSecurityContext();

        Long listingId = jobContext.getMergedJobDataMap().getLong("listing");
        ArrayList<String> criteria = new ArrayList<String>(Arrays.asList(jobContext.getMergedJobDataMap()
                .get("criteria").toString().split(";")));

        logger.info("********* Starting the Add Criteria to Single Pending Listing job. [" + listingId + "] *********");

        PendingCertifiedProductDetails pcp = getListing(listingId);
        JobResponse response;

        if (pcp != null) {
            try {
                for (String criterion : criteria) {
                    String[] criterionValues = criterion.split(":");
                    addCertToPendingListing(pcp, criterionValues[0], criterionValues[1]);
                }
                String msg = "Completed Updating pending certified product {" + pcp.getChplProductNumber() + "}: "
                        + "-" + criteria.toString();
                response = new JobResponse(pcp.getChplProductNumber(), true, msg);

            } catch (Exception e) {
                String msg = "Unsuccessful Update pending certified product {"
            + pcp.getChplProductNumber() + "} " + criteria.toString()
                + ':' + e.getMessage();
                response =  new JobResponse(pcp.getChplProductNumber(), false, msg);
            }
        } else {
            response = new JobResponse("No CHPL Product Number", false,
                    "No pending listing with id " + listingId + " was found.");
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

    private PendingCertifiedProductDetails getListing(Long pcpId) {
        PendingCertifiedProductDetails pcp = null;
        try {
            //using manager method here for transaction access
            pcp = pcpManager.getById(pcpId);
        } catch (Exception e) {
            logger.error(e);
        }
        return pcp;
    }

    private void addCertToPendingListing(
            PendingCertifiedProductDetails pcp, String criterionNumber, String criterionTitle)
                    throws EntityCreationException {
            TransactionTemplate txTemplate = new TransactionTemplate(txManager);
            txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
            txTemplate.execute(new TransactionCallbackWithoutResult() {

            @Override
            //can't use the manager method because we don't want to record activity
            //so wrapping the DAO call in a transaction
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                CertificationCriterionEntity criterion = criterionDAO.getEntityByNumberAndTitle(criterionNumber, criterionTitle);
                if (criterion == null || criterion.getId() == null) {
                    logger.error(
                            "Cannot create certification result mapping for unknown criteria " + criterionNumber);
                } else if (!certResultExists(pcp, criterionNumber, criterionTitle)) {
                    PendingCertificationResultEntity toCreate = new PendingCertificationResultEntity();
                    toCreate.setPendingCertifiedProductId(pcp.getId());
                    toCreate.setMappedCriterion(criterion);
                    toCreate.setMeetsCriteria(false);

                    try {
                        pendingCertifiedProductDAO.createCertificationResult(pcp.getId(), toCreate);
                    } catch (Exception e) {
                        logger.error("Error saving ParticipantAgeStatistics.", e);
                        status.setRollbackOnly();
                    }
                } else {
                    logger.info("Certification result mapping already exists for " + pcp.getChplProductNumber()
                        + " and " + criterionNumber + ":" + criterionTitle);
                }
            }
        });
    }

    private boolean certResultExists(PendingCertifiedProductDetails pcp, String criterionNumber, String criterionTitle) {
        boolean result = false;
        List<CertificationResult> certResults = pcp.getCertificationResults();
        for (CertificationResult certResult : certResults) {
            if (certResult.getCriterion().getNumber().equalsIgnoreCase(criterionNumber)
                    && certResult.getCriterion().getTitle().equalsIgnoreCase(criterionTitle)) {
                result = true;
            }
        }
        return result;
    }

    private void setSecurityContext() {
        JWTAuthenticatedUser adminUser = new JWTAuthenticatedUser();
        adminUser.setFullName("Administrator");
        adminUser.setId(ADMIN_ID);
        adminUser.setFriendlyName("Admin");
        adminUser.setSubjectName("admin");
        adminUser.getPermissions().add(new GrantedPermission("ROLE_ADMIN"));

        SecurityContextHolder.getContext().setAuthentication(adminUser);
    }
}
