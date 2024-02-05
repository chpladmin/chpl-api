package gov.healthit.chpl.scheduler.job.summarystatistics.pdf;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.itextpdf.layout.element.Table;

import gov.healthit.chpl.dao.CertificationBodyDAO;
import gov.healthit.chpl.scheduler.job.summarystatistics.data.CertificationBodyStatistic;
import gov.healthit.chpl.scheduler.job.summarystatistics.data.StatisticsSnapshot;

@Component
public class SurveillanceSummaryStatisticsSectionPdf extends SummaryStatisticsSectionPdf {

    @Autowired
    public SurveillanceSummaryStatisticsSectionPdf(CertificationBodyDAO certificationBodyDAO) {
        super(certificationBodyDAO);
    }

    @Override
    public Table generateTable(LocalDate currSnapshotDate, LocalDate prevSnapshotDate, StatisticsSnapshot currSnapshot, StatisticsSnapshot prevSnapshot) {
        Table table = new Table(getRelativeColumnWidths());
        table.useAllAvailableWidth();
        table = addHeaders(table, "Surveillance Statistics", currSnapshotDate, prevSnapshotDate);
        table = addTableRow(table, createDataForRow("1. Total # of Surveillance Activities",
                    currSnapshot == null ? null : currSnapshot.getSurveillanceAllStatusTotal(),
                    prevSnapshot == null ? null : prevSnapshot.getSurveillanceAllStatusTotal()),
                    true);

        table = addTableRow(table, createDataForRow("a. Open Surveillance Activities",
                    currSnapshot == null ? null : currSnapshot.getSurveillanceOpenStatus().getCount(),
                    prevSnapshot == null ? null : prevSnapshot.getSurveillanceOpenStatus().getCount()),
                    NUMBER_OF_INDENTS_SUMMARY_LEVEL_STAT,
                    true);

        table = addAcbRows(table,
                    currSnapshot == null ? null : currSnapshot.getSurveillanceOpenStatus().getAcbStatistics(),
                    prevSnapshot == null ? null : prevSnapshot.getSurveillanceOpenStatus().getAcbStatistics());

        table = addTableRow(table, createDataForRow("b. Closed Surveillance Activities",
                    currSnapshot == null ? null : currSnapshot.getSurveillanceClosedStatusTotal(),
                    prevSnapshot == null ? null : prevSnapshot.getSurveillanceClosedStatusTotal()),
                    NUMBER_OF_INDENTS_SUMMARY_LEVEL_STAT,
                    true);

        table = addTableRow(table, createDataForRow("c. Average Duration of Closed Surveillance (in days)",
                    currSnapshot == null ? null : currSnapshot.getSurveillanceAvgTimeToClose(),
                    prevSnapshot == null ? null : prevSnapshot.getSurveillanceAvgTimeToClose()),
                    NUMBER_OF_INDENTS_SUMMARY_LEVEL_STAT,
                    true);

        table = addTableRow(table, createDataForRow("2. Total # of Surveillance Non-conformities",
                    currSnapshot == null ? null : currSnapshot.getNonConfStatusAllTotal(),
                    prevSnapshot == null ? null : prevSnapshot.getNonConfStatusAllTotal()),
                true);

        table = addTableRow(table, createDataForRow("a. Open NCs",
                    currSnapshot == null ? null : currSnapshot.getNonConfStatusOpen().getCount(),
                    prevSnapshot == null ? null : prevSnapshot.getNonConfStatusOpen().getCount()),
                    NUMBER_OF_INDENTS_SUMMARY_LEVEL_STAT,
                    true);

        table = addAcbRows(table,
                    currSnapshot == null ? null : currSnapshot.getNonConfStatusOpen().getAcbStatistics(),
                    prevSnapshot == null ? null : prevSnapshot.getNonConfStatusOpen().getAcbStatistics());

        table = addTableRow(table, createDataForRow("b. Closed NCs",
                    currSnapshot == null ? null : currSnapshot.getNonConfStatusClosedTotal(),
                    prevSnapshot == null ? null : prevSnapshot.getNonConfStatusClosedTotal()),
                    NUMBER_OF_INDENTS_SUMMARY_LEVEL_STAT,
                    true);

        table = addTableRow(table, createDataForRow("c. Average Time to Assess Conformity (in days)",
                    currSnapshot == null ? null : currSnapshot.getNonConfAvgTimeToAssessConformity(),
                    prevSnapshot == null ? null : prevSnapshot.getNonConfAvgTimeToAssessConformity()),
                    NUMBER_OF_INDENTS_SUMMARY_LEVEL_STAT,
                    true);

        table = addTableRow(table, createDataForRow("d. Average Time to Approve CAP (in days)",
                    currSnapshot == null ? null : currSnapshot.getNonConfAvgTimeToApproveCAP(),
                    prevSnapshot == null ? null : prevSnapshot.getNonConfAvgTimeToApproveCAP()),
                    NUMBER_OF_INDENTS_SUMMARY_LEVEL_STAT,
                    true);

        table = addTableRow(table, createDataForRow("e. Average Duration of CAP (in days) (includes closed and ongoing CAPs)",
                    currSnapshot == null ? null : currSnapshot.getNonConfAvgDurationOfCAP(),
                    prevSnapshot == null ? null : prevSnapshot.getNonConfAvgDurationOfCAP()),
                    NUMBER_OF_INDENTS_SUMMARY_LEVEL_STAT,
                    true);

        table = addTableRow(table, createDataForRow("f. Average Time from CAP Approval to Surveillance Close (in days)",
                    currSnapshot == null ? null : currSnapshot.getNonConfAvgTimeFromCAPAprrovalToSurveillanceEnd(),
                    prevSnapshot == null ? null : prevSnapshot.getNonConfAvgTimeFromCAPAprrovalToSurveillanceEnd()),
                    NUMBER_OF_INDENTS_SUMMARY_LEVEL_STAT,
                    true);

        table = addTableRow(table, createDataForRow("g. Average Time from CAP Close to Surveillance Close (in days)",
                    currSnapshot == null ? null : currSnapshot.getNonConfAvgTimeFromCAPEndToSurveillanceEnd(),
                    prevSnapshot == null ? null : prevSnapshot.getNonConfAvgTimeFromCAPEndToSurveillanceEnd()),
                    NUMBER_OF_INDENTS_SUMMARY_LEVEL_STAT,
                    true);

        table = addTableRow(table, createDataForRow("h. Average Duration of Closed Non-Conformities (in days)",
                    currSnapshot == null ? null : currSnapshot.getNonConfAvgTimeFromSurveillanceOpenToSurveillanceClose(),
                    prevSnapshot == null ? null : prevSnapshot.getNonConfAvgTimeFromSurveillanceOpenToSurveillanceClose()),
                    NUMBER_OF_INDENTS_SUMMARY_LEVEL_STAT,
                    true);

        // These are calculated differently than all of the other rows.  The data is a little different
        table = addTableRow(table, createDataForRow("3. Total Number of CAPs",
                    currSnapshot == null ? null : sumAcbStatistics(currSnapshot.getNonConfCAPStatusOpen()) + sumAcbStatistics(currSnapshot.getNonConfCAPStatusClosed()),
                    prevSnapshot == null ? null : sumAcbStatistics(prevSnapshot.getNonConfCAPStatusOpen()) + sumAcbStatistics(prevSnapshot.getNonConfCAPStatusClosed())),
                    true);

        table = addTableRow(table, createDataForRow("a. Number of Open CAPs",
                    currSnapshot == null ? null : sumAcbStatistics(currSnapshot.getNonConfCAPStatusOpen()),
                    prevSnapshot == null ? null : sumAcbStatistics(prevSnapshot.getNonConfCAPStatusOpen())),
                    NUMBER_OF_INDENTS_SUMMARY_LEVEL_STAT,
                    true);

        table = addAcbRows(table,
                    currSnapshot == null ? null : currSnapshot.getNonConfCAPStatusOpen(),
                    prevSnapshot == null ? null : prevSnapshot.getNonConfCAPStatusOpen());

        table = addTableRow(table, createDataForRow("b. Number of Closed CAPs",
                    currSnapshot == null ? null : sumAcbStatistics(currSnapshot.getNonConfCAPStatusClosed()),
                    prevSnapshot == null ? null : sumAcbStatistics(prevSnapshot.getNonConfCAPStatusClosed())),
                    NUMBER_OF_INDENTS_SUMMARY_LEVEL_STAT,
                    true);

        table = addAcbRows(table,
                    currSnapshot == null ? null : currSnapshot.getNonConfCAPStatusClosed(),
                    prevSnapshot == null ? null : prevSnapshot.getNonConfCAPStatusClosed());

        return table;
    }

    private Long sumAcbStatistics(List<CertificationBodyStatistic> stats) {
        return stats.stream()
            .collect(Collectors.summarizingLong(CertificationBodyStatistic::getCount))
            .getSum();
    }
}
