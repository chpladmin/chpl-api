package gov.healthit.chpl.certifiedproduct.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.CertificationResultDetailsDAO;
import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertificationResultAdditionalSoftware;
import gov.healthit.chpl.domain.CertificationResultTestData;
import gov.healthit.chpl.domain.CertificationResultTestFunctionality;
import gov.healthit.chpl.domain.CertificationResultTestProcedure;
import gov.healthit.chpl.domain.CertificationResultTestStandard;
import gov.healthit.chpl.domain.CertificationResultTestTool;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.TestFunctionality;
import gov.healthit.chpl.domain.TestTask;
import gov.healthit.chpl.domain.UcdProcess;
import gov.healthit.chpl.dto.CertificationResultDetailsDTO;
import gov.healthit.chpl.dto.CertificationResultTestTaskDTO;
import gov.healthit.chpl.dto.CertificationResultUcdProcessDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.manager.CertificationResultManager;
import gov.healthit.chpl.manager.TestingFunctionalityManager;
import gov.healthit.chpl.svap.dao.SvapDAO;
import gov.healthit.chpl.svap.domain.Svap;
import gov.healthit.chpl.svap.domain.SvapCriteriaMap;
import gov.healthit.chpl.util.CertificationResultRules;

@Component
public class CertificationResultService {
    private CertificationResultRules certRules;
    private CertificationResultManager certResultManager;
    private TestingFunctionalityManager testFunctionalityManager;
    private CertificationResultDetailsDAO certificationResultDetailsDAO;
    private SvapDAO svapDao;

    @Autowired
    public CertificationResultService(CertificationResultRules certRules, CertificationResultManager certResultManager,
            TestingFunctionalityManager testFunctionalityManager, CertificationResultDetailsDAO certificationResultDetailsDAO,
            SvapDAO svapDao) {
        this.certRules = certRules;
        this.certResultManager = certResultManager;
        this.testFunctionalityManager = testFunctionalityManager;
        this.certificationResultDetailsDAO = certificationResultDetailsDAO;
        this.svapDao = svapDao;
    }

    public List<CertificationResult> getCertificationResults(CertifiedProductSearchDetails searchDetails) throws EntityRetrievalException {
        List<SvapCriteriaMap> svapCriteriaMap = svapDao.getAllSvapCriteriaMap();

        return getCertificationResultDetailsDTOs(searchDetails.getId()).stream()
                .map(dto -> getCertificationResult(dto, searchDetails, svapCriteriaMap))
                .collect(Collectors.toList());
    }

    public List<CertificationResultDetailsDTO> getCertificationResultDetailsDTOs(Long id) {
        List<CertificationResultDetailsDTO> certificationResultDetailsDTOs = null;
        certificationResultDetailsDTOs = certificationResultDetailsDAO.getAllCertResultsForListing(id);
        return certificationResultDetailsDTOs;
    }


    @SuppressWarnings({"checkstyle:methodlength"})
    private CertificationResult getCertificationResult(CertificationResultDetailsDTO certResult,
            CertifiedProductSearchDetails searchDetails, List<SvapCriteriaMap> svapCriteriaMap) {

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
        if (!certRules.hasCertOption(certResult.getNumber(), CertificationResultRules.EXPORT_DOCUMENTATION)) {
            result.setExportDocumentation(null);
        } else if (result.getExportDocumentation() == null) {
            result.setExportDocumentation("");
        }
        if (!certRules.hasCertOption(certResult.getNumber(), CertificationResultRules.DOCUMENTATION_URL)) {
            result.setDocumentationUrl(null);
        } else if (result.getDocumentationUrl() == null) {
            result.setDocumentationUrl("");
        }
        if (!certRules.hasCertOption(certResult.getNumber(), CertificationResultRules.USE_CASES)) {
            result.setUseCases(null);
        } else if (result.getUseCases() == null) {
            result.setUseCases("");
        }
        if (!certRules.hasCertOption(certResult.getNumber(), CertificationResultRules.PRIVACY_SECURITY)) {
            result.setPrivacySecurityFramework(null);
        } else if (result.getPrivacySecurityFramework() == null) {
            result.setPrivacySecurityFramework("");
        }
        if (!certRules.hasCertOption(certResult.getNumber(), CertificationResultRules.ATTESTATION_ANSWER)) {
            result.setAttestationAnswer(null);
        } else if (result.getAttestationAnswer() == null) {
            result.setAttestationAnswer(false);
        }
        // add all the other data
        if (certRules.hasCertOption(certResult.getNumber(), CertificationResultRules.ADDITIONAL_SOFTWARE)) {
            result.setAdditionalSoftware(certResult.getAdditionalSoftware().stream()
                    .filter(res -> !res.getDeleted())
                    .map(res -> new CertificationResultAdditionalSoftware(res))
                    .collect(Collectors.toList()));
        } else {
            result.setAdditionalSoftware(null);
        }

        if (certRules.hasCertOption(certResult.getNumber(), CertificationResultRules.STANDARDS_TESTED)) {
            result.setTestStandards(certResult.getTestStandards().stream()
                    .filter(res -> !res.getDeleted())
                    .map(res -> new CertificationResultTestStandard(res))
                    .collect(Collectors.toList()));
        } else {
            result.setTestStandards(null);
        }

        if (certRules.hasCertOption(certResult.getNumber(), CertificationResultRules.TEST_TOOLS_USED)) {
            result.setTestToolsUsed(certResult.getTestTools().stream()
                    .filter(res -> !res.getDeleted())
                    .map(res -> new CertificationResultTestTool(res))
                    .collect(Collectors.toList()));
        } else {
            result.setTestToolsUsed(null);
        }

        if (certRules.hasCertOption(certResult.getNumber(), CertificationResultRules.TEST_DATA)) {
            result.setTestDataUsed(certResult.getTestData().stream()
                    .filter(res -> !res.getDeleted())
                    .map(res -> new CertificationResultTestData(res))
                    .collect(Collectors.toList()));
        } else {
            result.setTestDataUsed(null);
        }

        if (certRules.hasCertOption(certResult.getNumber(), CertificationResultRules.TEST_PROCEDURE)) {
            result.setTestProcedures(certResult.getTestProcedures().stream()
                    .filter(res -> !res.getDeleted())
                    .map(res -> new CertificationResultTestProcedure(res))
                    .collect(Collectors.toList()));
        } else {
            result.setTestProcedures(null);
        }

        if (certRules.hasCertOption(certResult.getNumber(), CertificationResultRules.FUNCTIONALITY_TESTED)) {
            result.setTestFunctionality(certResult.getTestFunctionality().stream()
                    .filter(res -> !res.getDeleted())
                    .map(res -> new CertificationResultTestFunctionality(res))
                    .collect(Collectors.toList()));
        } else {
            result.setTestFunctionality(null);
        }

        // get all SED data for the listing
        // ucd processes and test tasks with participants
        CertificationCriterion criteria = result.getCriterion();

        if (certRules.hasCertOption(certResult.getNumber(), CertificationResultRules.UCD_FIELDS)) {
            List<CertificationResultUcdProcessDTO> ucdProcesses = certResultManager.getUcdProcessesForCertificationResult(result.getId());
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
            List<CertificationResultTestTaskDTO> testTask = certResultManager.getTestTasksForCertificationResult(certResult.getId());
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

        result.setAllowedTestFunctionalities(getAvailableTestFunctionalities(result, searchDetails));

        // set allowed svap for criteria
        result.setAllowedSvaps(getAvailableSvapForCriteria(result, svapCriteriaMap));

        if (result.getAllowedSvaps().size() > 0) {
            result.setSvaps(certResultManager.getSvapsForCertificationResult(result.getId()));
        } else {
            result.setSvaps(null);
        }

        return result;
    }

    private List<Svap> getAvailableSvapForCriteria(CertificationResult result, List<SvapCriteriaMap> svapCriteriaMap) {
        return svapCriteriaMap.stream()
                .filter(scm -> scm.getCriterion().getId().equals(result.getCriterion().getId()))
                .map(scm -> scm.getSvap())
                .collect(Collectors.toList());
    }

    private List<TestFunctionality> getAvailableTestFunctionalities(CertificationResult cr, CertifiedProductSearchDetails cp) {
        String edition = cp.getCertificationEdition().get(CertifiedProductSearchDetails.EDITION_NAME_KEY).toString();
        Long practiceTypeId = null;
        if (cp.getPracticeType().containsKey("id")) {
            if (cp.getPracticeType().get("id") != null) {
                practiceTypeId = Long.valueOf(cp.getPracticeType().get("id").toString());
            }
        }
        return testFunctionalityManager.getTestFunctionalities(cr.getCriterion().getId(), edition, practiceTypeId);
    }


}
