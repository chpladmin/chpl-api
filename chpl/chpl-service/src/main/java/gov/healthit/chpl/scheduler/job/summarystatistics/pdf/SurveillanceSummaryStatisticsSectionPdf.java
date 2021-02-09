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
                                    previousEmailStatistics.getSurveillanceAllStatusTotal()));

        table = addTableRow(table, createDataForRow("a. Open Surveillance Activities",
                recentEmailStatistics.getSurveillanceOpenStatus().getCount(),
                previousEmailStatistics.getSurveillanceOpenStatus().getCount()), 1);

        table = addAcbRows(table,
                recentEmailStatistics.getSurveillanceOpenStatus().getAcbStatistics(),
                previousEmailStatistics.getSurveillanceOpenStatus().getAcbStatistics());

        table = addTableRow(table, createDataForRow("b. Closed Surveillance Activities",
                recentEmailStatistics.getSurveillanceClosedStatusTotal(),
                previousEmailStatistics.getSurveillanceClosedStatusTotal()), 1);

        table = addTableRow(table, createDataForRow("c. Average Duration of Closed Surveillance (in days)",
                recentEmailStatistics.getSurveillanceAvgTimeToClose(),
                previousEmailStatistics.getSurveillanceAvgTimeToClose()), 1);

        return table;
    }
}
