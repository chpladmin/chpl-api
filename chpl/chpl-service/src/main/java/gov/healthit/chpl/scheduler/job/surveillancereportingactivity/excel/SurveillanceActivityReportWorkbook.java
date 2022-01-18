package gov.healthit.chpl.scheduler.job.surveillancereportingactivity.excel;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbookFactory;

import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.scheduler.job.surveillancereportingactivity.SurveillanceData;
import lombok.extern.log4j.Log4j2;

@Log4j2(topic = "surveillanceActivityReportJobLogger")
public class SurveillanceActivityReportWorkbook {
    private XSSFWorkbookFactory workbookFactory;

    public SurveillanceActivityReportWorkbook() {
        workbookFactory = new XSSFWorkbookFactory();
    }

    @SuppressWarnings("resource")
    public File generateWorkbook(List<SurveillanceData> surveillances, List<CertificationBodyDTO> allAcbs) throws IOException {
        Workbook workbook = null;
        try {
            LOGGER.info("Starting to build the Excel spreadhseet.");
            workbook = workbookFactory.create();

            SurveillanceDataWorksheet surveillanceDataWorksheet = new SurveillanceDataWorksheet(workbook);
            StatisticsWorksheet statsSheet = new StatisticsWorksheet(workbook, allAcbs);
            ChartsWorksheet chartsSheet = new ChartsWorksheet(workbook, allAcbs);
            surveillanceDataWorksheet.generateWorksheet(surveillances);
            statsSheet.generateWorksheet(surveillances);
            chartsSheet.generateWorksheet(surveillances);
            return writeFileToDisk(workbook);
        } finally {
            LOGGER.info("Completed to build the Excel spreadhseet.");
            workbook.close();
        }
    }

    private File writeFileToDisk(Workbook workbook) throws IOException {
        File saveFile = File.createTempFile("surveillance_activity_report", ".xlsx");
        try (FileOutputStream outputStream = new FileOutputStream(saveFile)) {
            workbook.write(outputStream);
        }
        return saveFile;
    }
}
