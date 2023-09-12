package gov.healthit.chpl.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;

public class DateUtilTest {

    @Test
    public void dateRangeOverlapsWithNull_returnsTrue() {
        Pair<LocalDate, LocalDate> dateRange1 = Pair.of(LocalDate.parse("2023-04-01"), LocalDate.parse("2023-04-30"));

        assertTrue(DateUtil.datesOverlap(dateRange1, null));
    }

    @Test
    public void dateRange1EqualsDateRange2_returnsTrue() {
        Pair<LocalDate, LocalDate> dateRange1 = Pair.of(LocalDate.parse("2023-04-01"), LocalDate.parse("2023-04-30"));
        Pair<LocalDate, LocalDate> dateRange2 = Pair.of(LocalDate.parse("2023-04-01"), LocalDate.parse("2023-04-30"));

        assertTrue(DateUtil.datesOverlap(dateRange1, dateRange2));
    }

    @Test
    public void dateRange1EndEqualsDateRange2Start_returnsTrue() {
        Pair<LocalDate, LocalDate> dateRange1 = Pair.of(LocalDate.parse("2023-04-01"), LocalDate.parse("2023-04-30"));
        Pair<LocalDate, LocalDate> dateRange2 = Pair.of(LocalDate.parse("2023-04-30"), LocalDate.parse("2023-05-31"));

        assertTrue(DateUtil.datesOverlap(dateRange1, dateRange2));
    }

    @Test
    public void dateRange1EndAfterDateRange2Start_returnsTrue() {
        Pair<LocalDate, LocalDate> dateRange1 = Pair.of(LocalDate.parse("2023-04-01"), LocalDate.parse("2023-05-01"));
        Pair<LocalDate, LocalDate> dateRange2 = Pair.of(LocalDate.parse("2023-04-30"), LocalDate.parse("2023-05-31"));

        assertTrue(DateUtil.datesOverlap(dateRange1, dateRange2));
    }

    @Test
    public void dateRange1EndAfterDateRange2StartAndDateRange2EndNull_returnsTrue() {
        Pair<LocalDate, LocalDate> dateRange1 = Pair.of(LocalDate.parse("2023-04-01"), LocalDate.parse("2023-05-01"));
        Pair<LocalDate, LocalDate> dateRange2 = Pair.of(LocalDate.parse("2023-04-30"), null);

        assertTrue(DateUtil.datesOverlap(dateRange1, dateRange2));
    }

    @Test
    public void dateRange1IncludesDateRange2_returnsTrue() {
        Pair<LocalDate, LocalDate> dateRange1 = Pair.of(LocalDate.parse("2023-05-01"), LocalDate.parse("2023-06-01"));
        Pair<LocalDate, LocalDate> dateRange2 = Pair.of(LocalDate.parse("2023-05-15"), LocalDate.parse("2023-05-31"));

        assertTrue(DateUtil.datesOverlap(dateRange1, dateRange2));
    }

    @Test
    public void dateRange1EndBeforeDateRange2Start_returnsFalse() {
        Pair<LocalDate, LocalDate> dateRange1 = Pair.of(LocalDate.parse("2023-04-01"), LocalDate.parse("2023-04-29"));
        Pair<LocalDate, LocalDate> dateRange2 = Pair.of(LocalDate.parse("2023-04-30"), LocalDate.parse("2023-05-31"));

        assertFalse(DateUtil.datesOverlap(dateRange1, dateRange2));
    }

    @Test
    public void dateRange1StartAfterDateRange2End_returnsFalse() {
        Pair<LocalDate, LocalDate> dateRange1 = Pair.of(LocalDate.parse("2023-05-01"), LocalDate.parse("2023-06-01"));
        Pair<LocalDate, LocalDate> dateRange2 = Pair.of(LocalDate.parse("2023-04-01"), LocalDate.parse("2023-04-15"));

        assertFalse(DateUtil.datesOverlap(dateRange1, dateRange2));
    }

}
