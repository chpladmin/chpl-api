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
import gov.healthit.chpl.scheduler.job.summarystatistics.data.StatisticsSnapshot;

@Component
public class DirectReviewSummaryStatisticsSectionPdf extends SummaryStatisticsSectionPdf {

    @Autowired
    public DirectReviewSummaryStatisticsSectionPdf(CertificationBodyDAO certificationBodyDAO) {
        super(certificationBodyDAO);
    }

    @Override
    public Table generateTable(LocalDate currSnapshotDate, LocalDate prevSnapshotDate, StatisticsSnapshot currSnapshot, StatisticsSnapshot prevSnapshot) {
        Table table = new Table(getRelativeColumnWidths());
        table.useAllAvailableWidth();
        table = addHeaders(table, "Direct Review Statistics", currSnapshotDate, prevSnapshotDate);

        table = addTableRow(table, createDataForRow("1. Total # of Direct Review Activities",
                    currSnapshot == null ? null : currSnapshot.getTotalDirectReviews(),
                    prevSnapshot == null ? null : prevSnapshot.getTotalDirectReviews()),
                    true);

        table = addTableRow(table, createDataForRow("a. Open Direct Review Activities",
                    currSnapshot == null ? null : currSnapshot.getOpenDirectReviews(),
                    prevSnapshot == null ? null : prevSnapshot.getOpenDirectReviews()),
                    NUMBER_OF_INDENTS_SUMMARY_LEVEL_STAT,
                    true);

        table = addTableRow(table, createDataForRow("b. Closed Direct Review Activities",
                    currSnapshot == null ? null : currSnapshot.getClosedDirectReviews(),
                    prevSnapshot == null ? null : prevSnapshot.getClosedDirectReviews()),
                    NUMBER_OF_INDENTS_SUMMARY_LEVEL_STAT,
                    true);

        table = addTableRow(table, createDataForRow("c. Average Duration of Closed Direct Review Activities (in days)",
                    currSnapshot == null ? null : currSnapshot.getAverageDaysOpenDirectReviews(),
                    prevSnapshot == null ? null : prevSnapshot.getAverageDaysOpenDirectReviews()),
                    NUMBER_OF_INDENTS_SUMMARY_LEVEL_STAT,
                    true);

        table = addTableRow(table, createDataForRow("2. Total # of Direct Review Non-conformities",
                    currSnapshot == null ? null : currSnapshot.getTotalNonConformities(),
                    prevSnapshot == null ? null : prevSnapshot.getTotalNonConformities()),
                    true);

        table = addTableRow(table, createDataForRow("a. Open NCs",
                    currSnapshot == null ? null : currSnapshot.getOpenNonConformities(),
                    prevSnapshot == null ? null : prevSnapshot.getOpenNonConformities()),
                    NUMBER_OF_INDENTS_SUMMARY_LEVEL_STAT,
                    true);

        table = addTableRow(table, createDataForRow("b. Closed NCs",
                    currSnapshot == null ? null : currSnapshot.getClosedNonConformities(),
                    prevSnapshot == null ? null : prevSnapshot.getClosedNonConformities()),
                    NUMBER_OF_INDENTS_SUMMARY_LEVEL_STAT,
                    true);

        table = addTableRow(table, createDataForRow("c. Number of Open CAPs",
                    currSnapshot == null ? null : currSnapshot.getOpenCaps(),
                    prevSnapshot == null ? null : prevSnapshot.getOpenCaps()),
                    NUMBER_OF_INDENTS_SUMMARY_LEVEL_STAT,
                    true);

        table = addTableRow(table, createDataForRow("d. Number of Closed CAPs",
                    currSnapshot == null ? null : currSnapshot.getClosedCaps(),
                    prevSnapshot == null ? null : prevSnapshot.getClosedCaps()),
                    NUMBER_OF_INDENTS_SUMMARY_LEVEL_STAT,
                    true);

        return table;
    }

    @Override
    public Document addTableEndNote(Document document, StatisticsSnapshot currSnapshot, StatisticsSnapshot prevSnapshot) {
        if (displayEndNote(currSnapshot, prevSnapshot)) {
            Text first = new Text("Not Available (N/A) ").setFont(SummaryStatisticsPdfDefaults.getDefaultBoldFont());
            first.setFontSize(SummaryStatisticsPdfDefaults.FOOTER_FONT_SIZE);
            Text second = new Text("indicates that data was not available at the time the report was generated.").setFont(SummaryStatisticsPdfDefaults.getDefaultFont());
            second.setFontSize(SummaryStatisticsPdfDefaults.FOOTER_FONT_SIZE);
            Paragraph paragraph = new Paragraph().add(first).add(second);
            document.add(paragraph);
        }
        return document;
    }

    private Boolean displayEndNote(StatisticsSnapshot currSnapshot, StatisticsSnapshot prevSnapshot) {
        return Stream.of(
                currSnapshot == null ? null : currSnapshot.getTotalDirectReviews(),
                prevSnapshot == null ? null : prevSnapshot.getTotalDirectReviews(),
                currSnapshot == null ? null : currSnapshot.getOpenDirectReviews(),
                prevSnapshot == null ? null : prevSnapshot.getOpenDirectReviews(),
                currSnapshot == null ? null : currSnapshot.getClosedDirectReviews(),
                prevSnapshot == null ? null : prevSnapshot.getClosedDirectReviews(),
                currSnapshot == null ? null : currSnapshot.getAverageDaysOpenDirectReviews(),
                prevSnapshot == null ? null : prevSnapshot.getAverageDaysOpenDirectReviews(),
                currSnapshot == null ? null : currSnapshot.getTotalNonConformities(),
                prevSnapshot == null ? null : prevSnapshot.getTotalNonConformities(),
                currSnapshot == null ? null : currSnapshot.getOpenNonConformities(),
                prevSnapshot == null ? null : prevSnapshot.getOpenNonConformities(),
                currSnapshot == null ? null : currSnapshot.getClosedNonConformities(),
                prevSnapshot == null ? null : prevSnapshot.getClosedNonConformities(),
                currSnapshot == null ? null : currSnapshot.getOpenCaps(),
                prevSnapshot == null ? null : prevSnapshot.getOpenCaps(),
                currSnapshot == null ? null : currSnapshot.getClosedCaps(),
                prevSnapshot == null ? null : prevSnapshot.getClosedCaps())
        .anyMatch(x -> x == null);
    }

}
