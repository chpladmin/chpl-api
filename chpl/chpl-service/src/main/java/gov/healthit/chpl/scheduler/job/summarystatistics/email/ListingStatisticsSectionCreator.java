package gov.healthit.chpl.scheduler.job.summarystatistics.email;

import java.util.List;

import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.scheduler.job.summarystatistics.StatisticsMassager;
import gov.healthit.chpl.scheduler.job.summarystatistics.data.StatisticsSnapshot;

public class ListingStatisticsSectionCreator extends StatisticsSectionCreator {
    private CertificationStatusIdHelper statusIdHelper;

    public ListingStatisticsSectionCreator(CertificationStatusIdHelper statusIdHelper) {
        super();
        this.statusIdHelper = statusIdHelper;
    }

    public String build(StatisticsSnapshot stats, List<CertificationBody> activeAcbs) {
        return buildListingSection(stats, new StatisticsMassager(activeAcbs));
    }

    private String buildListingSection(StatisticsSnapshot stats, StatisticsMassager massager) {
        StringBuilder section = new StringBuilder();
        section.append(buildHeader("Total # of Listings (2015 Edition to Present)",
                stats.getListingCountForStatuses(statusIdHelper.getNonRetiredStatusIds())));
        section.append("<ul>");

        section.append(buildSection(
                "Total # of Active (Including Suspended) Listings",
                stats.getListingCountForStatuses(statusIdHelper.getActiveAndSuspendedStatusIds()),
                massager.getStatistics(stats.getListingCountForStatusesByAcb(statusIdHelper.getActiveAndSuspendedStatusIds()))));

        section.append(buildSection(
                "Total # of Suspended Listings",
                stats.getListingCountForStatuses(statusIdHelper.getSuspendedStatusIds()),
                massager.getStatistics(stats.getListingCountForStatusesByAcb(statusIdHelper.getSuspendedStatusIds()))));

        section.append(buildSection(
                "Total # of Withdrawn by Developer Listings",
                stats.getListingCountForStatuses(statusIdHelper.getWithdrawnByDeveloperStatusIds()),
                massager.getStatistics(stats.getListingCountForStatusesByAcb(statusIdHelper.getWithdrawnByDeveloperStatusIds()))));

        section.append("</ul>");
        return section.toString();
    }
}
