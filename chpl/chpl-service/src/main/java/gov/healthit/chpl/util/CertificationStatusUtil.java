package gov.healthit.chpl.util;

import java.util.List;
import java.util.stream.Stream;

import gov.healthit.chpl.domain.CertificationStatus;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.entity.CertificationStatusType;

public final class CertificationStatusUtil {

    private static final List<CertificationStatusType> ACTIVE_STATUSES = Stream.of(
            CertificationStatusType.Active,
            CertificationStatusType.SuspendedByAcb,
            CertificationStatusType.SuspendedByOnc)
            .toList();

    private static final List<CertificationStatusType> SUSPENDED_STATUSES = Stream.of(
            CertificationStatusType.SuspendedByAcb,
            CertificationStatusType.SuspendedByOnc)
            .toList();

    private static final List<CertificationStatusType> NONRETIRED_STATUSES = Stream.of(
            CertificationStatusType.Active,
            CertificationStatusType.SuspendedByAcb,
            CertificationStatusType.SuspendedByOnc,
            CertificationStatusType.TerminatedByOnc,
            CertificationStatusType.WithdrawnByAcb,
            CertificationStatusType.WithdrawnByDeveloper,
            CertificationStatusType.WithdrawnByDeveloperUnderReview)
            .toList();

    private CertificationStatusUtil() {}

    public static List<CertificationStatusType> getActiveStatuses() {
        return ACTIVE_STATUSES;
    }

    public static List<String> getActiveStatusNames() {
        return ACTIVE_STATUSES.stream()
                .map(status -> status.getName())
                .toList();
    }

    public static List<CertificationStatusType> getSuspendedStatuses() {
        return SUSPENDED_STATUSES;
    }

    public static List<CertificationStatusType> getNonretiredStatuses() {
        return NONRETIRED_STATUSES;
    }

    public static boolean isActive(CertifiedProductSearchDetails listing) {
        CertificationStatus currentStatusName = NullSafeEvaluator.eval(() -> listing.getCurrentStatus().getStatus(), null);
        return currentStatusName != null && getActiveStatusNames().contains(currentStatusName.getName());
    }
}
