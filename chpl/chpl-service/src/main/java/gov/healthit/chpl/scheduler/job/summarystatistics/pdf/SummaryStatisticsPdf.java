package gov.healthit.chpl.scheduler.job.summarystatistics.pdf;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.events.PdfDocumentEvent;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;

import gov.healthit.chpl.dao.statistics.SummaryStatisticsDAO;
import gov.healthit.chpl.entity.SummaryStatisticsEntity;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.scheduler.job.summarystatistics.chart.DevelopersOverTimeChart;
import gov.healthit.chpl.scheduler.job.summarystatistics.chart.Listing2014OverTimeChart;
import gov.healthit.chpl.scheduler.job.summarystatistics.chart.Listing2015OverTimeChart;
import gov.healthit.chpl.scheduler.job.summarystatistics.chart.SummaryStatisticChart;
import gov.healthit.chpl.scheduler.job.summarystatistics.chart.SurveillanceActivitiesOverTimeChart;
import gov.healthit.chpl.scheduler.job.summarystatistics.chart.SurveillanceNonConformitiesOverTimeChart;
import gov.healthit.chpl.scheduler.job.summarystatistics.chart.TotalListingsOverTimeChart;
import gov.healthit.chpl.scheduler.job.summarystatistics.chart.TotalUniqueProductsOverTimeChart;
import gov.healthit.chpl.scheduler.job.summarystatistics.chart.UniqueProductsWithActiveListingsOverTimeChart;
import gov.healthit.chpl.scheduler.job.summarystatistics.data.EmailStatistics;

@Component
public class SummaryStatisticsPdf {
    private static final Integer ONE_WEEK = 7;
    private SummaryStatisticsDAO summaryStatisticsDAO;
    private DeveloperSummaryStatisticsSectionPdf developerSummaryStatisticsSectionPdf;
    private ProductSummaryStatisticsSectionPdf productSummaryStatisticsSectionPdf;
    private ListingSummaryStatisticsSectionPdf listingSummaryStatisticsSectionPdf;
    private SurveillanceSummaryStatisticsSectionPdf surveillanceSummaryStatisticsSectionPdf;
    private DirectReviewSummaryStatisticsSectionPdf directReviewSummaryStatisticsSectionPdf;

    @Autowired
    public SummaryStatisticsPdf(SummaryStatisticsDAO summaryStatisticsDAO,
            DeveloperSummaryStatisticsSectionPdf developerSummaryStatisticsSectionPdf,
            ProductSummaryStatisticsSectionPdf productSummaryStatisticsSectionPdf,
            ListingSummaryStatisticsSectionPdf listingSummaryStatisticsSectionPdf,
            SurveillanceSummaryStatisticsSectionPdf surveillanceSummaryStatisticsSectionPdf,
            DirectReviewSummaryStatisticsSectionPdf directReviewSummaryStatisticsSectionPdf) {

        this.summaryStatisticsDAO = summaryStatisticsDAO;
        this.developerSummaryStatisticsSectionPdf = developerSummaryStatisticsSectionPdf;
        this.productSummaryStatisticsSectionPdf = productSummaryStatisticsSectionPdf;
        this.listingSummaryStatisticsSectionPdf = listingSummaryStatisticsSectionPdf;
        this.surveillanceSummaryStatisticsSectionPdf = surveillanceSummaryStatisticsSectionPdf;
        this.directReviewSummaryStatisticsSectionPdf = directReviewSummaryStatisticsSectionPdf;
    }

    @SuppressWarnings("resource")
    public File generate(File csvData) throws IOException {
        File file = File.createTempFile("SummaryStatistics-" + getUniqueIdenftifierforFileName(), ".pdf");

        PdfWriter writer = new PdfWriter(file);
        PdfDocument pdf = new PdfDocument(writer);

        //This adds the footer at the end of each page
        pdf.addEventHandler(PdfDocumentEvent.END_PAGE, new SummaryStatisticsPdfFooterEvent());

        try (Document document = new Document(pdf)) {
            SummaryStatisticsEntity recentStats = getSummaryStatisticsAsOf(LocalDate.now());
            SummaryStatisticsEntity previousStats = getSummaryStatisticsAsOf(convertDateToLocalDate(recentStats.getEndDate()).minusDays(ONE_WEEK));
            addDocumentHeader(document);
            addTables(document, recentStats, previousStats);
            addCharts(document, csvData);
            document.close();
        }
        return file;
    }

    @SuppressWarnings("resource")
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
    @SuppressWarnings("resource")
    private void addTables(Document document, SummaryStatisticsEntity recentStats, SummaryStatisticsEntity previousStats) {
        EmailStatistics recentEmailStats = getEmailStatisticsFromSummaryStatistics(recentStats);
        EmailStatistics previousEmailStats = getEmailStatisticsFromSummaryStatistics(previousStats);

        List<SummaryStatisticsSectionPdf> tableGenerators = Arrays.asList(
                developerSummaryStatisticsSectionPdf,
                productSummaryStatisticsSectionPdf,
                listingSummaryStatisticsSectionPdf,
                surveillanceSummaryStatisticsSectionPdf,
                directReviewSummaryStatisticsSectionPdf);

        tableGenerators.forEach(generator -> {
            addTable(document,
                    generator,
                    convertDateToLocalDate(recentStats.getEndDate()),
                    convertDateToLocalDate(previousStats.getEndDate()),
                    recentEmailStats, previousEmailStats);
            generator.addTableEndNote(document, recentEmailStats, previousEmailStats);
            document.add(new Paragraph(""));
        });
    }

    @SuppressWarnings("resource")
    private void addTable(Document document, SummaryStatisticsSectionPdf tableGenerator, LocalDate recentDate, LocalDate previousDate,
            EmailStatistics recentEmailStats, EmailStatistics previousEmailStats) {

        document.add(tableGenerator.generateTable(recentDate, previousDate, recentEmailStats, previousEmailStats));

    }

    @SuppressWarnings("resource")
    private void addCharts(Document document, File csvData) throws IOException {
        List<SummaryStatisticChart> chartGenerators = Arrays.asList(
                new DevelopersOverTimeChart(),
                new UniqueProductsWithActiveListingsOverTimeChart(),
                new TotalUniqueProductsOverTimeChart(),
                new TotalListingsOverTimeChart(),
                new Listing2014OverTimeChart(),
                new Listing2015OverTimeChart(),
                new SurveillanceActivitiesOverTimeChart(),
                new SurveillanceNonConformitiesOverTimeChart());

        chartGenerators.forEach(chartGenerator -> {
            document.add(new Paragraph(""));
            try {
                addChart(document, csvData, chartGenerator);
            } catch (IOException e) {
                document.add(new Paragraph("[Could not generate chart]"));
            }
        });
    }

    @SuppressWarnings("resource")
    private void addChart(Document document, File csvData, SummaryStatisticChart chartGenerator) throws IOException {
        JFreeChart chart = chartGenerator.generate(csvData);
        document.add(getPdfImage(chart));
    }

    private Image getPdfImage(JFreeChart chart) throws IOException {
        final BufferedImage image = chart.createBufferedImage(600, 300, 600, 300, null);
        ImageData rawImage = ImageDataFactory.create(ChartUtils.encodeAsPNG(image));
        return new Image(rawImage);
    }

    private EmailStatistics getEmailStatisticsFromSummaryStatistics(SummaryStatisticsEntity stats) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(stats.getSummaryStatistics(), EmailStatistics.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private SummaryStatisticsEntity getSummaryStatisticsAsOf(LocalDate asOf) {
        try {
            SummaryStatisticsEntity stats = null;
            while (stats == null) {
                stats = summaryStatisticsDAO.getSummaryStatistics(asOf);
                asOf = asOf.minusDays(1);
            }
            return stats;
        } catch (EntityRetrievalException e) {
            throw new RuntimeException(e);
        }
    }

    private LocalDate convertDateToLocalDate(Date toConvert) {
        return toConvert.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
    }

    private String getUniqueIdenftifierforFileName() {
        return Long.valueOf(LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)).toString();
    }
}
