package gov.healthit.chpl.surveillance.report;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Objects;

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
                    && Objects.equal(item1.getId(), item2.getId()))
                || (ObjectUtils.allNotNull(item1.getName(), item2.getName())
                        && StringUtils.equals(item1.getName(), item2.getName())));
    }
}
