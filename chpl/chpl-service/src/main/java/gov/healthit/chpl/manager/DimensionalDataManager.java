package gov.healthit.chpl.manager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.caching.CacheNames;
import gov.healthit.chpl.dao.AccessibilityStandardDAO;
import gov.healthit.chpl.dao.AgeRangeDAO;
import gov.healthit.chpl.dao.CQMCriterionDAO;
import gov.healthit.chpl.dao.CertificationBodyDAO;
import gov.healthit.chpl.dao.CertificationCriterionDAO;
import gov.healthit.chpl.dao.CertificationEditionDAO;
import gov.healthit.chpl.dao.DeveloperDAO;
import gov.healthit.chpl.dao.DeveloperStatusDAO;
import gov.healthit.chpl.dao.EducationTypeDAO;
import gov.healthit.chpl.dao.ProductDAO;
import gov.healthit.chpl.dao.QmsStandardDAO;
import gov.healthit.chpl.dao.TargetedUserDAO;
import gov.healthit.chpl.dao.TestDataDAO;
import gov.healthit.chpl.dao.TestFunctionalityDAO;
import gov.healthit.chpl.dao.TestProcedureDAO;
import gov.healthit.chpl.dao.TestStandardDAO;
import gov.healthit.chpl.dao.UcdProcessDAO;
import gov.healthit.chpl.dao.UploadTemplateVersionDAO;
import gov.healthit.chpl.dao.surveillance.SurveillanceDAO;
import gov.healthit.chpl.domain.CQMCriterion;
import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.CertificationEdition;
import gov.healthit.chpl.domain.CriteriaSpecificDescriptiveModel;
import gov.healthit.chpl.domain.DescriptiveModel;
import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.domain.DeveloperStatus;
import gov.healthit.chpl.domain.DimensionalData;
import gov.healthit.chpl.domain.KeyValueModel;
import gov.healthit.chpl.domain.KeyValueModelStatuses;
import gov.healthit.chpl.domain.Measure;
import gov.healthit.chpl.domain.MeasureType;
import gov.healthit.chpl.domain.NonconformityType;
import gov.healthit.chpl.domain.Product;
import gov.healthit.chpl.domain.TestFunctionality;
import gov.healthit.chpl.domain.TestStandard;
import gov.healthit.chpl.domain.UploadTemplateVersion;
import gov.healthit.chpl.domain.concept.RequirementTypeEnum;
import gov.healthit.chpl.domain.surveillance.SurveillanceRequirementOptions;
import gov.healthit.chpl.domain.surveillance.SurveillanceRequirementType;
import gov.healthit.chpl.domain.surveillance.SurveillanceResultType;
import gov.healthit.chpl.domain.surveillance.SurveillanceType;
import gov.healthit.chpl.dto.AccessibilityStandardDTO;
import gov.healthit.chpl.dto.AgeRangeDTO;
import gov.healthit.chpl.dto.CQMCriterionDTO;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.dto.CertificationCriterionDTO;
import gov.healthit.chpl.dto.CertificationEditionDTO;
import gov.healthit.chpl.dto.EducationTypeDTO;
import gov.healthit.chpl.dto.QmsStandardDTO;
import gov.healthit.chpl.dto.TargetedUserDTO;
import gov.healthit.chpl.dto.TestDataCriteriaMapDTO;
import gov.healthit.chpl.dto.TestFunctionalityDTO;
import gov.healthit.chpl.dto.TestProcedureCriteriaMapDTO;
import gov.healthit.chpl.dto.TestStandardDTO;
import gov.healthit.chpl.dto.UcdProcessDTO;
import gov.healthit.chpl.dto.UploadTemplateVersionDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.listing.measure.ListingMeasureDAO;
import gov.healthit.chpl.listing.measure.MeasureDAO;
import gov.healthit.chpl.optionalStandard.dao.OptionalStandardDAO;
import gov.healthit.chpl.optionalStandard.domain.OptionalStandard;
import gov.healthit.chpl.optionalStandard.entity.OptionalStandardEntity;
import gov.healthit.chpl.surveillance.report.QuarterDAO;
import gov.healthit.chpl.surveillance.report.domain.Quarter;
import gov.healthit.chpl.util.Removable;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Service("dimensionalDataManager")
public class DimensionalDataManager {
    private CacheableDimensionalDataManager cacheableDimensionalDataManager;
    private CertificationBodyDAO certificationBodyDao;
    private CertificationCriterionDAO certificationCriterionDao;
    private EducationTypeDAO educationTypeDao;
    private AgeRangeDAO ageRangeDao;
    private OptionalStandardDAO optionalStandardDao;
    private TestFunctionalityDAO testFuncDao;
    private TestStandardDAO testStandardDao;
    private TestProcedureDAO testProcedureDao;
    private TestDataDAO testDataDao;
    private AccessibilityStandardDAO asDao;
    private UcdProcessDAO ucdDao;
    private QmsStandardDAO qmsDao;
    private TargetedUserDAO tuDao;
    private DeveloperStatusDAO devStatusDao;
    private SurveillanceDAO survDao;
    private UploadTemplateVersionDAO uploadTemplateDao;
    private QuarterDAO quarterDao;
    private ProductDAO productDao;
    private DeveloperDAO devDao;
    private MeasureDAO measureDao;
    private ListingMeasureDAO listingMeasureDao;
    private CQMCriterionDAO cqmCriterionDao;
    private CertificationEditionDAO certEditionDao;

    @Autowired
    @SuppressWarnings("checkstyle:parameternumber")
    public DimensionalDataManager(CacheableDimensionalDataManager cacheableDimensionalDataManager,
                                  CertificationBodyDAO certificationBodyDao, CertificationCriterionDAO certificationCriterionDao,
                                  EducationTypeDAO educationTypeDao, AgeRangeDAO ageRangeDao, TestFunctionalityDAO testFuncDao,
                                  TestStandardDAO testStandardDao, TestProcedureDAO testProcedureDao,
                                  TestDataDAO testDataDao, AccessibilityStandardDAO asDao, UcdProcessDAO ucdDao,
                                  QmsStandardDAO qmsDao, TargetedUserDAO tuDao, DeveloperStatusDAO devStatusDao,
                                  SurveillanceDAO survDao, UploadTemplateVersionDAO uploadTemplateDao, QuarterDAO quarterDao,
                                  ProductDAO productDao, DeveloperDAO devDao, MeasureDAO measureDao,
                                  ListingMeasureDAO listingMeasureDao, CQMCriterionDAO cqmCriterionDao,
                                  CertificationEditionDAO certEditionDao, OptionalStandardDAO optionalStandardDao) {
        this.cacheableDimensionalDataManager = cacheableDimensionalDataManager;
        this.certificationBodyDao = certificationBodyDao;
        this.certificationCriterionDao = certificationCriterionDao;
        this.educationTypeDao = educationTypeDao;
        this.ageRangeDao = ageRangeDao;
        this.optionalStandardDao = optionalStandardDao;
        this.testFuncDao = testFuncDao;
        this.testStandardDao = testStandardDao;
        this.testProcedureDao = testProcedureDao;
        this.testDataDao = testDataDao;
        this.asDao = asDao;
        this.ucdDao = ucdDao;
        this.qmsDao = qmsDao;
        this.tuDao = tuDao;
        this.devStatusDao = devStatusDao;
        this.survDao = survDao;
        this.uploadTemplateDao = uploadTemplateDao;
        this.quarterDao = quarterDao;
        this.productDao = productDao;
        this.devDao = devDao;
        this.measureDao = measureDao;
        this.listingMeasureDao = listingMeasureDao;
        this.cqmCriterionDao = cqmCriterionDao;
        this.certEditionDao = certEditionDao;
    }

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
    public Set<CertificationBody> getCertBodyNames() {
        LOGGER.debug("Getting all certification body names from the database (not cached).");
        List<CertificationBodyDTO> dtos = this.certificationBodyDao.findAll();
        Set<CertificationBody> acbNames = new HashSet<CertificationBody>();

        for (CertificationBodyDTO dto : dtos) {
            acbNames.add(new CertificationBody(dto));
        }
        return acbNames;
    }

    @Transactional
    public Set<KeyValueModel> getEducationTypes() {
        LOGGER.debug("Getting all education types from the database (not cached).");
        List<EducationTypeDTO> dtos = this.educationTypeDao.getAll();
        Set<KeyValueModel> educationTypes = new HashSet<KeyValueModel>();

        for (EducationTypeDTO dto : dtos) {
            educationTypes.add(new KeyValueModel(dto.getId(), dto.getName()));
        }

        return educationTypes;
    }

    @Transactional
    public Set<KeyValueModel> getAgeRanges() {
        LOGGER.debug("Getting all age ranges from the database (not cached).");
        List<AgeRangeDTO> dtos = this.ageRangeDao.getAll();
        Set<KeyValueModel> ageRanges = new HashSet<KeyValueModel>();

        for (AgeRangeDTO dto : dtos) {
            ageRanges.add(new KeyValueModel(dto.getId(), dto.getAge()));
        }

        return ageRanges;
    }

    @Transactional
    public Set<OptionalStandard> getOptionalStandards() {
        LOGGER.debug("Getting all optional standards from the database (not cached).");

        List<OptionalStandardEntity> entities = this.optionalStandardDao.findAll();
        Set<OptionalStandard> optionalStds = new HashSet<OptionalStandard>();

        for (OptionalStandardEntity entity : entities) {
            optionalStds.add(new OptionalStandard(entity));
        }

        return optionalStds;
    }

    @Transactional
    public Set<TestFunctionality> getTestFunctionality() {
        LOGGER.debug("Getting all test functionality from the database (not cached).");
        List<TestFunctionalityDTO> dtos = this.testFuncDao.findAll();
        Set<TestFunctionality> testFuncs = new HashSet<TestFunctionality>();

        for (TestFunctionalityDTO dto : dtos) {
            testFuncs.add(new TestFunctionality(dto));
        }

        return testFuncs;
    }

    @Transactional
    public Set<KeyValueModel> getDeveloperStatuses() {
        LOGGER.debug("Getting all developer statuses from the database (not cached).");
        List<DeveloperStatus> devStatuses = this.devStatusDao.findAll();
        Set<KeyValueModel> statuses = new HashSet<KeyValueModel>();

        for (DeveloperStatus devStatus : devStatuses) {
            statuses.add(new KeyValueModel(devStatus.getId(), devStatus.getStatus()));
        }

        return statuses;
    }

    public Set<KeyValueModel> getAccessibilityStandards() {
        LOGGER.debug("Getting all accessibility standards from the database (not cached).");

        List<AccessibilityStandardDTO> dtos = this.asDao.findAll();
        Set<KeyValueModel> standards = new HashSet<KeyValueModel>();

        for (AccessibilityStandardDTO dto : dtos) {
            standards.add(new KeyValueModel(dto.getId(), dto.getName()));
        }
        return standards;
    }

    public Set<KeyValueModel> getUcdProcesses() {
        LOGGER.debug("Getting all ucd processesfrom the database (not cached).");

        List<UcdProcessDTO> dtos = this.ucdDao.findAll();
        Set<KeyValueModel> ucds = new HashSet<KeyValueModel>();

        for (UcdProcessDTO dto : dtos) {
            ucds.add(new KeyValueModel(dto.getId(), dto.getName()));
        }
        return ucds;
    }

    public Set<KeyValueModel> getQmsStandards() {
        LOGGER.debug("Getting all qms standards from the database (not cached).");

        List<QmsStandardDTO> dtos = this.qmsDao.findAll();
        Set<KeyValueModel> qms = new HashSet<KeyValueModel>();

        for (QmsStandardDTO dto : dtos) {
            qms.add(new KeyValueModel(dto.getId(), dto.getName()));
        }
        return qms;
    }

    public Set<KeyValueModel> getTargetedUesrs() {
        List<TargetedUserDTO> dtos = this.tuDao.findAll();
        Set<KeyValueModel> standards = new HashSet<KeyValueModel>();

        for (TargetedUserDTO dto : dtos) {
            standards.add(new KeyValueModel(dto.getId(), dto.getName()));
        }
        return standards;
    }

    @Transactional
    public Set<TestStandard> getTestStandards() {
        LOGGER.debug("Getting all test standards from the database (not cached).");

        List<TestStandardDTO> dtos = this.testStandardDao.findAll();
        Set<TestStandard> testStds = new HashSet<TestStandard>();

        for (TestStandardDTO dto : dtos) {
            testStds.add(new TestStandard(dto));
        }

        return testStds;
    }

    public Set<KeyValueModel> getSurveillanceTypes() {
        LOGGER.debug("Getting all surveillance types from the database (not cached).");

        List<SurveillanceType> daoResults = survDao.getAllSurveillanceTypes();
        Set<KeyValueModel> results = new HashSet<KeyValueModel>();

        for (SurveillanceType result : daoResults) {
            results.add(new KeyValueModel(result.getId(), result.getName()));
        }
        return results;
    }

    public SurveillanceRequirementOptions getSurveillanceRequirementOptions() {
        LOGGER.debug("Getting all surveillance requirements from the database (not cached).");

        SurveillanceRequirementOptions result = new SurveillanceRequirementOptions();

        List<CertificationCriterionDTO> criteria2014 = certificationCriterionDao.findByCertificationEditionYear("2014");
        for (CertificationCriterionDTO crit : criteria2014) {
            result.getCriteriaOptions2014().add(new CertificationCriterion(crit));
        }
        List<CertificationCriterionDTO> criteria2015 = certificationCriterionDao.findByCertificationEditionYear("2015");
        for (CertificationCriterionDTO crit : criteria2015) {
            result.getCriteriaOptions2015().add(new CertificationCriterion(crit));
        }

        result.getTransparencyOptions().add(new Removable<String>(RequirementTypeEnum.K1.getName(), RequirementTypeEnum.K1.getRemoved()));
        result.getTransparencyOptions().add(new Removable<String>(RequirementTypeEnum.K2.getName(), RequirementTypeEnum.K2.getRemoved()));

        result.getRealWorldTestingOptions().add(new Removable<String>(RequirementTypeEnum.ANNUAL_RWT_PLAN.getName(), RequirementTypeEnum.ANNUAL_RWT_PLAN.getRemoved()));
        result.getRealWorldTestingOptions().add(new Removable<String>(RequirementTypeEnum.ANNUAL_RWT_RESULTS.getName(), RequirementTypeEnum.ANNUAL_RWT_RESULTS.getRemoved()));

        result.getAttestationsSubmissionOptions().add(new Removable<String>(RequirementTypeEnum.SEMIANNUAL_ATTESTATIONS_SUBMISSION.getName(), RequirementTypeEnum.SEMIANNUAL_ATTESTATIONS_SUBMISSION.getRemoved()));

        return result;
    }

    public Set<CertificationCriterion> getNonconformityTypeOptions() {
        LOGGER.debug("Getting all nonconformity types from the database (not cached).");

        Set<CertificationCriterion> result = new HashSet<CertificationCriterion>();

        List<CertificationCriterionDTO> criteria2014 = certificationCriterionDao.findByCertificationEditionYear("2014");
        for (CertificationCriterionDTO crit : criteria2014) {
            result.add(new CertificationCriterion(crit));
        }
        List<CertificationCriterionDTO> criteria2015 = certificationCriterionDao.findByCertificationEditionYear("2015");
        for (CertificationCriterionDTO crit : criteria2015) {
            result.add(new CertificationCriterion(crit));
        }

        CertificationCriterion k1Type = new CertificationCriterion();
        k1Type.setNumber(NonconformityType.K1.getName());
        k1Type.setRemoved(NonconformityType.K1.getRemoved());
        result.add(k1Type);

        CertificationCriterion k2Type = new CertificationCriterion();
        k2Type.setNumber(NonconformityType.K2.getName());
        k2Type.setRemoved(NonconformityType.K2.getRemoved());
        result.add(k2Type);

        CertificationCriterion lType = new CertificationCriterion();
        lType.setNumber(NonconformityType.L.getName());
        lType.setRemoved(NonconformityType.L.getRemoved());
        result.add(lType);

        CertificationCriterion rwtPlanType = new CertificationCriterion();
        rwtPlanType.setNumber(NonconformityType.ANNUAL_RWT_PLAN.getName());
        rwtPlanType.setRemoved(NonconformityType.ANNUAL_RWT_PLAN.getRemoved());
        result.add(rwtPlanType);

        CertificationCriterion rwtResultsType = new CertificationCriterion();
        rwtResultsType.setNumber(NonconformityType.ANNUAL_RWT_RESULTS.getName());
        rwtResultsType.setRemoved(NonconformityType.ANNUAL_RWT_RESULTS.getRemoved());
        result.add(rwtResultsType);

        CertificationCriterion otherType = new CertificationCriterion();
        otherType.setNumber(NonconformityType.OTHER.getName());
        otherType.setRemoved(NonconformityType.OTHER.getRemoved());
        result.add(otherType);

        CertificationCriterion attestationsSubmissionType = new CertificationCriterion();
        attestationsSubmissionType.setNumber(NonconformityType.SEMIANNUAL_ATTESTATIONS_SUBMISSION.getName());
        attestationsSubmissionType.setRemoved(NonconformityType.SEMIANNUAL_ATTESTATIONS_SUBMISSION.getRemoved());
        result.add(attestationsSubmissionType);

        return result;
    }

    public Set<KeyValueModel> getSurveillanceRequirementTypes() {
        LOGGER.debug("Getting all surveillance requirement types from the database (not cached).");

        List<SurveillanceRequirementType> daoResults = survDao.getAllSurveillanceRequirementTypes();
        Set<KeyValueModel> results = new HashSet<KeyValueModel>();

        for (SurveillanceRequirementType result : daoResults) {
            results.add(new KeyValueModel(result.getId(), result.getName()));
        }
        return results;
    }

    public Set<KeyValueModel> getSurveillanceResultTypes() {
        LOGGER.debug("Getting all surveillance result types from the database (not cached).");

        List<SurveillanceResultType> daoResults = survDao.getAllSurveillanceResultTypes();
        Set<KeyValueModel> results = new HashSet<KeyValueModel>();

        for (SurveillanceResultType result : daoResults) {
            results.add(new KeyValueModel(result.getId(), result.getName()));
        }
        return results;
    }

    @Transactional
    @Cacheable(value = CacheNames.UPLOAD_TEMPLATE_VERSIONS)
    public Set<UploadTemplateVersion> getUploadTemplateVersions() {
        LOGGER.debug("Getting all upload template verisons from the database (not cached).");

        List<UploadTemplateVersionDTO> dtos = this.uploadTemplateDao.findAll();
        Set<UploadTemplateVersion> templates = new HashSet<UploadTemplateVersion>();

        for (UploadTemplateVersionDTO dto : dtos) {
            templates.add(new UploadTemplateVersion(dto));
        }

        return templates;
    }

    @Transactional
    @Cacheable(value = CacheNames.MEASURES)
    public Set<Measure> getMeasures() {
        return measureDao.findAll();
    }

    @Transactional
    @Cacheable(value = CacheNames.MEASURE_TYPES)
    public Set<MeasureType> getMeasureTypes() {
        return listingMeasureDao.getMeasureTypes();
    }

    @Transactional
    @Cacheable(value = CacheNames.TEST_PROCEDURES)
    public Set<CriteriaSpecificDescriptiveModel> getTestProcedures() {
        LOGGER.debug("Getting all test procedures from the database (not cached).");

        List<TestProcedureCriteriaMapDTO> testProcedureDtos = testProcedureDao.findAllWithMappedCriteria();
        Set<CriteriaSpecificDescriptiveModel> testProcedures = new HashSet<CriteriaSpecificDescriptiveModel>();

        for (TestProcedureCriteriaMapDTO dto : testProcedureDtos) {
            testProcedures.add(new CriteriaSpecificDescriptiveModel(
                    dto.getTestProcedureId(), dto.getTestProcedure().getName(), null,
                    null, new CertificationCriterion(dto.getCriteria())));
        }
        return testProcedures;
    }

    @Transactional
    @Cacheable(value = CacheNames.TEST_DATA)
    public Set<CriteriaSpecificDescriptiveModel> getTestData() {
        LOGGER.debug("Getting all test data from the database (not cached).");

        List<TestDataCriteriaMapDTO> testDataDtos = testDataDao.findAllWithMappedCriteria();
        Set<CriteriaSpecificDescriptiveModel> testData = new HashSet<CriteriaSpecificDescriptiveModel>();

        for (TestDataCriteriaMapDTO dto : testDataDtos) {
            testData.add(new CriteriaSpecificDescriptiveModel(
                    dto.getTestDataId(), dto.getTestData().getName(), null,
                    null, new CertificationCriterion(dto.getCriteria())));
        }
        return testData;
    }

    @Transactional
    @Cacheable(value = CacheNames.CERTIFICATION_CRITERION_WITH_EDITIONS)
    public Set<CertificationCriterion> getCertificationCriterion() {
        LOGGER.debug("Getting all criterion with editions from the database (not cached).");

        List<CertificationCriterionDTO> dtos = this.certificationCriterionDao.findAll();
        Set<CertificationCriterion> criterion = new HashSet<CertificationCriterion>();

        for (CertificationCriterionDTO dto : dtos) {
            criterion.add(new CertificationCriterion(dto));
        }

        return criterion;
    }

    @Transactional
    @Cacheable(value = CacheNames.CQM_CRITERION)
    public List<CQMCriterion> getCQMCriteria() {
        List<CQMCriterion> result = new ArrayList<CQMCriterion>();
        List<CQMCriterionDTO> dtos = cqmCriterionDao.findAll();
        for (CQMCriterionDTO dto : dtos) {
            CQMCriterion criterion = new CQMCriterion();
            criterion.setCmsId(dto.getCmsId());
            criterion.setCqmCriterionTypeId(dto.getCqmCriterionTypeId());
            criterion.setCqmDomain(dto.getCqmDomain());
            criterion.setCqmVersionId(dto.getCqmVersionId());
            criterion.setCqmVersion(dto.getCqmVersion());
            criterion.setCriterionId(dto.getId());
            criterion.setDescription(dto.getDescription());
            criterion.setNqfNumber(dto.getNqfNumber());
            criterion.setNumber(dto.getNumber());
            criterion.setTitle(dto.getTitle());
            result.add(criterion);
        }
        return result;
    }

    @Transactional
    @Cacheable(value = CacheNames.EDITIONS)
    public List<CertificationEdition> getCertificationEditions() {
        List<CertificationEdition> result = new ArrayList<CertificationEdition>();
        List<CertificationEditionDTO> dtos = certEditionDao.findAll();
        for (CertificationEditionDTO dto : dtos) {
            CertificationEdition edition = new CertificationEdition(dto);
            result.add(edition);
        }
        return result;
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

        List<CertificationCriterionDTO> dtos = this.certificationCriterionDao.findAll();
        Set<CertificationCriterion> criteria = new HashSet<CertificationCriterion>();
        for (CertificationCriterionDTO dto : dtos) {
            criteria.add(new CertificationCriterion(dto));
        }
        result.setCertificationCriteria(criteria);

        result.setAcbs(getCertBodyNames());
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
    public Set<KeyValueModel> getClassificationNames() {
        return cacheableDimensionalDataManager.getClassificationNames();
    }

    public Set<KeyValueModel> getEditionNames(final Boolean simple) {
       return cacheableDimensionalDataManager.getEditionNames(simple);
    }

    public Set<KeyValueModel> getCertificationStatuses() {
        return cacheableDimensionalDataManager.getCertificationStatuses();
    }

    public Set<KeyValueModel> getPracticeTypeNames() {
        return cacheableDimensionalDataManager.getPracticeTypeNames();
    }

    public Set<KeyValueModelStatuses> getProducts() {
        return cacheableDimensionalDataManager.getProductsCached();
    }

    @Transactional
    public Set<KeyValueModelStatuses> getDevelopers() {
        return cacheableDimensionalDataManager.getDevelopers();
    }

    public Set<CriteriaSpecificDescriptiveModel> getCertificationCriterionNumbers() throws EntityRetrievalException {
        return cacheableDimensionalDataManager.getCertificationCriterionNumbers();
    }

    public Set<DescriptiveModel> getCQMCriterionNumbers(final Boolean simple) {
        return cacheableDimensionalDataManager.getCQMCriterionNumbers(simple);
    }
}
