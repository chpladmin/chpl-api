package gov.healthit.chpl.upload.listing.normalizer;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.ff4j.FF4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.FeatureList;
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
    private FF4j ff4j;
    private UcdProcess customUcdProcess;

    @Autowired
    public UcdProcessNormalizer(UcdProcessDAO ucdDao,
            FuzzyChoicesManager fuzzyChoicesManager,
            ErrorMessageUtil msgUtil,
            FF4j ff4j) {
        this.ucdDao = ucdDao;
        this.fuzzyChoicesManager = fuzzyChoicesManager;
        this.msgUtil = msgUtil;
        this.ff4j = ff4j;
        this.customUcdProcess = ucdDao.getById(CertifiedProductUcdProcess.CUSTOM_UCD_PROCESS_ID);
    }

    public void normalize(CertifiedProductSearchDetails listing) {
        if (listing.getSed() != null && !CollectionUtils.isEmpty(listing.getSed().getUcdProcesses())) {
            clearDataForUnattestedCriteria(listing);
            listing.getSed().getUcdProcesses().stream()
                .forEach(ucdProcess -> {
                    setEmptyStringFieldsToNull(ucdProcess);
                    populateUcdProcessId(ucdProcess);
                });
            findFuzzyMatchesForUnknownUcdProcesses(listing);

            List<CertifiedProductUcdProcess> ucdProcessesToRemove = getHopelessUcdProcesses(listing.getSed().getUcdProcesses());
            if (!CollectionUtils.isEmpty(ucdProcessesToRemove)) {
                listing.getSed().getUcdProcesses().removeAll(ucdProcessesToRemove);
            }

            groupUcdProcessList(listing, listing.getSed().getUcdProcesses());
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

    private void setEmptyStringFieldsToNull(CertifiedProductUcdProcess crup) {
        if (StringUtils.isEmpty(crup.getDetails())) {
            crup.setDetails(null);
        }
    }

    private void populateUcdProcessId(CertifiedProductUcdProcess ucdProcess) {
        if (!StringUtils.isEmpty(ucdProcess.getName())) {
            UcdProcess foundUcdProcess = ucdDao.getByName(ucdProcess.getName());
            if (foundUcdProcess != null) {
                ucdProcess.setId(foundUcdProcess.getId());
            }
        } else if (ff4j.check(FeatureList.HTI_5_ERD)) {
            ucdProcess.setId(customUcdProcess.getId());
            ucdProcess.setName(customUcdProcess.getName());
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

    private void groupUcdProcessList(CertifiedProductSearchDetails listing, List<CertifiedProductUcdProcess> allUcdProcesses) {
        List<CertifiedProductUcdProcess> groupedUcdProcesses = new ArrayList<CertifiedProductUcdProcess>();

        listing.getCertificationResults().stream()
            .forEach(certResult -> updateUcdProcessList(groupedUcdProcesses,
                    allUcdProcesses.stream().filter(ucd -> criterionUsesUcdProcess(certResult.getCriterion(), ucd)).collect(Collectors.toList()),
                    certResult.getCriterion()));
        listing.getSed().setUcdProcesses(groupedUcdProcesses);
    }

    private boolean criterionUsesUcdProcess(CertificationCriterion criterion, CertifiedProductUcdProcess ucdProcess) {
        return ucdProcess.getCriteria().stream()
                .map(crit -> crit.getId())
                .filter(ucdCriterionId -> ucdCriterionId.equals(criterion.getId()))
                .findAny()
                .isPresent();
    }

    private void updateUcdProcessList(List<CertifiedProductUcdProcess> allUcdProcessesOnListing,
            List<CertifiedProductUcdProcess> certResultUcdProcesses,
            CertificationCriterion criterion) {
        certResultUcdProcesses.stream().forEach(certResultUcdProcess -> {
            if (listingContainsUcdProcess(allUcdProcessesOnListing, certResultUcdProcess)) {
                addCriteriaToExistingUcdProcess(allUcdProcessesOnListing, certResultUcdProcess, criterion);
            } else {
                if (certResultUcdProcess.getCriteria() != null) {
                    certResultUcdProcess.getCriteria().add(criterion);
                } else {
                    LinkedHashSet<CertificationCriterion> criteriaSet = new LinkedHashSet<CertificationCriterion>();
                    criteriaSet.add(criterion);
                    certResultUcdProcess.setCriteria(criteriaSet);
                }
                allUcdProcessesOnListing.add(certResultUcdProcess);
            }
        });
    }

    private boolean listingContainsUcdProcess(List<CertifiedProductUcdProcess> listingUcdProcesses, CertifiedProductUcdProcess certResultUcdProcess) {
        return listingUcdProcesses.stream()
            .filter(listingUcdProcess -> listingUcdProcess.matches(certResultUcdProcess))
            .findAny().isPresent();
    }

    private void addCriteriaToExistingUcdProcess(List<CertifiedProductUcdProcess> listingUcdProcesses, CertifiedProductUcdProcess certResultUcdProcess,
            CertificationCriterion criterion) {
        listingUcdProcesses.stream()
            .filter(listingUcdProcess -> listingUcdProcess.matches(certResultUcdProcess))
            .forEach(listingUcdProcess -> {
                listingUcdProcess.getCriteria().add(criterion);
            });
    }

}
