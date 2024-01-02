package gov.healthit.chpl.validation.listing.reviewer;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.standard.Standard;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.Util;

@Component
public class StandardRemovalReviewer implements ComparisonReviewer {

    private ErrorMessageUtil errorMessageUtil;

    @Autowired
    public StandardRemovalReviewer(ErrorMessageUtil errorMessageUtil) {
        this.errorMessageUtil = errorMessageUtil;
    }

    @Override
    public void review(CertifiedProductSearchDetails existingListing, CertifiedProductSearchDetails updatedListing) {
        existingListing.getCertificationResults().forEach(existingCertResult -> {
            Optional<CertificationResult> updatedCertResults = findCertificationResult(updatedListing, existingCertResult.getId());

            if (updatedCertResults.isPresent()) {
                List<Standard> removedStandards = getRemovedStandards(existingCertResult.getStandards().stream().map(crs -> crs.getStandard()).toList(),
                        CollectionUtils.isEmpty(updatedCertResults.get().getStandards())  ? null : updatedCertResults.get().getStandards().stream().map(crs -> crs.getStandard()).toList());

                removedStandards.stream()
                        .filter(std -> !isStandardInteresting(std))
                        .forEach(std -> updatedListing.addBusinessErrorMessage(errorMessageUtil.getMessage("listing.criteria.standardNotRemovable", std.getRegulatoryTextCitation(),
                            Util.formatCriteriaNumber(existingCertResult.getCriterion()))));
            }
        });
    }

    private List<Standard> getRemovedStandards(List<Standard> originalStandards, List<Standard> updatedStandards) {
        List<Standard> origStandardsMutable = new ArrayList<Standard>(originalStandards);
        if (!CollectionUtils.isEmpty(updatedStandards)) {
            //need a mutable version of the list
            origStandardsMutable.removeIf(orig -> updatedStandards.contains(orig));
        }
        return origStandardsMutable;
    }

    private Boolean isStandardInteresting(Standard standard) {
        return (standard.getEndDay() != null)
                || (standard.getRequiredDay().isAfter(LocalDate.now()));
    }

    private Optional<CertificationResult> findCertificationResult(CertifiedProductSearchDetails listing, Long certificationResultId) {
        return listing.getCertificationResults().stream()
                .filter(cr -> cr.getId() != null && cr.getId().equals(certificationResultId))
                .findAny();
    }

}
