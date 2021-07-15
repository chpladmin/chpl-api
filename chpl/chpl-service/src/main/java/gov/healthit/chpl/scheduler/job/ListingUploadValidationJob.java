package gov.healthit.chpl.scheduler.job;

import java.util.List;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobDataMap;
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

import gov.healthit.chpl.auth.user.JWTAuthenticatedUser;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.ListingUpload;
import gov.healthit.chpl.dto.auth.UserDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.upload.listing.ListingUploadDao;
import gov.healthit.chpl.upload.listing.ListingUploadManager;
import gov.healthit.chpl.upload.listing.ListingUploadStatus;
import lombok.extern.log4j.Log4j2;

@DisallowConcurrentExecution
@Log4j2(topic = "listingUploadValidationJobLogger")
public class ListingUploadValidationJob implements Job {
    public static final String JOB_NAME = "listingUploadValidationJob";
    public static final String LISTING_UPLOAD_IDS = "listingUploadIds";
    public static final String USER_KEY = "user";

    @Autowired
    private JpaTransactionManager txManager;

    @Autowired
    private ListingUploadDao listingUploadDao;

    @Autowired
    private ListingUploadManager listingUploadManager;

    @Override
    public void execute(JobExecutionContext jobContext) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);

        LOGGER.info("********* Starting the Listing Upload Validation job. *********");

        JobDataMap jobDataMap = jobContext.getMergedJobDataMap();
        UserDTO user = (UserDTO) jobDataMap.get(USER_KEY);
        if (user == null) {
            LOGGER.fatal("No user could be found in the job data.");
        } else {
            try {
                setSecurityContext(user);

                @SuppressWarnings("unchecked")
                List<Long> listingUploadIds = (List<Long>) jobDataMap.get(LISTING_UPLOAD_IDS);
                if (listingUploadIds == null || listingUploadIds.size() == 0) {
                    LOGGER.error("Missing parameter for listing upload IDs.");
                } else {
                    for (Long listingUploadId : listingUploadIds) {
                        try {
                            calculateErrorAndWarningCounts(listingUploadId);
                        } catch (Exception ex) {
                            LOGGER.error("Unexpected exception calculating error/warning counts for listing upload with ID " + listingUploadId);
                            LOGGER.catching(ex);
                            setFailedValidationErrorAndWarningCounts(listingUploadId);
                        }
                    }
                }
            } catch (Exception ex) {
                LOGGER.fatal("Unexpected exception was caught. All listing uploads may not have been processed.", ex);
            }
        }
        LOGGER.info("********* Completed the Listing Upload Validation job. *********");
    }

    private void calculateErrorAndWarningCounts(Long listingUploadId) {
        TransactionTemplate txTemplate = new TransactionTemplate(txManager);
        txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        txTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                ListingUpload listingUpload = null;
                try {
                    listingUpload = listingUploadDao.getById(listingUploadId);
                } catch (EntityRetrievalException ex) {
                    LOGGER.error("Unable to get listing upload with id "
                            + listingUploadId + ". Error and warning counts will be set to -1.", ex);
                }

                LOGGER.info("Calculating error and warning counts for listing upload ID " + listingUploadId
                        + "; CHPL Product Number " + listingUpload.getChplProductNumber());
                CertifiedProductSearchDetails validatedListingUpload = null;
                try {
                    validatedListingUpload = listingUploadManager.getDetailsById(listingUpload.getId());
                } catch (Exception ex) {
                    LOGGER.error("Unable to get listing upload details with id "
                            + listingUpload.getId() + ". Error and warning counts will be set to -1.", ex);
                }

                if (listingUpload != null && validatedListingUpload != null) {
                    listingUpload.setStatus(ListingUploadStatus.SUCCESSFUL);
                    listingUpload.setErrorCount(validatedListingUpload.getErrorMessages() == null ? 0 : validatedListingUpload.getErrorMessages().size());
                    listingUpload.setWarningCount(validatedListingUpload.getWarningMessages() == null ? 0 : validatedListingUpload.getWarningMessages().size());
                    LOGGER.info("Listing upload with ID " + listingUpload.getId() + " had "
                            + listingUpload.getErrorCount() + " errors and " + listingUpload.getWarningCount()
                            + " warnings.");
                    listingUploadDao.updateErrorAndWarningCounts(listingUpload);
                }
            }
        });
    }

    private void setFailedValidationErrorAndWarningCounts(Long listingUploadId) {
        TransactionTemplate txTemplate = new TransactionTemplate(txManager);
        txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        txTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                listingUploadDao.updateStatus(listingUploadId, ListingUploadStatus.FAILED);
            }
        });
    }

    private void setSecurityContext(UserDTO user) {
        JWTAuthenticatedUser splitUser = new JWTAuthenticatedUser();
        splitUser.setFullName(user.getFullName());
        splitUser.setId(user.getId());
        splitUser.setFriendlyName(user.getFriendlyName());
        splitUser.setSubjectName(user.getUsername());
        splitUser.getPermissions().add(user.getPermission().getGrantedPermission());

        SecurityContextHolder.getContext().setAuthentication(splitUser);
        SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_INHERITABLETHREADLOCAL);
    }
}
