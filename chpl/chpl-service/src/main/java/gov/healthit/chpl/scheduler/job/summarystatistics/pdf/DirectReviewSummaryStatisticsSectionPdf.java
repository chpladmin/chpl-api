package gov.healthit.chpl.scheduler.job.summarystatistics.pdf;

import java.time.LocalDate;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Text;

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

        table = addTableRow(table, createDataForRow("2. Total # of Direct Review Non-conformities",
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

    @SuppressWarnings("resource")
    @Override
    public Document addTableEndNote(Document document, EmailStatistics recentEmailStatistics, EmailStatistics previousEmailStatistics) {
        if (displayEndNote(recentEmailStatistics, previousEmailStatistics)) {
            Text first = new Text("Not Available (N/A) ").setFont(SummaryStatisticsPdfDefaults.getDefaultBoldFont());
            first.setFontSize(SummaryStatisticsPdfDefaults.FOOTER_FONT_SIZE);
            Text second = new Text("indicates that data was not available at the time the report was generated.").setFont(SummaryStatisticsPdfDefaults.getDefaultFont());
            second.setFontSize(SummaryStatisticsPdfDefaults.FOOTER_FONT_SIZE);
            Paragraph paragraph = new Paragraph().add(first).add(second);
            document.add(paragraph);
        }
        return document;
    }


    private Boolean displayEndNote(EmailStatistics recentEmailStatistics, EmailStatistics previousEmailStatistics) {
        return Stream.of(
                recentEmailStatistics.getTotalDirectReviews(),
                previousEmailStatistics.getTotalDirectReviews(),
                recentEmailStatistics.getOpenDirectReviews(),
                previousEmailStatistics.getOpenDirectReviews(),
                recentEmailStatistics.getClosedDirectReviews(),
                previousEmailStatistics.getClosedDirectReviews(),
                recentEmailStatistics.getAverageDaysOpenDirectReviews(),
                previousEmailStatistics.getAverageDaysOpenDirectReviews(),
                recentEmailStatistics.getTotalNonConformities(),
                previousEmailStatistics.getTotalNonConformities(),
                recentEmailStatistics.getOpenNonConformities(),
                previousEmailStatistics.getOpenNonConformities(),
                recentEmailStatistics.getClosedNonConformities(),
                previousEmailStatistics.getClosedNonConformities(),
                recentEmailStatistics.getOpenCaps(),
                previousEmailStatistics.getOpenCaps(),
                recentEmailStatistics.getClosedCaps(),
                previousEmailStatistics.getClosedCaps())
        .anyMatch(x -> x == null);
    }

}
