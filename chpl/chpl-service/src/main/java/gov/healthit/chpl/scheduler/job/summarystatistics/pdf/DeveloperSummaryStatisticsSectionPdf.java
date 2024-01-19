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
public class DeveloperSummaryStatisticsSectionPdf extends SummaryStatisticsSectionPdf {
    private CertificationStatusIdHelper statusIdHelper;

    @Autowired
    public DeveloperSummaryStatisticsSectionPdf(CertificationBodyDAO certificationBodyDAO,
            CertificationStatusDAO certificationStatusDao) {
        super(certificationBodyDAO);
        this.statusIdHelper = new CertificationStatusIdHelper(certificationStatusDao);
    }

    @Override
    public Table generateTable(LocalDate currSnapshotDate, LocalDate prevSnapshotDate, StatisticsSnapshot currSnapshot, StatisticsSnapshot prevSnapshot) {
        Table table = new Table(getRelativeColumnWidths());
        table.useAllAvailableWidth();
        table = addHeaders(table, "Developer Statistics", currSnapshotDate, prevSnapshotDate);
        table = addTableRow(table, createDataForRow("1. Total # of Unique Developers (2015 Edition to Present)",
                    currSnapshot == null ? null : currSnapshot.getDeveloperCountForStatuses(statusIdHelper.getNonRetiredStatusIds()),
                    prevSnapshot == null ? null : prevSnapshot.getDeveloperCountForStatuses(statusIdHelper.getNonRetiredStatusIds())),
                    true);

        table = addTableRow(table, createDataForRow("a. Total # of Developers with Active (Including Suspended) Listings*",
                    currSnapshot == null ? null : currSnapshot.getDeveloperCountForStatuses(statusIdHelper.getActiveAndSuspendedStatusIds()),
                    prevSnapshot == null ? null : prevSnapshot.getDeveloperCountForStatuses(statusIdHelper.getActiveAndSuspendedStatusIds())),
                    NUMBER_OF_INDENTS_SUMMARY_LEVEL_STAT,
                    true);

        table = addAcbRows(table,
                    currSnapshot == null ? null : currSnapshot.getDeveloperCountForStatusesByAcb(statusIdHelper.getActiveAndSuspendedStatusIds()),
                    prevSnapshot == null ? null : prevSnapshot.getDeveloperCountForStatusesByAcb(statusIdHelper.getActiveAndSuspendedStatusIds()));

        table = addTableRow(table, createDataForRow("b. Total # of Developers with Suspended Listings*",
                    currSnapshot == null ? null : currSnapshot.getDeveloperCountForStatuses(statusIdHelper.getSuspendedStatusIds()),
                    prevSnapshot == null ? null : prevSnapshot.getDeveloperCountForStatuses(statusIdHelper.getSuspendedStatusIds())),
                    NUMBER_OF_INDENTS_SUMMARY_LEVEL_STAT,
                    true);

        table = addAcbRows(table,
                    currSnapshot == null ? null : currSnapshot.getDeveloperCountForStatusesByAcb(statusIdHelper.getSuspendedStatusIds()),
                    prevSnapshot == null ? null : prevSnapshot.getDeveloperCountForStatusesByAcb(statusIdHelper.getSuspendedStatusIds()));

        table = addTableRow(table, createDataForRow("c. Total # of Developers with Withdrawn by Developer Listings*",
                    currSnapshot == null ? null : currSnapshot.getDeveloperCountForStatuses(statusIdHelper.getWithdrawnByDeveloperStatusIds()),
                    prevSnapshot == null ? null : prevSnapshot.getDeveloperCountForStatuses(statusIdHelper.getWithdrawnByDeveloperStatusIds())),
                    NUMBER_OF_INDENTS_SUMMARY_LEVEL_STAT,
                    true);

        table = addAcbRows(table,
                    currSnapshot == null ? null : currSnapshot.getDeveloperCountForStatusesByAcb(statusIdHelper.getWithdrawnByDeveloperStatusIds()),
                    prevSnapshot == null ? null : prevSnapshot.getDeveloperCountForStatusesByAcb(statusIdHelper.getWithdrawnByDeveloperStatusIds()));

        return table;
    }
}
