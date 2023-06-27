package gov.healthit.chpl.scheduler.job.surveillancereportingactivity.excel;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;

import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.Picture;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;

import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.scheduler.job.surveillancereportingactivity.SurveillanceData;
import gov.healthit.chpl.scheduler.job.surveillancereportingactivity.charts.CapApprovalToSurveillanceCloseByIntervalChart;
import gov.healthit.chpl.scheduler.job.surveillancereportingactivity.charts.ClosedSurveillanceByIntervalChart;
import gov.healthit.chpl.scheduler.job.surveillancereportingactivity.charts.ClosedSurveillanceDurationByQuartileChart;
import gov.healthit.chpl.scheduler.job.surveillancereportingactivity.charts.ClosedSurveillanceDurationChart;
import lombok.extern.log4j.Log4j2;

@Log4j2(topic = "surveillanceActivityReportJobLogger")
public class ChartsWorksheet {
    private Workbook workbook;
    private List<CertificationBody> allAcbs;

    public ChartsWorksheet(Workbook workbook, List<CertificationBody> allAcbs) {
        this.workbook = workbook;
        this.allAcbs = allAcbs;
    }

    public Sheet generateWorksheet(List<SurveillanceData> surveillances) throws IOException {
        try {
            LOGGER.info("Starting to build the Charts worksheet.");
            Sheet sheet = workbook.createSheet("Charts");
            insertChart(sheet, getClosedSurveillanceDurationByQuartileChart(surveillances), 0, 1);
            insertChart(sheet, getClosedSurveillanceDurationChart(surveillances), 0, 28);
            insertChart(sheet, getClosedSurveillanceByIntervalChart(surveillances), 0, 55);
            insertChart(sheet, getCapApprovalToSurveillanceCloseByIntervalChart(surveillances), 0, 82);
            return sheet;
        } finally {
            LOGGER.info("Completed to building the Charts worksheet.");
        }
    }

    @SuppressWarnings("resource")
    private Sheet insertChart(Sheet sheet, JFreeChart chart, Integer columnAnchorCell, Integer rowAnchorCell) throws IOException {
        Drawing drawing = sheet.createDrawingPatriarch();

        BufferedImage image = chart.createBufferedImage(750, 500, 750, 500, null);
        int imageIndex = sheet.getWorkbook().addPicture(ChartUtils.encodeAsPNG(image), Workbook.PICTURE_TYPE_PNG);

        ClientAnchor anchor = sheet.getWorkbook().getCreationHelper().createClientAnchor();
        anchor.setCol1(columnAnchorCell);
        anchor.setRow1(rowAnchorCell);
        Picture picture = drawing.createPicture(anchor, imageIndex);
        picture.resize();
        return sheet;
    }

    private JFreeChart getClosedSurveillanceDurationByQuartileChart(List<SurveillanceData> surveillances) {
        ClosedSurveillanceDurationByQuartileChart chart = new ClosedSurveillanceDurationByQuartileChart();
        return chart.generateChart(surveillances, allAcbs);
    }

    private JFreeChart getClosedSurveillanceDurationChart(List<SurveillanceData> surveillances) {
        ClosedSurveillanceDurationChart chart = new ClosedSurveillanceDurationChart();
        return chart.generateChart(surveillances, allAcbs);
    }

    private JFreeChart getClosedSurveillanceByIntervalChart(List<SurveillanceData> surveillances) {
        ClosedSurveillanceByIntervalChart chart = new ClosedSurveillanceByIntervalChart();
        return chart.generateChart(surveillances, allAcbs);
    }

    private JFreeChart getCapApprovalToSurveillanceCloseByIntervalChart(List<SurveillanceData> surveillances) {
        CapApprovalToSurveillanceCloseByIntervalChart chart = new CapApprovalToSurveillanceCloseByIntervalChart();
        return chart.generateChart(surveillances, allAcbs);
    }
}
