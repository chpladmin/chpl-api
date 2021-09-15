package gov.healthit.chpl.validation.listing.reviewer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.util.ErrorMessageUtil;

@Component("certificationDateComparisonReviewer")
public class CertificationDateComparisonReviewer implements ComparisonReviewer {
    private static final String CERTIFICATION_DATE_FORMAT = "MMMM dd, yyyy";
    private ErrorMessageUtil msgUtil;

    @Autowired
    public CertificationDateComparisonReviewer(ErrorMessageUtil msgUtil) {
        this.msgUtil = msgUtil;
    }

    @Override
    public void review(CertifiedProductSearchDetails existingListing,
            CertifiedProductSearchDetails updatedListing) {
//        if (existingListing.getCertificationDate() == null || updatedListing.getCertificationDate() == null) {
//            updatedListing.getErrorMessages().add(
//                    msgUtil.getMessage("listing.certificationDateMissing"));
//        } else {
//            LocalDate existingCertificationDay = DateUtil.toLocalDate(existingListing.getCertificationDate());
//            LocalDate updatedCertificationDay = DateUtil.toLocalDate(updatedListing.getCertificationDate());
//            if (!existingCertificationDay.equals(updatedCertificationDay)) {
//                updatedListing.getErrorMessages().add(
//                        msgUtil.getMessage("listing.certificationDateChanged",
//                                DateUtil.formatInEasternTime(existingListing.getCertificationDate(), CERTIFICATION_DATE_FORMAT),
//                                DateUtil.formatInEasternTime(updatedListing.getCertificationDate(), CERTIFICATION_DATE_FORMAT)));
//            }
//        }
    }
}
