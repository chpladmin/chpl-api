package gov.healthit.chpl.util;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public final class DateUtil {

    private DateUtil() {

    }

    public static String formatInEasternTime(ZonedDateTime date) {
        ZonedDateTime dateWithCorrectZone = date.withZoneSameInstant(ZoneId.of("America/New_York"));
        return DateTimeFormatter.ofPattern("MMM d, yyyy, h:mm:ss a").format(dateWithCorrectZone) + " ET";
    }

    public static String formatInEasternTime(Date date) {
        ZonedDateTime zdt = ZonedDateTime.ofInstant(date.toInstant(), ZoneId.of("UTC"));
        return formatInEasternTime(zdt);
    }
}
