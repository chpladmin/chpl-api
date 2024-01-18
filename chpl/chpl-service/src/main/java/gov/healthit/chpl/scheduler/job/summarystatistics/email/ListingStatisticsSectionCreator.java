package gov.healthit.chpl.scheduler.job.summarystatistics.email;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import gov.healthit.chpl.dao.CertificationStatusDAO;
import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.domain.CertificationStatus;
import gov.healthit.chpl.entity.CertificationStatusType;
import gov.healthit.chpl.scheduler.job.summarystatistics.StatisticsMassager;
import gov.healthit.chpl.scheduler.job.summarystatistics.data.StatisticsSnapshot;
import gov.healthit.chpl.util.CertificationStatusUtil;

public class ListingStatisticsSectionCreator extends StatisticsSectionCreator {
    private List<Long> nonRetiredStatusIds, activeAndSuspendedStatusIds, suspendedStatusIds,
        withdrawnByDeveloperStatusIds;
    private List<CertificationStatus> certificationStatuses;

    public ListingStatisticsSectionCreator(CertificationStatusDAO certificationStatusDao) {
        super();
        certificationStatuses = certificationStatusDao.findAll();
        nonRetiredStatusIds = CertificationStatusUtil.getNonretiredStatuses().stream()
                .map(status -> getStatusId(status))
                .collect(Collectors.toList());
        activeAndSuspendedStatusIds = CertificationStatusUtil.getActiveStatuses().stream()
                .map(status -> getStatusId(status))
                .collect(Collectors.toList());
        suspendedStatusIds = CertificationStatusUtil.getSuspendedStatuses().stream()
                .map(status -> getStatusId(status))
                .collect(Collectors.toList());
        withdrawnByDeveloperStatusIds = Stream.of(CertificationStatusType.WithdrawnByDeveloper)
                .map(status -> getStatusId(status))
                .collect(Collectors.toList());
    }

    public String build(StatisticsSnapshot stats, List<CertificationBody> activeAcbs) {
        return buildListingSection(stats, new StatisticsMassager(activeAcbs));
    }

    private String buildListingSection(StatisticsSnapshot stats, StatisticsMassager massager) {
        StringBuilder section = new StringBuilder();
        section.append(buildHeader("Total # of Listings (2015 Edition to Present)",
                stats.getListingCountForStatuses(nonRetiredStatusIds)));
        section.append("<ul>");

        section.append(buildSection(
                "Total # of Active (Including Suspended) Listings",
                stats.getListingCountForStatuses(activeAndSuspendedStatusIds),
                massager.getStatistics(stats.getListingCountForStatusesByAcb(activeAndSuspendedStatusIds))));

        section.append(buildSection(
                "Total # of Suspended Listings",
                stats.getListingCountForStatuses(suspendedStatusIds),
                massager.getStatistics(stats.getListingCountForStatusesByAcb(suspendedStatusIds))));

        section.append(buildSection(
                "Total # of Withdrawn by Developer Listings",
                stats.getListingCountForStatuses(withdrawnByDeveloperStatusIds),
                massager.getStatistics(stats.getListingCountForStatusesByAcb(withdrawnByDeveloperStatusIds))));

        section.append("</ul>");
        return section.toString();
    }

    private Long getStatusId(CertificationStatusType statusType) {
        return certificationStatuses.stream()
            .filter(status -> status.getName().equalsIgnoreCase(statusType.getName()))
            .findAny().get().getId();
    }
}
