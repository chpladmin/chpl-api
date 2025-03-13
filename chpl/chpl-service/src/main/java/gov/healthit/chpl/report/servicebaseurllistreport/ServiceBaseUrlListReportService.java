package gov.healthit.chpl.report.servicebaseurllistreport;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.developer.search.DeveloperSearchService;
import gov.healthit.chpl.domain.IdNamePair;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.manager.CertificationBodyManager;
import gov.healthit.chpl.scheduler.job.urluptime.UrlUptimeMonitor;
import gov.healthit.chpl.scheduler.job.urluptime.UrlUptimeMonitorDAO;
import gov.healthit.chpl.scheduler.job.urluptime.UrlUptimeMonitorTestDAO;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class ServiceBaseUrlListReportService {
    private UrlUptimeMonitorDAO urlUptimeMonitorDAO;
    private UrlUptimeMonitorTestDAO urlUptimeMonitorTestDAO;
    private DeveloperSearchService developerSearchService;
    private CertificationBodyManager certificationBodyManager;

    @Autowired
    public ServiceBaseUrlListReportService(UrlUptimeMonitorDAO urlUptimeMonitorDAO, UrlUptimeMonitorTestDAO urlUptimeMonitorTestDAO, DeveloperSearchService developerSearchService,
            CertificationBodyManager certificationBodyManager) {
        this.urlUptimeMonitorDAO = urlUptimeMonitorDAO;
        this.urlUptimeMonitorTestDAO = urlUptimeMonitorTestDAO;
        this.developerSearchService = developerSearchService;
        this.certificationBodyManager = certificationBodyManager;
    }

    public List<UrlUptimeMonitorEx> getUrlUptimeMonitors() {
        return (List<UrlUptimeMonitorEx>) urlUptimeMonitorDAO.getAll().stream()
                .map(monitor -> UrlUptimeMonitorEx.builder()
                        .id(monitor.getId())
                        .developer(monitor.getDeveloper())
                        .url(monitor.getUrl())
                        .datadogPublicId(monitor.getDatadogPublicId())
                        .acbs(getAssocatedAcbs(monitor))
                        .tests(urlUptimeMonitorTestDAO.getChplUptimeMonitorTests(monitor.getId()).stream()
                                .filter(test -> test.getCheckTime().isAfter(LocalDateTime.now().minusYears(1)))
                                .toList())
                        .build())
                .toList();
    }

    private List<IdNamePair> getAssocatedAcbs(UrlUptimeMonitor monitor) {
        return Arrays.asList(monitor.getDelimitedAcbIds().split(",")).stream()
                .map(acbId -> Long.parseLong(acbId))
                .map(acbId -> {
                    try {
                        return certificationBodyManager.getById(acbId);
                    } catch (EntityRetrievalException e) {
                        return null;
                    }
                })
                .filter(acb -> acb != null && !acb.isRetired())
                .map(acb -> new IdNamePair(acb.getId(), acb.getName()))
                .toList();
    }
}
