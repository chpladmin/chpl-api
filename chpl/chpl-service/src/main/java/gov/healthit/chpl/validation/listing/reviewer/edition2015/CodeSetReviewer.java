package gov.healthit.chpl.validation.listing.reviewer.edition2015;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;

import gov.healthit.chpl.codeset.CertificationResultCodeSet;
import gov.healthit.chpl.codeset.CodeSet;
import gov.healthit.chpl.codeset.CodeSetDAO;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.util.CertificationResultRules;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.Util;
import gov.healthit.chpl.util.ValidationUtils;
import gov.healthit.chpl.validation.listing.reviewer.Reviewer;

public abstract class CodeSetReviewer implements Reviewer {

    private CertificationResultRules certResultRules;
    private CodeSetDAO codeSetDao;
    private ValidationUtils validationUtils;
    private ErrorMessageUtil msgUtil;

    public CodeSetReviewer(CertificationResultRules certResultRules,
            CodeSetDAO codeSetDao,
            ValidationUtils validationUtils,
            ErrorMessageUtil msgUtil) {
        this.certResultRules = certResultRules;
        this.codeSetDao = codeSetDao;
        this.validationUtils = validationUtils;
        this.msgUtil = msgUtil;
    }

    public abstract LocalDate getCodeSetCheckDate(CertifiedProductSearchDetails listing);

    @Override
    public void review(CertifiedProductSearchDetails listing) {
        List<CertificationResultCodeSet> invalidCodeSetsToRemove = new ArrayList<CertificationResultCodeSet>();
        listing.getCertificationResults().stream()
                .filter(certResult -> validationUtils.isEligibleForErrors(certResult))
                .forEach(certResult -> reviewCertificationResult(listing, certResult, invalidCodeSetsToRemove));
        listing.getCertificationResults().stream()
                .forEach(certResult -> {
                    removeInvalidCodeSets(certResult, invalidCodeSetsToRemove);
                    removeCodeSetsIfNotApplicable(certResult);
                });
    }

    private void reviewCertificationResult(CertifiedProductSearchDetails listing,
            CertificationResult certResult,
            List<CertificationResultCodeSet> invalidCodeSetsToRemove) {
        reviewCriteriaCanHaveCodeSets(listing, certResult);
        reviewRequiredCodeSetsPresent(listing, certResult);
        if (!CollectionUtils.isEmpty(certResult.getCodeSets())) {
            certResult.getCodeSets().stream()
                    .forEach(codeSet -> {
                        reviewCodeSetAllowedForCriterion(listing, certResult, codeSet, invalidCodeSetsToRemove);
                        reviewCodeSetFields(listing, certResult, codeSet, invalidCodeSetsToRemove);
                    });
        }
    }

    private void reviewCriteriaCanHaveCodeSets(CertifiedProductSearchDetails listing, CertificationResult certResult) {
        if (!certResultRules.hasCertOption(certResult.getCriterion().getId(), CertificationResultRules.CODE_SET)) {
            if (!CollectionUtils.isEmpty(certResult.getCodeSets())) {
                listing.addWarningMessage(msgUtil.getMessage(
                        "listing.criteria.codeSetNotApplicable", Util.formatCriteriaNumber(certResult.getCriterion())));
                certResult.setCodeSets(null);
            }
        }
    }

    private void removeInvalidCodeSets(CertificationResult certResult, List<CertificationResultCodeSet> invalidCodeSetsToRemove) {
        if (!CollectionUtils.isEmpty(invalidCodeSetsToRemove)) {
            Iterator<CertificationResultCodeSet> codeSetIter = certResult.getCodeSets().iterator();
            while (codeSetIter.hasNext()) {
                CertificationResultCodeSet crCodeSet = codeSetIter.next();
                if (invalidCodeSetsToRemove.contains(crCodeSet)) {
                    codeSetIter.remove();
                }
            }
        }
    }

    private void removeCodeSetsIfNotApplicable(CertificationResult certResult) {
        if (!certResultRules.hasCertOption(certResult.getCriterion().getId(), CertificationResultRules.CODE_SET)) {
            certResult.setCodeSets(null);
        }
    }

    private void reviewRequiredCodeSetsPresent(CertifiedProductSearchDetails listing, CertificationResult certResult) {
        Map<Long, List<CodeSet>> mapOfCodeSets = codeSetDao.getCodeSetCriteriaMaps();
        List<CodeSet> codeSetsForCriterion = mapOfCodeSets.get(certResult.getCriterion().getId());
        if (!CollectionUtils.isEmpty(codeSetsForCriterion)) {
            List<CodeSet> requiredCodeSetsForCriterion = codeSetsForCriterion.stream()
                    .filter(codeSet -> codeSet.getRequiredDay().isEqual(LocalDate.now())
                            || codeSet.getRequiredDay().isBefore(LocalDate.now()))
                    .collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(requiredCodeSetsForCriterion)) {
                requiredCodeSetsForCriterion.stream()
                    .filter(reqCodeSet -> !doesCertResultContainCodeSet(certResult, reqCodeSet))
                    .forEach(missingReqCodeSet -> listing.addBusinessErrorMessage(msgUtil.getMessage("listing.criteria.codeSetRequired",
                                    missingReqCodeSet.getName(),
                                    Util.formatCriteriaNumber(certResult.getCriterion()),
                                    missingReqCodeSet.getRequiredDay().toString())));
            }
        }
    }

    private boolean doesCertResultContainCodeSet(CertificationResult certResult, CodeSet codeSet) {
        return certResult.getCodeSets().stream()
                .filter(crCodeSet -> crCodeSet.getCodeSet().getId().equals(codeSet.getId()))
                .findAny().isPresent();
    }

    private void reviewCodeSetFields(CertifiedProductSearchDetails listing,
            CertificationResult certResult,
            CertificationResultCodeSet crCodeSet,
            List<CertificationResultCodeSet> invalidCodeSetsToRemove) {
        if (crCodeSet.getCodeSet().getId() == null) {
            listing.addWarningMessage(msgUtil.getMessage(
                    "codeSet.doesNotExist", crCodeSet.getCodeSet().getName(),
                    Util.formatCriteriaNumber(certResult.getCriterion())));
            invalidCodeSetsToRemove.add(crCodeSet);
        } else {
            List<CodeSet> codeSets = codeSetDao.findAll();
            CodeSet codeSetFromDb = getCodeSetFromDB(crCodeSet.getCodeSet().getId(), codeSets);
            if (codeSetFromDb == null) {
                listing.addWarningMessage(msgUtil.getMessage(
                        "codeSet.doesNotExist", crCodeSet.getCodeSet().getName(),
                        Util.formatCriteriaNumber(certResult.getCriterion())));
                invalidCodeSetsToRemove.add(crCodeSet);
            }
        }
    }

    private void reviewCodeSetAllowedForCriterion(CertifiedProductSearchDetails listing,
            CertificationResult certResult,
            CertificationResultCodeSet crCodeSet,
            List<CertificationResultCodeSet> invalidCodeSetsToRemove) {
        Map<Long, List<CodeSet>> mapOfCodeSets = codeSetDao.getCodeSetCriteriaMaps();
        List<CodeSet> codeSets = codeSetDao.findAll();

        CodeSet codeSetFromDb = getCodeSetFromDB(crCodeSet.getCodeSet().getId(), codeSets);
        if (codeSetFromDb != null) {
            if (!isCodeSetAvailableBasedOnStartDayCodeSet(codeSetFromDb, getCodeSetCheckDate(listing))) {
                listing.addWarningMessage(msgUtil.getMessage("codeSet.notAvailableBasedOnStartDay",
                        codeSetFromDb.getName(),
                        Util.formatCriteriaNumber(certResult.getCriterion()),
                        codeSetFromDb.getStartDay().toString()));
                invalidCodeSetsToRemove.add(crCodeSet);
            }
            if (!isCodeSetValidForCriteria(crCodeSet.getCodeSet(), mapOfCodeSets.get(certResult.getCriterion().getId()))) {
                listing.addWarningMessage(msgUtil.getMessage("codeSet.notAllowedForCriteria",
                        crCodeSet.getCodeSet().getName(),
                        Util.formatCriteriaNumber(certResult.getCriterion())));
                invalidCodeSetsToRemove.add(crCodeSet);
            }
        }
    }

    private boolean isCodeSetValidForCriteria(CodeSet codeSetToCheck, List<CodeSet> codeSetsAllowedForCriteria) {
        if (CollectionUtils.isEmpty(codeSetsAllowedForCriteria)) {
            return false;
        }

        return codeSetsAllowedForCriteria.stream()
                .filter(csd -> csd.getId().equals(codeSetToCheck.getId()))
                .findAny()
                .isPresent();
    }

    private boolean isCodeSetAvailableBasedOnStartDayCodeSet(CodeSet codeSet, LocalDate checkDate) {
        return codeSet.getStartDay().equals(checkDate) || codeSet.getStartDay().isBefore(checkDate);
    }

    private CodeSet getCodeSetFromDB(Long codeSetId, List<CodeSet> codeSetsFromDB) {
        if (CollectionUtils.isEmpty(codeSetsFromDB)) {
            return null;
        }

        return codeSetsFromDB.stream()
                .filter(cs -> cs.getId().equals(codeSetId))
                .findAny()
                .orElse(null);
    }
}
