package gov.healthit.chpl.web.controller.results;

import java.time.LocalDateTime;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import gov.healthit.chpl.util.SystemToEasternLocalDateTimeReportSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimestampResult {

    @JsonSerialize(using = SystemToEasternLocalDateTimeReportSerializer.class)
    private LocalDateTime localDateTime;
}
