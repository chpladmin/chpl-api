package gov.healthit.chpl.scheduler.job.surveillancereportingactivity;

import java.util.List;
import java.util.Map;

import org.apache.commons.csv.CSVRecord;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class SurveillanceDataWorksheet {

    public XSSFWorkbook generateWorksheet(XSSFWorkbook workbook, List<CSVRecord> surveillances) {
        XSSFSheet sheet = workbook.createSheet("surveillance-all");
        sheet = populateSheet(sheet, surveillances);

        return workbook;
    }

    private XSSFSheet populateSheet(XSSFSheet sheet, List<CSVRecord> surveillances) {
        sheet = populateHeaderRow(sheet, surveillances);

        return sheet;
    }

    private XSSFSheet populateHeaderRow(XSSFSheet sheet, List<CSVRecord> surveillances) {

        int colNum = 0;
        Row row = sheet.createRow(0);
        Map<String, String> mapOfFirstRecord = surveillances.get(0).toMap();

        for(String header : mapOfFirstRecord.keySet()) {
            Cell cell = row.createCell(colNum, CellType.STRING);
            cell.setCellValue(header);
            colNum++;
        }
        return sheet;
    }
}
