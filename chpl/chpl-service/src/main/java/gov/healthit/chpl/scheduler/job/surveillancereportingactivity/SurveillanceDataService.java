package gov.healthit.chpl.scheduler.job.surveillancereportingactivity;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public final class SurveillanceDataService {
    private SurveillanceDataService() {}

    public static List<SurveillanceData> getDataForAcb(List<SurveillanceData> surveillances, String acbName) {
        List<SurveillanceData> filteredSurveillances = surveillances.stream()
                .filter(surv -> surv.getAcbName().equalsIgnoreCase(acbName))
                .collect(Collectors.toList());
        if (filteredSurveillances != null) {
            return filteredSurveillances;
        } else {
            return new ArrayList<SurveillanceData>();
        }
    }
}
