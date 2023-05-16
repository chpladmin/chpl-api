package gov.healthit.chpl.scheduler.job.urluptime;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UrlUptimeReport {
    private String description;
    private String url;
    private Long testCount;
    private Long successfulTestCount;

    public List<String> toListOfStrings() {
        return List.of(
                description,
                url,
                testCount.toString(),
                successfulTestCount.toString());
    }

    public static List<String> getHeaders() {
        return List.of("Monitor Description", "URL", "Total Tests", "Successful Tests");
    }
}