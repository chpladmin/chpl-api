package gov.healthit.chpl.scheduler.job.summarystatistics.pdf;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.itextpdf.layout.element.Table;

import gov.healthit.chpl.dao.CertificationBodyDAO;
import gov.healthit.chpl.scheduler.job.summarystatistics.data.EmailStatistics;

@Component
public class NonConformitySummaryStatisticsSectionPdf extends SummaryStatisticsSectionPdf {

    @Autowired
    public NonConformitySummaryStatisticsSectionPdf(CertificationBodyDAO certificationBodyDAO) {
        super(certificationBodyDAO);
    }

    @Override
    public Table generateTable(LocalDate recent, LocalDate previous, EmailStatistics recentEmailStatistics, EmailStatistics previousEmailStatistics) {
        Table table = new Table(getRelativeColumnWidths());
        table.useAllAvailableWidth();
        table = addHeaders(table, "Non-conformity Statistics", recent, previous);
        table = addTableRow(table, createDataForRow("4. Total # of NCs",
                                    recentEmailStatistics.getNonconfStatusAllTotal(),
                                    previousEmailStatistics.getNonconfStatusAllTotal()));

        table = addTableRow(table, createDataForRow("a. Open NCs",
                recentEmailStatistics.getNonconfStatusOpen().getCount(),
                previousEmailStatistics.getNonconfStatusOpen().getCount()), 1);

        table = addAcbRows(table,
                recentEmailStatistics.getNonconfStatusOpen().getAcbStatistics(),
                previousEmailStatistics.getNonconfStatusOpen().getAcbStatistics());

        table = addTableRow(table, createDataForRow("b. Closed NCs",
                recentEmailStatistics.getNonconfStatusClosedTotal(),
                previousEmailStatistics.getNonconfStatusClosedTotal()), 1);

        table = addTableRow(table, createDataForRow("c. Average Time to Assess Conformity (in days)",
                recentEmailStatistics.getNonconfAvgTimeToAssessConformity(),
                previousEmailStatistics.getNonconfAvgTimeToAssessConformity()), 1);

        table = addTableRow(table, createDataForRow("d. Average Time to Approve CAP (in days)",
                recentEmailStatistics.getNonconfAvgTimeToApproveCAP(),
                previousEmailStatistics.getNonconfAvgTimeToApproveCAP()), 1);

        table = addTableRow(table, createDataForRow("e. Average Duration of CAP (in days) (includes closed and ongoing CAPs)",
                recentEmailStatistics.getNonconfAvgDurationOfCAP(),
                previousEmailStatistics.getNonconfAvgDurationOfCAP()), 1);

        table = addTableRow(table, createDataForRow("f. Average Time from CAP Approval to Surveillance Close (in days)",
                recentEmailStatistics.getNonconfAvgTimeFromCAPAprrovalToSurveillanceEnd(),
                previousEmailStatistics.getNonconfAvgTimeFromCAPAprrovalToSurveillanceEnd()), 1);

        table = addTableRow(table, createDataForRow("g. Average Time from CAP Close to Surveillance Close (in days)",
                recentEmailStatistics.getNonconfAvgTimeFromCAPEndToSurveillanceEnd(),
                previousEmailStatistics.getNonconfAvgTimeFromCAPEndToSurveillanceEnd()), 1);

        table = addTableRow(table, createDataForRow("h. Average Duration of Closed Non-Conformities (in days)",
                recentEmailStatistics.getNonconfAvgTimeFromSurveillanceOpenToSurveillanceClose(),
                previousEmailStatistics.getNonconfAvgTimeFromSurveillanceOpenToSurveillanceClose()), 1);

        //TODO - Where did this come from?
        /*
        table = addTableRow(table, createDataForRow("6. Total Number of CAPs",
                recentEmailStatistics.getNonconfAvgTimeFromSurveillanceOpenToSurveillanceClose(),
                previousEmailStatistics.getNonconfAvgTimeFromSurveillanceOpenToSurveillanceClose()));

        table = addTableRow(table, createDataForRow("a. Number of Open CAPs",
                recentEmailStatistics.getNonconfCAPStatusOpen(),
                previousEmailStatistics.getNonconfCAPStatusOpen()), 1);

        table = addAcbRows(table,
                recentEmailStatistics.getNonconfStatusOpen().getAcbStatistics(),
                previousEmailStatistics.getNonconfStatusOpen().getAcbStatistics());
        */


        return table;
    }
}
