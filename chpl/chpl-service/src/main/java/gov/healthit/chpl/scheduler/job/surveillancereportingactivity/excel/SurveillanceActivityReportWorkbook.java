package gov.healthit.chpl.scheduler.job.surveillancereportingactivity.excel;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbookFactory;

import gov.healthit.chpl.scheduler.job.surveillancereportingactivity.SurveillanceData;

public class SurveillanceActivityReportWorkbook {

    @SuppressWarnings("resource")
    public void generateWorkbook(List<SurveillanceData> surveillances) throws IOException {
        Workbook workbook = XSSFWorkbookFactory.create(true);

        SurveillanceDataWorksheet surveillanceDataWorksheet = new SurveillanceDataWorksheet(workbook);
        StatisticsWorksheet statsSheet = new StatisticsWorksheet(workbook);
        ChartsWorksheet chartsSheet = new ChartsWorksheet(workbook);

        surveillanceDataWorksheet.generateWorksheet(surveillances);
        statsSheet.generateWorksheet(surveillances);
        chartsSheet.generateWorksheet(surveillances);

        writeFileToDisk(workbook);
    }

    private void writeFileToDisk(Workbook workbook) {
        try {
            FileOutputStream outputStream = new FileOutputStream("c://CHPL//files//todd.xlsx");
            workbook.write(outputStream);
            workbook.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
