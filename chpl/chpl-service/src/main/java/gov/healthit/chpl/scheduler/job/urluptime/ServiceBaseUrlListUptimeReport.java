package gov.healthit.chpl.scheduler.job.urluptime;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.domain.auth.User;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ServiceBaseUrlListUptimeReport {
    private String developerName;
    private Long developerId;
    private List<User> developerUsers;
    private String url;
    private Long totalTestCount;
    private Long totalSuccessfulTestCount;
    private Long currentMonthTestCount;
    private Long currentMonthSuccessfulTestCount;
    private Long pastWeekTestCount;
    private Long pastWeekSuccessfulTestCount;
    private Map<Long, Boolean> applicableAcbsMap;

    public List<String> toListOfStrings(List<CertificationBody> activeAcbs) {
        List<String> stringifiedData = new ArrayList<String>(Arrays.asList(
                developerName,
                developerId.toString(),
                formatContacts(developerUsers),
                url,
                totalTestCount.toString(),
                totalSuccessfulTestCount.toString(),
                currentMonthTestCount.toString(),
                currentMonthSuccessfulTestCount.toString(),
                pastWeekTestCount.toString(),
                pastWeekSuccessfulTestCount.toString()));

        Collections.sort(activeAcbs, (acb1, acb2) -> acb1.getName().compareTo(acb2.getName()));
        activeAcbs.forEach(acb -> stringifiedData.add(applicableAcbsMap.get(acb.getId()).toString()));

        return stringifiedData;
    }

    public static List<String> getHeaders(List<CertificationBody> activeAcbs) {
        List<String> headers =  new ArrayList<String>(Arrays.asList("Developer",
                "Developer Id",
                "User Email Addresses",
                "URL",
                "All Time Total Tests",
                "All Time Successful Tests",
                "Current Month Days Total Tests",
                "Current Month Days Successful Tests",
                "Past 7 Days Total Tests",
                "Past 7 Days Successful Tests"));
        Collections.sort(activeAcbs, (acb1, acb2) -> acb1.getName().compareTo(acb2.getName()));
        activeAcbs.forEach(acb -> headers.add(acb.getName()));
        return headers;
    }

    private String formatContacts(List<User> users) {
        List<String> contactStrings = new ArrayList<String>();
        users.stream()
            .forEach(user -> contactStrings.add(user.getFullName() + " <" + user.getEmail() + ">"));
        return String.join("; ", contactStrings);
    }
}
