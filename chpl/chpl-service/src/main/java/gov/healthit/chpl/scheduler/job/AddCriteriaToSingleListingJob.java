package gov.healthit.chpl.scheduler.job;

import java.io.IOException;
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
import gov.healthit.chpl.dao.CertificationResultDAO;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.dto.CertificationCriterionDTO;
import gov.healthit.chpl.dto.CertificationResultDTO;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.manager.CertifiedProductDetailsManager;
import gov.healthit.chpl.scheduler.job.extra.JobResponse;

public class AddCriteriaToSingleListingJob extends QuartzJob {
    private static final long ADMIN_ID = -2L;

    // Default logger
    private Logger logger = LogManager.getLogger("addCriteriaToListingsJobLogger");

    @Autowired
    private CertifiedProductDetailsManager certifiedProductDetailsManager;

    @Autowired
    private CertificationCriterionDAO criterionDAO;

    @Autowired
    private CertificationResultDAO certResultDAO;

    @Autowired
    private JpaTransactionManager txManager;

    @Override
    public void execute(JobExecutionContext jobContext) throws JobExecutionException {

        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        setLogger(jobContext);
        setSecurityContext();

        Long listing = jobContext.getMergedJobDataMap().getLong("listing");
        ArrayList<String> criteria = new ArrayList<String>(Arrays.asList(jobContext.getMergedJobDataMap()
                .get("criteria").toString().split(";")));

        logger.info("********* Starting the Add Criteria to Single Listing job. [" + listing + "] *********");

        CertifiedProductSearchDetails cpsd = getListing(listing);
        JobResponse response;

        try {
            for (String criterion : criteria) {
                String[] criterionValues = criterion.split(":");
                create(cpsd, criterionValues[0], criterionValues[1]);
            }
            String msg = "Completed Updating certified product {" + cpsd.getId() + "}: "
                    + cpsd.getChplProductNumber() + "-" + criteria.toString();
            response = new JobResponse(cpsd.getChplProductNumber(), true, msg);

        } catch (Exception e) {
            String msg = "Unsuccessful Update certified product {" + cpsd.getId() + "} " + criteria.toString()
                    + ':' + e.getMessage();
            response =  new JobResponse(cpsd.getChplProductNumber(), false, msg);
        }

        jobContext.setResult(response);

        logger.info(response.toString());
        logger.info("********* Completed the Add Criteria to Single Listing job. [" + listing + "] *********");
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
            return cpsd;
        } catch (Exception e) {
            logger.error(e);
            return null;
        }
    }

    private void create(CertifiedProductSearchDetails listing, String criterionNumber, String criterionTitle)
            throws EntityCreationException, EntityRetrievalException, IOException {
            TransactionTemplate txTemplate = new TransactionTemplate(txManager);
            txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
            txTemplate.execute(new TransactionCallbackWithoutResult() {

            @Override
            //can't use the manager method because we don't want to record activity
            //so wrapping the dao call in a transaction
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                CertificationCriterionDTO criterion = criterionDAO.getByNumberAndTitle(criterionNumber, criterionTitle);
                if (criterion == null || criterion.getId() == null) {
                    logger.error(
                            "Cannot create certification result mapping for unknown criteria " + criterionNumber);
                } else if (!certResultExists(listing, criterionNumber, criterionTitle)) {
                    CertificationResultDTO toCreate = new CertificationResultDTO();
                    toCreate.setCertificationCriterionId(criterion.getId());
                    toCreate.setCertifiedProductId(listing.getId());
                    toCreate.setSuccessful(false);

                    try {
                        certResultDAO.create(toCreate);
                    } catch (Exception e) {
                        logger.error("Error saving certification result mapping.", e);
                        status.setRollbackOnly();
                    }
                } else {
                    logger.info("Certification result mapping already exists for " + listing.getChplProductNumber()
                        + " and " + criterionNumber + ":" + criterionTitle);
                }
            }
        });
    }

    private boolean certResultExists(CertifiedProductSearchDetails listing, String criterionNumber, String criterionTitle) {
        boolean result = false;
        List<CertificationResult> criteria = listing.getCertificationResults();
        for (CertificationResult crit : criteria) {
            if (crit.getNumber().equalsIgnoreCase(criterionNumber)
                    && crit.getTitle().equalsIgnoreCase(criterionTitle)) {
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
