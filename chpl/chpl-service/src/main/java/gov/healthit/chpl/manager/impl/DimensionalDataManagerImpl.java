package gov.healthit.chpl.manager.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.caching.CacheNames;
import gov.healthit.chpl.dao.AccessibilityStandardDAO;
import gov.healthit.chpl.dao.AgeRangeDAO;
import gov.healthit.chpl.dao.CertificationBodyDAO;
import gov.healthit.chpl.dao.CertificationCriterionDAO;
import gov.healthit.chpl.dao.DeveloperStatusDAO;
import gov.healthit.chpl.dao.EducationTypeDAO;
import gov.healthit.chpl.dao.JobDAO;
import gov.healthit.chpl.dao.MacraMeasureDAO;
import gov.healthit.chpl.dao.QmsStandardDAO;
import gov.healthit.chpl.dao.SurveillanceDAO;
import gov.healthit.chpl.dao.TargetedUserDAO;
import gov.healthit.chpl.dao.TestDataDAO;
import gov.healthit.chpl.dao.TestFunctionalityDAO;
import gov.healthit.chpl.dao.TestProcedureDAO;
import gov.healthit.chpl.dao.TestStandardDAO;
import gov.healthit.chpl.dao.TestToolDAO;
import gov.healthit.chpl.dao.UcdProcessDAO;
import gov.healthit.chpl.dao.UploadTemplateVersionDAO;
import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.CriteriaSpecificDescriptiveModel;
import gov.healthit.chpl.domain.DescriptiveModel;
import gov.healthit.chpl.domain.KeyValueModel;
import gov.healthit.chpl.domain.KeyValueModelStatuses;
import gov.healthit.chpl.domain.NonconformityType;
import gov.healthit.chpl.domain.SearchableDimensionalData;
import gov.healthit.chpl.domain.SurveillanceNonconformityStatus;
import gov.healthit.chpl.domain.SurveillanceRequirementOptions;
import gov.healthit.chpl.domain.SurveillanceRequirementType;
import gov.healthit.chpl.domain.SurveillanceResultType;
import gov.healthit.chpl.domain.SurveillanceType;
import gov.healthit.chpl.domain.TestFunctionality;
import gov.healthit.chpl.domain.TestStandard;
import gov.healthit.chpl.domain.TestTool;
import gov.healthit.chpl.domain.UploadTemplateVersion;
import gov.healthit.chpl.domain.concept.RequirementTypeEnum;
import gov.healthit.chpl.dto.AccessibilityStandardDTO;
import gov.healthit.chpl.dto.AgeRangeDTO;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.dto.CertificationCriterionDTO;
import gov.healthit.chpl.dto.DeveloperStatusDTO;
import gov.healthit.chpl.dto.EducationTypeDTO;
import gov.healthit.chpl.dto.MacraMeasureDTO;
import gov.healthit.chpl.dto.QmsStandardDTO;
import gov.healthit.chpl.dto.TargetedUserDTO;
import gov.healthit.chpl.dto.TestDataCriteriaMapDTO;
import gov.healthit.chpl.dto.TestFunctionalityDTO;
import gov.healthit.chpl.dto.TestProcedureCriteriaMapDTO;
import gov.healthit.chpl.dto.TestStandardDTO;
import gov.healthit.chpl.dto.TestToolDTO;
import gov.healthit.chpl.dto.UcdProcessDTO;
import gov.healthit.chpl.dto.UploadTemplateVersionDTO;
import gov.healthit.chpl.dto.job.JobTypeDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.manager.DimensionalDataManager;
import gov.healthit.chpl.manager.PrecacheableDimensionalDataManager;

@Service("dimensionalDataManager")
public class DimensionalDataManagerImpl implements DimensionalDataManager {
    private static final Logger LOGGER = LogManager.getLogger(DimensionalDataManagerImpl.class);

    @Autowired
    private PrecacheableDimensionalDataManager precache;

    @Autowired
    private CertificationBodyDAO certificationBodyDAO;

    @Autowired
    private CertificationCriterionDAO certificationCriterionDAO;

    @Autowired
    private EducationTypeDAO educationTypeDao;
    @Autowired
    private AgeRangeDAO ageRangeDao;
    @Autowired
    private TestFunctionalityDAO testFuncDao;
    @Autowired
    private TestStandardDAO testStandardDao;
    @Autowired
    private TestToolDAO testToolsDao;
    @Autowired private TestProcedureDAO testProcedureDao;
    @Autowired private TestDataDAO testDataDao;

    @Autowired
    private AccessibilityStandardDAO asDao;
    @Autowired
    private UcdProcessDAO ucdDao;
    @Autowired
    private QmsStandardDAO qmsDao;
    @Autowired
    private TargetedUserDAO tuDao;
    @Autowired
    private DeveloperStatusDAO devStatusDao;
    @Autowired
    private SurveillanceDAO survDao;
    @Autowired
    private UploadTemplateVersionDAO uploadTemplateDao;


    @Autowired
    private JobDAO jobDao;

    @Autowired
    private MacraMeasureDAO macraDao;

    @Transactional
    @Override
    @Cacheable(value = CacheNames.JOB_TYPES)
    public Set<KeyValueModel> getJobTypes() {
        LOGGER.debug("Getting all job types from the database (not cached).");
        List<JobTypeDTO> jobTypes = jobDao.findAllTypes();
        Set<KeyValueModel> results = new HashSet<KeyValueModel>();
        for (JobTypeDTO dto : jobTypes) {
            results.add(new KeyValueModel(dto.getId(), dto.getName(), dto.getDescription()));
        }
        return results;
    }

    @Transactional
    @Override
    public Set<CertificationBody> getCertBodyNames() {
        LOGGER.debug("Getting all certification body names from the database (not cached).");
        List<CertificationBodyDTO> dtos = this.certificationBodyDAO.findAll();
        Set<CertificationBody> acbNames = new HashSet<CertificationBody>();

        for (CertificationBodyDTO dto : dtos) {
            acbNames.add(new CertificationBody(dto));
        }
        return acbNames;
    }

    @Transactional
    @Override
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
    @Override
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
    @Override
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
    @Override
    public Set<KeyValueModel> getTestTools() {
        LOGGER.debug("Getting all test tools from the database (not cached).");
        List<TestToolDTO> dtos = this.testToolsDao.findAll();
        Set<KeyValueModel> testTools = new HashSet<KeyValueModel>();

        for (TestToolDTO dto : dtos) {
            TestTool tt = new TestTool(dto.getId(), dto.getName(), dto.getDescription());
            tt.setRetired(dto.isRetired());
            testTools.add(tt);
        }

        return testTools;
    }

    @Override
    @Transactional
    public Set<KeyValueModel> getDeveloperStatuses() {
        LOGGER.debug("Getting all developer statuses from the database (not cached).");
        List<DeveloperStatusDTO> dtos = this.devStatusDao.findAll();
        Set<KeyValueModel> statuses = new HashSet<KeyValueModel>();

        for (DeveloperStatusDTO dto : dtos) {
            statuses.add(new KeyValueModel(dto.getId(), dto.getStatusName()));
        }

        return statuses;
    }

    @Override
    public Set<KeyValueModel> getAccessibilityStandards() {
        LOGGER.debug("Getting all accessibility standards from the database (not cached).");

        List<AccessibilityStandardDTO> dtos = this.asDao.findAll();
        Set<KeyValueModel> standards = new HashSet<KeyValueModel>();

        for (AccessibilityStandardDTO dto : dtos) {
            standards.add(new KeyValueModel(dto.getId(), dto.getName()));
        }
        return standards;
    }

    @Override
    public Set<KeyValueModel> getUcdProcesses() {
        LOGGER.debug("Getting all ucd processesfrom the database (not cached).");

        List<UcdProcessDTO> dtos = this.ucdDao.findAll();
        Set<KeyValueModel> ucds = new HashSet<KeyValueModel>();

        for (UcdProcessDTO dto : dtos) {
            ucds.add(new KeyValueModel(dto.getId(), dto.getName()));
        }
        return ucds;
    }

    @Override
    public Set<KeyValueModel> getQmsStandards() {
        LOGGER.debug("Getting all qms standards from the database (not cached).");

        List<QmsStandardDTO> dtos = this.qmsDao.findAll();
        Set<KeyValueModel> qms = new HashSet<KeyValueModel>();

        for (QmsStandardDTO dto : dtos) {
            qms.add(new KeyValueModel(dto.getId(), dto.getName()));
        }
        return qms;
    }

    @Override
    public Set<KeyValueModel> getTargetedUesrs() {
        List<TargetedUserDTO> dtos = this.tuDao.findAll();
        Set<KeyValueModel> standards = new HashSet<KeyValueModel>();

        for (TargetedUserDTO dto : dtos) {
            standards.add(new KeyValueModel(dto.getId(), dto.getName()));
        }
        return standards;
    }

    @Transactional
    @Override
    public Set<TestStandard> getTestStandards() {
        LOGGER.debug("Getting all test standards from the database (not cached).");

        List<TestStandardDTO> dtos = this.testStandardDao.findAll();
        Set<TestStandard> testStds = new HashSet<TestStandard>();

        for (TestStandardDTO dto : dtos) {
            testStds.add(new TestStandard(dto));
        }

        return testStds;
    }

    @Override
    public Set<KeyValueModel> getSurveillanceTypes() {
        LOGGER.debug("Getting all surveillance types from the database (not cached).");

        List<SurveillanceType> daoResults = survDao.getAllSurveillanceTypes();
        Set<KeyValueModel> results = new HashSet<KeyValueModel>();

        for (SurveillanceType result : daoResults) {
            results.add(new KeyValueModel(result.getId(), result.getName()));
        }
        return results;
    }

    @Override
    public SurveillanceRequirementOptions getSurveillanceRequirementOptions() {
        LOGGER.debug("Getting all surveillance requirements from the database (not cached).");

        SurveillanceRequirementOptions result = new SurveillanceRequirementOptions();

        List<CertificationCriterionDTO> criteria2014 = certificationCriterionDAO.findByCertificationEditionYear("2014");
        for (CertificationCriterionDTO crit : criteria2014) {
            result.getCriteriaOptions2014()
                    .add(new KeyValueModel(crit.getId(), crit.getNumber(), crit.getDescription()));
        }
        List<CertificationCriterionDTO> criteria2015 = certificationCriterionDAO.findByCertificationEditionYear("2015");
        for (CertificationCriterionDTO crit : criteria2015) {
            result.getCriteriaOptions2015()
                    .add(new KeyValueModel(crit.getId(), crit.getNumber(), crit.getDescription()));
        }

        result.getTransparencyOptions().add(RequirementTypeEnum.K1.getName());
        result.getTransparencyOptions().add(RequirementTypeEnum.K2.getName());
        return result;
    }

    @Override
    public Set<KeyValueModel> getNonconformityTypeOptions() {
        LOGGER.debug("Getting all nonconformity types from the database (not cached).");

        Set<KeyValueModel> result = new HashSet<KeyValueModel>();

        List<CertificationCriterionDTO> criteria2014 = certificationCriterionDAO.findByCertificationEditionYear("2014");
        for (CertificationCriterionDTO crit : criteria2014) {
            result.add(new KeyValueModel(crit.getId(), crit.getNumber(), crit.getDescription()));
        }
        List<CertificationCriterionDTO> criteria2015 = certificationCriterionDAO.findByCertificationEditionYear("2015");
        for (CertificationCriterionDTO crit : criteria2015) {
            result.add(new KeyValueModel(crit.getId(), crit.getNumber(), crit.getDescription()));
        }

        result.add(new KeyValueModel(null, NonconformityType.K1.getName()));
        result.add(new KeyValueModel(null, NonconformityType.K2.getName()));
        result.add(new KeyValueModel(null, NonconformityType.L.getName()));
        result.add(new KeyValueModel(null, NonconformityType.OTHER.getName()));
        return result;
    }

    @Override
    public Set<KeyValueModel> getSurveillanceRequirementTypes() {
        LOGGER.debug("Getting all surveillance requirement types from the database (not cached).");

        List<SurveillanceRequirementType> daoResults = survDao.getAllSurveillanceRequirementTypes();
        Set<KeyValueModel> results = new HashSet<KeyValueModel>();

        for (SurveillanceRequirementType result : daoResults) {
            results.add(new KeyValueModel(result.getId(), result.getName()));
        }
        return results;
    }

    @Override
    public Set<KeyValueModel> getSurveillanceResultTypes() {
        LOGGER.debug("Getting all surveillance result types from the database (not cached).");

        List<SurveillanceResultType> daoResults = survDao.getAllSurveillanceResultTypes();
        Set<KeyValueModel> results = new HashSet<KeyValueModel>();

        for (SurveillanceResultType result : daoResults) {
            results.add(new KeyValueModel(result.getId(), result.getName()));
        }
        return results;
    }

    @Override
    public Set<KeyValueModel> getNonconformityStatusTypes() {
        LOGGER.debug("Getting all nonconformity status types from the database (not cached).");

        List<SurveillanceNonconformityStatus> daoResults = survDao.getAllSurveillanceNonconformityStatusTypes();
        Set<KeyValueModel> results = new HashSet<KeyValueModel>();

        for (SurveillanceNonconformityStatus result : daoResults) {
            results.add(new KeyValueModel(result.getId(), result.getName()));
        }
        return results;
    }

    @Transactional
    @Override
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
    @Override
    @Cacheable(value = CacheNames.MACRA_MEASURES)
    public Set<CriteriaSpecificDescriptiveModel> getMacraMeasures() {
        LOGGER.debug("Getting all macra measuresfrom the database (not cached).");

        List<MacraMeasureDTO> measureDtos = macraDao.findAll();
        Set<CriteriaSpecificDescriptiveModel> measures = new HashSet<CriteriaSpecificDescriptiveModel>();

        for (MacraMeasureDTO dto : measureDtos) {
            measures.add(new CriteriaSpecificDescriptiveModel(dto.getId(), dto.getValue(), dto.getName(),
                    dto.getDescription(), new CertificationCriterion(dto.getCriteria())));
        }
        return measures;
    }

    @Transactional
    @Override
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
    @Override
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
    @Override
    @Cacheable(value = CacheNames.CERTIFICATION_CRITERION_WITH_EDITIONS)
    public Set<CertificationCriterion> getCertificationCriterion() {
        LOGGER.debug("Getting all criterion with editions from the database (not cached).");

        List<CertificationCriterionDTO> dtos = this.certificationCriterionDAO.findAll();
        Set<CertificationCriterion> criterion = new HashSet<CertificationCriterion>();

        for (CertificationCriterionDTO dto : dtos) {
            criterion.add(new CertificationCriterion(dto));
        }

        return criterion;

    }

    // The following methods are called from inside this class as searchable dimensional data.
    // Since they are called from within the same class, any annotations on the below methods
    // are ignored due to Spring's proxying mechanism. Therefore the caching of these methods
    // has been moved to the PrecacheableDimensionalDataModel class which is called through.

    @Override
    public Set<KeyValueModel> getClassificationNames() {
        return precache.getClassificationNames();
    }

    @Override
    public Set<KeyValueModel> getEditionNames(final Boolean simple) {
       return precache.getEditionNames(simple);
    }

    @Override
    public Set<KeyValueModel> getCertificationStatuses() {
        return precache.getCertificationStatuses();
    }

    @Override
    public Set<KeyValueModel> getPracticeTypeNames() {
        return precache.getPracticeTypeNames();
    }

    @Override
    public Set<KeyValueModelStatuses> getProductNames() {
        return precache.getProductNamesCached();
    }

    @Transactional
    @Override
    public Set<KeyValueModelStatuses> getDeveloperNames() {
        return precache.getDeveloperNamesCached();
    }

    @Override
    public Set<DescriptiveModel> getCertificationCriterionNumbers(final Boolean simple) throws EntityRetrievalException {
        return precache.getCertificationCriterionNumbers(simple);
    }

    @Override
    public Set<DescriptiveModel> getCQMCriterionNumbers(final Boolean simple) {
        return precache.getCQMCriterionNumbers(simple);
    }

    @Override
    public SearchableDimensionalData getSearchableDimensionalData(final Boolean simple) throws EntityRetrievalException {
        SearchableDimensionalData searchOptions = new SearchableDimensionalData();
        //the following calls contain data that could possibly change
        //without the system rebooting so we need to make sure to
        //keep their cached data up-to-date
        searchOptions.setProductNames(getProductNames());
        searchOptions.setDeveloperNames(getDeveloperNames());
        //acb names can change but there are so few that it's fine to not cache them
        searchOptions.setCertBodyNames(getCertBodyNames());

        //the following calls will be cached and their data
        //will never change without the system being rebooted
        //so we do not need to worry about re-getting the data
        searchOptions.setEditions(getEditionNames(simple));
        searchOptions.setCertificationStatuses(getCertificationStatuses());
        searchOptions.setPracticeTypeNames(getPracticeTypeNames());
        searchOptions.setProductClassifications(getClassificationNames());
        searchOptions.setCqmCriterionNumbers(getCQMCriterionNumbers(simple));
        searchOptions.setCertificationCriterionNumbers(getCertificationCriterionNumbers(simple));
        return searchOptions;
    }
}
