package gov.healthit.chpl.upload.listing.validation.reviewer;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.util.ErrorMessageUtil;

@Component("editionReviewer")
public class EditionReviewer  {
    private ErrorMessageUtil msgUtil;

    @Autowired
    public EditionReviewer(ErrorMessageUtil msgUtil) {
        this.msgUtil = msgUtil;
    }

    public void review(CertifiedProductSearchDetails listing) {
        Map<String, Object> certEditionMap = listing.getCertificationEdition();
        if (certEditionMap == null) {
            listing.getErrorMessages().add(msgUtil.getMessage("listing.missingEdition"));
            return;
        }

        String editionYear = null, editionId = null;

        Object editionYearValue = certEditionMap.get(CertifiedProductSearchDetails.EDITION_NAME_KEY);
        if (editionYearValue != null) {
            editionYear = editionYearValue.toString();
            if (StringUtils.isEmpty(editionYear)) {
                listing.getErrorMessages().add(msgUtil.getMessage("listing.missingEditionYear"));
            }
        } else {
            listing.getErrorMessages().add(msgUtil.getMessage("listing.missingEditionYear"));
        }

        Object editionIdValue = certEditionMap.get(CertifiedProductSearchDetails.EDITION_ID_KEY);
        if (editionIdValue != null) {
            editionId = editionIdValue.toString();
            if (!StringUtils.isEmpty(editionYear) && StringUtils.isEmpty(editionId)) {
                listing.getErrorMessages().add(msgUtil.getMessage("listing.invalidEdition", editionYear));
            }
        } else if (editionIdValue == null && !StringUtils.isEmpty(editionYear)) {
            listing.getErrorMessages().add(msgUtil.getMessage("listing.invalidEdition", editionYear));
        }
    }
}
