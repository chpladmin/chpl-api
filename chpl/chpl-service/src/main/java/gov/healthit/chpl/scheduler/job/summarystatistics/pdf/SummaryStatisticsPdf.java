package gov.healthit.chpl.scheduler.job.summarystatistics.pdf;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import org.dom4j.DocumentException;
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
    private NonConformitySummaryStatisticsSectionPdf nonConformitySummaryStatisticsSectionPdf;

    @Autowired
    public SummaryStatisticsPdf(SummaryStatisticsDAO summaryStatisticsDAO,
            DeveloperSummaryStatisticsSectionPdf developerSummaryStatisticsSectionPdf,
            ProductSummaryStatisticsSectionPdf productSummaryStatisticsSectionPdf,
            ListingSummaryStatisticsSectionPdf listingSummaryStatisticsSectionPdf,
            SurveillanceSummaryStatisticsSectionPdf surveillanceSummaryStatisticsSectionPdf,
            NonConformitySummaryStatisticsSectionPdf nonConformitySummaryStatisticsSectionPdf) {

        this.summaryStatisticsDAO = summaryStatisticsDAO;
        this.developerSummaryStatisticsSectionPdf = developerSummaryStatisticsSectionPdf;
        this.productSummaryStatisticsSectionPdf = productSummaryStatisticsSectionPdf;
        this.listingSummaryStatisticsSectionPdf = listingSummaryStatisticsSectionPdf;
        this.surveillanceSummaryStatisticsSectionPdf = surveillanceSummaryStatisticsSectionPdf;
        this.nonConformitySummaryStatisticsSectionPdf = nonConformitySummaryStatisticsSectionPdf;
    }

    @SuppressWarnings("resource")
    public File generate(File csv) throws DocumentException, IOException {
        String dest = "C:/chpl/files/SummaryStatistics.pdf";
        File file = new File(dest);

        PdfWriter writer = new PdfWriter(dest);
        PdfDocument pdf = new PdfDocument(writer);
        pdf.addEventHandler(PdfDocumentEvent.END_PAGE, new SummaryStatisticsPdfFooterEvent());
        try (Document document = new Document(pdf)) {

            SummaryStatisticsEntity recentStats = getSummaryStatisticsAsOf(LocalDate.now());
            EmailStatistics recentEmailStats = getEmailStatisticsFromSummaryStatistics(recentStats);
            SummaryStatisticsEntity previousStats = getSummaryStatisticsAsOf(convertDateToLocalDate(recentStats.getEndDate()).minusDays(ONE_WEEK));
            EmailStatistics previousEmailStats = getEmailStatisticsFromSummaryStatistics(previousStats);

            Paragraph title = new Paragraph("ONC CHPL");
            title.setFont(SummaryStatisticsPdfDefaults.getDefaultFont());
            title.setFontSize(SummaryStatisticsPdfDefaults.TITLE_FONT_SIZE);
            document.add(title);

            Paragraph subtitle = new Paragraph("Weekly Summary Statistics Report");
            subtitle.setFont(SummaryStatisticsPdfDefaults.getDefaultFont());
            subtitle.setFontColor(SummaryStatisticsPdfDefaults.getSubtitleFontColor());
            subtitle.setFontSize(SummaryStatisticsPdfDefaults.SUBTITLE_FONT_SIZE);
            document.add(subtitle);

            Paragraph currentDate = new Paragraph(LocalDate.now().format(DateTimeFormatter.ofPattern("LLLL dd, yyyy")));
            currentDate.setFont(SummaryStatisticsPdfDefaults.getDefaultFont());
            currentDate.setFontSize(SummaryStatisticsPdfDefaults.DEFAULT_FONT_SIZE);
            currentDate.setItalic();
            document.add(currentDate);

            document.add(developerSummaryStatisticsSectionPdf.generateTable(
                    convertDateToLocalDate(recentStats.getEndDate()),
                    convertDateToLocalDate(previousStats.getEndDate()),
                    recentEmailStats,
                    previousEmailStats));

            document.add(new Paragraph(""));

            document.add(productSummaryStatisticsSectionPdf.generateTable(
                    convertDateToLocalDate(recentStats.getEndDate()),
                    convertDateToLocalDate(previousStats.getEndDate()),
                    recentEmailStats,
                    previousEmailStats));

            document.add(new Paragraph(""));

            document.add(listingSummaryStatisticsSectionPdf.generateTable(
                    convertDateToLocalDate(recentStats.getEndDate()),
                    convertDateToLocalDate(previousStats.getEndDate()),
                    recentEmailStats,
                    previousEmailStats));

            document.add(new Paragraph(""));

            document.add(surveillanceSummaryStatisticsSectionPdf.generateTable(
                    convertDateToLocalDate(recentStats.getEndDate()),
                    convertDateToLocalDate(previousStats.getEndDate()),
                    recentEmailStats,
                    previousEmailStats));

            document.add(new Paragraph(""));

            document.add(nonConformitySummaryStatisticsSectionPdf.generateTable(
                    convertDateToLocalDate(recentStats.getEndDate()),
                    convertDateToLocalDate(previousStats.getEndDate()),
                    recentEmailStats,
                    previousEmailStats));

            document.add(new Paragraph(""));

            DevelopersOverTimeChart developersOverTimeGenerator = new DevelopersOverTimeChart();
            JFreeChart developersOverTimeChart = developersOverTimeGenerator.generate(csv);
            document.add(getPdfImage(developersOverTimeChart));

            document.add(new Paragraph(""));

            UniqueProductsWithActiveListingsOverTimeChart uniqueProductsOverTimeGenerator = new UniqueProductsWithActiveListingsOverTimeChart();
            JFreeChart uniqueProductsOverTimeChart = uniqueProductsOverTimeGenerator.generate(csv);
            document.add(getPdfImage(uniqueProductsOverTimeChart));

            document.add(new Paragraph(""));

            TotalUniqueProductsOverTimeChart totalUniqueProductsOverTimeChartGenerator = new TotalUniqueProductsOverTimeChart();
            JFreeChart totalUniqueProductsOverTimeChartChart = totalUniqueProductsOverTimeChartGenerator.generate(csv);
            document.add(getPdfImage(totalUniqueProductsOverTimeChartChart));

            document.close();
        }

        return file;
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

    //TODO: Need to add some sort of check to only look so far back...
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
}
