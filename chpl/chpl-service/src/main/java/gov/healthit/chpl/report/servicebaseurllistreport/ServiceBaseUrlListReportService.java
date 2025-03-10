package gov.healthit.chpl.report.servicebaseurllistreport;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.developer.search.DeveloperSearchRequest;
import gov.healthit.chpl.developer.search.DeveloperSearchService;
import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.domain.IdNamePair;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.manager.CertificationBodyManager;
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
        List<UrlUptimeMonitorEx> urlUptimeMonitors = (List<UrlUptimeMonitorEx>) urlUptimeMonitorDAO.getAll().stream()
                .map(monitor -> UrlUptimeMonitorEx.builder()
                        .id(monitor.getId())
                        .developer(monitor.getDeveloper())
                        .url(monitor.getUrl())
                        .datadogPublicId(monitor.getDatadogPublicId())
                        .build())
                .toList();


        urlUptimeMonitors.forEach(monitor -> {
            monitor.setTests(urlUptimeMonitorTestDAO.getChplUptimeMonitorTests(monitor.getId()).stream()
                    .filter(test -> test.getCheckTime().isAfter(LocalDateTime.now().minusYears(1)))
                    .toList());
        });

        return urlUptimeMonitors;
    }

    private Set<IdNamePair> getAssocatedAcbs(Developer developer) {
        DeveloperSearchRequest request = DeveloperSearchRequest.builder()
                .developerIds(Set.of(developer.getId()))
                .build();

        return developerSearchService.getAllPagesOfSearchResults(request, LOGGER).stream()
                .findFirst()
                .map(res -> res.getAcbsForActiveListings().stream()
                        .filter(acb -> isAcbActive(acb.getId()))
                        .collect(Collectors.toSet()))
                .orElse(Set.of());
    }

    private Boolean isAcbActive(Long acbId) {
        try {
            return !certificationBodyManager.getById(acbId).isRetired();
        } catch (EntityRetrievalException e) {
            LOGGER.error("Could not determine is ACB {} is retired.", acbId);
            return false;
        }
    }
}
