package gov.healthit.chpl.upload.listing.validation.reviewer;

import java.util.Collection;
import java.util.function.Predicate;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CQMResultDetails;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.validation.listing.reviewer.Reviewer;
import gov.healthit.chpl.validation.listing.reviewer.edition2015.AttestedCriteriaCqmReviewer;
import gov.healthit.chpl.validation.listing.reviewer.edition2015.CqmAttestedCriteriaReviewer;

@Component
public class CqmResultReviewer implements Reviewer {
    private CqmAttestedCriteriaReviewer cqmAttestedCriteriaReviewer;
    private AttestedCriteriaCqmReviewer attestedCriteriaCqmReviewer;
    private ErrorMessageUtil msgUtil;

    @Autowired
    @SuppressWarnings("checkstyle:parameternumber")
    public CqmResultReviewer(@Qualifier("cqmAttestedCriteriaReviewer") CqmAttestedCriteriaReviewer cqmAttestedCriteriaReviewer,
            @Qualifier("attestedCriteriaCqmReviewer") AttestedCriteriaCqmReviewer attestedCriteriaCqmReviewer,
            ErrorMessageUtil msgUtil) {
        this.cqmAttestedCriteriaReviewer = cqmAttestedCriteriaReviewer;
        this.attestedCriteriaCqmReviewer = attestedCriteriaCqmReviewer;
        this.msgUtil = msgUtil;
    }

    @Override
    public void review(CertifiedProductSearchDetails listing) {
        if (!CollectionUtils.isEmpty(listing.getCqmResults())) {
            listing.getCqmResults().stream()
                    .filter(cqmResult -> BooleanUtils.isTrue(cqmResult.getSuccess()))
                    .forEach(cqmResult -> reviewCqmResultRequiredFields(listing, cqmResult));

            Predicate<CQMResultDetails> shouldRemove = cqm -> isMissingCmsIdButHasOtherData(cqm) || isMissingCqmCriterionId(cqm);
            listing.getCqmResults().removeIf(shouldRemove);
        }

        cqmAttestedCriteriaReviewer.review(listing);
        attestedCriteriaCqmReviewer.review(listing);
    }

    private void reviewCqmResultRequiredFields(CertifiedProductSearchDetails listing, CQMResultDetails cqmResult) {
        if (isMissingCmsIdButHasOtherData(cqmResult)) {
            listing.addWarningMessage(msgUtil.getMessage("listing.cqm.missingCmsId"));
        } else if (!StringUtils.isEmpty(cqmResult.getCmsId())) {
            if (isMissingCqmCriterionId(cqmResult)) {
                listing.addWarningMessage(msgUtil.getMessage("listing.cqm.invalidCmsId", cqmResult.getCmsId()));
            } else {
                if (CollectionUtils.isEmpty(cqmResult.getSuccessVersions())) {
                    listing.addDataErrorMessage(msgUtil.getMessage("listing.cqm.missingVersion", cqmResult.getCmsId()));
                } else {
                    reviewSuccessVersions(listing, cqmResult);
                }
            }
        }
    }

    private boolean isMissingCmsIdButHasOtherData(CQMResultDetails cqmResult) {
        return StringUtils.isEmpty(cqmResult.getCmsId())
                && (!CollectionUtils.isEmpty(cqmResult.getSuccessVersions()) || !CollectionUtils.isEmpty(cqmResult.getCriteria()));
    }

    private boolean isMissingCqmCriterionId(CQMResultDetails cqmResult) {
        return cqmResult.getCqmCriterionId() == null;
    }

    private void reviewSuccessVersions(CertifiedProductSearchDetails listing, CQMResultDetails cqmResult) {
        cqmResult.getSuccessVersions().stream()
                .filter(successVersion -> !inCqmAllVersions(successVersion, cqmResult.getAllVersions()))
                .forEach(invalidSuccessVersion -> listing.addDataErrorMessage(
                        msgUtil.getMessage("listing.cqm.invalidCqmVersion", cqmResult.getCmsId(), invalidSuccessVersion)));
    }

    private boolean inCqmAllVersions(String version, Collection<String> allVersions) {
        return allVersions.stream()
                .filter(ver -> ver.equals(version))
                .findAny().isPresent();
    }

}
