package gov.healthit.chpl.scheduler.job;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.quartz.Job;
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
import gov.healthit.chpl.certifiedproduct.CertifiedProductDetailsManager;
import gov.healthit.chpl.dao.CuresUpdateEventDAO;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.activity.ActivityConcept;
import gov.healthit.chpl.dto.CuresUpdateEventDTO;
import gov.healthit.chpl.entity.CertificationStatusType;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.manager.ActivityManager;
import gov.healthit.chpl.service.CuresUpdateService;
import lombok.extern.log4j.Log4j2;

@Log4j2(topic = "addCuresUpdateToListingsJobLogger")
public class AddCuresUpdateToListingsJob extends CertifiedProduct2015Gatherer implements Job {

    @Autowired
    private CuresUpdateService curesUpdateService;

    @Autowired
    private CertifiedProductDetailsManager certifiedProductDetailsManager;

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

        try {
            getAll2015CertifiedProducts(LOGGER, 2).stream()
                    .forEach(dto -> processListing(dto));
        } catch (Exception e) {
            LOGGER.catching(e);
        }
        LOGGER.info("********* Completed the Update Listing To Cures Updated  job. *********");

    }

    private void processListing(CertifiedProductSearchDetails listing) {
        try {
            if (isListingActive(listing)) {
                Boolean isCuresUpdate = curesUpdateService.isCuresUpdate(listing);
                LOGGER.info(String.format("Listing ID: %s     isCuresUpdated: %s" , listing.getId(), isCuresUpdate));
                if (isCuresUpdate && !isCuresUpdate.equals(listing.getCuresUpdate())) {
                    LOGGER.info("************************** Updating " + listing.getId() + " as Cures Updated **************************");
                    updateListingAsCuresUpdated(listing);
                } else if (!isCuresUpdate && !isCuresUpdate.equals(listing.getCuresUpdate())) {
                    LOGGER.info("************************** Updating " + listing.getId() + " as NOT Cures Updated **************************");
                    updateListingAsNotCuresUpdated(listing);
                }
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
                    addCuresUpdateEvent(origListing.getId(), true, specialProperties.getEffectiveRuleDatePlus18Months());
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
                        addCuresUpdateEvent(origListing.getId(), false, specialProperties.getEffectiveRuleDatePlus18Months());
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

