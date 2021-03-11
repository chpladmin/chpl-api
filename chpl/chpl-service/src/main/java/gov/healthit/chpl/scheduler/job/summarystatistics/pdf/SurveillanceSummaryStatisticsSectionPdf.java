package gov.healthit.chpl.scheduler.job.summarystatistics.pdf;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.itextpdf.layout.element.Table;

import gov.healthit.chpl.dao.CertificationBodyDAO;
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
        table = addTableRow(table, createDataForRow("4. Total # of Surveillance Activities",
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

        return table;
    }
}
