package gov.healthit.chpl.upload.listing.validation.reviewer;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import gov.healthit.chpl.domain.CertifiedProductQmsStandard;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.entity.FuzzyType;
import gov.healthit.chpl.util.ErrorMessageUtil;

public class QmsStandardReviewer {
    private ErrorMessageUtil msgUtil;

    @Autowired
    public QmsStandardReviewer(ErrorMessageUtil msgUtil) {
        this.msgUtil = msgUtil;
    }

    public void review(CertifiedProductSearchDetails listing) {
        doQmsStandardsExist(listing);
        areQmsStandardsValid(listing);
        addFuzzyMatchWarnings(listing);
    }

    private void doQmsStandardsExist(CertifiedProductSearchDetails listing) {
        if (listing.getQmsStandards() == null || listing.getQmsStandards().size() == 0) {
            listing.getErrorMessages().add(msgUtil.getMessage("listing.qmsStandardsNotFound"));
        }
    }

    private void areQmsStandardsValid(CertifiedProductSearchDetails listing) {
        if (listing.getQmsStandards() != null) {
            listing.getQmsStandards().stream()
            .forEach(qmsStandard -> {
                checkQmsStandardNameRequired(listing, qmsStandard);
                checkApplicableCriteriaRequired(listing, qmsStandard);
            });
        }
    }

    private void checkQmsStandardNameRequired(CertifiedProductSearchDetails listing, CertifiedProductQmsStandard qmsStandard) {
        if (StringUtils.isEmpty(qmsStandard.getQmsStandardName())) {
            listing.getErrorMessages().add(msgUtil.getMessage("listing.qmsStandardMissingName"));
        }
    }

    private void checkApplicableCriteriaRequired(CertifiedProductSearchDetails listing, CertifiedProductQmsStandard qmsStandard) {
        if (StringUtils.isEmpty(qmsStandard.getApplicableCriteria())) {
            listing.getErrorMessages().add(msgUtil.getMessage("listing.qmsStandardMissingApplicableCriteria"));
        }
    }

    private void addFuzzyMatchWarnings(CertifiedProductSearchDetails listing) {
        if (listing.getQmsStandards() != null) {
            listing.getQmsStandards().stream()
                .filter(qmsStandard -> hasFuzzyMatch(qmsStandard))
                .forEach(qmsStandard -> addFuzzyMatchWarning(listing, qmsStandard));
        }
    }

    private boolean hasFuzzyMatch(CertifiedProductQmsStandard qmsStandard) {
        return qmsStandard.getQmsStandardId() == null
                && !StringUtils.isEmpty(qmsStandard.getUserEnteredQmsStandardName())
                && !StringUtils.equals(qmsStandard.getQmsStandardName(), qmsStandard.getUserEnteredQmsStandardName());
    }

    private void addFuzzyMatchWarning(CertifiedProductSearchDetails listing, CertifiedProductQmsStandard qmsStandard) {
        String warningMsg = msgUtil.getMessage("listing.fuzzyMatch", FuzzyType.QMS_STANDARD.fuzzyType(),
                qmsStandard.getUserEnteredQmsStandardName(), qmsStandard.getQmsStandardName());
        listing.getWarningMessages().add(warningMsg);
    }
}
