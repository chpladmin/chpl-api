package gov.healthit.chpl.upload.listing.normalizer;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
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

@Component
public class UcdProcessNormalizer {
    private UcdProcessDAO ucdDao;
    private FuzzyChoicesManager fuzzyChoicesManager;

    @Autowired
    public UcdProcessNormalizer(UcdProcessDAO ucdDao,
            FuzzyChoicesManager fuzzyChoicesManager) {
        this.ucdDao = ucdDao;
        this.fuzzyChoicesManager = fuzzyChoicesManager;
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
        List<Long> unattestedCriteriaIds = listing.getCertificationResults().stream()
                .filter(certResult -> certResult.isSuccess() == null || BooleanUtils.isFalse(certResult.isSuccess()))
                .map(unattestedCertResult -> unattestedCertResult.getCriterion().getId())
                .toList();

        listing.getSed().getUcdProcesses().stream()
            .forEach(ucdProcess -> removeUnattestedCriteriaFromUcdProcess(unattestedCriteriaIds, ucdProcess));
    }

    private void removeUnattestedCriteriaFromUcdProcess(List<Long> unattestedCriteriaIds, CertifiedProductUcdProcess ucdProcess) {
        List<CertificationCriterion> ucdProcessCriteria = ucdProcess.getCriteria().stream().toList();
        List<CertificationCriterion> ucdCriteriaToRemove = new ArrayList<CertificationCriterion>();
        ucdProcessCriteria.stream()
            .filter(criterion -> unattestedCriteriaIds.contains(criterion.getId()))
            .forEach(unattestedCriterion -> ucdCriteriaToRemove.add(unattestedCriterion));

        ucdProcess.getCriteria().removeAll(ucdCriteriaToRemove);
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
