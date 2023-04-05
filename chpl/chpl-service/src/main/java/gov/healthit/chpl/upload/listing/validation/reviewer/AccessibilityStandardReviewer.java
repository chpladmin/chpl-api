package gov.healthit.chpl.upload.listing.validation.reviewer;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertifiedProductAccessibilityStandard;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.fuzzyMatching.FuzzyType;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.validation.listing.reviewer.Reviewer;

@Component
public class AccessibilityStandardReviewer implements Reviewer {
    private ErrorMessageUtil msgUtil;

    @Autowired
    public AccessibilityStandardReviewer(ErrorMessageUtil msgUtil) {
        this.msgUtil = msgUtil;
    }

    @Override
    public void review(CertifiedProductSearchDetails listing) {
        removeAccessibilityStandardsNotFound(listing);
        isAccessibilityCertifiedValidBoolean(listing);
        doesAccessibilityCertifiedBooleanMatchPresenceOfStandards(listing);
        doAccessibilityStandardsExist(listing);
        areAccessibilityStandardsValid(listing);
        addFuzzyMatchWarnings(listing);
    }

    private void removeAccessibilityStandardsNotFound(CertifiedProductSearchDetails listing) {
        List<CertifiedProductAccessibilityStandard> accessibilityStandards = listing.getAccessibilityStandards();
        if (!CollectionUtils.isEmpty(accessibilityStandards)) {
            List<CertifiedProductAccessibilityStandard> accessibilityStandardsWithoutIds = accessibilityStandards.stream()
                    .filter(currAccStd -> currAccStd.getAccessibilityStandardId() == null)
                    .collect(Collectors.toList());

            if (!CollectionUtils.isEmpty(accessibilityStandardsWithoutIds)) {
                accessibilityStandards.removeAll(accessibilityStandardsWithoutIds);

                accessibilityStandardsWithoutIds.stream()
                        .forEach(accStdWithoutId -> listing.getWarningMessages().add(
                                msgUtil.getMessage("listing.accessibilityStandardNotFoundAndRemoved",
                                        accStdWithoutId.getAccessibilityStandardName() == null ? "" : accStdWithoutId.getAccessibilityStandardName())));
            }
        }
    }

    private void isAccessibilityCertifiedValidBoolean(CertifiedProductSearchDetails listing) {
        if (listing.getAccessibilityCertified() == null && !StringUtils.isEmpty(listing.getAccessibilityCertifiedStr())) {
            listing.addDataErrorMessage(msgUtil.getMessage("listing.accessibilityCertified.invalidBoolean", listing.getAccessibilityCertifiedStr()));
        }
    }

    private void doesAccessibilityCertifiedBooleanMatchPresenceOfStandards(CertifiedProductSearchDetails listing) {
        if (listing.getAccessibilityCertified() != null && listing.getAccessibilityCertified()
                && (listing.getAccessibilityStandards() == null || listing.getAccessibilityStandards().size() == 0)) {
            listing.addDataErrorMessage(msgUtil.getMessage("listing.accessibilityCertified.standardsMismatch", "true", "0"));
        } else if (listing.getAccessibilityCertified() != null && !listing.getAccessibilityCertified()
                && listing.getAccessibilityStandards() != null && listing.getAccessibilityStandards().size() > 0) {
            listing.addDataErrorMessage(msgUtil.getMessage("listing.accessibilityCertified.standardsMismatch", "false", listing.getAccessibilityStandards().size()));
        }
    }

    private void doAccessibilityStandardsExist(CertifiedProductSearchDetails listing) {
        if (listing.getAccessibilityStandards() == null || listing.getAccessibilityStandards().size() == 0) {
            listing.addDataErrorMessage(msgUtil.getMessage("listing.accessibilityStandardsNotFound"));
        }
    }

    private void areAccessibilityStandardsValid(CertifiedProductSearchDetails listing) {
        if (listing.getAccessibilityStandards() != null) {
            listing.getAccessibilityStandards().stream()
                    .forEach(accessibilityStandard -> checkAccessibilityStandardNameRequired(listing, accessibilityStandard));
        }
    }

    private void checkAccessibilityStandardNameRequired(CertifiedProductSearchDetails listing, CertifiedProductAccessibilityStandard accessibilityStandard) {
        if (StringUtils.isEmpty(accessibilityStandard.getAccessibilityStandardName())) {
            listing.addDataErrorMessage(msgUtil.getMessage("listing.accessibilityStandardMissingName"));
        }
    }

    private void addFuzzyMatchWarnings(CertifiedProductSearchDetails listing) {
        if (listing.getAccessibilityStandards() != null) {
            listing.getAccessibilityStandards().stream()
                    .filter(accStd -> hasFuzzyMatch(accStd))
                    .forEach(accStd -> addFuzzyMatchWarning(listing, accStd));
        }
    }

    private boolean hasFuzzyMatch(CertifiedProductAccessibilityStandard accStd) {
        return !StringUtils.isEmpty(accStd.getUserEnteredAccessibilityStandardName())
                && !StringUtils.equals(accStd.getAccessibilityStandardName(), accStd.getUserEnteredAccessibilityStandardName());
    }

    private void addFuzzyMatchWarning(CertifiedProductSearchDetails listing, CertifiedProductAccessibilityStandard accStd) {
        String warningMsg = msgUtil.getMessage("listing.fuzzyMatch", FuzzyType.ACCESSIBILITY_STANDARD.fuzzyType(),
                accStd.getUserEnteredAccessibilityStandardName(), accStd.getAccessibilityStandardName());
        listing.getWarningMessages().add(warningMsg);
    }
}
