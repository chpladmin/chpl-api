package gov.healthit.chpl.scheduler.job.surveillancereportingactivity;

import java.util.HashMap;
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
        sheet = populateSurveillanceRows(sheet, surveillances);
        sheet = addDurationOfClosedSurveillanceFormula(sheet, surveillances.size());
        return sheet;
    }

    private XSSFSheet populateSurveillanceRows(XSSFSheet sheet, List<CSVRecord> surveillances) {
        int colNum = 0;
        int rowNum = 1;
        Map<Integer, String> mapOfHeaders = getHeaderMap();

        for (CSVRecord surveillance : surveillances) {
            Row row = sheet.createRow(rowNum);
            for (String header : mapOfHeaders.values()) {
                row.createCell(colNum).setCellValue(surveillance.get(header));
                colNum++;
            }
            colNum = 0;
            rowNum++;
        }
        return sheet;
    }

    private XSSFSheet populateHeaderRow(XSSFSheet sheet, List<CSVRecord> surveillances) {
        int colNum = 0;
        Row row = sheet.createRow(0);
        Map<Integer, String> mapOfHeaders = getHeaderMap();

        for (String header : mapOfHeaders.values()) {
            Cell cell = row.createCell(colNum, CellType.STRING);
            cell.setCellValue(header);
            colNum++;
        }
        return sheet;
    }

    private XSSFSheet addDurationOfClosedSurveillanceFormula(XSSFSheet sheet, Integer totalRows) {
        sheet.getRow(0).createCell(30).setCellValue("Duration of Closed Surveillance (days)");

        String unformattedFormula = "IF(K%s<>\"\",DAYS360(J%s,K%s,FALSE),\"\")";
        for (int row = 1; row >= totalRows; row++) {
            sheet.getRow(row).createCell(30).setCellFormula(String.format(unformattedFormula, row, row, row));
        }
        return sheet;

    }

    //private CellType getCellType(String headerName) {
    //    switch (headerName) {
    //    case "":
    //        return CellType.
    //    }
    //}

    private static Map<Integer, String> getHeaderMap() {
        Map<Integer, String> map = new HashMap<Integer, String>();
        map.put(0, "RECORD_STATUS__C");
        map.put(1, "UNIQUE_CHPL_ID__C");
        map.put(2, "URL");
        map.put(3, "ACB_NAME");
        map.put(4, "CERTIFICATION_STATUS");
        map.put(5, "DEVELOPER_NAME");
        map.put(6, "PRODUCT_NAME");
        map.put(7, "PRODUCT_VERSION");
        map.put(8, "SURVEILLANCE_ID");
        map.put(9, "SURVEILLANCE_BEGAN");
        map.put(10, "SURVEILLANCE_ENDED");
        map.put(11, "SURVEILLANCE_TYPE");
        map.put(12, "RANDOMIZED_SITES_USED");
        map.put(13, "SURVEILLED_REQUIREMENT_TYPE");
        map.put(14, "SURVEILLED_REQUIREMENT");
        map.put(15, "SURVEILLANCE_RESULT");
        map.put(16, "NON_CONFORMITY_TYPE");
        map.put(17, "NON_CONFORMITY_STATUS");
        map.put(18, "DATE_OF_DETERMINATION");
        map.put(19, "CAP_APPROVAL_DATE");
        map.put(20, "ACTION_BEGAN_DATE");
        map.put(21, "MUST_COMPLETE_DATE");
        map.put(22, "WAS_COMPLETE_DATE");
        map.put(23, "NON_CONFORMITY_SUMMARY");
        map.put(24, "NON_CONFORMITY_FINDINGS");
        map.put(25, "SITES_PASSED");
        map.put(26, "TOTAL_SITES");
        map.put(27, "DEVELOPER_EXPLANATION");
        map.put(28, "RESOLUTION_DESCRIPTION");

        return map;
    }
}
