package gov.healthit.chpl.scheduler.job.summarystatistics.pdf;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itextpdf.kernel.events.PdfDocumentEvent;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;

import gov.healthit.chpl.dao.statistics.SummaryStatisticsDAO;
import gov.healthit.chpl.entity.statistics.SummaryStatisticsEntity;
import gov.healthit.chpl.scheduler.job.summarystatistics.data.StatisticsSnapshot;
import gov.healthit.chpl.util.DateUtil;
import lombok.extern.log4j.Log4j2;

@Log4j2(topic = "summaryStatisticsCreatorJobLogger")
@Component
public class SummaryStatisticsPdf {
    private static final Integer DAYS_IN_ONE_WEEK = 7;
    private SummaryStatisticsDAO summaryStatisticsDao;
    private DeveloperSummaryStatisticsSectionPdf developerSummaryStatisticsSectionPdf;
    private ProductSummaryStatisticsSectionPdf productSummaryStatisticsSectionPdf;
    private ListingSummaryStatisticsSectionPdf listingSummaryStatisticsSectionPdf;
    private SurveillanceSummaryStatisticsSectionPdf surveillanceSummaryStatisticsSectionPdf;
    private DirectReviewSummaryStatisticsSectionPdf directReviewSummaryStatisticsSectionPdf;

    @Autowired
    public SummaryStatisticsPdf(SummaryStatisticsDAO summaryStatisticsDao,
            DeveloperSummaryStatisticsSectionPdf developerSummaryStatisticsSectionPdf,
            ProductSummaryStatisticsSectionPdf productSummaryStatisticsSectionPdf,
            ListingSummaryStatisticsSectionPdf listingSummaryStatisticsSectionPdf,
            SurveillanceSummaryStatisticsSectionPdf surveillanceSummaryStatisticsSectionPdf,
            DirectReviewSummaryStatisticsSectionPdf directReviewSummaryStatisticsSectionPdf) {

        this.summaryStatisticsDao = summaryStatisticsDao;
        this.developerSummaryStatisticsSectionPdf = developerSummaryStatisticsSectionPdf;
        this.productSummaryStatisticsSectionPdf = productSummaryStatisticsSectionPdf;
        this.listingSummaryStatisticsSectionPdf = listingSummaryStatisticsSectionPdf;
        this.surveillanceSummaryStatisticsSectionPdf = surveillanceSummaryStatisticsSectionPdf;
        this.directReviewSummaryStatisticsSectionPdf = directReviewSummaryStatisticsSectionPdf;
    }

    public File generate() throws IOException {
        File file = File.createTempFile("SummaryStatistics-" + getUniqueIdentifierforFileName(), ".pdf");

        PdfWriter writer = new PdfWriter(file);
        PdfDocument pdf = new PdfDocument(writer);

        //This adds the footer at the end of each page
        pdf.addEventHandler(PdfDocumentEvent.END_PAGE, new SummaryStatisticsPdfFooterEvent());

        try (Document document = new Document(pdf)) {
            SummaryStatisticsEntity recentStats = getSummaryStatisticsAsOf(LocalDate.now());
            SummaryStatisticsEntity previousStats = getSummaryStatisticsAsOf(
                    DateUtil.toLocalDate(recentStats.getEndDate().getTime()).minusDays(DAYS_IN_ONE_WEEK));
            addDocumentHeader(document);
            addTables(document, recentStats, previousStats);
            document.close();
        }
        return file;
    }

    private void addDocumentHeader(Document document) {
        Paragraph title = new Paragraph("ONC CHPL");
        title.setFont(SummaryStatisticsPdfDefaults.getDefaultFont());
        title.setFontSize(SummaryStatisticsPdfDefaults.TITLE_FONT_SIZE);
        document.add(title);

        Paragraph subtitle = new Paragraph("Weekly Summary Statistics Report");
        subtitle.setFont(SummaryStatisticsPdfDefaults.getDefaultFont());
        subtitle.setFontColor(SummaryStatisticsPdfDefaults.getSubtitleFontColor());
        subtitle.setFontSize(SummaryStatisticsPdfDefaults.SUBTITLE_FONT_SIZE);
        document.add(subtitle);

        Paragraph currentDate = new Paragraph(LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")));
        currentDate.setFont(SummaryStatisticsPdfDefaults.getDefaultFont());
        currentDate.setFontSize(SummaryStatisticsPdfDefaults.DEFAULT_FONT_SIZE);
        currentDate.setItalic();
        document.add(currentDate);

    }
    private void addTables(Document document, SummaryStatisticsEntity currStats, SummaryStatisticsEntity prevStats) {
        StatisticsSnapshot currSnapshot = getSnapshotFromSummaryStatistics(currStats);
        StatisticsSnapshot prevSnapshot = getSnapshotFromSummaryStatistics(prevStats);

        List<SummaryStatisticsSectionPdf> tableGenerators = Arrays.asList(
                developerSummaryStatisticsSectionPdf,
                productSummaryStatisticsSectionPdf,
                listingSummaryStatisticsSectionPdf,
                surveillanceSummaryStatisticsSectionPdf,
                directReviewSummaryStatisticsSectionPdf);

        tableGenerators.forEach(generator -> {
            addTable(document,
                    generator,
                    DateUtil.toLocalDate(currStats.getEndDate().getTime()),
                    DateUtil.toLocalDate(prevStats.getEndDate().getTime()),
                    currSnapshot, prevSnapshot);
            generator.addTableEndNote(document, currSnapshot, prevSnapshot);
            document.add(new Paragraph(""));
        });
    }

    private void addTable(Document document, SummaryStatisticsSectionPdf tableGenerator, LocalDate recentDate, LocalDate previousDate,
            StatisticsSnapshot recentEmailStats, StatisticsSnapshot previousEmailStats) {
        document.add(tableGenerator.generateTable(recentDate, previousDate, recentEmailStats, previousEmailStats));
    }

    private StatisticsSnapshot getSnapshotFromSummaryStatistics(SummaryStatisticsEntity stats) {
        if (stats == null) {
            return null;
        }

        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(stats.getSummaryStatistics(), StatisticsSnapshot.class);
        } catch (IOException ex) {
            LOGGER.error("Unable to convert SummaryStatisticsEntity JSON into StatisticsSnapshot java object.", ex);
            return null;
        }
    }

    private SummaryStatisticsEntity getSummaryStatisticsAsOf(LocalDate asOf) {
        SummaryStatisticsEntity stats = null;
        while (stats == null) {
            stats = summaryStatisticsDao.getSummaryStatistics(asOf);
            asOf = asOf.minusDays(1);
        }
        return stats;
    }

    private String getUniqueIdentifierforFileName() {
        return Long.valueOf(LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)).toString();
    }
}
