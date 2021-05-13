package gov.healthit.chpl.upload.listing.normalizer;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.AccessibilityStandardDAO;
import gov.healthit.chpl.domain.CertifiedProductAccessibilityStandard;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.dto.AccessibilityStandardDTO;
import gov.healthit.chpl.entity.FuzzyType;
import gov.healthit.chpl.manager.FuzzyChoicesManager;

@Component
public class AccessibilityStandardNormalizer {
    private AccessibilityStandardDAO accessibilityStandardDao;
    private FuzzyChoicesManager fuzzyChoicesManager;

    @Autowired
    public AccessibilityStandardNormalizer(AccessibilityStandardDAO accessibilityStandardDao,
            FuzzyChoicesManager fuzzyChoicesManager) {
        this.accessibilityStandardDao = accessibilityStandardDao;
        this.fuzzyChoicesManager = fuzzyChoicesManager;
    }

    public void normalize(CertifiedProductSearchDetails listing) {
        if (listing.getAccessibilityStandards() != null && listing.getAccessibilityStandards().size() > 0) {
            listing.getAccessibilityStandards().stream()
                .forEach(accessibilityStandard -> populateAccessibilityStandardId(accessibilityStandard));
            findFuzzyMatchesForUnknownStandards(listing);
        }
    }

    private void populateAccessibilityStandardId(CertifiedProductAccessibilityStandard accessibilityStandard) {
        if (!StringUtils.isEmpty(accessibilityStandard.getAccessibilityStandardName())) {
            AccessibilityStandardDTO accStdDto =
                    accessibilityStandardDao.getByName(accessibilityStandard.getAccessibilityStandardName());
            if (accStdDto != null) {
                accessibilityStandard.setAccessibilityStandardId(accStdDto.getId());
            }
        }
    }

    private void findFuzzyMatchesForUnknownStandards(CertifiedProductSearchDetails listing) {
        listing.getAccessibilityStandards().stream()
            .filter(accessibilityStandard -> accessibilityStandard.getId() == null)
            .forEach(accessibilityStandard -> lookForFuzzyMatch(listing, accessibilityStandard));
    }

    private void lookForFuzzyMatch(CertifiedProductSearchDetails listing, CertifiedProductAccessibilityStandard accessibilityStandard) {
        String topFuzzyChoice = fuzzyChoicesManager.getTopFuzzyChoice(accessibilityStandard.getAccessibilityStandardName(), FuzzyType.ACCESSIBILITY_STANDARD);
        if (!StringUtils.isEmpty(topFuzzyChoice)) {
            accessibilityStandard.setUserEnteredAccessibilityStandardName(accessibilityStandard.getAccessibilityStandardName());
            accessibilityStandard.setAccessibilityStandardName(topFuzzyChoice);
        }
    }
}
