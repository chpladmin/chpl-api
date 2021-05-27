package gov.healthit.chpl.scheduler.job.surveillancereportingactivity;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public final class Statistics {

    private Statistics() {}

    public static Integer getMinimum(List<Integer> values) {
        return values.stream()
                .filter(val -> val != null)
                .min(Comparator.comparing(Integer::intValue))
                .orElseGet(() -> null);
    }

    public static Integer getMaximum(List<Integer> values) {
        return values.stream()
                .filter(val -> val != null)
                .max(Comparator.comparing(Integer::intValue))
                .orElseGet(() -> null);
    }

    public static Integer getMean(List<Integer> values) {
        if (values == null || values.size() == 0) {
            return null;
        }
        return (int) Math.round(values.stream()
                .filter(item -> item != null)
                .mapToDouble(a -> a)
                .average()
                .orElse(0));
    }

    public static Integer getMedian(List<Integer> values) {

        List<Integer> filtered = values.stream()
                .filter(item -> item != null)
                .collect(Collectors.toList());

        int middle = Math.round(filtered.size() / 2);
        filtered.sort((v1, v2) -> v1 - v2);

        if (filtered.size() > middle) {
            return values.get(middle);
        } else {
            return null;
        }
    }

    public static Integer getMode(List<Integer> values) {
        if (values == null || values.size() == 0) {
            return null;
        }

        Integer modeCount = 0;  // The count of the mode value
        Integer mode = 0;       // The value of the mode

        Integer currCount = 0;
        List<Integer> filtered = values.stream()
                .filter(val -> val != null)
                .collect(Collectors.toList());

        // Iterate through all values in our array and consider it as a possible mode
        for (Integer candidateMode : filtered) {
            // Reset the number of times we have seen the current value
            currCount = 0;

            // Iterate through the array counting the number of times we see the current candidate mode
            for (Integer element : filtered) {
                // If they match, increment the current count
                if (candidateMode == element) {
                    currCount++;
                }
            }
            // We only save this candidate mode, if its count is greater than the current mode
            // we have stored in the "mode" variable
            if (currCount > modeCount) {
                modeCount = currCount;
                mode = candidateMode;
            }
        }
        return mode;
    }

    public static Integer getCountInRange(List<Integer> values, Integer minRange, Integer maxRange) {
        if (values == null || values.size() == 0) {
            return null;
        }
        Integer min = minRange == null ? Integer.MIN_VALUE : minRange;
        Integer max = maxRange == null ? Integer.MAX_VALUE : maxRange;
        return Math.toIntExact(values.stream()
                .filter(val -> val != null && val >= min && val <= max)
                .count());
    }

    public static Integer getDateDiff(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            return null;
        } else {
            return Long.valueOf(ChronoUnit.DAYS.between(startDate, endDate)).intValue();
        }
    }
}
