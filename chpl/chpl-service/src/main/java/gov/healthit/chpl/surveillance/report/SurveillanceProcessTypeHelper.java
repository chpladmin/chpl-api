package gov.healthit.chpl.surveillance.report;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Objects;

import gov.healthit.chpl.surveillance.report.domain.SurveillanceProcessType;

public class SurveillanceProcessTypeHelper {

    public static List<SurveillanceProcessType> getAddedSurveillanceProcessTypes(List<SurveillanceProcessType> existingProcessTypes,
            List<SurveillanceProcessType> updatedProcessTypes) {
        return subtractLists(updatedProcessTypes, existingProcessTypes);
    }

    public static List<SurveillanceProcessType> getRemovedSurveillanceProcessTypes(List<SurveillanceProcessType> existingProcessTypes,
            List<SurveillanceProcessType> updatedProcessTypes) {
        return subtractLists(existingProcessTypes, updatedProcessTypes);
    }

    private static List<SurveillanceProcessType> subtractLists(List<SurveillanceProcessType> listA, List<SurveillanceProcessType> listB) {
        Predicate<SurveillanceProcessType> notInListB = eventFromA -> !listB.stream()
                .anyMatch(event -> doValuesMatch(eventFromA, event));

        return listA.stream()
                .filter(notInListB)
                .collect(Collectors.toList());
    }

    private static boolean doValuesMatch(SurveillanceProcessType procType1, SurveillanceProcessType procType2) {
        return ((procType1.getId() != null && procType2.getId() != null
                    && Objects.equal(procType1.getId(), procType2.getId()))
                || (ObjectUtils.allNotNull(procType1.getName(), procType2.getName())
                        && StringUtils.equals(procType1.getName(), procType2.getName())));
    }

}
