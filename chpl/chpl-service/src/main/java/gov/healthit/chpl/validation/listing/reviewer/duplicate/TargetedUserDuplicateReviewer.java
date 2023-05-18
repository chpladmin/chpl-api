package gov.healthit.chpl.validation.listing.reviewer.duplicate;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.CertifiedProductTargetedUser;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.validation.DuplicateReviewResult;

@Component("targetedUserDuplicateReviewer")
public class TargetedUserDuplicateReviewer {
    private ErrorMessageUtil errorMessageUtil;

    @Autowired
    public TargetedUserDuplicateReviewer(ErrorMessageUtil errorMessageUtil) {
        this.errorMessageUtil = errorMessageUtil;
    }

    public void review(CertifiedProductSearchDetails listing) {

        DuplicateReviewResult<CertifiedProductTargetedUser> targetedUserDuplicateResults =
                new DuplicateReviewResult<CertifiedProductTargetedUser>(getPredicate());

        if (listing.getTargetedUsers() != null) {
            for (CertifiedProductTargetedUser dto : listing.getTargetedUsers()) {
                targetedUserDuplicateResults.addObject(dto);
            }
        }
        if (targetedUserDuplicateResults.duplicatesExist()) {
            listing.addAllWarningMessages(
                    getWarnings(targetedUserDuplicateResults.getDuplicateList()).stream()
                    .collect(Collectors.toSet()));
            listing.setTargetedUsers(targetedUserDuplicateResults.getUniqueList());
        }
    }

    private List<String> getWarnings(List<CertifiedProductTargetedUser> duplicates) {
        List<String> warnings = new ArrayList<String>();
        for (CertifiedProductTargetedUser duplicate : duplicates) {
            String warning = errorMessageUtil.getMessage("listing.duplicateTargetedUser",
                    duplicate.getTargetedUserName());
            warnings.add(warning);
        }
        return warnings;
    }

    private BiPredicate<CertifiedProductTargetedUser, CertifiedProductTargetedUser> getPredicate() {
        return new BiPredicate<CertifiedProductTargetedUser, CertifiedProductTargetedUser>() {
            @Override
            public boolean test(CertifiedProductTargetedUser dto1,
                    CertifiedProductTargetedUser dto2) {
                return ObjectUtils.allNotNull(dto1.getTargetedUserName(), dto2.getTargetedUserName())
                        && dto1.getTargetedUserName().equals(dto2.getTargetedUserName());
            }
        };
    }
}
