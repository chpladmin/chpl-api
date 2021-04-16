package gov.healthit.chpl.scheduler.job.surveillancereportingactivity;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import org.apache.commons.csv.CSVRecord;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class SurveillanceActivityReportWorkbook {

    public void generateWorkbook(List<CSVRecord> surveillances) {
        SurveillanceDataWorksheet dataSheet = new SurveillanceDataWorksheet();

        XSSFWorkbook workbook = new XSSFWorkbook();

        dataSheet.generateWorksheet(workbook, surveillances);

        writeFileToDisk(workbook);
    }

    private void writeFileToDisk(XSSFWorkbook workbook) {
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
