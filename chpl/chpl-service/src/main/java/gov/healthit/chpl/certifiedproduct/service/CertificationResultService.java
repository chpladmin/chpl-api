package gov.healthit.chpl.certifiedproduct.service;

import java.util.List;
import java.util.stream.Collectors;

import org.ff4j.FF4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.FeatureList;
import gov.healthit.chpl.conformanceMethod.dao.ConformanceMethodDAO;
import gov.healthit.chpl.conformanceMethod.domain.ConformanceMethod;
import gov.healthit.chpl.conformanceMethod.domain.ConformanceMethodCriteriaMap;
import gov.healthit.chpl.dao.CertificationResultDetailsDAO;
import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.CertificationResult;
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
import gov.healthit.chpl.optionalStandard.dao.OptionalStandardDAO;
import gov.healthit.chpl.optionalStandard.domain.OptionalStandard;
import gov.healthit.chpl.optionalStandard.domain.OptionalStandardCriteriaMap;
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
    private OptionalStandardDAO optionalStandardDAO;
    private ConformanceMethodDAO conformanceMethodDAO;
    private FF4j ff4j;

    @Autowired
    public CertificationResultService(CertificationResultRules certRules, CertificationResultManager certResultManager,
            TestingFunctionalityManager testFunctionalityManager, CertificationResultDetailsDAO certificationResultDetailsDAO,
            SvapDAO svapDAO, OptionalStandardDAO optionalStandardDAO, ConformanceMethodDAO conformanceMethodDAO, FF4j ff4j) {
        this.certRules = certRules;
        this.certResultManager = certResultManager;
        this.testFunctionalityManager = testFunctionalityManager;
        this.certificationResultDetailsDAO = certificationResultDetailsDAO;
        this.svapDao = svapDAO;
        this.optionalStandardDAO = optionalStandardDAO;
        this.conformanceMethodDAO = conformanceMethodDAO;
        this.ff4j = ff4j;
    }

    public List<CertificationResult> getCertificationResults(CertifiedProductSearchDetails searchDetails) throws EntityRetrievalException {
        List<SvapCriteriaMap> svapCriteriaMap = svapDao.getAllSvapCriteriaMap();
        List<OptionalStandardCriteriaMap> optionalStandardCriteriaMap = optionalStandardDAO.getAllOptionalStandardCriteriaMap();
        List<ConformanceMethodCriteriaMap> conformanceMethodCriteriaMap = conformanceMethodDAO.getAllConformanceMethodCriteriaMap();

        return getCertificationResultDetailsDTOs(searchDetails.getId()).stream()
                .map(dto -> getCertificationResult(dto, searchDetails, svapCriteriaMap, optionalStandardCriteriaMap, conformanceMethodCriteriaMap))
                .collect(Collectors.toList());
    }

    public List<CertificationResultDetailsDTO> getCertificationResultDetailsDTOs(Long id) {
        List<CertificationResultDetailsDTO> certificationResultDetailsDTOs = null;
        certificationResultDetailsDTOs = certificationResultDetailsDAO.getAllCertResultsForListing(id);
        return certificationResultDetailsDTOs;
    }

    private CertificationResult getCertificationResult(CertificationResultDetailsDTO certResult,
            CertifiedProductSearchDetails searchDetails, List<SvapCriteriaMap> svapCriteriaMap,
            List<OptionalStandardCriteriaMap> optionalStandardCriteriaMap, List<ConformanceMethodCriteriaMap> conformanceMethodCriteriaMap) {

        CertificationResult result = new CertificationResult(certResult, certRules);

        CertificationCriterion criteria = result.getCriterion();
        populateSed(certResult, searchDetails, result, criteria);
        populateTestTasks(certResult, searchDetails, criteria);

        result.setAllowedConformanceMethods(getAvailableConformanceMethodsForCriteria(result, conformanceMethodCriteriaMap));
        result.setAllowedOptionalStandards(getAvailableOptionalStandardsForCriteria(result, optionalStandardCriteriaMap));
        result.setAllowedSvaps(getAvailableSvapForCriteria(result, svapCriteriaMap));
        result.setAllowedTestFunctionalities(getAvailableTestFunctionalities(result, searchDetails));

        return result;
    }

    private void populateTestTasks(CertificationResultDetailsDTO certResult, CertifiedProductSearchDetails searchDetails, CertificationCriterion criteria) {
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
    }

    private void populateSed(CertificationResultDetailsDTO certResult, CertifiedProductSearchDetails searchDetails, CertificationResult result, CertificationCriterion criteria) {
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
    }

    private List<ConformanceMethod> getAvailableConformanceMethodsForCriteria(CertificationResult result, List<ConformanceMethodCriteriaMap> conformanceMethodCriteriaMap) {
        return conformanceMethodCriteriaMap.stream()
                .filter(cmcm -> ff4j.check(FeatureList.CONFORMANCE_METHOD) && cmcm.getCriterion().getId().equals(result.getCriterion().getId()))
                .map(cmcm -> cmcm.getConformanceMethod())
                .collect(Collectors.toList());
    }

    private List<OptionalStandard> getAvailableOptionalStandardsForCriteria(CertificationResult result, List<OptionalStandardCriteriaMap> optionalStandardCriteriaMap) {
        return optionalStandardCriteriaMap.stream()
                .filter(osm -> ff4j.check(FeatureList.OPTIONAL_STANDARDS) && osm.getCriterion().getId().equals(result.getCriterion().getId()))
                .map(osm -> osm.getOptionalStandard())
                .collect(Collectors.toList());
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
