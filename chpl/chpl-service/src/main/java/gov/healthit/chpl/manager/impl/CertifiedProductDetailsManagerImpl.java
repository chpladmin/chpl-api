package gov.healthit.chpl.manager.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import gov.healthit.chpl.dao.CQMCriterionDAO;
import gov.healthit.chpl.dao.CQMResultDAO;
import gov.healthit.chpl.dao.CQMResultDetailsDAO;
import gov.healthit.chpl.dao.CertificationEditionDAO;
import gov.healthit.chpl.dao.CertificationResultDetailsDAO;
import gov.healthit.chpl.dao.CertificationStatusDAO;
import gov.healthit.chpl.dao.CertificationStatusEventDAO;
import gov.healthit.chpl.dao.CertifiedProductAccessibilityStandardDAO;
import gov.healthit.chpl.dao.CertifiedProductQmsStandardDAO;
import gov.healthit.chpl.dao.CertifiedProductSearchResultDAO;
import gov.healthit.chpl.dao.CertifiedProductTargetedUserDAO;
import gov.healthit.chpl.dao.CertifiedProductTestingLabDAO;
import gov.healthit.chpl.dao.ListingGraphDAO;
import gov.healthit.chpl.dao.MacraMeasureDAO;
import gov.healthit.chpl.dao.MeaningfulUseUserDAO;
import gov.healthit.chpl.domain.CQMCriterion;
import gov.healthit.chpl.domain.CQMResultCertification;
import gov.healthit.chpl.domain.CQMResultDetails;
import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertificationResultAdditionalSoftware;
import gov.healthit.chpl.domain.CertificationResultTestData;
import gov.healthit.chpl.domain.CertificationResultTestFunctionality;
import gov.healthit.chpl.domain.CertificationResultTestProcedure;
import gov.healthit.chpl.domain.CertificationResultTestStandard;
import gov.healthit.chpl.domain.CertificationResultTestTool;
import gov.healthit.chpl.domain.CertificationStatus;
import gov.healthit.chpl.domain.CertificationStatusEvent;
import gov.healthit.chpl.domain.CertifiedProduct;
import gov.healthit.chpl.domain.CertifiedProductAccessibilityStandard;
import gov.healthit.chpl.domain.CertifiedProductQmsStandard;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.CertifiedProductTargetedUser;
import gov.healthit.chpl.domain.CertifiedProductTestingLab;
import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.domain.InheritedCertificationStatus;
import gov.healthit.chpl.domain.MacraMeasure;
import gov.healthit.chpl.domain.MeaningfulUseUser;
import gov.healthit.chpl.domain.Product;
import gov.healthit.chpl.domain.ProductVersion;
import gov.healthit.chpl.domain.TestFunctionality;
import gov.healthit.chpl.domain.TestTask;
import gov.healthit.chpl.domain.UcdProcess;
import gov.healthit.chpl.dto.CQMCriterionDTO;
import gov.healthit.chpl.dto.CQMResultCriteriaDTO;
import gov.healthit.chpl.dto.CQMResultDetailsDTO;
import gov.healthit.chpl.dto.CertificationEditionDTO;
import gov.healthit.chpl.dto.CertificationResultAdditionalSoftwareDTO;
import gov.healthit.chpl.dto.CertificationResultDetailsDTO;
import gov.healthit.chpl.dto.CertificationResultMacraMeasureDTO;
import gov.healthit.chpl.dto.CertificationResultTestDataDTO;
import gov.healthit.chpl.dto.CertificationResultTestFunctionalityDTO;
import gov.healthit.chpl.dto.CertificationResultTestProcedureDTO;
import gov.healthit.chpl.dto.CertificationResultTestStandardDTO;
import gov.healthit.chpl.dto.CertificationResultTestTaskDTO;
import gov.healthit.chpl.dto.CertificationResultTestToolDTO;
import gov.healthit.chpl.dto.CertificationResultUcdProcessDTO;
import gov.healthit.chpl.dto.CertificationStatusDTO;
import gov.healthit.chpl.dto.CertificationStatusEventDTO;
import gov.healthit.chpl.dto.CertifiedProductAccessibilityStandardDTO;
import gov.healthit.chpl.dto.CertifiedProductDTO;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.dto.CertifiedProductQmsStandardDTO;
import gov.healthit.chpl.dto.CertifiedProductTargetedUserDTO;
import gov.healthit.chpl.dto.CertifiedProductTestingLabDTO;
import gov.healthit.chpl.dto.MacraMeasureDTO;
import gov.healthit.chpl.dto.MeaningfulUseUserDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.manager.CertificationResultManager;
import gov.healthit.chpl.manager.CertifiedProductDetailsManager;
import gov.healthit.chpl.manager.SurveillanceManager;
import gov.healthit.chpl.manager.TestingFunctionalityManager;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.util.AuthUtil;
import gov.healthit.chpl.util.CertificationResultRules;
import gov.healthit.chpl.util.ChplProductNumberUtil;
import gov.healthit.chpl.util.PropertyUtil;

/**
 * Certified Product Details Manager implementation.
 * 
 * @author alarned
 *
 */
@Service("certifiedProductDetailsManager")
public class CertifiedProductDetailsManagerImpl implements CertifiedProductDetailsManager {
    private static final Logger LOGGER = LogManager.getLogger(CertifiedProductDetailsManagerImpl.class);

    @Autowired
    private CertifiedProductSearchResultDAO certifiedProductSearchResultDAO;

    @Autowired
    private CQMResultDetailsDAO cqmResultDetailsDAO;

    @Autowired
    private CQMResultDAO cqmResultDao;

    @Autowired
    private CertificationResultDetailsDAO certificationResultDetailsDAO;

    @Autowired
    private CertifiedProductQmsStandardDAO certifiedProductQmsStandardDao;

    @Autowired
    private CertifiedProductTargetedUserDAO certifiedProductTargetedUserDao;

    @Autowired
    private CertifiedProductAccessibilityStandardDAO certifiedProductAsDao;

    @Autowired
    private CertificationResultManager certResultManager;

    @Autowired
    private CertificationStatusEventDAO certStatusEventDao;

    @Autowired
    private CertificationStatusDAO certStatusDao;

    @Autowired
    private MeaningfulUseUserDAO muuDao;

    @Autowired
    private CertificationResultRules certRules;

    @Autowired
    private ListingGraphDAO listingGraphDao;

    @Autowired
    private CertifiedProductTestingLabDAO certifiedProductTestingLabDao;

    @Autowired
    private SurveillanceManager survManager;

    @Autowired
    private CertifiedProductDetailsManagerAsync async;

    @Autowired
    private TestingFunctionalityManager testFunctionalityManager;

    @Autowired
    private PropertyUtil propUtil;

    @Autowired
    private ChplProductNumberUtil chplProductNumberUtil;

    @Autowired
    private CertificationStatusEventDAO certificationStatusEventDAO;

    @Autowired
    private ResourcePermissions resourcePermissions;

    private CertificationEditionDAO certificationEditionDAO;
    private List<CertificationEditionDTO> editions = null;
    private CQMCriterionDAO cqmCriterionDAO;
    private MacraMeasureDAO macraDao;
    private List<CQMCriterion> cqmCriteria = new ArrayList<CQMCriterion>();
    private List<MacraMeasure> macraMeasures = new ArrayList<MacraMeasure>();

    /**
     * Default constructor.
     * 
     * @param cqmCriterionDAO
     *            DAO for CQMs
     * @param macraDao
     *            DAO for Macra Measures
     * @param certificationEditionDAO
     *            DAO for Certification Edition
     */
    @Autowired
    public CertifiedProductDetailsManagerImpl(final CQMCriterionDAO cqmCriterionDAO, final MacraMeasureDAO macraDao,
            final CertificationEditionDAO certificationEditionDAO) {
        this.cqmCriterionDAO = cqmCriterionDAO;
        this.macraDao = macraDao;
        this.certificationEditionDAO = certificationEditionDAO;

        loadCQMCriteria();
        loadCriteriaMacraMeasures();
        editions = certificationEditionDAO.findAll();
    }

    @Override
    @Transactional
    public CertifiedProductSearchDetails getCertifiedProductDetailsByChplProductNumber(final String chplProductNumber)
            throws EntityRetrievalException {

        CertifiedProductDetailsDTO dto = getCertifiedProductDetailsDtoByChplProductNumber(chplProductNumber);
        return createCertifiedSearchDetails(dto, propUtil.isAsyncListingDetailsEnabled());
    }

    @Override
    @Transactional
    public CertifiedProductSearchDetails getCertifiedProductDetailsByChplProductNumber(final String chplProductNumber,
            final Boolean retrieveAsynchronously) throws EntityRetrievalException {

        CertifiedProductDetailsDTO dto = getCertifiedProductDetailsDtoByChplProductNumber(chplProductNumber);
        return createCertifiedSearchDetails(dto, retrieveAsynchronously);
    }

    @Override
    @Transactional
    public CertifiedProductSearchDetails getCertifiedProductDetails(final Long certifiedProductId)
            throws EntityRetrievalException {

        return getCertifiedProductDetails(certifiedProductId, propUtil.isAsyncListingDetailsEnabled());
    }

    @Override
    @Transactional
    public CertifiedProductSearchDetails getCertifiedProductDetails(final Long certifiedProductId,
            final Boolean retrieveAsynchronously) throws EntityRetrievalException {

        CertifiedProductDetailsDTO dto = certifiedProductSearchResultDAO.getById(certifiedProductId);
        return createCertifiedSearchDetails(dto, retrieveAsynchronously);
    }

    @Override
    @Transactional
    public CertifiedProductSearchDetails getCertifiedProductDetailsBasicByChplProductNumber(
            final String chplProductNumber) throws EntityRetrievalException {

        return getCertifiedProductDetailsBasicByChplProductNumber(chplProductNumber,
                propUtil.isAsyncListingDetailsEnabled());
    }

    @Override
    @Transactional
    public CertifiedProductSearchDetails getCertifiedProductDetailsBasicByChplProductNumber(
            final String chplProductNumber, final Boolean retrieveAsynchronously) throws EntityRetrievalException {

        CertifiedProductDetailsDTO dto = getCertifiedProductDetailsDtoByChplProductNumber(chplProductNumber);
        return createCertifiedProductDetailsBasic(dto, retrieveAsynchronously);
    }

    @Override
    @Transactional
    public CertifiedProductSearchDetails getCertifiedProductDetailsBasic(final Long certifiedProductId)
            throws EntityRetrievalException {

        return getCertifiedProductDetailsBasic(certifiedProductId, propUtil.isAsyncListingDetailsEnabled());
    }

    @Override
    @Transactional
    public CertifiedProductSearchDetails getCertifiedProductDetailsBasic(final Long certifiedProductId,
            final Boolean retrieveAsynchronously) throws EntityRetrievalException {

        CertifiedProductDetailsDTO dto = certifiedProductSearchResultDAO.getById(certifiedProductId);
        return createCertifiedProductDetailsBasic(dto, retrieveAsynchronously);

    }

    @Override
    @Transactional
    public List<CQMResultDetails> getCertifiedProductCqms(final Long certifiedProductId)
            throws EntityRetrievalException {

        CertifiedProductDetailsDTO dto = certifiedProductSearchResultDAO.getById(certifiedProductId);
        Future<List<CQMResultDetailsDTO>> cqmResultsFuture = getCqmResultDetailsDTOs(certifiedProductId, false);

        return getCqmResultDetails(cqmResultsFuture, dto.getYear());
    }

    @Override
    @Transactional
    public List<CQMResultDetails> getCertifiedProductCqms(final String chplProductNumber)
            throws EntityRetrievalException {

        CertifiedProductDetailsDTO dto = getCertifiedProductDetailsDtoByChplProductNumber(chplProductNumber);
        Future<List<CQMResultDetailsDTO>> cqmResultsFuture = getCqmResultDetailsDTOs(dto.getId(), false);

        return getCqmResultDetails(cqmResultsFuture, dto.getYear());
    }

    @Override
    @Transactional
    public List<CertificationResult> getCertifiedProductCertificationResults(final Long certifiedProductId)
            throws EntityRetrievalException {

        CertifiedProductDetailsDTO dto = certifiedProductSearchResultDAO.getById(certifiedProductId);

        Future<List<CertificationResultDetailsDTO>> certificationResultsFuture = getCertificationResultDetailsDTOs(
                dto.getId(), true);

        CertifiedProductSearchDetails searchDetails = getCertifiedProductSearchDetails(dto);

        return getCertificationResults(certificationResultsFuture, searchDetails);

    }

    @Override
    @Transactional
    public List<CertificationResult> getCertifiedProductCertificationResults(final String chplProductNumber)
            throws EntityRetrievalException {

        CertifiedProductDetailsDTO dto = getCertifiedProductDetailsDtoByChplProductNumber(chplProductNumber);

        Future<List<CertificationResultDetailsDTO>> certificationResultsFuture = getCertificationResultDetailsDTOs(
                dto.getId(), true);

        CertifiedProductSearchDetails searchDetails = getCertifiedProductSearchDetails(dto);

        return getCertificationResults(certificationResultsFuture, searchDetails);
    }

    private CertifiedProductSearchDetails createCertifiedSearchDetails(final CertifiedProductDetailsDTO dto,
            final Boolean retrieveAsynchronously) throws EntityRetrievalException {

        Future<List<CertifiedProductDTO>> childrenFuture = getCertifiedProductChildren(dto.getId(),
                retrieveAsynchronously);
        Future<List<CertifiedProductDTO>> parentsFuture = getCertifiedProductParents(dto.getId(),
                retrieveAsynchronously);
        Future<List<CertificationResultDetailsDTO>> certificationResultsFuture = getCertificationResultDetailsDTOs(
                dto.getId(), retrieveAsynchronously);
        Future<List<CQMResultDetailsDTO>> cqmResultsFuture = getCqmResultDetailsDTOs(dto.getId(),
                retrieveAsynchronously);

        CertifiedProductSearchDetails searchDetails = getCertifiedProductSearchDetails(dto);

        searchDetails.setCertificationResults(getCertificationResults(certificationResultsFuture, searchDetails));
        searchDetails.setCqmResults(getCqmResultDetails(cqmResultsFuture, dto.getYear()));
        searchDetails.setCertificationEvents(getCertificationStatusEvents(dto.getId()));
        searchDetails.setMeaningfulUseUserHistory(getMeaningfulUseUserHistory(dto.getId()));
        // get first-level parents and children
        searchDetails.getIcs().setParents(populateParents(parentsFuture, searchDetails));
        searchDetails.getIcs().setChildren(populateChildren(childrenFuture, searchDetails));

        searchDetails = populateTestingLab(dto, searchDetails);
        return searchDetails;
    }

    private CertifiedProductSearchDetails createCertifiedProductDetailsBasic(final CertifiedProductDetailsDTO dto,
            final Boolean retrieveAsynchronously) throws EntityRetrievalException {

        Future<List<CertifiedProductDTO>> childrenFuture = getCertifiedProductChildren(dto.getId(),
                retrieveAsynchronously);
        Future<List<CertifiedProductDTO>> parentsFuture = getCertifiedProductParents(dto.getId(),
                retrieveAsynchronously);

        CertifiedProductSearchDetails searchDetails = getCertifiedProductSearchDetails(dto);
        searchDetails.setCertificationEvents(getCertificationStatusEvents(dto.getId()));
        searchDetails.setMeaningfulUseUserHistory(getMeaningfulUseUserHistory(dto.getId()));

        // get first-level parents and children
        searchDetails.getIcs().setParents(populateParents(parentsFuture, searchDetails));
        searchDetails.getIcs().setChildren(populateChildren(childrenFuture, searchDetails));

        searchDetails = populateTestingLab(dto, searchDetails);

        return searchDetails;
    }

    private CertifiedProductDetailsDTO getCertifiedProductDetailsDtoByChplProductNumber(final String chplProductNumber)
            throws EntityRetrievalException {

        List<CertifiedProductDetailsDTO> dtos = certifiedProductSearchResultDAO
                .getByChplProductNumber(chplProductNumber);
        if (dtos.size() == 0) {
            throw new EntityRetrievalException("Could not retrieve CertifiedProductSearchDetails.");
        }
        return dtos.get(0);
    }

    private CertifiedProductSearchDetails populateTestingLab(final CertifiedProductDetailsDTO dto,
            final CertifiedProductSearchDetails searchDetails) throws EntityRetrievalException {
        List<CertifiedProductTestingLabDTO> testingLabDtos = certifiedProductTestingLabDao
                .getTestingLabsByCertifiedProductId(dto.getId());
        List<CertifiedProductTestingLab> testingLabResults = new ArrayList<CertifiedProductTestingLab>();
        for (CertifiedProductTestingLabDTO testingLabDto : testingLabDtos) {
            CertifiedProductTestingLab result = new CertifiedProductTestingLab(testingLabDto);
            testingLabResults.add(result);
        }
        searchDetails.setTestingLabs(testingLabResults);
        return searchDetails;
    }

    private List<CertifiedProduct> populateParents(final Future<List<CertifiedProductDTO>> parentsFuture,
            final CertifiedProductSearchDetails searchDetails) throws EntityRetrievalException {
        try {
            List<CertifiedProduct> parents = new ArrayList<CertifiedProduct>();
            List<CertifiedProductDTO> parentDTOs = parentsFuture.get();
            if (parentDTOs != null && parentDTOs.size() > 0) {
                for (CertifiedProductDTO parentDTO : parentDTOs) {
                    parents.add(createCertifiedProduct(parentDTO));
                }
            }
            return parents;
        } catch (InterruptedException e) {
            throw new EntityRetrievalException("Error retrieving Parent Listings: " + e.getMessage());
        } catch (ExecutionException e) {
            throw new EntityRetrievalException("Error retrieving Parent Listings: " + e.getMessage());
        }
    }

    private List<CertifiedProduct> populateChildren(final Future<List<CertifiedProductDTO>> childrenFuture,
            final CertifiedProductSearchDetails searchDetails) throws EntityRetrievalException {
        try {
            List<CertifiedProduct> children = new ArrayList<CertifiedProduct>();
            List<CertifiedProductDTO> childrenDTOs = childrenFuture.get();
            if (childrenDTOs != null && childrenDTOs.size() > 0) {
                for (CertifiedProductDTO childDTO : childrenDTOs) {
                    children.add(createCertifiedProduct(childDTO));
                }
            }
            return children;
        } catch (InterruptedException e) {
            throw new EntityRetrievalException("Error retrieving Parent Listings: " + e.getMessage());
        } catch (ExecutionException e) {
            throw new EntityRetrievalException("Error retrieving Parent Listings: " + e.getMessage());
        }
    }

    public List<CQMCriterion> getCqmCriteria() {
        return cqmCriteria;
    }

    public void setCqmCriteria(final List<CQMCriterion> cqmCriteria) {
        this.cqmCriteria = cqmCriteria;
    }

    private List<CQMResultDetails> getCqmResultDetails(final Future<List<CQMResultDetailsDTO>> cqmResultsFuture,
            final String year) throws EntityRetrievalException {

        List<CQMResultDetails> details = new ArrayList<CQMResultDetails>();
        try {
            List<CQMResultDetailsDTO> cqmResultDTOs = cqmResultsFuture.get();
            details = getCqmResultDetails(cqmResultDTOs, year);
        } catch (InterruptedException e) {
            throw new EntityRetrievalException("Error retrieving CQM Result Details: " + e.getMessage());
        } catch (ExecutionException e) {
            throw new EntityRetrievalException("Error retrieving CQM Result Details: " + e.getMessage());
        }
        return details;
    }

    private List<CertificationResult> getCertificationResults(
            final Future<List<CertificationResultDetailsDTO>> certificationResultsFuture,
            final CertifiedProductSearchDetails searchDetails) throws EntityRetrievalException {

        List<CertificationResult> certificationResults = new ArrayList<CertificationResult>();
        try {
            List<CertificationResultDetailsDTO> certificationResultDetailsDTOs = new ArrayList<CertificationResultDetailsDTO>();
            certificationResultDetailsDTOs = certificationResultsFuture.get();
            for (CertificationResultDetailsDTO certResult : certificationResultDetailsDTOs) {
                certificationResults.add(getCertificationResult(certResult, searchDetails));
            }
        } catch (InterruptedException e) {
            throw new EntityRetrievalException("Error retrieving Certification Results: " + e.getMessage());
        } catch (ExecutionException e) {
            throw new EntityRetrievalException("Error retrieving Certification Resultss: " + e.getMessage());
        }
        return certificationResults;
    }

    @Override
    public List<CertificationStatusEvent> getCertificationStatusEvents(final Long certifiedProductId)
            throws EntityRetrievalException {

        List<CertificationStatusEvent> certEvents = new ArrayList<CertificationStatusEvent>();
        List<CertificationStatusEventDTO> certStatusDtos = certStatusEventDao
                .findByCertifiedProductId(certifiedProductId);

        for (CertificationStatusEventDTO certStatusDto : certStatusDtos) {
            CertificationStatusEvent cse = new CertificationStatusEvent();
            cse.setId(certStatusDto.getId());
            cse.setEventDate(certStatusDto.getEventDate().getTime());
            cse.setLastModifiedUser(certStatusDto.getLastModifiedUser());
            cse.setLastModifiedDate(certStatusDto.getLastModifiedDate().getTime());

            if (AuthUtil.getCurrentUser() != null
                    && (resourcePermissions.isUserRoleAcbAdmin() || resourcePermissions.isUserRoleAdmin())) {
                cse.setReason(certStatusDto.getReason());
            }

            CertificationStatusDTO statusDto = certStatusDao.getById(certStatusDto.getStatus().getId());
            cse.setStatus(new CertificationStatus(statusDto));
            certEvents.add(cse);
        }
        return certEvents;
    }

    private List<MeaningfulUseUser> getMeaningfulUseUserHistory(final Long certifiedProductId)
            throws EntityRetrievalException {

        List<MeaningfulUseUser> muuHistory = new ArrayList<MeaningfulUseUser>();
        List<MeaningfulUseUserDTO> muuDtos = muuDao.findByCertifiedProductId(certifiedProductId);

        for (MeaningfulUseUserDTO muuDto : muuDtos) {
            MeaningfulUseUser muu = new MeaningfulUseUser(muuDto);
            muuHistory.add(muu);
        }
        return muuHistory;
    }

    private void loadCriteriaMacraMeasures() {
        List<MacraMeasureDTO> dtos = macraDao.findAll();
        for (MacraMeasureDTO dto : dtos) {
            MacraMeasure measure = new MacraMeasure(dto);
            macraMeasures.add(measure);
        }
    }

    private void loadCQMCriteria() {
        List<CQMCriterionDTO> dtos = cqmCriterionDAO.findAll();
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
            cqmCriteria.add(criterion);
        }
    }

    private List<CQMCriterion> getAvailableCQMVersions() {
        List<CQMCriterion> criteria = new ArrayList<CQMCriterion>();
        for (CQMCriterion criterion : cqmCriteria) {
            if (!StringUtils.isEmpty(criterion.getCmsId()) && criterion.getCmsId().startsWith("CMS")) {
                criteria.add(criterion);
            }
        }
        return criteria;
    }

    private CertificationResult getCertificationResult(final CertificationResultDetailsDTO certResult,
            final CertifiedProductSearchDetails searchDetails) {

        CertificationResult result = new CertificationResult(certResult);
        // override optional boolean values
        if (!certRules.hasCertOption(certResult.getNumber(), CertificationResultRules.GAP)) {
            result.setGap(null);
        } else if (result.isGap() == null) {
            result.setGap(Boolean.FALSE);
        }
        if (!certRules.hasCertOption(certResult.getNumber(), CertificationResultRules.G1_SUCCESS)) {
            result.setG1Success(null);
        } else if (result.isG1Success() == null) {
            result.setG1Success(Boolean.FALSE);
        }
        if (!certRules.hasCertOption(certResult.getNumber(), CertificationResultRules.G2_SUCCESS)) {
            result.setG2Success(null);
        } else if (result.isG2Success() == null) {
            result.setG2Success(Boolean.FALSE);
        }
        if (!certRules.hasCertOption(certResult.getNumber(), CertificationResultRules.API_DOCUMENTATION)) {
            result.setApiDocumentation(null);
        } else if (result.getApiDocumentation() == null) {
            result.setApiDocumentation("");
        }
        if (!certRules.hasCertOption(certResult.getNumber(), CertificationResultRules.PRIVACY_SECURITY)) {
            result.setPrivacySecurityFramework(null);
        } else if (result.getPrivacySecurityFramework() == null) {
            result.setPrivacySecurityFramework("");
        }

        // add all the other data
        if (certRules.hasCertOption(certResult.getNumber(), CertificationResultRules.ADDITIONAL_SOFTWARE)) {
            List<CertificationResultAdditionalSoftwareDTO> certResultSoftware = certResultManager
                    .getAdditionalSoftwareMappingsForCertificationResult(certResult.getId());
            for (CertificationResultAdditionalSoftwareDTO currResult : certResultSoftware) {
                CertificationResultAdditionalSoftware softwareResult = new CertificationResultAdditionalSoftware(
                        currResult);
                result.getAdditionalSoftware().add(softwareResult);
            }
        } else {
            result.setAdditionalSoftware(null);
        }

        if (certRules.hasCertOption(certResult.getNumber(), CertificationResultRules.STANDARDS_TESTED)) {
            List<CertificationResultTestStandardDTO> testStandards = certResultManager
                    .getTestStandardsForCertificationResult(certResult.getId());
            for (CertificationResultTestStandardDTO currResult : testStandards) {
                CertificationResultTestStandard testStandardResult = new CertificationResultTestStandard(currResult);
                result.getTestStandards().add(testStandardResult);
            }
        } else {
            result.setTestStandards(null);
        }

        if (certRules.hasCertOption(certResult.getNumber(), CertificationResultRules.TEST_TOOLS_USED)) {
            List<CertificationResultTestToolDTO> testTools = certResultManager
                    .getTestToolsForCertificationResult(certResult.getId());
            for (CertificationResultTestToolDTO currResult : testTools) {
                CertificationResultTestTool testToolResult = new CertificationResultTestTool(currResult);
                result.getTestToolsUsed().add(testToolResult);
            }
        } else {
            result.setTestToolsUsed(null);
        }

        if (certRules.hasCertOption(certResult.getNumber(), CertificationResultRules.TEST_DATA)) {
            List<CertificationResultTestDataDTO> testData = certResultManager
                    .getTestDataForCertificationResult(certResult.getId());
            for (CertificationResultTestDataDTO currResult : testData) {
                CertificationResultTestData testDataResult = new CertificationResultTestData(currResult);
                result.getTestDataUsed().add(testDataResult);
            }
        } else {
            result.setTestDataUsed(null);
        }

        if (certRules.hasCertOption(certResult.getNumber(), CertificationResultRules.TEST_PROCEDURE)) {
            List<CertificationResultTestProcedureDTO> testProcedure = certResultManager
                    .getTestProceduresForCertificationResult(certResult.getId());
            for (CertificationResultTestProcedureDTO currResult : testProcedure) {
                CertificationResultTestProcedure testProcedureResult = new CertificationResultTestProcedure(currResult);
                result.getTestProcedures().add(testProcedureResult);
            }
        } else {
            result.setTestProcedures(null);
        }

        if (certRules.hasCertOption(certResult.getNumber(), CertificationResultRules.FUNCTIONALITY_TESTED)) {
            List<CertificationResultTestFunctionalityDTO> testFunctionality = certResultManager
                    .getTestFunctionalityForCertificationResult(certResult.getId());
            for (CertificationResultTestFunctionalityDTO currResult : testFunctionality) {
                CertificationResultTestFunctionality testFunctionalityResult = new CertificationResultTestFunctionality(
                        currResult);
                result.getTestFunctionality().add(testFunctionalityResult);
            }
        } else {
            result.setTestFunctionality(null);
        }

        if (!certRules.hasCertOption(certResult.getNumber(), CertificationResultRules.G1_MACRA)
                && !certRules.hasCertOption(certResult.getNumber(), CertificationResultRules.G2_MACRA)) {
            result.setAllowedMacraMeasures(null);
            result.setG1MacraMeasures(null);
            result.setG2MacraMeasures(null);
        } else {
            if (certRules.hasCertOption(certResult.getNumber(), CertificationResultRules.G1_MACRA)) {
                List<CertificationResultMacraMeasureDTO> measures = certResultManager
                        .getG1MacraMeasuresForCertificationResult(certResult.getId());
                for (CertificationResultMacraMeasureDTO currResult : measures) {
                    MacraMeasure mmResult = new MacraMeasure(currResult.getMeasure());
                    result.getG1MacraMeasures().add(mmResult);
                }
            } else {
                result.setG1MacraMeasures(null);
            }

            if (certRules.hasCertOption(certResult.getNumber(), CertificationResultRules.G2_MACRA)) {
                List<CertificationResultMacraMeasureDTO> measures = certResultManager
                        .getG2MacraMeasuresForCertificationResult(certResult.getId());
                for (CertificationResultMacraMeasureDTO currResult : measures) {
                    MacraMeasure mmResult = new MacraMeasure(currResult.getMeasure());
                    result.getG2MacraMeasures().add(mmResult);
                }
            } else {
                result.setG2MacraMeasures(null);
            }
        }

        // get all SED data for the listing
        // ucd processes and test tasks with participants
        CertificationCriterion criteria = new CertificationCriterion();
        criteria.setNumber(result.getNumber());
        criteria.setTitle(result.getTitle());

        if (certRules.hasCertOption(certResult.getNumber(), CertificationResultRules.UCD_FIELDS)) {
            List<CertificationResultUcdProcessDTO> ucdProcesses = certResultManager
                    .getUcdProcessesForCertificationResult(result.getId());
            for (CertificationResultUcdProcessDTO currResult : ucdProcesses) {
                boolean alreadyExists = false;
                UcdProcess newUcd = new UcdProcess(currResult);
                for (UcdProcess currUcd : searchDetails.getSed().getUcdProcesses()) {
                    if (newUcd.matches(currUcd)) {
                        alreadyExists = true;
                        currUcd.getCriteria().add(criteria);
                    }
                }
                if (!alreadyExists) {
                    newUcd.getCriteria().add(criteria);
                    searchDetails.getSed().getUcdProcesses().add(newUcd);
                }
            }
        } else {
            result.setSed(null);
        }

        if (certRules.hasCertOption(certResult.getNumber(), CertificationResultRules.TEST_TASK)) {
            List<CertificationResultTestTaskDTO> testTask = certResultManager
                    .getTestTasksForCertificationResult(certResult.getId());
            for (CertificationResultTestTaskDTO currResult : testTask) {
                boolean alreadyExists = false;
                TestTask newTestTask = new TestTask(currResult);
                for (TestTask currTestTask : searchDetails.getSed().getTestTasks()) {
                    if (newTestTask.matches(currTestTask)) {
                        alreadyExists = true;
                        currTestTask.getCriteria().add(criteria);
                    }
                }
                if (!alreadyExists) {
                    newTestTask.getCriteria().add(criteria);
                    searchDetails.getSed().getTestTasks().add(newTestTask);
                }
            }
        }

        // set allowed macra measures (if any)
        for (MacraMeasure measure : macraMeasures) {
            if (measure.getCriteria().getNumber().equals(result.getNumber())) {
                result.getAllowedMacraMeasures().add(measure);
            }
        }
        result.setAllowedTestFunctionalities(getAvailableTestFunctionalities(result, searchDetails));
        return result;
    }

    private List<TestFunctionality> getAvailableTestFunctionalities(final CertificationResult cr,
            final CertifiedProductSearchDetails cp) {

        String edition = cp.getCertificationEdition().get(CertifiedProductSearchDetails.EDITION_NAME_KEY).toString();
        Long practiceTypeId = null;
        if (cp.getPracticeType().containsKey("id")) {
            if (cp.getPracticeType().get("id") != null) {
                practiceTypeId = Long.valueOf(cp.getPracticeType().get("id").toString());
            }
        }
        String criteriaNumber = cr.getNumber();
        return testFunctionalityManager.getTestFunctionalities(criteriaNumber, edition, practiceTypeId);
    }

    private CertifiedProductSearchDetails getCertifiedProductSearchDetails(final CertifiedProductDetailsDTO dto)
            throws EntityRetrievalException {

        CertifiedProductSearchDetails searchDetails = new CertifiedProductSearchDetails();
        searchDetails.setId(dto.getId());
        searchDetails.setAcbCertificationId(dto.getAcbCertificationId());

        if (dto.getCertificationDate() != null) {
            searchDetails.setCertificationDate(dto.getCertificationDate().getTime());
        }

        if (dto.getDecertificationDate() != null) {
            searchDetails.setDecertificationDate(dto.getDecertificationDate().getTime());
        }

        searchDetails.setCertificationEdition(getCertifificationEdition(dto));
        searchDetails.setChplProductNumber(getChplProductNumber(dto));
        searchDetails.setCertifyingBody(getCertifyingBody(dto));
        searchDetails.setClassificationType(getClassificationType(dto));
        searchDetails.setOtherAcb(dto.getOtherAcb());
        searchDetails.setPracticeType(getPracticeType(dto));
        searchDetails.setReportFileLocation(dto.getReportFileLocation());
        searchDetails.setSedReportFileLocation(dto.getSedReportFileLocation());
        searchDetails.setSedIntendedUserDescription(dto.getSedIntendedUserDescription());
        searchDetails.setSedTestingEndDate(dto.getSedTestingEnd());
        searchDetails.setTestingLabs(getTestingLabs(dto.getId()));
        searchDetails.setDeveloper(new Developer(dto.getDeveloper()));
        searchDetails.setProduct(new Product(dto.getProduct()));
        searchDetails.setVersion(new ProductVersion(dto.getVersion()));
        searchDetails.setProductAdditionalSoftware(dto.getProductAdditionalSoftware());
        searchDetails.setTransparencyAttestationUrl(dto.getTransparencyAttestationUrl());
        searchDetails.setTransparencyAttestation(dto.getTransparencyAttestation());
        searchDetails.setLastModifiedDate(dto.getLastModifiedDate().getTime());
        searchDetails.setCountCerts(dto.getCountCertifications());
        searchDetails.setCountCqms(dto.getCountCqms());
        searchDetails.setCountSurveillance(dto.getCountSurveillance());
        searchDetails.setCountOpenSurveillance(dto.getCountOpenSurveillance());
        searchDetails.setCountClosedSurveillance(dto.getCountClosedSurveillance());
        searchDetails.setCountOpenNonconformities(dto.getCountOpenNonconformities());
        searchDetails.setCountClosedNonconformities(dto.getCountClosedNonconformities());
        searchDetails.setSurveillance(survManager.getByCertifiedProduct(dto.getId()));
        searchDetails.setQmsStandards(getCertifiedProductQmsStandards(dto.getId()));
        searchDetails.setTargetedUsers(getCertifiedProductTargetedUsers(dto.getId()));
        searchDetails.setAccessibilityStandards(getCertifiedProductAccessibilityStandards(dto.getId()));

        InheritedCertificationStatus ics = new InheritedCertificationStatus();
        ics.setInherits(dto.getIcs());
        searchDetails.setIcs(ics);

        return searchDetails;
    }

    // This should probably be refactored to use ChplProductNumberUtil
    private String getChplProductNumber(final CertifiedProductDetailsDTO dto) {
        if (!StringUtils.isEmpty(dto.getChplProductNumber())) {
            return dto.getChplProductNumber();
        } else {
            return dto.getYearCode() + "." + dto.getTestingLabCode() + "." + dto.getCertificationBodyCode() + "."
                    + dto.getDeveloper().getDeveloperCode() + "." + dto.getProductCode() + "." + dto.getVersionCode()
                    + "." + dto.getIcsCode() + "." + dto.getAdditionalSoftwareCode() + "." + dto.getCertifiedDateCode();
        }
    }

    private Map<String, Object> getCertifificationEdition(final CertifiedProductDetailsDTO dto) {
        Map<String, Object> certificationEdition = new HashMap<String, Object>();
        certificationEdition.put("id", dto.getCertificationEditionId());
        certificationEdition.put("name", dto.getYear());
        return certificationEdition;
    }

    private Map<String, Object> getCertifyingBody(final CertifiedProductDetailsDTO dto) {
        Map<String, Object> certifyingBody = new HashMap<String, Object>();
        certifyingBody.put("id", dto.getCertificationBodyId());
        certifyingBody.put("name", dto.getCertificationBodyName());
        certifyingBody.put("code", dto.getCertificationBodyCode());
        return certifyingBody;
    }

    private Map<String, Object> getClassificationType(final CertifiedProductDetailsDTO dto) {
        Map<String, Object> classificationType = new HashMap<String, Object>();
        classificationType.put("id", dto.getProductClassificationTypeId());
        classificationType.put("name", dto.getProductClassificationName());
        return classificationType;
    }

    private Map<String, Object> getPracticeType(final CertifiedProductDetailsDTO dto) {
        Map<String, Object> practiceType = new HashMap<String, Object>();
        practiceType.put("id", dto.getPracticeTypeId());
        practiceType.put("name", dto.getPracticeTypeName());
        return practiceType;
    }

    private List<CertifiedProductTestingLab> getTestingLabs(final Long id) throws EntityRetrievalException {
        List<CertifiedProductTestingLabDTO> testingLabDtos = new ArrayList<CertifiedProductTestingLabDTO>();
        testingLabDtos = certifiedProductTestingLabDao.getTestingLabsByCertifiedProductId(id);

        List<CertifiedProductTestingLab> testingLabResults = new ArrayList<CertifiedProductTestingLab>();
        for (CertifiedProductTestingLabDTO testingLabDto : testingLabDtos) {
            CertifiedProductTestingLab result = new CertifiedProductTestingLab(testingLabDto);
            testingLabResults.add(result);
        }
        return testingLabResults;
    }

    private List<CertifiedProductQmsStandard> getCertifiedProductQmsStandards(final Long id)
            throws EntityRetrievalException {

        List<CertifiedProductQmsStandardDTO> qmsStandardDTOs = new ArrayList<CertifiedProductQmsStandardDTO>();
        qmsStandardDTOs = certifiedProductQmsStandardDao.getQmsStandardsByCertifiedProductId(id);

        List<CertifiedProductQmsStandard> qmsStandardResults = new ArrayList<CertifiedProductQmsStandard>();
        for (CertifiedProductQmsStandardDTO qmsStandardResult : qmsStandardDTOs) {
            CertifiedProductQmsStandard result = new CertifiedProductQmsStandard(qmsStandardResult);
            qmsStandardResults.add(result);
        }
        return qmsStandardResults;
    }

    private List<CertifiedProductTargetedUser> getCertifiedProductTargetedUsers(final Long id)
            throws EntityRetrievalException {
        List<CertifiedProductTargetedUserDTO> targetedUserDtos = new ArrayList<CertifiedProductTargetedUserDTO>();
        targetedUserDtos = certifiedProductTargetedUserDao.getTargetedUsersByCertifiedProductId(id);

        List<CertifiedProductTargetedUser> targetedUserResults = new ArrayList<CertifiedProductTargetedUser>();
        for (CertifiedProductTargetedUserDTO targetedUserDto : targetedUserDtos) {
            CertifiedProductTargetedUser result = new CertifiedProductTargetedUser(targetedUserDto);
            targetedUserResults.add(result);
        }
        return targetedUserResults;
    }

    private List<CertifiedProductAccessibilityStandard> getCertifiedProductAccessibilityStandards(final Long id)
            throws EntityRetrievalException {

        List<CertifiedProductAccessibilityStandardDTO> accessibilityStandardDtos = new ArrayList<CertifiedProductAccessibilityStandardDTO>();

        accessibilityStandardDtos = certifiedProductAsDao.getAccessibilityStandardsByCertifiedProductId(id);

        List<CertifiedProductAccessibilityStandard> accessibilityStandardResults = new ArrayList<CertifiedProductAccessibilityStandard>();

        for (CertifiedProductAccessibilityStandardDTO accessibilityStandardDto : accessibilityStandardDtos) {
            CertifiedProductAccessibilityStandard result = new CertifiedProductAccessibilityStandard(
                    accessibilityStandardDto);
            accessibilityStandardResults.add(result);
        }
        return accessibilityStandardResults;
    }

    private List<CQMResultDetails> getCqmResultDetails(final List<CQMResultDetailsDTO> cqmResultDTOs,
            final String year) {

        List<CQMResultDetails> cqmResults = new ArrayList<CQMResultDetails>();
        for (CQMResultDetailsDTO cqmResultDTO : cqmResultDTOs) {
            boolean existingCms = false;
            // for a CMS, first check to see if we already have an object with
            // the same CMS id
            // so we can just add to it's success versions.
            if (!year.equals("2011") && !StringUtils.isEmpty(cqmResultDTO.getCmsId())) {
                for (CQMResultDetails result : cqmResults) {
                    if (cqmResultDTO.getCmsId().equals(result.getCmsId())) {
                        existingCms = true;
                        result.getSuccessVersions().add(cqmResultDTO.getVersion());
                    }
                }
            }

            if (!existingCms) {
                CQMResultDetails result = new CQMResultDetails();
                result.setId(cqmResultDTO.getId());
                result.setCmsId(cqmResultDTO.getCmsId());
                result.setNqfNumber(cqmResultDTO.getNqfNumber());
                result.setNumber(cqmResultDTO.getNumber());
                result.setTitle(cqmResultDTO.getTitle());
                result.setDescription(cqmResultDTO.getDescription());
                result.setTypeId(cqmResultDTO.getCqmCriterionTypeId());
                if (!year.equals("2011") && !StringUtils.isEmpty(cqmResultDTO.getCmsId())) {
                    result.getSuccessVersions().add(cqmResultDTO.getVersion());
                } else {
                    result.setSuccess(cqmResultDTO.getSuccess());
                }
                cqmResults.add(result);
            }
        }

        // now add allVersions for CMSs
        if (!year.startsWith("2011")) {
            List<CQMCriterion> cqms = getAvailableCQMVersions();
            for (CQMCriterion cqm : cqms) {
                boolean cqmExists = false;
                for (CQMResultDetails details : cqmResults) {
                    if (cqm.getCmsId().equals(details.getCmsId())) {
                        cqmExists = true;
                        details.getAllVersions().add(cqm.getCqmVersion());
                    }
                }
                if (!cqmExists) {
                    CQMResultDetails result = new CQMResultDetails();
                    result.setCmsId(cqm.getCmsId());
                    result.setNqfNumber(cqm.getNqfNumber());
                    result.setNumber(cqm.getNumber());
                    result.setTitle(cqm.getTitle());
                    result.setDescription(cqm.getDescription());
                    result.setSuccess(Boolean.FALSE);
                    result.getAllVersions().add(cqm.getCqmVersion());
                    result.setTypeId(cqm.getCqmCriterionTypeId());
                    cqmResults.add(result);
                }
            }
        }

        // now add criteria mappings to all of our cqms
        for (CQMResultDetails cqmResult : cqmResults) {
            cqmResult.setCriteria(getCqmCriteriaMapping(cqmResult));
        }
        return cqmResults;
    }

    private List<CQMResultCertification> getCqmCriteriaMapping(final CQMResultDetails cqmResult) {
        List<CQMResultCertification> cqmResultCertifications = new ArrayList<CQMResultCertification>();
        if (cqmResult.isSuccess() && cqmResult.getId() != null) {
            List<CQMResultCriteriaDTO> criteria = cqmResultDao.getCriteriaForCqmResult(cqmResult.getId());
            if (criteria != null && criteria.size() > 0) {
                for (CQMResultCriteriaDTO criteriaDTO : criteria) {
                    CQMResultCertification c = new CQMResultCertification();
                    c.setCertificationId(criteriaDTO.getCriterionId());
                    c.setId(criteriaDTO.getId());
                    if (criteriaDTO.getCriterion() != null) {
                        c.setCertificationNumber(criteriaDTO.getCriterion().getNumber());
                    }
                    cqmResultCertifications.add(c);
                }
            }
        }
        return cqmResultCertifications;
    }

    private Future<List<CertifiedProductDTO>> getCertifiedProductChildren(final Long id,
            final Boolean retrieveAsynchronously) {
        if (retrieveAsynchronously) {
            return async.getCertifiedProductChildren(listingGraphDao, id);
        } else {
            return async.getFutureCertifiedProductChildren(listingGraphDao, id);
        }
    }

    private Future<List<CertifiedProductDTO>> getCertifiedProductParents(final Long id,
            final Boolean retrieveAsynchronously) {
        if (retrieveAsynchronously) {
            return async.getCertifiedProductParent(listingGraphDao, id);
        } else {
            return async.getFutureCertifiedProductParent(listingGraphDao, id);
        }
    }

    private Future<List<CertificationResultDetailsDTO>> getCertificationResultDetailsDTOs(final Long id,
            final Boolean retrieveAsynchronously) {
        if (retrieveAsynchronously) {
            return async.getCertificationResultDetailsDTOs(certificationResultDetailsDAO, id);
        } else {
            return async.getFutureCertificationResultDetailsDTOs(certificationResultDetailsDAO, id);
        }
    }

    private Future<List<CQMResultDetailsDTO>> getCqmResultDetailsDTOs(final Long id,
            final Boolean retrieveAsynchronously) {
        if (retrieveAsynchronously) {
            return async.getCqmResultDetailsDTOs(cqmResultDetailsDAO, id);
        } else {
            return async.getFutureCqmResultDetailsDTOs(cqmResultDetailsDAO, id);
        }
    }

    private CertifiedProduct createCertifiedProduct(final CertifiedProductDTO dto) {
        CertifiedProduct cp = new CertifiedProduct();
        cp.setId(dto.getId());
        cp.setChplProductNumber(chplProductNumberUtil.generate(dto.getId()));
        cp.setLastModifiedDate(dto.getLastModifiedDate() != null ? dto.getLastModifiedDate().getTime() + "" : "");
        CertificationEditionDTO edition = getEdition(dto.getCertificationEditionId());
        if (edition != null) {
            cp.setEdition(edition.getYear());
        }
        CertificationStatusEventDTO cseDTO = certificationStatusEventDAO
                .findInitialCertificationEventForCertifiedProduct(dto.getId());
        if (cseDTO != null) {
            cp.setCertificationDate(cseDTO.getEventDate().getTime());
        } else {
            cp.setCertificationDate(-1);
        }
        return cp;
    }

    private CertificationEditionDTO getEdition(final Long editionId) {
        for (CertificationEditionDTO dto : this.editions) {
            if (dto.getId().equals(editionId)) {
                return dto;
            }
        }
        return null;
    }
}
