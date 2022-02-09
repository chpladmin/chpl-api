package gov.healthit.chpl.validation.listing.reviewer;

import java.util.Objects;

import org.ff4j.FF4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.FeatureList;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.util.ErrorMessageUtil;

@Component("deprecatedFieldReviewer")
public class DeprecatedFieldReviewer implements ComparisonReviewer {

    private ErrorMessageUtil msgUtil;
    private FF4j ff4j;

    @Autowired
    public DeprecatedFieldReviewer(ErrorMessageUtil msgUtil, FF4j ff4j) {
        this.msgUtil = msgUtil;
        this.ff4j = ff4j;
    }

    @Override
    public void review(CertifiedProductSearchDetails existingListing,
            CertifiedProductSearchDetails updatedListing) {
        if (!Objects.equals(existingListing.getTransparencyAttestationUrl(), updatedListing.getTransparencyAttestationUrl())) {
            updatedListing.getWarningMessages()
            .add(msgUtil.getMessage("deprecated.field.update", "transparencyAttestationUrl", "mandatoryDisclosures"));
        }
        if (ff4j.check(FeatureList.PROMOTING_INTEROPERABILITY)) {
            boolean onlyInOld = existingListing.getMeaningfulUseUserHistory()
                    .stream()
                    .anyMatch(item -> !updatedListing.getMeaningfulUseUserHistory().contains(item));
            boolean onlyInNew = updatedListing.getMeaningfulUseUserHistory()
                    .stream()
                    .anyMatch(item -> !existingListing.getMeaningfulUseUserHistory().contains(item));
            if (onlyInOld || onlyInNew) {
                updatedListing.getWarningMessages()
                .add(msgUtil.getMessage("deprecated.field.update", "meaningfulUseUserHistory", "promotingInteroperabilityUserHistory"));
            }
        }
    }
}
