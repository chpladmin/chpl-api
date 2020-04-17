package gov.healthit.chpl.scheduler.job;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import com.fasterxml.jackson.core.JsonProcessingException;

import gov.healthit.chpl.SpecialProperties;
import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.dao.CuresUpdateEventDAO;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.activity.ActivityConcept;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.dto.CuresUpdateEventDTO;
import gov.healthit.chpl.entity.CertificationStatusType;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.manager.ActivityManager;
import gov.healthit.chpl.manager.CertifiedProductDetailsManager;
import gov.healthit.chpl.service.CuresUpdateService;

public class AddCuresUpdateToListingsJob extends QuartzJob {
    private static final Logger LOGGER = LogManager.getLogger("addCuresUpdateToListingsJobLogger");

    @Autowired
    private CuresUpdateService curesUpdateService;

    @Autowired
    private CertifiedProductDetailsManager certifiedProductDetailsManager;

    @Autowired
    private CertifiedProductDAO certifiedProductDAO;

    @Autowired
    private SpecialProperties specialProperties;

    @Autowired
    private CuresUpdateEventDAO curesUpdateDao;

    @Autowired
    private ActivityManager activityManager;

    @Autowired
    private JpaTransactionManager txManager;

    private List<CertificationStatusType> activeCertificationStatusTypes = new ArrayList<CertificationStatusType>();

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        LOGGER.info("********* Starting the Update Listing To Cures Updated job. *********");

        activeCertificationStatusTypes.add(CertificationStatusType.Active);
        activeCertificationStatusTypes.add(CertificationStatusType.SuspendedByAcb);
        activeCertificationStatusTypes.add(CertificationStatusType.SuspendedByOnc);

        getAll2015Listings().stream()
                .forEach(dto -> processListing(dto));

        LOGGER.info("********* Completed the Update Listing To Cures Updated  job. *********");

    }

    private void processListing(CertifiedProductDetailsDTO cp) {
        try {
            LOGGER.info("Retrieving listing information for  (" + cp.getId() + ") " + cp.getChplProductNumber());
            CertifiedProductSearchDetails listing = certifiedProductDetailsManager.getCertifiedProductDetails(cp.getId());
            if (isListingActive(listing) && curesUpdateService.isCuresUpdate(listing)) {
                LOGGER.info("************************** Updating " + cp.getId() + " as Cures Updated **************************");
                updateListingAsCuresUpdated(listing);
            } else {
                updateListingAsNotCuresUpdated(listing);
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    private void updateListingAsCuresUpdated(CertifiedProductSearchDetails origListing)
            throws EntityCreationException, EntityRetrievalException, JsonProcessingException {

        TransactionTemplate txTemplate = new TransactionTemplate(txManager);
        txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        txTemplate.execute(new TransactionCallbackWithoutResult() {

            @Override
            // Wrapping the dao call in a transaction, since we are not using managers for the txn
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                try {
                    addCuresUpdateEvent(origListing.getId(), true, specialProperties.getEffectiveRuleDate());
                    addCuresUpdateActivity(origListing,
                            certifiedProductDetailsManager.getCertifiedProductDetails(origListing.getId()));
                } catch (Exception e) {
                    LOGGER.error("Error trying to mark listing " + origListing.getChplProductNumber() + "as Cures updated. "
                            + e.getMessage(), e);
                    status.setRollbackOnly();
                }
            }
        });
    }

    private void updateListingAsNotCuresUpdated(CertifiedProductSearchDetails origListing)
            throws EntityCreationException, EntityRetrievalException, JsonProcessingException {

        if (!doesListingHaveExistingCurrentCuresEventThatIsFalse(origListing)) {
            TransactionTemplate txTemplate = new TransactionTemplate(txManager);
            txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
            txTemplate.execute(new TransactionCallbackWithoutResult() {

                @Override
                // Wrapping the dao call in a transaction, since we are not using managers for the txn
                protected void doInTransactionWithoutResult(TransactionStatus status) {
                    try {
                        addCuresUpdateEvent(origListing.getId(), false, specialProperties.getEffectiveRuleDate());
                    } catch (Exception e) {
                        LOGGER.error(
                                "Error trying to mark listing " + origListing.getChplProductNumber() + "as NOT Cures updated. "
                                        + e.getMessage(),
                                e);
                        status.setRollbackOnly();
                    }
                }
            });
        }
    }

    private void addCuresUpdateActivity(CertifiedProductSearchDetails origListing, CertifiedProductSearchDetails updatedListing)
            throws JsonProcessingException, EntityCreationException, EntityRetrievalException {
        activityManager.addActivity(ActivityConcept.CERTIFIED_PRODUCT, origListing.getId(),
                "Updated certified product " + updatedListing.getChplProductNumber() + ".", origListing,
                updatedListing, "");
    }

    private void addCuresUpdateEvent(Long certifiedProductId, Boolean curesUpdate, Date eventDate)
            throws EntityCreationException, EntityRetrievalException {
        CuresUpdateEventDTO dto = new CuresUpdateEventDTO();
        dto.setCertifiedProductId(certifiedProductId);
        dto.setCuresUpdate(curesUpdate);
        dto.setEventDate(eventDate);
        curesUpdateDao.create(dto);
    }

    private boolean isListingActive(CertifiedProductSearchDetails listing) {
        return activeCertificationStatusTypes.stream()
                .filter(status -> status.getName().equals(listing.getCurrentStatus().getStatus().getName()))
                .findAny()
                .isPresent();
    }

    private List<CertifiedProductDetailsDTO> getAll2015Listings() {
        return certifiedProductDAO.findByEdition("2015");
    }

    private Boolean doesListingHaveExistingCurrentCuresEventThatIsFalse(CertifiedProductSearchDetails listing) {
        Optional<CuresUpdateEventDTO> curesUpdateEvent = getCurrentCuresEventForListing(listing);
        if (curesUpdateEvent.isPresent()) {
            return !curesUpdateEvent.get().getCuresUpdate();
        } else {
            return false;
        }
    }

    private Optional<CuresUpdateEventDTO> getCurrentCuresEventForListing(CertifiedProductSearchDetails listing) {
        return curesUpdateDao.findByCertifiedProductId(listing.getId()).stream()
                .sorted(Comparator.comparing(CuresUpdateEventDTO::getEventDate))
                .findFirst();
    }
}
