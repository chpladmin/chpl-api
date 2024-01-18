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

public class ProductStatisticsSectionCreator extends StatisticsSectionCreator {
    private List<Long> nonRetiredStatusIds, activeAndSuspendedStatusIds, suspendedStatusIds,
        withdrawnByDeveloperStatusIds;
    private List<CertificationStatus> certificationStatuses;

    public ProductStatisticsSectionCreator(CertificationStatusDAO certificationStatusDao) {
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
        return buildUniqueProductSection(stats, new StatisticsMassager(activeAcbs));
    }

    private String buildUniqueProductSection(StatisticsSnapshot stats, StatisticsMassager massager) {
        StringBuilder section = new StringBuilder();
        section.append(buildHeader("Total # of Certified Unique Products (2015 Edition to Present)",
                stats.getProductCountForStatuses(nonRetiredStatusIds)));
        section.append("<i>The sum of the ONC-ACB breakdown may not match the total since a product may be associated to more than one ONC-ACB</i>");
        section.append("<ul>");

        section.append(buildSection(
                "Total # of Unique Products with Active (Including Suspended) Listings",
                stats.getProductCountForStatuses(activeAndSuspendedStatusIds),
                massager.getStatistics(stats.getProductCountForStatusesByAcb(activeAndSuspendedStatusIds))));

        section.append(buildSection(
                "Total # of Unique Products with Suspended Listings",
                stats.getProductCountForStatuses(suspendedStatusIds),
                massager.getStatistics(stats.getProductCountForStatusesByAcb(suspendedStatusIds))));

        section.append(buildSection(
                "Total # of Unique Products with Withdrawn by Developer Listings",
                stats.getProductCountForStatuses(withdrawnByDeveloperStatusIds),
                massager.getStatistics(stats.getProductCountForStatusesByAcb(withdrawnByDeveloperStatusIds))));

        section.append("</ul>");
        return section.toString();
    }

    private Long getStatusId(CertificationStatusType statusType) {
        return certificationStatuses.stream()
            .filter(status -> status.getName().equalsIgnoreCase(statusType.getName()))
            .findAny().get().getId();
    }
}
