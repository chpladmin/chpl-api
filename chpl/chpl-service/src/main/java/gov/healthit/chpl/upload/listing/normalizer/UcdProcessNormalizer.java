package gov.healthit.chpl.upload.listing.normalizer;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.CertifiedProductUcdProcess;
import gov.healthit.chpl.fuzzyMatching.FuzzyChoicesManager;
import gov.healthit.chpl.fuzzyMatching.FuzzyType;
import gov.healthit.chpl.ucdProcess.UcdProcess;
import gov.healthit.chpl.ucdProcess.UcdProcessDAO;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.Util;

@Component
public class UcdProcessNormalizer {
    private UcdProcessDAO ucdDao;
    private FuzzyChoicesManager fuzzyChoicesManager;
    private ErrorMessageUtil msgUtil;

    @Autowired
    public UcdProcessNormalizer(UcdProcessDAO ucdDao,
            FuzzyChoicesManager fuzzyChoicesManager,
            ErrorMessageUtil msgUtil) {
        this.ucdDao = ucdDao;
        this.fuzzyChoicesManager = fuzzyChoicesManager;
        this.msgUtil = msgUtil;
    }

    public void normalize(CertifiedProductSearchDetails listing) {
        if (listing.getSed() != null && !CollectionUtils.isEmpty(listing.getSed().getUcdProcesses())) {
            clearDataForUnattestedCriteria(listing);
            listing.getSed().getUcdProcesses().stream()
                .forEach(ucdProcess -> populateUcdProcessId(ucdProcess));
            findFuzzyMatchesForUnknownUcdProcesses(listing);

            List<CertifiedProductUcdProcess> ucdProcessesToRemove = getHopelessUcdProcesses(listing.getSed().getUcdProcesses());
            if (!CollectionUtils.isEmpty(ucdProcessesToRemove)) {
                listing.getSed().getUcdProcesses().removeAll(ucdProcessesToRemove);
            }
        }
    }

    private void clearDataForUnattestedCriteria(CertifiedProductSearchDetails listing) {
        List<Long> attestedCriteriaIds = listing.getCertificationResults().stream()
                .map(attestedCertResult -> attestedCertResult.getCriterion().getId())
                .toList();

        listing.getSed().getUcdProcesses().stream()
            .forEach(ucdProcess -> clearUnattestedCriteriaInUcdProcess(listing, attestedCriteriaIds, ucdProcess));
    }

    private void clearUnattestedCriteriaInUcdProcess(CertifiedProductSearchDetails listing, List<Long> attestedCriteriaIds,
            CertifiedProductUcdProcess ucdProcess) {
        List<CertificationCriterion> unattestedCriteriaInUcdProcess = getUnattestedCriteriaForUcdProcess(attestedCriteriaIds, ucdProcess);
        unattestedCriteriaInUcdProcess.stream()
            .forEach(criterionToRemove -> listing.addWarningMessage(
                msgUtil.getMessage("listing.ucdProcess.unattestedCriterionRemoved",
                        Util.formatCriteriaNumber(criterionToRemove))));
        ucdProcess.getCriteria().removeAll(unattestedCriteriaInUcdProcess);
    }

    private List<CertificationCriterion> getUnattestedCriteriaForUcdProcess(List<Long> attestedCriteriaIds, CertifiedProductUcdProcess ucdProcess) {
        List<CertificationCriterion> ucdProcessCriteria = ucdProcess.getCriteria().stream().toList();
        return ucdProcessCriteria.stream()
                .filter(ucdCriterion -> !attestedCriteriaIds.contains(ucdCriterion.getId()))
                .collect(Collectors.toList());
    }

    private List<CertifiedProductUcdProcess> getHopelessUcdProcesses(List<CertifiedProductUcdProcess> ucdProcesses) {
        return ucdProcesses.stream()
                .filter(cpUcd -> cpUcd.getId() == null && StringUtils.isBlank(cpUcd.getName())
                    && StringUtils.isBlank(cpUcd.getDetails())
                    && StringUtils.isBlank(cpUcd.getUserEnteredName()))
                .toList();
    }

    private void populateUcdProcessId(CertifiedProductUcdProcess ucdProcess) {
        if (!StringUtils.isEmpty(ucdProcess.getName())) {
            UcdProcess foundUcdProcess = ucdDao.getByName(ucdProcess.getName());
            if (foundUcdProcess != null) {
                ucdProcess.setId(foundUcdProcess.getId());
            }
        }
    }

    private void findFuzzyMatchesForUnknownUcdProcesses(CertifiedProductSearchDetails listing) {
        listing.getSed().getUcdProcesses().stream()
            .filter(ucdProcess -> ucdProcess.getId() == null)
            .forEach(ucdProcess -> lookForFuzzyMatch(listing, ucdProcess));
    }

    private void lookForFuzzyMatch(CertifiedProductSearchDetails listing, CertifiedProductUcdProcess ucdProcess) {
        if (StringUtils.isEmpty(ucdProcess.getName())) {
            return;
        }

        String topFuzzyChoice = fuzzyChoicesManager.getTopFuzzyChoice(ucdProcess.getName(), FuzzyType.UCD_PROCESS);
        if (!StringUtils.isEmpty(topFuzzyChoice)) {
            ucdProcess.setUserEnteredName(ucdProcess.getName());
            ucdProcess.setName(topFuzzyChoice);
            populateUcdProcessId(ucdProcess);
        }
    }
}
