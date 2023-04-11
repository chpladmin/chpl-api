package gov.healthit.chpl.upload.listing.validation.reviewer;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertifiedProductQmsStandard;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.fuzzyMatching.FuzzyType;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.validation.listing.reviewer.Reviewer;

@Component
public class QmsStandardReviewer implements Reviewer {
    private ErrorMessageUtil msgUtil;

    @Autowired
    public QmsStandardReviewer(ErrorMessageUtil msgUtil) {
        this.msgUtil = msgUtil;
    }

    @Override
    public void review(CertifiedProductSearchDetails listing) {
        removeQmsStandardsNotFound(listing);
        doQmsStandardsExist(listing);
        areQmsStandardsValid(listing);
        addFuzzyMatchWarnings(listing);
    }

    private void removeQmsStandardsNotFound(CertifiedProductSearchDetails listing) {
        List<CertifiedProductQmsStandard> qmsStandards = listing.getQmsStandards();
        if (!CollectionUtils.isEmpty(qmsStandards)) {
            List<CertifiedProductQmsStandard> qmsStandardsWithoutIds = qmsStandards.stream()
                    .filter(currQmsStd -> currQmsStd.getQmsStandardId() == null)
                    .collect(Collectors.toList());

            if (!CollectionUtils.isEmpty(qmsStandardsWithoutIds)) {
                qmsStandards.removeAll(qmsStandardsWithoutIds);

                qmsStandardsWithoutIds.stream()
                        .forEach(qmsStdWithoutId -> listing.getWarningMessages().add(
                                msgUtil.getMessage("listing.qmsStandardNotFoundAndRemoved",
                                        qmsStdWithoutId.getQmsStandardName() == null ? "" : qmsStdWithoutId.getQmsStandardName())));
            }
        }
    }

    private void doQmsStandardsExist(CertifiedProductSearchDetails listing) {
        if (listing.getQmsStandards() == null || listing.getQmsStandards().size() == 0) {
            addQmsStandardsAreRequiredErrorMessage(listing);
        }
    }

    private void addQmsStandardsAreRequiredErrorMessage(CertifiedProductSearchDetails listing) {
        if (isListingNew(listing)) {
            listing.addDataErrorMessage(msgUtil.getMessage("listing.qmsStandardsNotFound"));
        } else {
            listing.addBusinessErrorMessage(msgUtil.getMessage("listing.qmsStandardsNotFound"));
        }
    }

    private boolean isListingNew(CertifiedProductSearchDetails listing) {
        return listing.getId() == null;
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
            listing.addDataErrorMessage(msgUtil.getMessage("listing.qmsStandardMissingName"));
        }
    }

    private void checkApplicableCriteriaRequired(CertifiedProductSearchDetails listing, CertifiedProductQmsStandard qmsStandard) {
        if (StringUtils.isEmpty(qmsStandard.getApplicableCriteria())) {
            addNotAppicableCriteriaErrorMessage(listing);
        }
    }

    private void addNotAppicableCriteriaErrorMessage(CertifiedProductSearchDetails listing) {
        if (isListingNew(listing)) {
            listing.addDataErrorMessage(msgUtil.getMessage("listing.qmsStandardMissingApplicableCriteria"));
        } else {
            listing.addBusinessErrorMessage(msgUtil.getMessage("listing.qmsStandardMissingApplicableCriteria"));
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
        return !StringUtils.isEmpty(qmsStandard.getUserEnteredQmsStandardName())
                && !StringUtils.equals(qmsStandard.getQmsStandardName(), qmsStandard.getUserEnteredQmsStandardName());
    }

    private void addFuzzyMatchWarning(CertifiedProductSearchDetails listing, CertifiedProductQmsStandard qmsStandard) {
        String warningMsg = msgUtil.getMessage("listing.fuzzyMatch", FuzzyType.QMS_STANDARD.fuzzyType(),
                qmsStandard.getUserEnteredQmsStandardName(), qmsStandard.getQmsStandardName());
        listing.getWarningMessages().add(warningMsg);
    }
}
