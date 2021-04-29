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

import gov.healthit.chpl.scheduler.job.surveillancereportingactivity.SurveillanceData;
import gov.healthit.chpl.scheduler.job.surveillancereportingactivity.charts.ClosedSurveillanceDurationChart;

public class ChartsWorksheet {
    private Workbook workbook;

    public ChartsWorksheet(Workbook workbook) {
        this.workbook = workbook;
    }

    public Sheet generateWorksheet(List<SurveillanceData> surveillances) throws IOException {
        Sheet sheet = workbook.createSheet("Charts");
        insertChart(sheet, getClosedSurveillanceDurationChart(surveillances));
        return sheet;
    }

    @SuppressWarnings("resource")
    private Sheet insertChart(Sheet sheet, JFreeChart chart) throws IOException {
        Drawing drawing = sheet.createDrawingPatriarch();

        BufferedImage image = chart.createBufferedImage(600, 300, 600, 300, null);
        int imageIndex = sheet.getWorkbook().addPicture(ChartUtils.encodeAsPNG(image), Workbook.PICTURE_TYPE_PNG);

        ClientAnchor anchor = sheet.getWorkbook().getCreationHelper().createClientAnchor();
        anchor.setCol1(3);
        anchor.setRow1(2);
        Picture picture = drawing.createPicture(anchor, imageIndex);
        picture.resize();
        return sheet;
    }

    private JFreeChart getClosedSurveillanceDurationChart(List<SurveillanceData> surveillances) {
        ClosedSurveillanceDurationChart chart = new ClosedSurveillanceDurationChart();
        return chart.generateChart(surveillances);
    }
}
