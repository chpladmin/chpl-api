package gov.healthit.chpl.certifiedproduct.service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.dao.CertificationResultDetailsDAO;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.CertifiedProductUcdProcess;
import gov.healthit.chpl.domain.comparator.CertificationResultComparator;
import gov.healthit.chpl.dto.CertificationResultDetailsDTO;
import gov.healthit.chpl.dto.CertificationResultUcdProcessDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.manager.CertificationResultManager;
import gov.healthit.chpl.sed.CertificationResultTestTask;
import gov.healthit.chpl.sed.TestTask;
import gov.healthit.chpl.util.CertificationResultRules;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class CertificationResultService {
    private CertificationResultRules certRules;
    private CertificationResultManager certResultManager;
    private CertificationResultDetailsDAO certificationResultDetailsDao;
    private CertificationResultUpToDateService certResultUpToDateService;
    private CertificationResultComparator certResultComparator;

    @Autowired
    public CertificationResultService(CertificationResultRules certRules,
            CertificationResultManager certResultManager,
            CertificationResultDetailsDAO certificationResultDetailsDao,
            CertificationResultUpToDateService certResultUpToDateService,
            CertificationResultComparator certResultComparator) {
        this.certRules = certRules;
        this.certResultManager = certResultManager;
        this.certificationResultDetailsDao = certificationResultDetailsDao;
        this.certResultUpToDateService = certResultUpToDateService;
        this.certResultComparator = certResultComparator;
    }

    public List<CertificationResult> getCertificationResults(CertifiedProductSearchDetails searchDetails) throws EntityRetrievalException {
        return getCertificationResultDetailsDTOs(searchDetails.getId()).stream()
                .map(dto -> getCertificationResult(dto, searchDetails))
                .sorted(certResultComparator)
                .collect(Collectors.toList());
    }

    private List<CertificationResultDetailsDTO> getCertificationResultDetailsDTOs(Long id) {
        List<CertificationResultDetailsDTO> certificationResultDetailsDTOs = null;
        certificationResultDetailsDTOs = certificationResultDetailsDao.getAllCertResultsForListing(id);
        return certificationResultDetailsDTOs;
    }

    private CertificationResult getCertificationResult(CertificationResultDetailsDTO certResult,
            CertifiedProductSearchDetails listing) {
        CertificationResult result = new CertificationResult(certResult, certRules);
        CertificationCriterion criteria = result.getCriterion();
        populateSed(certResult, listing, result, criteria);
        populateTestTasks(certResult, listing, criteria);
        populateUpToDateToday(result);
        return result;
    }

    private void populateTestTasks(CertificationResultDetailsDTO certResult, CertifiedProductSearchDetails searchDetails, CertificationCriterion criteria) {
        if (certRules.hasCertOption(certResult.getCertificationCriterionId(), CertificationResultRules.SED)) {
            List<CertificationResultTestTask> testTask = certResultManager.getTestTasksForCertificationResult(certResult.getId());
            for (CertificationResultTestTask currResult : testTask) {
                boolean alreadyExists = false;
                TestTask newTestTask = currResult.getTestTask();
                for (TestTask currTestTask : searchDetails.getSed().getTestTasks()) {
                    if (newTestTask.matches(currTestTask)) {
                        alreadyExists = true;
                        if (!currTestTask.getCriteria().add(criteria)) {
                            LOGGER.debug("Cannot add criteria " + criteria.getNumber() + " to test task " + currTestTask.getId() + " for listing " + searchDetails.getId() + " because it already exists.");
                        }
                    }
                }
                if (!alreadyExists) {
                    newTestTask.getCriteria().add(criteria);
                    searchDetails.getSed().getTestTasks().add(newTestTask);
                } else {
                    LOGGER.debug("Not adding test task " + newTestTask.getId() + " to the listing " + searchDetails.getId() + " because one with the same data has already been found.");
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

    private void populateUpToDateToday(CertificationResult certResult) {
        certResult.setUpToDate(certResultUpToDateService.isUpToDate(certResult.getId(), LocalDate.now()));
    }
}
