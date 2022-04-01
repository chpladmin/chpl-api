package gov.healthit.chpl.util;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import lombok.extern.log4j.Log4j2;

@Log4j2
public final class DateUtil {
    private static final String UTC_ZONE_ID = "UTC";
    private static final String ET_ZONE_ID = "America/New_York";
    private static final String ET_SUFFIX = " ET";
    private static final int HOUR_MAX = 23;
    private static final int MINUTE_MAX = 59;
    private static final int SECOND_MAX = 59;
    private static final int NANOSECOND_MAX = 999999999;

    private DateUtil() {

    }

    public static String formatInEasternTime(ZonedDateTime date) {
        ZonedDateTime dateWithCorrectZone = date.withZoneSameInstant(ZoneId.of(ET_ZONE_ID));
        return DateTimeFormatter.ofPattern("MMM d, yyyy, h:mm:ss a").format(dateWithCorrectZone) + ET_SUFFIX;
    }

    public static String formatInEasternTime(ZonedDateTime date, String dtFormat) {
        ZonedDateTime dateWithCorrectZone = date.withZoneSameInstant(ZoneId.of(ET_ZONE_ID));
        return DateTimeFormatter.ofPattern(dtFormat).format(dateWithCorrectZone) + ET_SUFFIX;
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

    public static LocalDate toLocalDate(long epochMillis) {
        return Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault()).toLocalDate();
    }

    public static LocalDateTime toLocalDateTime(long epochMillis) {
        return Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    public static Date getNowInEasternTime() {
        Date result = Date.from(ZonedDateTime.now().withZoneSameInstant(ZoneId.of(ET_ZONE_ID)).toInstant());
        LOGGER.info("Now in Eastern Time: " + result);
        return result;
    }
}
