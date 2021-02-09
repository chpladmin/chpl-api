package gov.healthit.chpl.scheduler.job.summarystatistics.pdf;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import org.dom4j.DocumentException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;

import gov.healthit.chpl.dao.statistics.SummaryStatisticsDAO;
import gov.healthit.chpl.entity.SummaryStatisticsEntity;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.scheduler.job.summarystatistics.data.EmailStatistics;

@Component
public class SummaryStatisticsPdf {
    private static final Integer ONE_WEEK = 7;
    private SummaryStatisticsDAO summaryStatisticsDAO;
    private DeveloperSummaryStatisticsSectionPdf developerSummaryStatisticsSectionPdf;
    private ProductSummaryStatisticsSectionPdf productSummaryStatisticsSectionPdf;
    private ListingSummaryStatisticsSectionPdf listingSummaryStatisticsSectionPdf;

    @Autowired
    public SummaryStatisticsPdf(SummaryStatisticsDAO summaryStatisticsDAO,
            DeveloperSummaryStatisticsSectionPdf developerSummaryStatisticsSectionPdf,
            ProductSummaryStatisticsSectionPdf productSummaryStatisticsSectionPdf,
            ListingSummaryStatisticsSectionPdf listingSummaryStatisticsSectionPdf) {
        this.summaryStatisticsDAO = summaryStatisticsDAO;
        this.developerSummaryStatisticsSectionPdf = developerSummaryStatisticsSectionPdf;
        this.productSummaryStatisticsSectionPdf = productSummaryStatisticsSectionPdf;
        this.listingSummaryStatisticsSectionPdf = listingSummaryStatisticsSectionPdf;
    }

    @SuppressWarnings("resource")
    public File generate() throws DocumentException, IOException {
        String dest = "C:/chpl/files/SummaryStatistics.pdf";
        File file = new File(dest);

        PdfDocument pdf = new PdfDocument(new PdfWriter(dest));
        try (Document document = new Document(pdf)) {
            SummaryStatisticsEntity recentStats = getSummaryStatisticsAsOf(LocalDate.now());
            EmailStatistics recentEmailStats = getEmailStatisticsFromSummaryStatistics(recentStats);
            SummaryStatisticsEntity previousStats = getSummaryStatisticsAsOf(convertDateToLocalDate(recentStats.getEndDate()).minusDays(ONE_WEEK));
            EmailStatistics previousEmailStats = getEmailStatisticsFromSummaryStatistics(previousStats);

            Paragraph title = new Paragraph("ONC CHPL");
            title.setFont(SummaryStatisticsPDFDefaults.getDefaultFont());
            title.setFontSize(SummaryStatisticsPDFDefaults.TITLE_FONT_SIZE);
            document.add(title);

            Paragraph subtitle = new Paragraph("Weekly Summary Statistics Report");
            subtitle.setFont(SummaryStatisticsPDFDefaults.getDefaultFont());
            Color fontColor = new DeviceRgb(163, 209, 235);
            subtitle.setFontColor(fontColor);
            subtitle.setFontSize(SummaryStatisticsPDFDefaults.SUBTITLE_FONT_SIZE);
            document.add(subtitle);

            Paragraph currentDate = new Paragraph(LocalDate.now().format(DateTimeFormatter.ofPattern("LLLL dd, yyyy")));
            currentDate.setFont(SummaryStatisticsPDFDefaults.getDefaultFont());
            currentDate.setFontSize(SummaryStatisticsPDFDefaults.DEFAULT_FONT_SIZE);
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

            document.close();
        }

        return file;
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
