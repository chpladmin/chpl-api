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
public class ListingSummaryStatisticsSectionPdf extends SummaryStatisticsSectionPdf {
    private CertificationStatusIdHelper statusIdHelper;

    @Autowired
    public ListingSummaryStatisticsSectionPdf(CertificationBodyDAO certificationBodyDAO,
            CertificationStatusDAO certificationStatusDao) {
        super(certificationBodyDAO);
        this.statusIdHelper = new CertificationStatusIdHelper(certificationStatusDao);
    }

    @Override
    public Table generateTable(LocalDate currSnapshotDate, LocalDate prevSnapshotDate, StatisticsSnapshot currSnapshot, StatisticsSnapshot prevSnapshot) {
        Table table = new Table(getRelativeColumnWidths());
        table.useAllAvailableWidth();
        table = addHeaders(table, "Listing Statistics", currSnapshotDate, prevSnapshotDate);
        table = addTableRow(table, createDataForRow("1. Total # of Listings (2015 Edition to Present)",
                    currSnapshot == null ? null : currSnapshot.getListingCountForStatuses(statusIdHelper.getNonRetiredStatusIds()),
                    prevSnapshot == null ? null : prevSnapshot.getListingCountForStatuses(statusIdHelper.getNonRetiredStatusIds())),
                    true);

        table = addTableRow(table, createDataForRow("a. Total # of Active (Including Suspended) Listings",
                    currSnapshot == null ? null : currSnapshot.getListingCountForStatuses(statusIdHelper.getActiveAndSuspendedStatusIds()),
                    prevSnapshot == null ? null : prevSnapshot.getListingCountForStatuses(statusIdHelper.getActiveAndSuspendedStatusIds())),
                    NUMBER_OF_INDENTS_SUMMARY_LEVEL_STAT,
                    true);

        table = addAcbRows(table,
                    currSnapshot == null ? null : currSnapshot.getListingCountForStatusesByAcb(statusIdHelper.getActiveAndSuspendedStatusIds()),
                    prevSnapshot == null ? null : prevSnapshot.getListingCountForStatusesByAcb(statusIdHelper.getActiveAndSuspendedStatusIds()));

        table = addTableRow(table, createDataForRow("b. Total # of Suspended Listings",
                    currSnapshot == null ? null : currSnapshot.getListingCountForStatuses(statusIdHelper.getSuspendedStatusIds()),
                    prevSnapshot == null ? null : prevSnapshot.getListingCountForStatuses(statusIdHelper.getSuspendedStatusIds())),
                    NUMBER_OF_INDENTS_SUMMARY_LEVEL_STAT,
                    true);

        table = addAcbRows(table,
                    currSnapshot == null ? null : currSnapshot.getListingCountForStatusesByAcb(statusIdHelper.getSuspendedStatusIds()),
                    prevSnapshot == null ? null : prevSnapshot.getListingCountForStatusesByAcb(statusIdHelper.getSuspendedStatusIds()));

        table = addTableRow(table, createDataForRow("c. Total # of Withdrawn by Developer Listings",
                    currSnapshot == null ? null : currSnapshot.getListingCountForStatuses(statusIdHelper.getWithdrawnByDeveloperStatusIds()),
                    prevSnapshot == null ? null : prevSnapshot.getListingCountForStatuses(statusIdHelper.getWithdrawnByDeveloperStatusIds())),
                    NUMBER_OF_INDENTS_SUMMARY_LEVEL_STAT,
                    true);

        table = addAcbRows(table,
                    currSnapshot == null ? null : currSnapshot.getListingCountForStatusesByAcb(statusIdHelper.getWithdrawnByDeveloperStatusIds()),
                    prevSnapshot == null ? null : prevSnapshot.getListingCountForStatusesByAcb(statusIdHelper.getWithdrawnByDeveloperStatusIds()));

        return table;
    }
}
