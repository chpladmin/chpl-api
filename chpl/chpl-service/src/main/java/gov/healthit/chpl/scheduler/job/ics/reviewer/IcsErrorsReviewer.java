package gov.healthit.chpl.scheduler.job.ics.reviewer;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.util.ChplProductNumberUtil;

public abstract class IcsErrorsReviewer {
    public abstract String getIcsError(CertifiedProductSearchDetails listing);

    protected Integer getIcsCode(CertifiedProductSearchDetails listing) {
        String uniqueId = listing.getChplProductNumber();
        String[] uniqueIdParts = uniqueId.split("\\.");
        if (uniqueIdParts.length != ChplProductNumberUtil.CHPL_PRODUCT_ID_PARTS) {
            return null;
        }
        String icsCodePart = uniqueIdParts[ChplProductNumberUtil.ICS_CODE_INDEX];
        Integer icsCode = Integer.valueOf(icsCodePart);
        return icsCode;
    }

    protected boolean hasIcs(CertifiedProductSearchDetails listing) {
        String uniqueId = listing.getChplProductNumber();
        String[] uniqueIdParts = uniqueId.split("\\.");
        if (uniqueIdParts.length != ChplProductNumberUtil.CHPL_PRODUCT_ID_PARTS) {
            return false;
        }
        String icsCodePart = uniqueIdParts[ChplProductNumberUtil.ICS_CODE_INDEX];
        Integer icsCode = Integer.valueOf(icsCodePart);
        boolean hasIcs = icsCode.intValue() > 0
                || (listing.getIcs() != null && listing.getIcs().getInherits().booleanValue());

        return hasIcs;
    }
}
