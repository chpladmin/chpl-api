package gov.healthit.chpl.upload.listing.normalizer;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.AccessibilityStandardDAO;
import gov.healthit.chpl.domain.CertifiedProductAccessibilityStandard;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.dto.AccessibilityStandardDTO;

@Component
public class AccessibilityStandardNormalizer {
    private AccessibilityStandardDAO accessibilityStandardDao;

    @Autowired
    public AccessibilityStandardNormalizer(AccessibilityStandardDAO accessibilityStandardDao) {
        this.accessibilityStandardDao = accessibilityStandardDao;
    }

    public void normalize(CertifiedProductSearchDetails listing) {
        if (listing.getAccessibilityStandards() != null && listing.getAccessibilityStandards().size() > 0) {
            listing.getAccessibilityStandards().stream()
                .forEach(accessibilityStandard -> populateAccessibilityStandardId(accessibilityStandard));
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
}
