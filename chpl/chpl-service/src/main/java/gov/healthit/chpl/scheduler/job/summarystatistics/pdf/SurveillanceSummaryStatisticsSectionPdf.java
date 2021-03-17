package gov.healthit.chpl.scheduler.job.summarystatistics.pdf;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.itextpdf.layout.element.Table;

import gov.healthit.chpl.dao.CertificationBodyDAO;
import gov.healthit.chpl.scheduler.job.summarystatistics.data.EmailCertificationBodyStatistic;
import gov.healthit.chpl.scheduler.job.summarystatistics.data.EmailStatistics;

@Component
public class SurveillanceSummaryStatisticsSectionPdf extends SummaryStatisticsSectionPdf {

    @Autowired
    public SurveillanceSummaryStatisticsSectionPdf(CertificationBodyDAO certificationBodyDAO) {
        super(certificationBodyDAO);
    }

    @Override
    public Table generateTable(LocalDate recent, LocalDate previous, EmailStatistics recentEmailStatistics, EmailStatistics previousEmailStatistics) {
        Table table = new Table(getRelativeColumnWidths());
        table.useAllAvailableWidth();
        table = addHeaders(table, "Surveillance Statistics", recent, previous);
        table = addTableRow(table, createDataForRow("1. Total # of Surveillance Activities",
                                    recentEmailStatistics.getSurveillanceAllStatusTotal(),
                                    previousEmailStatistics.getSurveillanceAllStatusTotal()), true);

        table = addTableRow(table, createDataForRow("a. Open Surveillance Activities",
                recentEmailStatistics.getSurveillanceOpenStatus().getCount(),
                previousEmailStatistics.getSurveillanceOpenStatus().getCount()),
                NUMBER_OF_INDENTS_SUMMARY_LEVEL_STAT, true);

        table = addAcbRows(table,
                recentEmailStatistics.getSurveillanceOpenStatus().getAcbStatistics(),
                previousEmailStatistics.getSurveillanceOpenStatus().getAcbStatistics());

        table = addTableRow(table, createDataForRow("b. Closed Surveillance Activities",
                recentEmailStatistics.getSurveillanceClosedStatusTotal(),
                previousEmailStatistics.getSurveillanceClosedStatusTotal()),
                NUMBER_OF_INDENTS_SUMMARY_LEVEL_STAT, true);

        table = addTableRow(table, createDataForRow("c. Average Duration of Closed Surveillance (in days)",
                recentEmailStatistics.getSurveillanceAvgTimeToClose(),
                previousEmailStatistics.getSurveillanceAvgTimeToClose()),
                NUMBER_OF_INDENTS_SUMMARY_LEVEL_STAT, true);

        table = addTableRow(table, createDataForRow("2. Total # of NCs",
                recentEmailStatistics.getNonconfStatusAllTotal(),
                previousEmailStatistics.getNonconfStatusAllTotal()), true);

        table = addTableRow(table, createDataForRow("a. Open NCs",
                recentEmailStatistics.getNonconfStatusOpen().getCount(),
                previousEmailStatistics.getNonconfStatusOpen().getCount()),
                NUMBER_OF_INDENTS_SUMMARY_LEVEL_STAT, true);

        table = addAcbRows(table,
                recentEmailStatistics.getNonconfStatusOpen().getAcbStatistics(),
                previousEmailStatistics.getNonconfStatusOpen().getAcbStatistics());

        table = addTableRow(table, createDataForRow("b. Closed NCs",
                recentEmailStatistics.getNonconfStatusClosedTotal(),
                previousEmailStatistics.getNonconfStatusClosedTotal()),
                NUMBER_OF_INDENTS_SUMMARY_LEVEL_STAT, true);

        table = addTableRow(table, createDataForRow("c. Average Time to Assess Conformity (in days)",
                recentEmailStatistics.getNonconfAvgTimeToAssessConformity(),
                previousEmailStatistics.getNonconfAvgTimeToAssessConformity()),
                NUMBER_OF_INDENTS_SUMMARY_LEVEL_STAT, true);

        table = addTableRow(table, createDataForRow("d. Average Time to Approve CAP (in days)",
                recentEmailStatistics.getNonconfAvgTimeToApproveCAP(),
                previousEmailStatistics.getNonconfAvgTimeToApproveCAP()),
                NUMBER_OF_INDENTS_SUMMARY_LEVEL_STAT, true);

        table = addTableRow(table, createDataForRow("e. Average Duration of CAP (in days) (includes closed and ongoing CAPs)",
                recentEmailStatistics.getNonconfAvgDurationOfCAP(),
                previousEmailStatistics.getNonconfAvgDurationOfCAP()),
                NUMBER_OF_INDENTS_SUMMARY_LEVEL_STAT, true);

        table = addTableRow(table, createDataForRow("f. Average Time from CAP Approval to Surveillance Close (in days)",
                recentEmailStatistics.getNonconfAvgTimeFromCAPAprrovalToSurveillanceEnd(),
                previousEmailStatistics.getNonconfAvgTimeFromCAPAprrovalToSurveillanceEnd()),
                NUMBER_OF_INDENTS_SUMMARY_LEVEL_STAT, true);

        table = addTableRow(table, createDataForRow("g. Average Time from CAP Close to Surveillance Close (in days)",
                recentEmailStatistics.getNonconfAvgTimeFromCAPEndToSurveillanceEnd(),
                previousEmailStatistics.getNonconfAvgTimeFromCAPEndToSurveillanceEnd()),
                NUMBER_OF_INDENTS_SUMMARY_LEVEL_STAT, true);

        table = addTableRow(table, createDataForRow("h. Average Duration of Closed Non-Conformities (in days)",
                recentEmailStatistics.getNonconfAvgTimeFromSurveillanceOpenToSurveillanceClose(),
                previousEmailStatistics.getNonconfAvgTimeFromSurveillanceOpenToSurveillanceClose()),
                NUMBER_OF_INDENTS_SUMMARY_LEVEL_STAT, true);

        // These are calculated differently than all of the other rows.  The data is a little different
        table = addTableRow(table, createDataForRow("3. Total Number of CAPs",
                sumEmailAcbStatisticList(recentEmailStatistics.getNonconfCAPStatusOpen()) + sumEmailAcbStatisticList(recentEmailStatistics.getNonconfCAPStatusClosed()),
                sumEmailAcbStatisticList(previousEmailStatistics.getNonconfCAPStatusOpen()) + sumEmailAcbStatisticList(previousEmailStatistics.getNonconfCAPStatusClosed()))
                , true);

        table = addTableRow(table, createDataForRow("a. Number of Open CAPs",
                sumEmailAcbStatisticList(recentEmailStatistics.getNonconfCAPStatusOpen()),
                sumEmailAcbStatisticList(previousEmailStatistics.getNonconfCAPStatusOpen())),
                NUMBER_OF_INDENTS_SUMMARY_LEVEL_STAT, true);

        table = addAcbRows(table,
                recentEmailStatistics.getNonconfCAPStatusOpen(),
                previousEmailStatistics.getNonconfCAPStatusOpen());

        table = addTableRow(table, createDataForRow("b. Number of Closed CAPs",
                sumEmailAcbStatisticList(recentEmailStatistics.getNonconfCAPStatusClosed()),
                sumEmailAcbStatisticList(previousEmailStatistics.getNonconfCAPStatusClosed())),
                NUMBER_OF_INDENTS_SUMMARY_LEVEL_STAT, true);

        table = addAcbRows(table,
        recentEmailStatistics.getNonconfCAPStatusClosed(),
        previousEmailStatistics.getNonconfCAPStatusClosed());

        return table;
    }

    private Long sumEmailAcbStatisticList(List<EmailCertificationBodyStatistic> stats) {
        return stats.stream()
        .collect(Collectors.summarizingLong(EmailCertificationBodyStatistic::getCount))
        .getSum();
    }
}
