package gov.healthit.chpl.manager.impl;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

import gov.healthit.chpl.caching.CacheNames;
import gov.healthit.chpl.dao.AccessibilityStandardDAO;
import gov.healthit.chpl.dao.AgeRangeDAO;
import gov.healthit.chpl.dao.CQMCriterionDAO;
import gov.healthit.chpl.dao.CertificationBodyDAO;
import gov.healthit.chpl.dao.CertificationCriterionDAO;
import gov.healthit.chpl.dao.CertificationEditionDAO;
import gov.healthit.chpl.dao.CertificationStatusDAO;
import gov.healthit.chpl.dao.DeveloperDAO;
import gov.healthit.chpl.dao.DeveloperStatusDAO;
import gov.healthit.chpl.dao.EducationTypeDAO;
import gov.healthit.chpl.dao.FuzzyChoicesDAO;
import gov.healthit.chpl.dao.JobDAO;
import gov.healthit.chpl.dao.MacraMeasureDAO;
import gov.healthit.chpl.dao.PracticeTypeDAO;
import gov.healthit.chpl.dao.ProductClassificationTypeDAO;
import gov.healthit.chpl.dao.ProductDAO;
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
import gov.healthit.chpl.domain.FuzzyChoices;
import gov.healthit.chpl.domain.KeyValueModel;
import gov.healthit.chpl.domain.KeyValueModelStatuses;
import gov.healthit.chpl.domain.NonconformityType;
import gov.healthit.chpl.domain.SearchOptions;
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
import gov.healthit.chpl.dto.CQMCriterionDTO;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.dto.CertificationCriterionDTO;
import gov.healthit.chpl.dto.CertificationEditionDTO;
import gov.healthit.chpl.dto.CertificationStatusDTO;
import gov.healthit.chpl.dto.DeveloperDTO;
import gov.healthit.chpl.dto.DeveloperStatusDTO;
import gov.healthit.chpl.dto.EducationTypeDTO;
import gov.healthit.chpl.dto.FuzzyChoicesDTO;
import gov.healthit.chpl.dto.MacraMeasureDTO;
import gov.healthit.chpl.dto.PracticeTypeDTO;
import gov.healthit.chpl.dto.ProductClassificationTypeDTO;
import gov.healthit.chpl.dto.ProductDTO;
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
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.manager.SearchMenuManager;

@Service("searchMenuManager")
public class SearchMenuManagerImpl implements SearchMenuManager {

    @Autowired
    private CertificationBodyDAO certificationBodyDAO;

    @Autowired
    private CQMCriterionDAO cqmCriterionDAO;

    @Autowired
    private CertificationCriterionDAO certificationCriterionDAO;

    @Autowired
    private CertificationEditionDAO certificationEditionDAO;

    @Autowired
    private CertificationStatusDAO certificationStatusDao;

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
    private ProductClassificationTypeDAO productClassificationTypeDAO;

    @Autowired
    private ProductDAO productDAO;

    @Autowired
    private JobDAO jobDao;

    @Autowired
    private FuzzyChoicesDAO fuzzyChoicesDAO;

    @Autowired
    private PracticeTypeDAO practiceTypeDAO;

    @Autowired
    private DeveloperDAO developerDAO;

    @Autowired
    private MacraMeasureDAO macraDao;

    @Transactional
    @Override
    @Cacheable(CacheNames.JOB_TYPES)
    public Set<KeyValueModel> getJobTypes() {
        List<JobTypeDTO> jobTypes = jobDao.findAllTypes();
        Set<KeyValueModel> results = new HashSet<KeyValueModel>();
        for (JobTypeDTO dto : jobTypes) {
            results.add(new KeyValueModel(dto.getId(), dto.getName(), dto.getDescription()));
        }
        return results;
    }

    @Transactional
    @Override
    public Set<FuzzyChoices> getFuzzyChoices() throws EntityRetrievalException, JsonParseException,
    JsonMappingException, IOException {
        List<FuzzyChoicesDTO> fuzzyChoices = fuzzyChoicesDAO.findAllTypes();
        Set<FuzzyChoices> results = new HashSet<FuzzyChoices>();
        for (FuzzyChoicesDTO dto : fuzzyChoices) {
            results.add(new FuzzyChoices(dto));
        }
        return results;
    }

    @Transactional
    @Override
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_ONC')")
    public FuzzyChoices updateFuzzyChoices(final FuzzyChoicesDTO fuzzyChoicesDTO)
        throws EntityRetrievalException, JsonProcessingException, EntityCreationException, IOException {

        FuzzyChoices result = null;
        result = new FuzzyChoices(fuzzyChoicesDAO.update(fuzzyChoicesDTO));
        return result;
    }

    @Transactional
    @Override
    @Cacheable(CacheNames.CLASSIFICATION_NAMES)
    public Set<KeyValueModel> getClassificationNames() {

        List<ProductClassificationTypeDTO> classificationTypes = productClassificationTypeDAO.findAll();
        Set<KeyValueModel> classificationTypeNames = new HashSet<KeyValueModel>();

        for (ProductClassificationTypeDTO dto : classificationTypes) {
            classificationTypeNames.add(new KeyValueModel(dto.getId(), dto.getName()));
        }

        return classificationTypeNames;
    }

    @Transactional
    @Override
    @Cacheable(CacheNames.EDITION_NAMES)
    public Set<KeyValueModel> getEditionNames(final Boolean simple) {

        List<CertificationEditionDTO> certificationEditions = certificationEditionDAO.findAll();
        Set<KeyValueModel> editionNames = new HashSet<KeyValueModel>();

        for (CertificationEditionDTO dto : certificationEditions) {

            if (simple) {
                if (dto.getYear().equals("2011")) {
                    continue;
                }
            }
            editionNames.add(new KeyValueModel(dto.getId(), dto.getYear()));
        }

        return editionNames;
    }

    @Transactional
    @Override
    @Cacheable(CacheNames.CERTIFICATION_STATUSES)
    public Set<KeyValueModel> getCertificationStatuses() {
        List<CertificationStatusDTO> certificationStatuses = certificationStatusDao.findAll();
        Set<KeyValueModel> results = new HashSet<KeyValueModel>();

        for (CertificationStatusDTO dto : certificationStatuses) {
            results.add(new KeyValueModel(dto.getId(), dto.getStatus()));
        }

        return results;
    }

    @Transactional
    @Override
    @Cacheable(CacheNames.PRACTICE_TYPE_NAMES)
    public Set<KeyValueModel> getPracticeTypeNames() {

        List<PracticeTypeDTO> practiceTypeDTOs = practiceTypeDAO.findAll();
        Set<KeyValueModel> practiceTypeNames = new HashSet<KeyValueModel>();

        for (PracticeTypeDTO dto : practiceTypeDTOs) {
            practiceTypeNames.add(new KeyValueModel(dto.getId(), dto.getName()));
        }

        return practiceTypeNames;
    }

    /**
     * Get all product names.
     * Users of this class should call this method to get the product names
     * and statuses, which should always be returned quickly from the cache.
     */
    @Transactional
    @Override
    @Cacheable(CacheNames.PRODUCT_NAMES)
    public Set<KeyValueModelStatuses> getProductNamesCached() {
        return getProductNames();
    }

    /**
     * This method gets all the product names but does
     * not cache the result. This method should only be used for
     * filling in the pre-fetched cache and is here to avoid duplicating
     * manager logic elsewhere.
     */
    @Transactional
    @Override
    public Set<KeyValueModelStatuses> getProductNames() {
        List<ProductDTO> productDTOs = this.productDAO.findAll();
        Set<KeyValueModelStatuses> productNames = new HashSet<KeyValueModelStatuses>();
        for (ProductDTO dto : productDTOs) {
            productNames.add(new KeyValueModelStatuses(dto.getId(), dto.getName(), dto.getStatuses()));
        }
        return productNames;
    }

    /**
     * Get all developer names.
     * Users of this class should call this method to get the developer names
     * and statuses, which should always be returned quickly from the cache.
     */
    @Transactional
    @Override
    @Cacheable(CacheNames.DEVELOPER_NAMES)
    public Set<KeyValueModelStatuses> getDeveloperNamesCached() {
        return getDeveloperNames();
    }

    /**
     * This method gets all the developer names but does
     * not cache the result. This method should only be used for
     * filling in the pre-fetched cache and is here to avoid duplicating
     * manager logic elsewhere.
     */
    @Transactional
    @Override
    public Set<KeyValueModelStatuses> getDeveloperNames() {
        List<DeveloperDTO> developerDTOs = this.developerDAO.findAll();
        Set<KeyValueModelStatuses> developerNames = new HashSet<KeyValueModelStatuses>();
        for (DeveloperDTO dto : developerDTOs) {
            developerNames.add(new KeyValueModelStatuses(dto.getId(), dto.getName(), dto.getStatuses()));
        }
        return developerNames;
    }

    @Transactional
    @Override
    public Set<CertificationBody> getCertBodyNames() {
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
    public Set<KeyValueModel> getDeveloperStatuses() {
        List<DeveloperStatusDTO> dtos = this.devStatusDao.findAll();
        Set<KeyValueModel> statuses = new HashSet<KeyValueModel>();

        for (DeveloperStatusDTO dto : dtos) {
            statuses.add(new KeyValueModel(dto.getId(), dto.getStatusName()));
        }

        return statuses;
    }

    @Override
    public Set<KeyValueModel> getAccessibilityStandards() {
        List<AccessibilityStandardDTO> dtos = this.asDao.findAll();
        Set<KeyValueModel> standards = new HashSet<KeyValueModel>();

        for (AccessibilityStandardDTO dto : dtos) {
            standards.add(new KeyValueModel(dto.getId(), dto.getName()));
        }
        return standards;
    }

    @Override
    public Set<KeyValueModel> getUcdProcesses() {
        List<UcdProcessDTO> dtos = this.ucdDao.findAll();
        Set<KeyValueModel> ucds = new HashSet<KeyValueModel>();

        for (UcdProcessDTO dto : dtos) {
            ucds.add(new KeyValueModel(dto.getId(), dto.getName()));
        }
        return ucds;
    }

    @Override
    public Set<KeyValueModel> getQmsStandards() {
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

        List<TestStandardDTO> dtos = this.testStandardDao.findAll();
        Set<TestStandard> testStds = new HashSet<TestStandard>();

        for (TestStandardDTO dto : dtos) {
            testStds.add(new TestStandard(dto));
        }

        return testStds;
    }

    @Override
    public Set<KeyValueModel> getSurveillanceTypes() {
        List<SurveillanceType> daoResults = survDao.getAllSurveillanceTypes();
        Set<KeyValueModel> results = new HashSet<KeyValueModel>();

        for (SurveillanceType result : daoResults) {
            results.add(new KeyValueModel(result.getId(), result.getName()));
        }
        return results;
    }

    @Override
    public SurveillanceRequirementOptions getSurveillanceRequirementOptions() {
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
        List<SurveillanceRequirementType> daoResults = survDao.getAllSurveillanceRequirementTypes();
        Set<KeyValueModel> results = new HashSet<KeyValueModel>();

        for (SurveillanceRequirementType result : daoResults) {
            results.add(new KeyValueModel(result.getId(), result.getName()));
        }
        return results;
    }

    @Override
    public Set<KeyValueModel> getSurveillanceResultTypes() {
        List<SurveillanceResultType> daoResults = survDao.getAllSurveillanceResultTypes();
        Set<KeyValueModel> results = new HashSet<KeyValueModel>();

        for (SurveillanceResultType result : daoResults) {
            results.add(new KeyValueModel(result.getId(), result.getName()));
        }
        return results;
    }

    @Override
    public Set<KeyValueModel> getNonconformityStatusTypes() {
        List<SurveillanceNonconformityStatus> daoResults = survDao.getAllSurveillanceNonconformityStatusTypes();
        Set<KeyValueModel> results = new HashSet<KeyValueModel>();

        for (SurveillanceNonconformityStatus result : daoResults) {
            results.add(new KeyValueModel(result.getId(), result.getName()));
        }
        return results;
    }

    @Transactional
    @Override
    @Cacheable(CacheNames.UPLOAD_TEMPLATE_VERSIONS)
    public Set<UploadTemplateVersion> getUploadTemplateVersions() {

        List<UploadTemplateVersionDTO> dtos = this.uploadTemplateDao.findAll();
        Set<UploadTemplateVersion> templates = new HashSet<UploadTemplateVersion>();

        for (UploadTemplateVersionDTO dto : dtos) {
            templates.add(new UploadTemplateVersion(dto));
        }

        return templates;
    }

    @Transactional
    @Override
    @Cacheable(CacheNames.MACRA_MEASURES)
    public Set<CriteriaSpecificDescriptiveModel> getMacraMeasures() {
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
    @Cacheable(CacheNames.TEST_PROCEDURES)
    public Set<CriteriaSpecificDescriptiveModel> getTestProcedures() {
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
    @Cacheable(CacheNames.TEST_DATA)
    public Set<CriteriaSpecificDescriptiveModel> getTestData() {
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
    @Cacheable(CacheNames.CERTIFICATION_CRITERION_NUMBERS)
    public Set<DescriptiveModel> getCertificationCriterionNumbers(final Boolean simple) throws EntityRetrievalException {

        List<CertificationCriterionDTO> dtos = this.certificationCriterionDAO.findAll();
        Set<DescriptiveModel> criterionNames = new HashSet<DescriptiveModel>();

        for (CertificationCriterionDTO dto : dtos) {
            criterionNames.add(new DescriptiveModel(dto.getId(), dto.getNumber(), dto.getTitle()));
        }

        return criterionNames;

    }

    @Transactional
    @Override
    @Cacheable(CacheNames.CERTIFICATION_CRITERION_WITH_EDITIONS)
    public Set<CertificationCriterion> getCertificationCriterion() {

        List<CertificationCriterionDTO> dtos = this.certificationCriterionDAO.findAll();
        Set<CertificationCriterion> criterion = new HashSet<CertificationCriterion>();

        for (CertificationCriterionDTO dto : dtos) {
            criterion.add(new CertificationCriterion(dto));
        }

        return criterion;

    }

    @Transactional
    @Override
    @Cacheable(CacheNames.CQM_CRITERION_NUMBERS)
    public Set<DescriptiveModel> getCQMCriterionNumbers(final Boolean simple) {

        List<CQMCriterionDTO> dtos = this.cqmCriterionDAO.findAll();
        Set<DescriptiveModel> criterionNames = new HashSet<DescriptiveModel>();

        for (CQMCriterionDTO dto : dtos) {

            String idNumber;

            if (simple) {
                if (dto.getCmsId() != null) {
                    idNumber = dto.getCmsId();
                } else {
                    continue;
                }
            } else {
                if (dto.getCmsId() != null) {
                    idNumber = dto.getCmsId();
                } else {
                    idNumber = dto.getNqfNumber();
                }
            }

            criterionNames.add(new DescriptiveModel(dto.getId(), idNumber, dto.getTitle()));
        }
        return criterionNames;
    }

    @Override
    public SearchOptions getSearchOptions(final Boolean simple) throws EntityRetrievalException {
        SearchOptions searchOptions = new SearchOptions();
        //the following calls contain data that could possibly change
        //without the system rebooting so we need to make sure to
        //keep their cached data up-to-date
        searchOptions.setProductNames(getProductNamesCached());
        searchOptions.setDeveloperNames(getDeveloperNamesCached());
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
