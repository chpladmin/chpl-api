package gov.healthit.chpl.surveillance.report;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Objects;

import gov.healthit.chpl.surveillance.report.domain.SurveillanceGroundsForInitiating;

public final class SurveillanceGroundsForInitiatingHelper {

    private SurveillanceGroundsForInitiatingHelper() { }

    public static List<SurveillanceGroundsForInitiating> getAddedSurveillanceGroundsForInitiating(List<SurveillanceGroundsForInitiating> existing,
            List<SurveillanceGroundsForInitiating> updated) {
        return subtractLists(updated, existing);
    }

    public static List<SurveillanceGroundsForInitiating> getRemovedSurveillanceGroundsForInitiating(List<SurveillanceGroundsForInitiating> existing,
            List<SurveillanceGroundsForInitiating> updated) {
        return subtractLists(existing, updated);
    }

    private static List<SurveillanceGroundsForInitiating> subtractLists(List<SurveillanceGroundsForInitiating> listA, List<SurveillanceGroundsForInitiating> listB) {
        Predicate<SurveillanceGroundsForInitiating> notInListB = eventFromA -> !listB.stream()
                .anyMatch(event -> doValuesMatch(eventFromA, event));

        return listA.stream()
                .filter(notInListB)
                .collect(Collectors.toList());
    }

    private static boolean doValuesMatch(SurveillanceGroundsForInitiating item1, SurveillanceGroundsForInitiating item2) {
        return ((item1.getId() != null && item2.getId() != null
                    && Objects.equal(item1.getId(), item2.getId()))
                || (ObjectUtils.allNotNull(item1.getName(), item2.getName())
                        && StringUtils.equals(item1.getName(), item2.getName())));
    }
}
