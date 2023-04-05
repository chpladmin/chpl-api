package gov.healthit.chpl.validation.listing.reviewer.duplicate;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiPredicate;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.PromotingInteroperabilityUser;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.validation.DuplicateReviewResult;

@Component("promotingInteroperabilityUserCountDuplicateReviewer")
public class PromotingInteroperabilityUserCountReviewer {
    private ErrorMessageUtil errorMessageUtil;

    @Autowired
    public PromotingInteroperabilityUserCountReviewer(ErrorMessageUtil errorMessageUtil) {
        this.errorMessageUtil = errorMessageUtil;
    }

    public void review(CertifiedProductSearchDetails listing) {
        DuplicateReviewResult<PromotingInteroperabilityUser> promotingInteroperabilityUserDuplicateDateResults = new DuplicateReviewResult<PromotingInteroperabilityUser>(getDuplicateDatePredicate());
        if (listing.getPromotingInteroperabilityUserHistory() != null) {
            for (PromotingInteroperabilityUser piu : listing.getPromotingInteroperabilityUserHistory()) {
                promotingInteroperabilityUserDuplicateDateResults.addObject(piu);
            }
        }
        if (promotingInteroperabilityUserDuplicateDateResults.duplicatesExist()) {
            listing.addAllBusinessErrorMessages(getErrors(promotingInteroperabilityUserDuplicateDateResults.getDuplicateList()));
        }

        DuplicateReviewResult<PromotingInteroperabilityUser> promotingInteroperabilityUserDuplicateResults = new DuplicateReviewResult<PromotingInteroperabilityUser>(getDuplicateCountAndDatePredicate());
        if (listing.getPromotingInteroperabilityUserHistory() != null) {
            for (PromotingInteroperabilityUser piu : listing.getPromotingInteroperabilityUserHistory()) {
                promotingInteroperabilityUserDuplicateResults.addObject(piu);
            }
        }
        if (promotingInteroperabilityUserDuplicateResults.duplicatesExist()) {
            listing.getWarningMessages().addAll(getWarnings(promotingInteroperabilityUserDuplicateResults.getDuplicateList()));
            listing.setPromotingInteroperabilityUserHistory(promotingInteroperabilityUserDuplicateResults.getUniqueList());
        }
    }

    private Set<String> getErrors(List<PromotingInteroperabilityUser> duplicates) {
        Set<String> errors = new HashSet<String>();
        for (PromotingInteroperabilityUser duplicate : duplicates) {
            String error = errorMessageUtil.getMessage("listing.duplicatePromotingInteroperabilityDate",
                    duplicate.getUserCountDate().toString());
            errors.add(error);
        }
        return errors;
    }

    private List<String> getWarnings(List<PromotingInteroperabilityUser> duplicates) {
        List<String> warnings = new ArrayList<String>();
        for (PromotingInteroperabilityUser duplicate : duplicates) {
            String warning = errorMessageUtil.getMessage("listing.duplicatePromotingInteroperability",
                    duplicate.getUserCount(), duplicate.getUserCountDate().toString());
            warnings.add(warning);
        }
        return warnings;
    }

    private BiPredicate<PromotingInteroperabilityUser, PromotingInteroperabilityUser> getDuplicateDatePredicate() {
        return new BiPredicate<PromotingInteroperabilityUser, PromotingInteroperabilityUser>() {
            @Override
            public boolean test(PromotingInteroperabilityUser piUser1,
                    PromotingInteroperabilityUser piUser2) {
                return ObjectUtils.allNotNull(piUser1.getUserCount(), piUser2.getUserCount(), piUser1.getUserCountDate(), piUser2.getUserCountDate())
                        && piUser1.getUserCountDate().equals(piUser2.getUserCountDate())
                        && !piUser1.getUserCount().equals(piUser2.getUserCount());
            }
        };
    }

    private BiPredicate<PromotingInteroperabilityUser, PromotingInteroperabilityUser> getDuplicateCountAndDatePredicate() {
        return new BiPredicate<PromotingInteroperabilityUser, PromotingInteroperabilityUser>() {
            @Override
            public boolean test(PromotingInteroperabilityUser piUser1,
                    PromotingInteroperabilityUser piUser2) {
                return ObjectUtils.allNotNull(piUser1.getUserCount(), piUser2.getUserCount(), piUser1.getUserCountDate(), piUser2.getUserCountDate())
                        && piUser1.getUserCount().equals(piUser2.getUserCount())
                        && piUser1.getUserCountDate().equals(piUser2.getUserCountDate());
            }
        };
    }
}
