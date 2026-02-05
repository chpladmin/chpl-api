package gov.healthit.chpl.surveillance.report;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ObjectUtils;

import gov.healthit.chpl.surveillance.report.domain.SurveillanceCapStatus;

public final class SurveillanceCapStatusHelper {

    private SurveillanceCapStatusHelper() { }

    public static List<SurveillanceCapStatus> getAddedSurveillanceCapStatuses(List<SurveillanceCapStatus> existing,
            List<SurveillanceCapStatus> updated) {
        return subtractLists(updated, existing);
    }

    public static List<SurveillanceCapStatus> getRemovedSurveillanceCapStatuses(List<SurveillanceCapStatus> existing,
            List<SurveillanceCapStatus> updated) {
        return subtractLists(existing, updated);
    }

    private static List<SurveillanceCapStatus> subtractLists(List<SurveillanceCapStatus> listA, List<SurveillanceCapStatus> listB) {
        Predicate<SurveillanceCapStatus> notInListB = eventFromA -> !listB.stream()
                .anyMatch(event -> doValuesMatch(eventFromA, event));

        return listA.stream()
                .filter(notInListB)
                .collect(Collectors.toList());
    }

    private static boolean doValuesMatch(SurveillanceCapStatus item1, SurveillanceCapStatus item2) {
        return ((item1.getId() != null && item2.getId() != null
                    && Objects.equals(item1.getId(), item2.getId()))
                || (ObjectUtils.allNotNull(item1.getName(), item2.getName())
                        && Objects.equals(item1.getName(), item2.getName())));
    }
}
