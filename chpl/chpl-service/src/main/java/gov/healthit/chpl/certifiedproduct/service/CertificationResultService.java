package gov.healthit.chpl.certifiedproduct.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.conformanceMethod.ConformanceMethodDAO;
import gov.healthit.chpl.conformanceMethod.domain.ConformanceMethodCriteriaMap;
import gov.healthit.chpl.dao.CertificationResultDetailsDAO;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.CertifiedProductUcdProcess;
import gov.healthit.chpl.domain.TestTask;
import gov.healthit.chpl.domain.TestToolCriteriaMap;
import gov.healthit.chpl.domain.comparator.CertificationResultComparator;
import gov.healthit.chpl.dto.CertificationResultDetailsDTO;
import gov.healthit.chpl.dto.CertificationResultTestTaskDTO;
import gov.healthit.chpl.dto.CertificationResultUcdProcessDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.manager.CertificationResultManager;
import gov.healthit.chpl.optionalStandard.OptionalStandardDAO;
import gov.healthit.chpl.optionalStandard.domain.OptionalStandardCriteriaMap;
import gov.healthit.chpl.svap.dao.SvapDAO;
import gov.healthit.chpl.svap.domain.SvapCriteriaMap;
import gov.healthit.chpl.testtool.TestTool;
import gov.healthit.chpl.testtool.TestToolComparator;
import gov.healthit.chpl.testtool.TestToolDAO;
import gov.healthit.chpl.util.CertificationResultRules;

@Component
public class CertificationResultService {
    private CertificationResultRules certRules;
    private CertificationResultManager certResultManager;
    private CertificationResultDetailsDAO certificationResultDetailsDAO;
    private SvapDAO svapDao;
    private OptionalStandardDAO optionalStandardDAO;
    private ConformanceMethodDAO conformanceMethodDAO;
    private TestToolDAO testToolDAO;

    private CertificationResultComparator certResultComparator;
    private TestToolComparator testToolComparator;

    @Autowired
    public CertificationResultService(CertificationResultRules certRules, CertificationResultManager certResultManager,
            CertificationResultDetailsDAO certificationResultDetailsDAO,
            SvapDAO svapDAO, OptionalStandardDAO optionalStandardDAO, TestToolDAO testToolDAO,
            ConformanceMethodDAO conformanceMethodDAO,
            CertificationResultComparator certResultComparator) {
        this.certRules = certRules;
        this.certResultManager = certResultManager;
        this.certificationResultDetailsDAO = certificationResultDetailsDAO;
        this.svapDao = svapDAO;
        this.optionalStandardDAO = optionalStandardDAO;
        this.conformanceMethodDAO = conformanceMethodDAO;
        this.testToolDAO = testToolDAO;
        this.certResultComparator = certResultComparator;

        this.testToolComparator = new TestToolComparator();
    }

    public List<CertificationResult> getCertificationResults(CertifiedProductSearchDetails searchDetails) throws EntityRetrievalException {
        List<SvapCriteriaMap> svapCriteriaMap = svapDao.getAllSvapCriteriaMap();
        List<OptionalStandardCriteriaMap> optionalStandardCriteriaMap = optionalStandardDAO.getAllOptionalStandardCriteriaMap();
        List<TestToolCriteriaMap> testToolCriteriaMap = testToolDAO.getAllTestToolCriteriaMaps();
        List<ConformanceMethodCriteriaMap> conformanceMethodCriteriaMap = conformanceMethodDAO.getAllConformanceMethodCriteriaMap();

        return getCertificationResultDetailsDTOs(searchDetails.getId()).stream()
                .map(dto -> getCertificationResult(dto, searchDetails, svapCriteriaMap, optionalStandardCriteriaMap, testToolCriteriaMap, conformanceMethodCriteriaMap))
                .sorted(certResultComparator)
                .collect(Collectors.toList());
    }

    private List<CertificationResultDetailsDTO> getCertificationResultDetailsDTOs(Long id) {
        List<CertificationResultDetailsDTO> certificationResultDetailsDTOs = null;
        certificationResultDetailsDTOs = certificationResultDetailsDAO.getAllCertResultsForListing(id);
        return certificationResultDetailsDTOs;
    }


    public List<TestTool> getAvailableTestToolForCriteria(CertificationResult result) throws EntityRetrievalException {
        return getAvailableTestToolForCriteria(result, testToolDAO.getAllTestToolCriteriaMaps());
    }

    private CertificationResult getCertificationResult(CertificationResultDetailsDTO certResult,
            CertifiedProductSearchDetails searchDetails, List<SvapCriteriaMap> svapCriteriaMap,
            List<OptionalStandardCriteriaMap> optionalStandardCriteriaMap, List<TestToolCriteriaMap> testToolCriteriaMap,
            List<ConformanceMethodCriteriaMap> conformanceMethodCriteriaMap) {

        CertificationResult result = new CertificationResult(certResult, certRules);

        CertificationCriterion criteria = result.getCriterion();
        populateSed(certResult, searchDetails, result, criteria);
        populateTestTasks(certResult, searchDetails, criteria);

        return result;
    }

    private void populateTestTasks(CertificationResultDetailsDTO certResult, CertifiedProductSearchDetails searchDetails, CertificationCriterion criteria) {
        if (certRules.hasCertOption(certResult.getCertificationCriterionId(), CertificationResultRules.SED)) {
            List<CertificationResultTestTaskDTO> testTask = certResultManager.getTestTasksForCertificationResult(certResult.getId());
            for (CertificationResultTestTaskDTO currResult : testTask) {
                boolean alreadyExists = false;
                TestTask newTestTask = currResult.getTestTask();
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
        if (certRules.hasCertOption(certResult.getCertificationCriterionId(), CertificationResultRules.SED)) {
            List<CertificationResultUcdProcessDTO> ucdProcesses = certResultManager.getUcdProcessesForCertificationResult(result.getId());
            for (CertificationResultUcdProcessDTO currResult : ucdProcesses) {
                boolean alreadyExists = false;
                CertifiedProductUcdProcess newUcd = new CertifiedProductUcdProcess(currResult);
                for (CertifiedProductUcdProcess currUcd : searchDetails.getSed().getUcdProcesses()) {
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

    private List<TestTool> getAvailableTestToolForCriteria(CertificationResult result, List<TestToolCriteriaMap> testToolCriteriaMap) {
        return testToolCriteriaMap.stream()
                .filter(ttcm -> ttcm.getCriterion().getId().equals(result.getCriterion().getId()))
                .map(ttm -> ttm.getTestTool())
                .sorted(testToolComparator)
                .collect(Collectors.toList());
    }
}
