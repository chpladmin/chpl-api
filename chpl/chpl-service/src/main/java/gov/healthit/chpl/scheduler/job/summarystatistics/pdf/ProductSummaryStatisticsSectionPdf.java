package gov.healthit.chpl.scheduler.job.summarystatistics.pdf;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.itextpdf.layout.element.Table;

import gov.healthit.chpl.dao.CertificationBodyDAO;
import gov.healthit.chpl.dao.CertificationStatusDAO;
import gov.healthit.chpl.scheduler.job.summarystatistics.data.StatisticsSnapshot;
import gov.healthit.chpl.scheduler.job.summarystatistics.email.CertificationStatusIdHelper;

@Component
public class ProductSummaryStatisticsSectionPdf extends SummaryStatisticsSectionPdf {
    private CertificationStatusIdHelper statusIdHelper;

    @Autowired
    public ProductSummaryStatisticsSectionPdf(CertificationBodyDAO certificationBodyDAO,
            CertificationStatusDAO certificationStatusDao) {
        super(certificationBodyDAO);
        this.statusIdHelper = new CertificationStatusIdHelper(certificationStatusDao);
    }

    @Override
    public Table generateTable(LocalDate currSnapshotDate, LocalDate prevSnapshotDate, StatisticsSnapshot currSnapshot, StatisticsSnapshot prevSnapshot) {
        Table table = new Table(getRelativeColumnWidths());
        table.useAllAvailableWidth();
        table = addHeaders(table, "Product Statistics", currSnapshotDate, prevSnapshotDate);
        table = addTableRow(table, createDataForRow("1. Total # of Certified Unique Products (2015 Edition to Present)",
                    currSnapshot == null ? null : currSnapshot.getProductCountForStatuses(statusIdHelper.getNonRetiredStatusIds()),
                    prevSnapshot == null ? null : prevSnapshot.getProductCountForStatuses(statusIdHelper.getNonRetiredStatusIds())),
                    true);

        table = addTableRow(table, createDataForRow("a. Total # of Unique Products with Active (Including Suspended) Listings*",
                    currSnapshot == null ? null : currSnapshot.getProductCountForStatuses(statusIdHelper.getActiveAndSuspendedStatusIds()),
                    prevSnapshot == null ? null : prevSnapshot.getProductCountForStatuses(statusIdHelper.getActiveAndSuspendedStatusIds())),
                    NUMBER_OF_INDENTS_SUMMARY_LEVEL_STAT,
                    true);

        table = addAcbRows(table,
                    currSnapshot == null ? null : currSnapshot.getProductCountForStatusesByAcb(statusIdHelper.getActiveAndSuspendedStatusIds()),
                    prevSnapshot == null ? null : prevSnapshot.getProductCountForStatusesByAcb(statusIdHelper.getActiveAndSuspendedStatusIds()));

        table = addTableRow(table, createDataForRow("b. Total # of Unique Products with Suspended Listings*",
                    currSnapshot == null ? null : currSnapshot.getProductCountForStatuses(statusIdHelper.getSuspendedStatusIds()),
                    prevSnapshot == null ? null : prevSnapshot.getProductCountForStatuses(statusIdHelper.getSuspendedStatusIds())),
                    NUMBER_OF_INDENTS_SUMMARY_LEVEL_STAT,
                    true);

        table = addAcbRows(table,
                    currSnapshot == null ? null : currSnapshot.getProductCountForStatusesByAcb(statusIdHelper.getSuspendedStatusIds()),
                    prevSnapshot == null ? null : prevSnapshot.getProductCountForStatusesByAcb(statusIdHelper.getSuspendedStatusIds()));

        table = addTableRow(table, createDataForRow("c. Total # of Unique Products with Withdrawn by Developer Listings*",
                    currSnapshot == null ? null : currSnapshot.getProductCountForStatuses(statusIdHelper.getWithdrawnByDeveloperStatusIds()),
                    prevSnapshot == null ? null : prevSnapshot.getProductCountForStatuses(statusIdHelper.getWithdrawnByDeveloperStatusIds())),
                    NUMBER_OF_INDENTS_SUMMARY_LEVEL_STAT,
                    true);

        table = addAcbRows(table,
                    currSnapshot == null ? null : currSnapshot.getProductCountForStatusesByAcb(statusIdHelper.getWithdrawnByDeveloperStatusIds()),
                    prevSnapshot == null ? null : prevSnapshot.getProductCountForStatusesByAcb(statusIdHelper.getWithdrawnByDeveloperStatusIds()));

        return table;
    }
}
