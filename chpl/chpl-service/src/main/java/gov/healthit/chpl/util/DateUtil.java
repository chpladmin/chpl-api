package gov.healthit.chpl.util;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import org.apache.commons.lang3.tuple.Pair;

import lombok.extern.log4j.Log4j2;

@Log4j2
public final class DateUtil {
    private static final String UTC_ZONE_ID = "UTC";
    public static final String ET_ZONE_ID = "America/New_York";
    private static final String ET_SUFFIX = " ET";
    private static final int HOUR_MAX = 23;
    private static final int MINUTE_MAX = 59;
    private static final int SECOND_MAX = 59;
    private static final int NANOSECOND_MAX = 999999999;

    private DateUtil() {
    }

    public static boolean datesOverlap(LocalDate startDate1, LocalDate endDate1, LocalDate startDate2, LocalDate endDate2) {
        Pair<LocalDate, LocalDate> dateRange1 = Pair.of(startDate1, endDate1);
        Pair<LocalDate, LocalDate> dateRange2 = Pair.of(startDate2, endDate2);
        return datesOverlap(dateRange1, dateRange2);
    }

    public static boolean datesOverlap(Pair<LocalDate, LocalDate> dateRange1, Pair<LocalDate, LocalDate> dateRange2) {
        if (dateRange2 == null || (dateRange2.getLeft() == null && dateRange2.getRight() == null)) {
            return true;
        } else if (dateRange2.getLeft() == null && dateRange2.getRight() != null) {
            return dateRange1.getLeft().isEqual(dateRange2.getRight()) || dateRange1.getLeft().isBefore(dateRange2.getRight());
        } else if (dateRange2.getLeft() != null && dateRange2.getRight() == null) {
            return dateRange1.getRight() == null
                    || (dateRange1.getRight().isEqual(dateRange2.getLeft()) || dateRange1.getRight().isAfter(dateRange2.getLeft()));
        } else {
            return (dateRange1.getLeft().isEqual(dateRange2.getRight())
                        || dateRange1.getLeft().isBefore(dateRange2.getRight()))
                        && (dateRange1.getRight() == null
                        || (dateRange1.getRight().isEqual(dateRange2.getLeft())
                        || dateRange1.getRight().isAfter(dateRange2.getLeft())));
        }
    }

    public static boolean isDateBetweenInclusive(Pair<LocalDate, LocalDate> dateRange, LocalDate dateToCheck) {
        if (dateToCheck == null) {
            return false;
        }

        Pair<LocalDate, LocalDate> modifiedDateRange = Pair.of(dateRange);
        if (modifiedDateRange.getLeft() == null) {
            modifiedDateRange = Pair.of(LocalDate.MIN, modifiedDateRange.getRight());
        }
        if (modifiedDateRange.getRight() == null) {
            modifiedDateRange = Pair.of(modifiedDateRange.getLeft(), LocalDate.MAX);
        }

        return (modifiedDateRange.getLeft().equals(dateToCheck)
                || modifiedDateRange.getRight().equals(dateToCheck)
                || (modifiedDateRange.getLeft().isBefore(dateToCheck)
                        && modifiedDateRange.getRight().isAfter(dateToCheck)));
    }

    public static String formatInEasternTime(ZonedDateTime date) {
        ZonedDateTime dateWithCorrectZone = date.withZoneSameInstant(ZoneId.of(ET_ZONE_ID));
        return DateTimeFormatter.ofPattern("MMM d, yyyy, h:mm:ss a").format(dateWithCorrectZone) + ET_SUFFIX;
    }

    public static String formatInEasternTime(ZonedDateTime date, String dtFormat) {
        ZonedDateTime dateWithCorrectZone = date.withZoneSameInstant(ZoneId.of(ET_ZONE_ID));
        return DateTimeFormatter.ofPattern(dtFormat).format(dateWithCorrectZone) + ET_SUFFIX;
    }

    public static String formatInEasternTime(LocalDateTime date) {
        ZonedDateTime dateWithCorrectZone = date.atZone(ZoneId.of(ET_ZONE_ID));
        return DateTimeFormatter.ofPattern("MMM d, yyyy, h:mm:ss a").format(dateWithCorrectZone) + ET_SUFFIX;
    }

    public static String formatInEasternTime(Date date) {
        ZonedDateTime zdt = ZonedDateTime.ofInstant(date.toInstant(), ZoneId.of(UTC_ZONE_ID));
        return formatInEasternTime(zdt);
    }

    public static String format(LocalDate date) {
        return date.format(DateTimeFormatter.ISO_LOCAL_DATE);
    }

    public static String formatInEasternTime(Date date, String dtFormat) {
        ZonedDateTime zdt = ZonedDateTime.ofInstant(date.toInstant(), ZoneId.of(UTC_ZONE_ID));
        return formatInEasternTime(zdt, dtFormat);
    }

    public static String formatInEasternTime(Long millis, String dtFormat) {
        Date date = new Date(millis);
        return formatInEasternTime(date, dtFormat);
    }

    public static Long toEpochMillis(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }
        Instant instant = localDateTime.atZone(ZoneId.systemDefault()).toInstant();
        return instant.toEpochMilli();
    }

    public static Long toEpochMillis(LocalDate localDate) {
        if (localDate == null) {
            return null;
        }
        Instant instant = localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant();
        return instant.toEpochMilli();
    }

    public static Long toEpochMillisEndOfDay(LocalDate localDate) {
        if (localDate == null) {
            return null;
        }
        Instant instant = localDate.atTime(HOUR_MAX, MINUTE_MAX, SECOND_MAX, NANOSECOND_MAX)
                .atZone(ZoneId.systemDefault()).toInstant();
        return instant.toEpochMilli();
    }

    public static Date toDate(LocalDate localDate) {
        if (localDate == null) {
            return null;
        }
        return Date.from(localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
    }

    public static Date toDate(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }
        return new Date(toEpochMillis(localDateTime));
    }

    public static LocalDate toLocalDate(long epochMillis) {
        return Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault()).toLocalDate();
    }

    public static LocalDateTime toLocalDateTime(long epochMillis) {
        return Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    public static LocalDateTime fromEasternToSystem(LocalDateTime input) {
        ZonedDateTime easternZoned = ZonedDateTime.of(input, ZoneId.of(ET_ZONE_ID));
        ZonedDateTime utc = easternZoned.withZoneSameInstant(ZoneId.systemDefault());
        return utc.toLocalDateTime();
    }

    public static LocalDateTime fromSystemToEastern(LocalDateTime input) {
        ZonedDateTime utcZoned = ZonedDateTime.of(input, ZoneId.systemDefault());
        ZonedDateTime eastern = utcZoned.withZoneSameInstant(ZoneId.of(ET_ZONE_ID));
        return eastern.toLocalDateTime();
    }
}
