package gov.healthit.chpl.util;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public final class DateUtil {
    private static final String UTC_ZONE_ID = "UTC";
    private static final String ET_ZONE_ID = "America/New_York";
    private static final String ET_SUFFIX = " ET";
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

    public static String formatInEasternTime(Date date, String dtFormat) {
        ZonedDateTime zdt = ZonedDateTime.ofInstant(date.toInstant(), ZoneId.of(UTC_ZONE_ID));
        return formatInEasternTime(zdt, dtFormat);
    }
}
