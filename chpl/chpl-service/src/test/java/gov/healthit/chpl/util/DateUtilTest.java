package gov.healthit.chpl.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;

import org.junit.Test;

public class DateUtilTest {

    @Test
    public void convertEasternLocalDateTimeToUtc_hasCorrectValue() {
        LocalDateTime input = LocalDateTime.parse("2022-04-06T11:20:00");
        LocalDateTime output = DateUtil.fromEasternToUtc(input);
        assertEquals(output.toString(), "2022-04-06T15:20");
    }

    @Test
    public void convertUtcLocalDateTimeToEastern_hasCorrectValue() {
        LocalDateTime input = LocalDateTime.parse("2022-04-06T15:20:00");
        LocalDateTime output = DateUtil.fromUtcToEastern(input);
        assertEquals(output.toString(), "2022-04-06T11:20");
    }
}
