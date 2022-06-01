package gov.healthit.chpl.scheduler.job.onetime.measures;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.Query;
import javax.transaction.Transactional;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import gov.healthit.chpl.auth.user.User;
import gov.healthit.chpl.dao.MacraMeasureDAO;
import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.ListingMeasure;
import gov.healthit.chpl.domain.Measure;
import gov.healthit.chpl.domain.MeasureDomain;
import gov.healthit.chpl.entity.MacraMeasureEntity;
import gov.healthit.chpl.entity.listing.CertifiedProductDetailsEntitySimple;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.listing.measure.LegacyMacraMeasureCriterionMapEntity;
import gov.healthit.chpl.listing.measure.ListingMeasureDAO;
import gov.healthit.chpl.listing.measure.MeasureCriterionMapEntity;
import gov.healthit.chpl.listing.measure.MeasureDAO;
import gov.healthit.chpl.listing.measure.MeasureDomainEntity;
import gov.healthit.chpl.listing.measure.MeasureEntity;
import gov.healthit.chpl.scheduler.job.CertifiedProduct2015Gatherer;
import gov.healthit.chpl.service.CertificationCriterionService;
import gov.healthit.chpl.service.CertificationCriterionService.Criteria2015;
import gov.healthit.chpl.util.Util;
import lombok.extern.log4j.Log4j2;

@Log4j2(topic = "addMissingMeasuresToListingJobLogger")
public class AddMissingMeasuresToListingsJob extends CertifiedProduct2015Gatherer implements Job {

    @Autowired
    private AddMissingMeasuresToListingsJobDao jobDao;

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
        addMissingB1Measure(listing);
        addMissingB1CuresMeasure(listing);
        //TODO: More
    }

    private void addMissingB1Measure(CertifiedProductSearchDetails listing) {
        String removedMeasureVal = "RT7 EH/CAH Medicare and Medicaid PI";
        String replacedMeasureVal = "RT7 EH/CAH Medicare PI";
        CertificationCriterion b1 = criterionService.get(Criteria2015.B_1_OLD);
        Long removedMacraMeasureId = macraMeasureDao.getMacraMeasureIdByCriterionAndValue(b1.getId(), removedMeasureVal);
        Measure removedMeasure = measureDao.getMeasureByMacraMeasureId(removedMacraMeasureId);
        Long replacedMacraMeasureId = macraMeasureDao.getMacraMeasureIdByCriterionAndValue(b1.getId(), replacedMeasureVal);
        Measure replacedMeasure = measureDao.getMeasureByMacraMeasureId(replacedMacraMeasureId);

        Optional<ListingMeasure> removedMeasureOnListing = listing.getMeasures().stream()
            .filter(measure -> measure.getMeasure().getId().equals(removedMeasure.getId()))
            .filter(measure -> measure.getAssociatedCriteria().contains(b1))
            .findAny();

        Optional<ListingMeasure> replacedMeasureOnListing = listing.getMeasures().stream()
                .filter(measure -> measure.getMeasure().getId().equals(replacedMeasure.getId()))
                .filter(measure -> measure.getAssociatedCriteria().contains(b1))
                .findAny();

        if (removedMeasureOnListing.isEmpty()) {
            LOGGER.info("Certified Product " + listing.getId() + " does not have the removed measure '" + removedMeasure.getName() + "' for " + Util.formatCriteriaNumber(b1));
        } else if (removedMeasureOnListing.isPresent() && replacedMeasureOnListing.isPresent()) {
            LOGGER.info("Certified Product " + listing.getId() + " has the removed measure '" + removedMeasure.getName() + "' and the replaced measure '" + replacedMeasure.getName() + "' for " + Util.formatCriteriaNumber(b1));
        } else if (removedMeasureOnListing.isPresent() && replacedMeasureOnListing.isEmpty()) {
            LOGGER.info("Certified Product " + listing.getId() + " needs the replaced measure '" + replacedMeasure.getName() + "' for " + Util.formatCriteriaNumber(b1));
            //TODO: Add the measure

        }
    }

    private void addMissingB1CuresMeasure(CertifiedProductSearchDetails listing) {
        //"Required Test 1: Medicare and Medicaid Promoting Interoperability Programs" to "Required Test 1: Medicare Promoting Interoperability Programs"
    }


    @Component
    private static class AddMissingMeasuresToListingsJobDao extends BaseDAOImpl {
        @Autowired
        private MeasureDAO measureDAO;

        @Transactional
        public List<Long> getListingIdsAttestingToCriterion(Long criterionId) {
            Query query = entityManager.createQuery("SELECT listing "
                    + "FROM CertifiedProductDetailsEntitySimple listing, CertificationResultEntity cre "
                    + "WHERE listing.id = cre.certifiedProductId "
                    + "AND cre.deleted = false "
                    + "AND cre.certificationCriterionId = :criterionId "
                    + "AND cre.success = true "
                    + "AND listing.deleted = false ",
                    CertifiedProductDetailsEntitySimple.class);
            query.setParameter("criterionId", criterionId);
            List<CertifiedProductDetailsEntitySimple> results = query.getResultList();
            return results.stream()
                    .map(result -> result.getId())
                    .collect(Collectors.toList());
        }

        private MacraMeasureEntity getMacraMeasureById(Long id) {
            Query query = entityManager.createQuery(
                    "SELECT mme "
                    + "FROM MacraMeasureEntity mme "
                    + "LEFT OUTER JOIN FETCH mme.certificationCriterion cce "
                    + "LEFT OUTER JOIN FETCH cce.certificationEdition "
                    + "WHERE (NOT mme.deleted = true) "
                    + "AND mme.id = :id ",
                    MacraMeasureEntity.class);
            query.setParameter("id", id);
            List<MacraMeasureEntity> result = query.getResultList();
            if (result == null || result.size() == 0) {
                return null;
            }
            return result.get(0);
        }

        public MeasureDomain findMeasureDomainByDomain(String domain) throws EntityRetrievalException {
            MeasureDomainEntity entity = getMeasureDomainByDomain(domain);
            if (entity != null) {
                return entity.convert();
            } else {
                return null;
            }
        }

        private MeasureDomainEntity getMeasureDomainByDomain(String domain) throws EntityRetrievalException {
            Query query = entityManager.createQuery(
                    "SELECT md "
                    + "FROM MeasureDomainEntity md "
                    + "WHERE md.deleted = false "
                    + "AND md.domain = :domain ",
                    MeasureDomainEntity.class);
            query.setParameter("domain", domain);
            List<MeasureDomainEntity> entities = query.getResultList();

            if (entities != null && entities.size() > 0) {
                return entities.get(0);
            } else {
                throw new EntityRetrievalException(String.format("Could not locate measure domain: %s", domain));
            }

        }

        public Measure updateMeasure(Measure measure) {
            MeasureEntity result = getMeasureEntity(measure.getId());
            result.setAbbreviation(measure.getAbbreviation());
            result.setName(measure.getName());
            result.setRemoved(measure.getRemoved());

            update(result);

            return measureDAO.getById(result.getId());
        }

        public Measure createMeasure(Measure measure) {
            MeasureEntity entity = new MeasureEntity();
            entity.setDomain(MeasureDomainEntity.builder().id(measure.getDomain().getId()).build());
            entity.setAbbreviation(measure.getAbbreviation());
            entity.setRequiredTest(measure.getRequiredTest());
            entity.setName(measure.getName());
            entity.setCriteriaSelectionRequired(measure.getRequiresCriteriaSelection());
            entity.setRemoved(measure.getRemoved());
            entity.setLastModifiedUser(User.SYSTEM_USER_ID);
            entity.setDeleted(false);
            super.create(entity);
            return measureDAO.getById(entity.getId());
        }

        public MeasureEntity getMeasureEntity(Long id) {
            Query query = entityManager.createQuery(
                    MeasureDAO.MEASURE_HQL_BEGIN
                    + "WHERE measure.deleted = false "
                    + "AND measure.id = :id ",
                    MeasureEntity.class);
            query.setParameter("id", id);
            List<MeasureEntity> entities = query.getResultList();

            MeasureEntity result = null;
            if (entities != null && entities.size() > 0) {
                result = entities.get(0);
            }
            return result;
        }

        public Set<Measure> findAllMeasures() {
            return measureDAO.findAll();
        }

        public MeasureCriterionMapEntity createMeasureCrierionMap(Long certificationCriterionId, Long measureId, Long lastUpdateUserId) {
            MeasureCriterionMapEntity entity = new MeasureCriterionMapEntity();
            entity.setCertificationCriterionId(certificationCriterionId);
            entity.setMeasureId(measureId);
            entity.setLastModifiedUser(lastUpdateUserId);
            entity.setDeleted(false);

            super.create(entity);

            return entity;
        }

        public LegacyMacraMeasureCriterionMapEntity createLegacyMacraMeasureCriterionMap(LegacyMacraMeasureCriterionMapEntity entity) {
            super.create(entity);
            return entity;
        }
    }
}
