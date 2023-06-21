package gov.healthit.chpl.scheduler.job.urluptime;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ServiceBasedUrlUptimeReport {
    private String developerName;
    private Long developerId;
    private String url;
    private Long totalTestCount;
    private Long totalSuccessfulTestCount;
    private Long currentMonthTestCount;
    private Long currentMonthSuccessfulTestCount;
    private Long pastWeekTestCount;
    private Long pastWeekSuccessfulTestCount;

    public List<String> toListOfStrings() {
        return List.of(
                developerName,
                developerId.toString(),
                url,
                totalTestCount.toString(),
                totalSuccessfulTestCount.toString(),
                currentMonthTestCount.toString(),
                currentMonthSuccessfulTestCount.toString(),
                pastWeekTestCount.toString(),
                pastWeekSuccessfulTestCount.toString());

    }

    public static List<String> getHeaders() {
        return List.of("Developer",
                "Developer Id",
                "URL",
                "All Time Total Tests",
                "All Time Successful Tests",
                "Current Month Days Total Tests",
                "Current Month Days Successful Tests",
                "Past 7 Days Total Tests",
                "Past 7 Days Successful Tests");
    }
}