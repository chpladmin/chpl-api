package gov.healthit.chpl.scheduler.job.onetime.measures;

import java.util.Optional;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import gov.healthit.chpl.dao.MacraMeasureDAO;
import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.ListingMeasure;
import gov.healthit.chpl.domain.Measure;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.listing.measure.ListingMeasureDAO;
import gov.healthit.chpl.listing.measure.MeasureDAO;
import gov.healthit.chpl.scheduler.job.CertifiedProduct2015Gatherer;
import gov.healthit.chpl.service.CertificationCriterionService;
import gov.healthit.chpl.service.CertificationCriterionService.Criteria2015;
import gov.healthit.chpl.util.Util;
import lombok.extern.log4j.Log4j2;

@Log4j2(topic = "addMissingMeasuresToListingJobLogger")
public class AddMissingMeasuresToListingsJob extends CertifiedProduct2015Gatherer implements Job {

    @Autowired
    private MacraMeasureDAO macraMeasureDao;

    @Autowired
    private MeasureDAO measureDao;

    @Autowired
    private ListingMeasureDAO listingMeasureDao;

    @Autowired
    private CertificationCriterionService criterionService;

    @Autowired
    private JpaTransactionManager txManager;

    @Value("${executorThreadCountForQuartzJobs}")
    private Integer threadCount;

    public AddMissingMeasuresToListingsJob() throws Exception {
        super();
    }

    @Override
    public void execute(JobExecutionContext jobContext) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        LOGGER.info("********* Starting the Add Missing Measures to Listings job. *********");

        // We need to manually create a transaction in this case because of how AOP works. When a method is
        // annotated with @Transactional, the transaction wrapper is only added if the object's proxy is called.
        // The object's proxy is not called when the method is called from within this class. The object's proxy
        // is called when the method is public and is called from a different object.
        // https://stackoverflow.com/questions/3037006/starting-new-transaction-in-spring-bean
        TransactionTemplate txTemplate = new TransactionTemplate(txManager);
        txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        txTemplate.execute(new TransactionCallbackWithoutResult() {

            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                try {

                    getAll2015CertifiedProducts(LOGGER, threadCount, false).stream()
                        .forEach(listing -> addMissingMeasures(listing));

                } catch (Exception e) {
                    LOGGER.error(e);
                }
            }
        });
        LOGGER.info("********* Completed the Add Missing Measures to Listings job. *********");
    }

    private void addMissingMeasures(CertifiedProductSearchDetails listing) {
        addMissingMeasure(listing, Criteria2015.B_1_OLD, "RT7 EH/CAH Medicare and Medicaid PI", "RT7 EH/CAH Medicare PI");
        addMissingMeasure(listing, Criteria2015.B_1_CURES, "RT7 EH/CAH Medicare and Medicaid PI", "RT7 EH/CAH Medicare PI");
        addMissingMeasure(listing, Criteria2015.B_3_OLD, "RT1 EH/CAH Medicare and Medicaid PI", "RT1 EH/CAH Medicare PI");
        addMissingMeasure(listing, Criteria2015.B_3_CURES, "RT1 EH/CAH Medicare and Medicaid PI", "RT1 EH/CAH Medicare PI");
        addMissingMeasure(listing, Criteria2015.E_1_OLD, "RT2a EH/CAH Medicare and Medicaid PI", "RT2a EH/CAH Medicare PI");
        addMissingMeasure(listing, Criteria2015.E_1_CURES, "RT2a EH/CAH Medicare and Medicaid PI", "RT2a EH/CAH Medicare PI");
        addMissingMeasure(listing, Criteria2015.E_1_OLD, "RT2b EH/CAH Medicare and Medicaid PI", "RT2b EH/CAH Medicare PI");
        addMissingMeasure(listing, Criteria2015.E_1_CURES, "RT2b EH/CAH Medicare and Medicaid PI", "RT2b EH/CAH Medicare PI");
        addMissingMeasure(listing, Criteria2015.G_8, "RT2a EH/CAH Medicare and Medicaid PI", "RT2a EH/CAH Medicare PI");
        addMissingMeasure(listing, Criteria2015.G_8, "RT2c EH/CAH Medicare and Medicaid PI", "RT2c EH/CAH Medicare PI");
        addMissingMeasure(listing, Criteria2015.G_9_OLD, "RT2a EH/CAH Medicare and Medicaid PI", "RT2a EH/CAH Medicare PI");
        addMissingMeasure(listing, Criteria2015.G_9_CURES, "RT2a EH/CAH Medicare and Medicaid PI", "RT2a EH/CAH Medicare PI");
        addMissingMeasure(listing, Criteria2015.G_9_OLD, "RT2c EH/CAH Medicare and Medicaid PI", "RT2c EH/CAH Medicare PI");
        addMissingMeasure(listing, Criteria2015.G_9_CURES, "RT2c EH/CAH Medicare and Medicaid PI", "RT2c EH/CAH Medicare PI");
        addMissingMeasure(listing, Criteria2015.G_10, "RT2a EH/CAH Medicare and Medicaid PI", "RT2a EH/CAH Medicare PI");
        addMissingMeasure(listing, Criteria2015.G_10, "RT2c EH/CAH Medicare and Medicaid PI", "RT2c EH/CAH Medicare PI");
    }

    private void addMissingMeasure(CertifiedProductSearchDetails listing, String criteriaIdentifier,
            String removedMacraMeasureVal, String replacedMacraMeasureVal) {
        CertificationCriterion criterion = criterionService.get(criteriaIdentifier);
        Long removedMacraMeasureId = macraMeasureDao.getMacraMeasureIdByCriterionAndValue(criterion.getId(), removedMacraMeasureVal);
        Measure removedMeasure = measureDao.getMeasureByMacraMeasureId(removedMacraMeasureId);
        Long replacedMacraMeasureId = macraMeasureDao.getMacraMeasureIdByCriterionAndValue(criterion.getId(), replacedMacraMeasureVal);
        Measure replacedMeasure = measureDao.getMeasureByMacraMeasureId(replacedMacraMeasureId);
        addMissingMeasureIfApplicable(listing, criterion, removedMeasure, replacedMeasure);
    }

    private void addMissingMeasureIfApplicable(CertifiedProductSearchDetails listing, CertificationCriterion criterion,
            Measure removedMeasure, Measure replacedMeasure) {
        Optional<ListingMeasure> removedMeasureOnListing = listing.getMeasures().stream()
                .filter(measure -> measure.getMeasure().getId().equals(removedMeasure.getId()))
                .filter(measure -> measure.getAssociatedCriteria().contains(criterion))
                .findAny();

        Optional<ListingMeasure> replacedMeasureOnListing = listing.getMeasures().stream()
                .filter(measure -> measure.getMeasure().getId().equals(replacedMeasure.getId()))
                .filter(measure -> measure.getAssociatedCriteria().contains(criterion))
                .findAny();

        if (removedMeasureOnListing.isEmpty()) {
            LOGGER.info("Certified Product " + listing.getId() + " does not have the removed measure '" + removedMeasure.getName() + "' for " + Util.formatCriteriaNumber(criterion));
        } else if (removedMeasureOnListing.isPresent() && replacedMeasureOnListing.isPresent()) {
            LOGGER.info("Certified Product " + listing.getId() + " has the removed measure '" + removedMeasure.getName() + "' and the replaced measure '" + replacedMeasure.getName() + "' for " + Util.formatCriteriaNumber(criterion));
        } else if (removedMeasureOnListing.isPresent() && replacedMeasureOnListing.isEmpty()) {
            LOGGER.info("Certified Product " + listing.getId() + " needs the replaced measure '" + replacedMeasure.getName() + "' for " + Util.formatCriteriaNumber(criterion));
            ListingMeasure replacedListingMeasure = ListingMeasure.builder()
                    .associatedCriteria(removedMeasureOnListing.get().getAssociatedCriteria())
                    .measure(replacedMeasure)
                    .measureType(removedMeasureOnListing.get().getMeasureType())
                    .build();
            try {
                listingMeasureDao.createCertifiedProductMeasureMapping(listing.getId(), replacedListingMeasure);
            } catch (EntityCreationException ex) {
                LOGGER.error("Error adding '" + replacedMeasure.getName() + "' to listing " + listing.getId(), ex);
            }
        }
    }
}
