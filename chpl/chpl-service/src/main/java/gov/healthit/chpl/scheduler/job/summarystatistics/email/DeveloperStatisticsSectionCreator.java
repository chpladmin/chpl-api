package gov.healthit.chpl.scheduler.job.summarystatistics.email;

import java.util.List;

import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.scheduler.job.summarystatistics.StatisticsMassager;
import gov.healthit.chpl.scheduler.job.summarystatistics.data.StatisticsSnapshot;

public class DeveloperStatisticsSectionCreator extends StatisticsSectionCreator {
    private CertificationStatusIdHelper statusIdHelper;

    public DeveloperStatisticsSectionCreator(CertificationStatusIdHelper statusIdHelper) {
        super();
        this.statusIdHelper = statusIdHelper;
    }

    public String build(StatisticsSnapshot stats, List<CertificationBody> activeAcbs) {
        return buildUniqueDeveloperSection(stats, new StatisticsMassager(activeAcbs));
    }

    private String buildUniqueDeveloperSection(StatisticsSnapshot stats, StatisticsMassager massager) {
        StringBuilder section = new StringBuilder();

        section.append(buildHeader("Total # of Unique Developers (2015 Edition to Present)",
                stats.getDeveloperCountForStatuses(statusIdHelper.getNonRetiredStatusIds())));
        section.append("<i>The sum of the ONC-ACB breakdown may not match the total since a developer may be associated to more than one ONC-ACB</i>");
        section.append("<ul>");

        section.append(buildSection(
                "Total # of Developers with Active (Including Suspended) Listings",
                stats.getDeveloperCountForStatuses(statusIdHelper.getActiveAndSuspendedStatusIds()),
                massager.getStatistics(stats.getDeveloperCountForStatusesByAcb(statusIdHelper.getActiveAndSuspendedStatusIds()))));

        section.append(buildSection(
                "Total # of Developers with Suspended Listings",
                stats.getDeveloperCountForStatuses(statusIdHelper.getSuspendedStatusIds()),
                massager.getStatistics(stats.getDeveloperCountForStatusesByAcb(statusIdHelper.getSuspendedStatusIds()))));

        section.append(buildSection(
                "Total # of Developers with Withdrawn by Developer Listings",
                stats.getDeveloperCountForStatuses(statusIdHelper.getWithdrawnByDeveloperStatusIds()),
                massager.getStatistics(stats.getDeveloperCountForStatusesByAcb(statusIdHelper.getWithdrawnByDeveloperStatusIds()))));

        section.append("</ul>");
        return section.toString();
    }
}
