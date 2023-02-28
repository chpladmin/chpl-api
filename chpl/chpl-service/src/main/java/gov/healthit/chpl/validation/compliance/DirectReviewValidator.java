package gov.healthit.chpl.validation.compliance;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.compliance.DirectReview;
import gov.healthit.chpl.domain.compliance.DirectReviewNonConformity;
import gov.healthit.chpl.util.ErrorMessageUtil;

@Component
public class DirectReviewValidator {
    private ErrorMessageUtil msgUtil;

    @Autowired
    public DirectReviewValidator(ErrorMessageUtil msgUtil) {
        this.msgUtil = msgUtil;
    }

    public void review(DirectReview directReview) {
        if (CollectionUtils.isEmpty(directReview.getNonConformities())) {
            directReview.getErrorMessages().add(
                    msgUtil.getMessage("compliance.directReview.missingNonConformity", directReview.getJiraKey()));
        }
        if (directReview.getDeveloperId() == null) {
            directReview.getErrorMessages().add(
                    msgUtil.getMessage("compliance.directReview.missingDeveloperId", directReview.getJiraKey()));
        }
        directReview.getNonConformities().stream()
            .forEach(nonConformity -> reviewNonConformity(directReview, nonConformity));
    }

    private void reviewNonConformity(DirectReview directReview, DirectReviewNonConformity nonConformity) {
        if (StringUtils.isEmpty(nonConformity.getNonConformityStatus())) {
            directReview.getErrorMessages().add(
                    msgUtil.getMessage("compliance.directReview.missingNonConformityStatus", directReview.getJiraKey()));
        }
    }
}
