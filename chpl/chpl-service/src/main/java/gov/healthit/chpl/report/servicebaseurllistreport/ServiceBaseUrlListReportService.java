package gov.healthit.chpl.report.servicebaseurllistreport;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.CertificationBodyDAO;
import gov.healthit.chpl.dao.DeveloperDAO;
import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.domain.IdNamePair;
import gov.healthit.chpl.scheduler.job.urluptime.UrlUptimeMonitorDAO;
import gov.healthit.chpl.scheduler.job.urluptime.UrlUptimeMonitorTestDAO;

@Component
public class ServiceBaseUrlListReportService {
    private UrlUptimeMonitorDAO urlUptimeMonitorDAO;
    private UrlUptimeMonitorTestDAO urlUptimeMonitorTestDAO;
    private DeveloperDAO developerDAO;
    private CertificationBodyDAO certificationBodyDAO;

    @Autowired
    public ServiceBaseUrlListReportService(UrlUptimeMonitorDAO urlUptimeMonitorDAO, UrlUptimeMonitorTestDAO urlUptimeMonitorTestDAO, DeveloperDAO developerDAO,
            CertificationBodyDAO certificationBodyDAO) {
        this.urlUptimeMonitorDAO = urlUptimeMonitorDAO;
        this.urlUptimeMonitorTestDAO = urlUptimeMonitorTestDAO;
        this.developerDAO = developerDAO;
        this.certificationBodyDAO = certificationBodyDAO;
    }

    public List<UrlUptimeMonitorEx> getUrlUptimeMonitors() {
        List<UrlUptimeMonitorEx> urlUptimeMonitors = (List<UrlUptimeMonitorEx>) urlUptimeMonitorDAO.getAll().stream()
                .map(monitor -> UrlUptimeMonitorEx.builder()
                        .id(monitor.getId())
                        .developer(monitor.getDeveloper())
                        .url(monitor.getUrl())
                        .datadogPublicId(monitor.getDatadogPublicId())
                        .build())
                .toList();


        Map<Long, Set<CertificationBody>> developerAcbMaps = developerDAO.findAllDevelopersWithAcbs().entrySet().stream()
                .collect(Collectors.toMap(o -> o.getKey().getId(), o -> o.getValue()));
        List<CertificationBody> activeAcbs = certificationBodyDAO.findAllActive();

        urlUptimeMonitors.forEach(monitor -> {
            monitor.setTests(urlUptimeMonitorTestDAO.getChplUptimeMonitorTests(monitor.getId()).stream()
                    .filter(test -> test.getCheckTime().isAfter(LocalDateTime.now().minusYears(1)))
                    .toList());

            if (developerAcbMaps.containsKey(monitor.getDeveloper().getId())) {
                monitor.setAcbs(developerAcbMaps.get(monitor.getDeveloper().getId()).stream()
                        .filter(acb -> isAcbInActiveList(acb.getId(), activeAcbs))
                        .map(acb -> IdNamePair.builder()
                                .id(acb.getId())
                                .name(acb.getName())
                                .build())
                        .toList());
            }
        });

        return urlUptimeMonitors;
    }

    private Boolean isAcbInActiveList(Long acbId, List<CertificationBody> activeAcbs) {
        return activeAcbs.stream()
                .filter(acb -> acb.getId().equals(acbId))
                .findFirst()
                .isPresent();
    }
}
