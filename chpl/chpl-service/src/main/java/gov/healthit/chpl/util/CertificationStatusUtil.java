package gov.healthit.chpl.util;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import gov.healthit.chpl.entity.CertificationStatusType;

public final class CertificationStatusUtil {

    private static final Set<CertificationStatusType> ACTIVE_STATUSES = Stream.of(
            CertificationStatusType.Active,
            CertificationStatusType.SuspendedByAcb,
            CertificationStatusType.SuspendedByOnc)
            .collect(Collectors.toSet());

    private CertificationStatusUtil() {}

    public static Set<CertificationStatusType> getActiveStatuses() {
        return ACTIVE_STATUSES;
    }
}
