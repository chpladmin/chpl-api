package gov.healthit.chpl.scheduler.job.summarystatistics.email;

import java.util.List;

import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.scheduler.job.summarystatistics.StatisticsMassager;
import gov.healthit.chpl.scheduler.job.summarystatistics.data.StatisticsSnapshot;

public class ProductStatisticsSectionCreator extends StatisticsSectionCreator {
    private CertificationStatusIdHelper statusIdHelper;

    public ProductStatisticsSectionCreator(CertificationStatusIdHelper statusIdHelper) {
        super();
        this.statusIdHelper = statusIdHelper;
    }

    public String build(StatisticsSnapshot stats, List<CertificationBody> activeAcbs) {
        return buildUniqueProductSection(stats, new StatisticsMassager(activeAcbs));
    }

    private String buildUniqueProductSection(StatisticsSnapshot stats, StatisticsMassager massager) {
        StringBuilder section = new StringBuilder();
        section.append(buildHeader("Total # of Certified Unique Products (2015 Edition to Present)",
                stats.getProductCountForStatuses(statusIdHelper.getNonRetiredStatusIds())));
        section.append("<i>The sum of the ONC-ACB breakdown may not match the total since a product may be associated to more than one ONC-ACB</i>");
        section.append("<ul>");

        section.append(buildSection(
                "Total # of Unique Products with Active (Including Suspended) Listings",
                stats.getProductCountForStatuses(statusIdHelper.getActiveAndSuspendedStatusIds()),
                massager.getStatistics(stats.getProductCountForStatusesByAcb(statusIdHelper.getActiveAndSuspendedStatusIds()))));

        section.append(buildSection(
                "Total # of Unique Products with Suspended Listings",
                stats.getProductCountForStatuses(statusIdHelper.getSuspendedStatusIds()),
                massager.getStatistics(stats.getProductCountForStatusesByAcb(statusIdHelper.getSuspendedStatusIds()))));

        section.append(buildSection(
                "Total # of Unique Products with Withdrawn by Developer Listings",
                stats.getProductCountForStatuses(statusIdHelper.getWithdrawnByDeveloperStatusIds()),
                massager.getStatistics(stats.getProductCountForStatusesByAcb(statusIdHelper.getWithdrawnByDeveloperStatusIds()))));

        section.append("</ul>");
        return section.toString();
    }
}
