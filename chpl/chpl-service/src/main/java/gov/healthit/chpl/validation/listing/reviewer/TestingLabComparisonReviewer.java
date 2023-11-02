package gov.healthit.chpl.validation.listing.reviewer;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.CertifiedProductTestingLab;
import gov.healthit.chpl.util.ErrorMessageUtil;

@Component
public class TestingLabComparisonReviewer implements ComparisonReviewer {

    private ErrorMessageUtil errorMessageUtil;

    public TestingLabComparisonReviewer(ErrorMessageUtil errorMessageUtil) {
        this.errorMessageUtil = errorMessageUtil;
    }

    @Override
    public void review(CertifiedProductSearchDetails existingListing, CertifiedProductSearchDetails updatedListing) {
        //An exiting CertifiedProductTestingLab cannot have the assoc TestingLab changed
        updatedListing.getTestingLabs()
                .forEach(cptl -> {
                    getMatchingCertifiedProductTestingLab(cptl, existingListing.getTestingLabs())
                        .ifPresent(found -> {
                            if (hasTestingLabBeenUpdatedInExistingCertifiedProductTestingLab(found, cptl)) {
                                updatedListing.addBusinessErrorMessage(
                                        errorMessageUtil.getMessage("atl.updateNotAllowed"));
                            }
                    });
                });
    }

    private Boolean hasTestingLabBeenUpdatedInExistingCertifiedProductTestingLab(CertifiedProductTestingLab existing, CertifiedProductTestingLab updating) {
        return !existing.getTestingLab().getId().equals(updating.getTestingLab().getId());
    }

    private Optional<CertifiedProductTestingLab> getMatchingCertifiedProductTestingLab(CertifiedProductTestingLab find, List<CertifiedProductTestingLab> existing) {
        if (find == null) {
            return Optional.empty();
        } else {
            return existing.stream()
                    .filter(cptl -> cptl.getId().equals(find.getId()))
                    .findAny();
        }
    }
}
