package gov.healthit.chpl.scheduler.job.summarystatistics.pdf;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.itextpdf.layout.element.Table;

import gov.healthit.chpl.dao.CertificationBodyDAO;
import gov.healthit.chpl.scheduler.job.summarystatistics.data.EmailStatistics;

@Component
public class DirectReviewSummaryStatisticsSectionPdf extends SummaryStatisticsSectionPdf {

    @Autowired
    public DirectReviewSummaryStatisticsSectionPdf(CertificationBodyDAO certificationBodyDAO) {
        super(certificationBodyDAO);
    }

    @Override
    public Table generateTable(LocalDate recent, LocalDate previous, EmailStatistics recentEmailStatistics, EmailStatistics previousEmailStatistics) {
        Table table = new Table(getRelativeColumnWidths());
        table.useAllAvailableWidth();
        table = addHeaders(table, "Direct Review Statistics", recent, previous);

        table = addTableRow(table, createDataForRow("1. Total # of Direct Review Activities",
                recentEmailStatistics.getTotalDirectReviews(),
                previousEmailStatistics.getTotalDirectReviews()), true);

        table = addTableRow(table, createDataForRow("a. Open Direct Review Activities",
                recentEmailStatistics.getOpenDirectReviews(),
                previousEmailStatistics.getOpenDirectReviews()),
                NUMBER_OF_INDENTS_SUMMARY_LEVEL_STAT, true);

        table = addTableRow(table, createDataForRow("b. Closed Direct Review Activities",
                recentEmailStatistics.getClosedDirectReviews(),
                previousEmailStatistics.getClosedDirectReviews()),
                NUMBER_OF_INDENTS_SUMMARY_LEVEL_STAT, true);

        table = addTableRow(table, createDataForRow("c. Average Duration of Closed Direct Review Activities (in days)",
                recentEmailStatistics.getAverageDaysOpenDirectReviews(),
                previousEmailStatistics.getAverageDaysOpenDirectReviews()),
                NUMBER_OF_INDENTS_SUMMARY_LEVEL_STAT, true);

        table = addTableRow(table, createDataForRow("2. Total # of Direct Review NCs",
                recentEmailStatistics.getTotalNonConformities(),
                previousEmailStatistics.getTotalNonConformities()), true);

        table = addTableRow(table, createDataForRow("a. Open NCs",
                recentEmailStatistics.getOpenNonConformities(),
                previousEmailStatistics.getOpenNonConformities()),
                NUMBER_OF_INDENTS_SUMMARY_LEVEL_STAT, true);

        table = addTableRow(table, createDataForRow("b. Closed NCs",
                recentEmailStatistics.getClosedNonConformities(),
                previousEmailStatistics.getClosedNonConformities()),
                NUMBER_OF_INDENTS_SUMMARY_LEVEL_STAT, true);

        table = addTableRow(table, createDataForRow("c. Number of Open CAPs",
                recentEmailStatistics.getOpenCaps(),
                previousEmailStatistics.getOpenCaps()),
                NUMBER_OF_INDENTS_SUMMARY_LEVEL_STAT, true);

        table = addTableRow(table, createDataForRow("d. Number of Closed CAPs",
                recentEmailStatistics.getClosedCaps(),
                previousEmailStatistics.getClosedCaps()),
                NUMBER_OF_INDENTS_SUMMARY_LEVEL_STAT, true);

        return table;
    }


}
