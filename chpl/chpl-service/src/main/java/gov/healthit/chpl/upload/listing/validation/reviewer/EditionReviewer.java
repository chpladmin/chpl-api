package gov.healthit.chpl.upload.listing.validation.reviewer;

import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.validation.listing.reviewer.Reviewer;

@Component("editionReviewer")
public class EditionReviewer implements Reviewer {
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

        Long editionId = MapUtils.getLong(certEditionMap, CertifiedProductSearchDetails.EDITION_ID_KEY);
        String editionYear = MapUtils.getString(certEditionMap, CertifiedProductSearchDetails.EDITION_NAME_KEY);

        if (editionId == null && StringUtils.isEmpty(editionYear)) {
            listing.getErrorMessages().add(msgUtil.getMessage("listing.missingEdition"));
        } else  if (editionId != null && StringUtils.isEmpty(editionYear)) {
            listing.getErrorMessages().add(msgUtil.getMessage("listing.missingEditionYear"));
        } else if (!StringUtils.isEmpty(editionYear) && editionId == null) {
            listing.getErrorMessages().add(msgUtil.getMessage("listing.invalidEdition", editionYear));
        }
    }
}
