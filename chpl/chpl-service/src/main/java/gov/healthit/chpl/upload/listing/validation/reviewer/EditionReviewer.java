package gov.healthit.chpl.upload.listing.validation.reviewer;

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

    @Override
    public void review(CertifiedProductSearchDetails listing) {
        if (listing.getEdition() == null) {
            listing.addDataErrorMessage(msgUtil.getMessage("listing.missingEdition"));
            return;
        }

        Long editionId = listing.getEdition().getId();
        String editionYear = listing.getEdition().getName();

        if (editionId == null && StringUtils.isEmpty(editionYear)) {
            listing.addDataErrorMessage(msgUtil.getMessage("listing.missingEdition"));
        } else if (editionId != null && StringUtils.isEmpty(editionYear)) {
            listing.addDataErrorMessage(msgUtil.getMessage("listing.missingEditionYear"));
        } else if (!StringUtils.isEmpty(editionYear) && editionId == null) {
            listing.addDataErrorMessage(msgUtil.getMessage("listing.invalidEdition", editionYear));
        }
    }
}
