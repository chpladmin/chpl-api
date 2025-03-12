package gov.healthit.chpl.validation.listing.reviewer.edition2015;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.svap.dao.SvapDAO;
import gov.healthit.chpl.svap.domain.CertificationResultSvap;
import gov.healthit.chpl.svap.domain.Svap;
import gov.healthit.chpl.svap.domain.SvapCriteriaMap;
import gov.healthit.chpl.util.CertificationResultRules;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.validation.listing.reviewer.ComparisonReviewer;

@Component("svapComparisonReviewer")
public class SvapComparisonReviewer implements ComparisonReviewer {
    private SvapDAO svapDao;
    private CertificationResultRules certResultRules;
    private ErrorMessageUtil errorMessageUtil;

    @Autowired
    public SvapComparisonReviewer(SvapDAO svapDao, CertificationResultRules certResultRules,
            ErrorMessageUtil errorMessageUtil) {
        this.svapDao = svapDao;
        this.certResultRules = certResultRules;
        this.errorMessageUtil = errorMessageUtil;
    }

    @Override
    public void review(CertifiedProductSearchDetails existingListing, CertifiedProductSearchDetails updatedListing) {
        List<SvapCriteriaMap> allSvapCriteriaMap = null;
        try {
            allSvapCriteriaMap = svapDao.getAllSvapCriteriaMap();
        } catch (EntityRetrievalException e) {
            updatedListing.addDataErrorMessage(errorMessageUtil.getMessage("listing.criteria.svap.couldNotValidate"));
            return;
        }

        final Map<Long, List<SvapCriteriaMap>> svapCriteriaMap = allSvapCriteriaMap.stream()
                .collect(Collectors.groupingBy(scm -> scm.getCriterion().getId()));

       //Add warning for any replaced SVAPs included on newly attested criteria
        List<CertificationResult> addedCertificationResults = getAddedCertificationResults(existingListing, updatedListing);
        addedCertificationResults.stream()
            .filter(cr -> !CollectionUtils.isEmpty(cr.getSvaps()))
            .forEach(cr -> addWarningForAnyReplacedSvap(updatedListing, cr, cr.getSvaps(), svapCriteriaMap));

        //Add warning for any newly added replaced SVAPs on existing attested criteria
        List<CertificationResult> updatedCertificationResults = getUpdatedCertificationResults(existingListing, updatedListing);
        updatedCertificationResults.stream()
            .filter(cr -> certResultRules.hasCertOption(cr.getCriterion().getId(), CertificationResultRules.SVAP))
            .forEach(cr -> {
                List<CertificationResultSvap> addedSvaps = getAddedSvaps(getSvapsForCertResult(existingListing, cr.getCriterion().getId()), cr.getSvaps());
                addWarningForAnyReplacedSvap(updatedListing, cr, addedSvaps, svapCriteriaMap);
            });

    }

    private void addWarningForAnyReplacedSvap(CertifiedProductSearchDetails listing, CertificationResult cr,
            List<CertificationResultSvap> svaps, Map<Long, List<SvapCriteriaMap>> svapCriteriaMap) {
        svaps.stream()
            .filter(svap -> isSvapMarkedAsReplaced(svap, svapCriteriaMap))
            .forEach(replacedSvap ->
                listing.addWarningMessage(errorMessageUtil.getMessage("listing.criteria.svap.replaced",
                            replacedSvap.getRegulatoryTextCitation(), cr.getCriterion().getNumber())));
    }

    private List<CertificationResult> getAddedCertificationResults(CertifiedProductSearchDetails existingListing, CertifiedProductSearchDetails updatedListing) {
        return updatedListing.getCertificationResults().stream()
            .filter(updatedCertResult -> !isCriterionAttested(existingListing, updatedCertResult.getCriterion().getId()))
            .collect(Collectors.toList());
    }

    private List<CertificationResult> getUpdatedCertificationResults(CertifiedProductSearchDetails existingListing, CertifiedProductSearchDetails updatedListing) {
        return updatedListing.getCertificationResults().stream()
            .filter(updatedCertResult -> isCriterionAttested(existingListing, updatedCertResult.getCriterion().getId()))
            .collect(Collectors.toList());
    }

    private boolean isCriterionAttested(CertifiedProductSearchDetails listing, Long criterionId) {
        return listing.getCertificationResults().stream()
                .filter(certResult -> certResult.getCriterion().getId().equals(criterionId))
                .findAny().isPresent();
    }

    private List<CertificationResultSvap> getAddedSvaps(List<CertificationResultSvap> existingSvaps, List<CertificationResultSvap> updatedSvaps) {
        if (CollectionUtils.isEmpty(updatedSvaps)) {
            return new ArrayList<CertificationResultSvap>();
        }

        return updatedSvaps.stream()
            .filter(updatedSvap -> !containsSvap(existingSvaps, updatedSvap.getSvapId()))
            .collect(Collectors.toList());
    }

    private boolean containsSvap(List<CertificationResultSvap> svaps, Long svapId) {
        if (CollectionUtils.isEmpty(svaps)) {
            return false;
        }

        return svaps.stream()
                .filter(svap -> svap.getSvapId().equals(svapId))
                .findAny().isPresent();
    }

    private List<CertificationResultSvap> getSvapsForCertResult(CertifiedProductSearchDetails listing, Long criterionId) {
        CertificationResult certResult = listing.getCertificationResults().stream()
            .filter(cr -> cr.getCriterion().getId().equals(criterionId))
            .findAny()
            .orElse(null);
        if (certResult == null) {
            return List.of();
        } else {
            return certResult.getSvaps();
        }
    }

    private boolean isSvapMarkedAsReplaced(CertificationResultSvap crs, Map<Long, List<SvapCriteriaMap>> svapCriteriaMap) {
        Optional<Svap> svap = getSvap(crs.getSvapId(), svapCriteriaMap);
        return isSvapAdded(crs) && svap.isPresent() && svap.get().isReplaced();
    }

    private boolean isSvapAdded(CertificationResultSvap crSvap) {
        return crSvap.getId() == null;
    }

    private Optional<Svap> getSvap(Long svapId, Map<Long, List<SvapCriteriaMap>> svapCriteriaMap) {
        return svapCriteriaMap.values().stream()
                .flatMap(List::stream)
                .map(scm -> scm.getSvap())
                .filter(svap -> svap.getSvapId().equals(svapId))
                .findAny();
    }
}
