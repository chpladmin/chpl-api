package gov.healthit.chpl.manager;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.compliance.surveillance.SurveillanceDAO;
import gov.healthit.chpl.dao.CertificationBodyDAO;
import gov.healthit.chpl.dao.CertificationCriterionDAO;
import gov.healthit.chpl.dao.CertificationEditionDAO;
import gov.healthit.chpl.dao.DeveloperDAO;
import gov.healthit.chpl.dao.DeveloperStatusDAO;
import gov.healthit.chpl.dao.ProductDAO;
import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.domain.CertificationEdition;
import gov.healthit.chpl.domain.CriteriaSpecificDescriptiveModel;
import gov.healthit.chpl.domain.DescriptiveModel;
import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.domain.DeveloperStatus;
import gov.healthit.chpl.domain.DimensionalData;
import gov.healthit.chpl.domain.KeyValueModel;
import gov.healthit.chpl.domain.Measure;
import gov.healthit.chpl.domain.MeasureType;
import gov.healthit.chpl.domain.NonconformityType;
import gov.healthit.chpl.domain.Product;
import gov.healthit.chpl.domain.surveillance.RequirementGroupType;
import gov.healthit.chpl.domain.surveillance.RequirementType;
import gov.healthit.chpl.domain.surveillance.SurveillanceResultType;
import gov.healthit.chpl.domain.surveillance.SurveillanceType;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.listing.measure.ListingMeasureDAO;
import gov.healthit.chpl.listing.measure.MeasureDAO;
import gov.healthit.chpl.sed.AgeRange;
import gov.healthit.chpl.sed.AgeRangeDAO;
import gov.healthit.chpl.sed.EducationType;
import gov.healthit.chpl.sed.EducationTypeDAO;
import gov.healthit.chpl.surveillance.report.QuarterDAO;
import gov.healthit.chpl.surveillance.report.domain.Quarter;
import gov.healthit.chpl.targeteduser.TargetedUser;
import gov.healthit.chpl.targeteduser.TargetedUserDAO;
import gov.healthit.chpl.testdata.TestDataCriteriaMap;
import gov.healthit.chpl.testdata.TestDataDAO;
import gov.healthit.chpl.teststandard.TestStandard;
import gov.healthit.chpl.teststandard.TestStandardDAO;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Service("dimensionalDataManager")
public class DimensionalDataManager {
    private CacheableDimensionalDataManager cacheableDimensionalDataManager;
    private CertificationBodyDAO certificationBodyDao;
    private CertificationCriterionDAO certificationCriterionDao;
    private EducationTypeDAO educationTypeDao;
    private AgeRangeDAO ageRangeDao;
    private TestStandardDAO testStandardDao;
    private TestDataDAO testDataDao;
    private TargetedUserDAO tuDao;
    private DeveloperStatusDAO devStatusDao;
    private SurveillanceDAO survDao;
    private QuarterDAO quarterDao;
    private ProductDAO productDao;
    private DeveloperDAO devDao;
    private MeasureDAO measureDao;
    private ListingMeasureDAO listingMeasureDao;
    private CertificationEditionDAO certEditionDao;

    @Autowired
    @SuppressWarnings("checkstyle:parameternumber")
    public DimensionalDataManager(CacheableDimensionalDataManager cacheableDimensionalDataManager,
                                  CertificationBodyDAO certificationBodyDao, CertificationCriterionDAO certificationCriterionDao,
                                  EducationTypeDAO educationTypeDao, AgeRangeDAO ageRangeDao,
                                  TestStandardDAO testStandardDao,
                                  TestDataDAO testDataDao,
                                  TargetedUserDAO tuDao, DeveloperStatusDAO devStatusDao,
                                  SurveillanceDAO survDao, QuarterDAO quarterDao,
                                  ProductDAO productDao, DeveloperDAO devDao, MeasureDAO measureDao,
                                  ListingMeasureDAO listingMeasureDao,
                                  CertificationEditionDAO certEditionDao) {
        this.cacheableDimensionalDataManager = cacheableDimensionalDataManager;
        this.certificationBodyDao = certificationBodyDao;
        this.certificationCriterionDao = certificationCriterionDao;
        this.educationTypeDao = educationTypeDao;
        this.ageRangeDao = ageRangeDao;
        this.testStandardDao = testStandardDao;
        this.testDataDao = testDataDao;
        this.tuDao = tuDao;
        this.devStatusDao = devStatusDao;
        this.survDao = survDao;
        this.quarterDao = quarterDao;
        this.productDao = productDao;
        this.devDao = devDao;
        this.measureDao = measureDao;
        this.listingMeasureDao = listingMeasureDao;
        this.certEditionDao = certEditionDao;
    }

    @Deprecated
    @Transactional
    public Set<KeyValueModel> getQuarters() {
        LOGGER.debug("Getting all quarters from the database (not cached).");
        List<Quarter> quarters = quarterDao.getAll();
        Set<KeyValueModel> results = new HashSet<KeyValueModel>();
        for (Quarter dto : quarters) {
            String description = dto.getStartMonth() + "/" + dto.getStartDay()
                + " - " + dto.getEndMonth() + "/" + dto.getEndDay();
            results.add(new KeyValueModel(dto.getId(), dto.getName(), description));
        }
        return results;
    }

    @Transactional
    @Deprecated
    public Set<CertificationBody> getAllAcbs() {
        LOGGER.debug("Getting all certification body names from the database (not cached).");
        List<CertificationBody> acbs = this.certificationBodyDao.findAll();
        return acbs.stream()
                .collect(Collectors.toSet());
    }

    @Deprecated
    @Transactional
    public Set<KeyValueModel> getEducationTypes() {
        LOGGER.debug("Getting all education types from the database (not cached).");
        List<EducationType> educationTypes = this.educationTypeDao.getAll();
        return educationTypes.stream()
                .map(et -> new KeyValueModel(et.getId(), et.getName()))
                .collect(Collectors.toSet());
    }

    @Deprecated
    @Transactional
    public Set<KeyValueModel> getAgeRanges() {
        LOGGER.debug("Getting all age ranges from the database (not cached).");
        List<AgeRange> ageRanges = this.ageRangeDao.getAll();
        return ageRanges.stream()
                .map(ar -> new KeyValueModel(ar.getId(), ar.getName()))
                .collect(Collectors.toSet());
    }

    @Deprecated
    @Transactional
    public Set<KeyValueModel> getDeveloperStatuses() {
        LOGGER.debug("Getting all developer statuses from the database (not cached).");
        List<DeveloperStatus> devStatuses = this.devStatusDao.findAll();
        Set<KeyValueModel> statuses = new HashSet<KeyValueModel>();

        for (DeveloperStatus devStatus : devStatuses) {
            statuses.add(new KeyValueModel(devStatus.getId(), devStatus.getName()));
        }

        return statuses;
    }

    @Deprecated
    public Set<KeyValueModel> getTargetedUesrs() {
        List<TargetedUser> dtos = this.tuDao.findAll();
        Set<KeyValueModel> standards = new HashSet<KeyValueModel>();

        for (TargetedUser dto : dtos) {
            standards.add(new KeyValueModel(dto.getId(), dto.getName()));
        }
        return standards;
    }

    @Deprecated
    @Transactional
    public Set<TestStandard> getTestStandards() {
        LOGGER.debug("Getting all test standards from the database (not cached).");
        return this.testStandardDao.findAll().stream()
                .collect(Collectors.toSet());
    }

    @Deprecated
    public Set<KeyValueModel> getSurveillanceTypes() {
        LOGGER.debug("Getting all surveillance types from the database (not cached).");

        List<SurveillanceType> daoResults = survDao.getAllSurveillanceTypes();
        Set<KeyValueModel> results = new HashSet<KeyValueModel>();

        for (SurveillanceType result : daoResults) {
            results.add(new KeyValueModel(result.getId(), result.getName()));
        }
        return results;
    }

    @Deprecated
    @Transactional
    public Set<RequirementType> getRequirementTypes() {
        LOGGER.debug("Getting all requirement detail types from the database (not cached).");

        return survDao.getRequirementTypes().stream()
                .collect(Collectors.toSet());
    }

    @Deprecated
    public Set<NonconformityType> getNonconformityTypes() {
        LOGGER.debug("Getting all nonconformity types from the database (not cached).");

        return survDao.getNonconformityTypes().stream()
                .collect(Collectors.toSet());
    }

    @Deprecated
    public Set<KeyValueModel> getRequirementGroupTypes() {
        LOGGER.debug("Getting all requirement group types from the database (not cached).");

        List<RequirementGroupType> daoResults = survDao.getAllRequirementGroupTypes();
        Set<KeyValueModel> results = new HashSet<KeyValueModel>();

        for (RequirementGroupType result : daoResults) {
            results.add(new KeyValueModel(result.getId(), result.getName()));
        }
        return results;
    }

    @Deprecated
    public Set<KeyValueModel> getSurveillanceResultTypes() {
        LOGGER.debug("Getting all surveillance result types from the database (not cached).");

        List<SurveillanceResultType> daoResults = survDao.getAllSurveillanceResultTypes();
        Set<KeyValueModel> results = new HashSet<KeyValueModel>();

        for (SurveillanceResultType result : daoResults) {
            results.add(new KeyValueModel(result.getId(), result.getName()));
        }
        return results;
    }

    @Deprecated
    @Transactional
    public Set<Measure> getMeasures() {
        return measureDao.findAll();
    }

    @Deprecated
    @Transactional
    public Set<MeasureType> getMeasureTypes() {
        return listingMeasureDao.getMeasureTypes();
    }

    @Deprecated
    @Transactional
    public Set<CriteriaSpecificDescriptiveModel> getTestData() {
        LOGGER.debug("Getting all test data from the database (not cached).");

        List<TestDataCriteriaMap> testDataDtos = testDataDao.findAllWithMappedCriteria();
        Set<CriteriaSpecificDescriptiveModel> testData = new HashSet<CriteriaSpecificDescriptiveModel>();

        for (TestDataCriteriaMap dto : testDataDtos) {
            testData.add(new CriteriaSpecificDescriptiveModel(
                    dto.getTestDataId(), dto.getTestData().getName(), null,
                    null, dto.getCriteria()));
        }
        return testData;
    }

    @Deprecated
    @Transactional
    public List<CertificationEdition> getCertificationEditions() {
        return certEditionDao.findAll();
    }

    public DimensionalData getDimensionalData(final Boolean simple) throws EntityRetrievalException {
        DimensionalData result = new DimensionalData();

        List<Product> products = productDao.findAllIdsAndNames();
        Set<KeyValueModel> productNames = new HashSet<KeyValueModel>();
        for (Product product : products) {
            productNames.add(new KeyValueModel(product.getId(), product.getName()));
        }
        result.setProducts(productNames);

        List<Developer> developers = devDao.findAllIdsAndNames();
        Set<KeyValueModel> developerNames = new HashSet<KeyValueModel>();
        for (Developer dev : developers) {
            developerNames.add(new KeyValueModel(dev.getId(), dev.getName()));
        }
        result.setDevelopers(developerNames);

        Set<CertificationCriterion> criteria = this.certificationCriterionDao.findAll().stream()
                .collect(Collectors.toSet());
        result.setCertificationCriteria(criteria);
        result.setAcbs(getAllAcbs());
        result.setEditions(getEditionNames(simple));
        result.setCertificationStatuses(getCertificationStatuses());
        result.setPracticeTypes(getPracticeTypeNames());
        result.setProductClassifications(getClassificationNames());
        result.setCqms(getCQMCriterionNumbers(simple));
        return result;
    }

    // The following methods are called from inside this class as searchable dimensional data.
    // Since they are called from within the same class, any annotations on the below methods
    // are ignored due to Spring's proxying mechanism. Therefore the caching of these methods
    // has been moved to the CacheableDimensionalDataModel class which is called through.
    @Deprecated
    public Set<KeyValueModel> getClassificationNames() {
        return cacheableDimensionalDataManager.getClassificationNames();
    }

    @Deprecated
    public Set<KeyValueModel> getEditionNames(final Boolean simple) {
       return cacheableDimensionalDataManager.getEditionNames(simple);
    }

    @Deprecated
    public Set<KeyValueModel> getCertificationStatuses() {
        return cacheableDimensionalDataManager.getCertificationStatuses();
    }

    @Deprecated
    public Set<KeyValueModel> getPracticeTypeNames() {
        return cacheableDimensionalDataManager.getPracticeTypeNames();
    }

    @Deprecated
    public Set<DescriptiveModel> getCQMCriterionNumbers(final Boolean simple) {
        return cacheableDimensionalDataManager.getCQMCriterionNumbers(simple);
    }
}
