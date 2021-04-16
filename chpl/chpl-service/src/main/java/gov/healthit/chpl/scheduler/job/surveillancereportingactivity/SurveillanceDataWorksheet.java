package gov.healthit.chpl.scheduler.job.surveillancereportingactivity;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVRecord;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFFormulaEvaluator;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class SurveillanceDataWorksheet {

    private XSSFFormulaEvaluator formulaEvaluator;

    public XSSFWorkbook generateWorksheet(XSSFWorkbook workbook, List<CSVRecord> surveillances) {
        formulaEvaluator = workbook.getCreationHelper().createFormulaEvaluator();
        XSSFSheet sheet = workbook.createSheet("surveillance-all");

        sheet = populateSheet(sheet, surveillances);

        return workbook;
    }

    private XSSFSheet populateSheet(XSSFSheet sheet, List<CSVRecord> surveillances) {
        sheet = populateHeaderRow(sheet, surveillances);
        sheet = populateSurveillanceRows(sheet, surveillances);
        sheet = addFormulaToColumn(sheet, 30, "Duration of Closed Surveillance (days)", "IF(K:K<>\"\",DAYS360(J:J,K:K,FALSE),\"\")", surveillances.size());
        sheet = addFormulaToColumn(sheet, 31, "Time to Assess Conformity (days)", "IF(P:P=\"No Non-Conformity\",AD:AD,DAYS360(J:J,S:S,FALSE))", surveillances.size());
        sheet = addFormulaToColumn(sheet, 32, "Time to Approve CAP (days)", "IF(T:T<>\"\",DAYS360(S:S,T:T),\"\")", surveillances.size());
        sheet = addFormulaToColumn(sheet, 33, "MUST_COMPLETE_MET?", "IF(W:W<>\"\",IF(V:V>=W:W,\"Y\",\"N\"),\"\")", surveillances.size());
        sheet = addFormulaToColumn(sheet, 34, "Duration of CAP (days)", "IF(T:T<>\"\",IF(W:W<>\"\",DAYS360(T:T,W:W),DAYS360(T:T,TODAY())),\"\")", surveillances.size());
        sheet = addFormulaToColumn(sheet, 35, "Time from CAP Approval to Surveillance Close (days)", "IF(T:T<>\"\",IF(K:K<>\"\",DAYS360(T:T,K:K,FALSE),\"\"),\"\")", surveillances.size());
        sheet = addFormulaToColumn(sheet, 36, "Time from CAP Close to Surveillance Close (days)", "IF(W:W<>\"\",IF(K:K<>\"\",DAYS360(W:W,K:K,FALSE),\"\"),\"\")", surveillances.size());
        return sheet;
    }

    private XSSFSheet populateSurveillanceRows(XSSFSheet sheet, List<CSVRecord> surveillances) {
        int colNum = 0;
        int rowNum = 1;
        List<String> headers = getHeaders();

        for (CSVRecord surveillance : surveillances) {
            Row row = sheet.createRow(rowNum);
            for (String header : headers) {
                if (doesItemExistInSurveillanceCsvRecord(surveillance, header)) {
                    row.createCell(colNum).setCellValue(surveillance.get(header));
                }
                colNum++;
            }
            colNum = 0;
            rowNum++;
        }
        return sheet;
    }

    private Boolean doesItemExistInSurveillanceCsvRecord(CSVRecord record, String header) {
     return record.toMap().containsKey(header);
    }

    private XSSFSheet populateHeaderRow(XSSFSheet sheet, List<CSVRecord> surveillances) {
        int colNum = 0;
        Row row = sheet.createRow(0);
        List<String> headers = getHeaders();

        for (String header : headers) {
            Cell cell = row.createCell(colNum, CellType.STRING);
            cell.setCellValue(header);
            colNum++;
        }
        return sheet;
    }

    private XSSFSheet addFormulaToColumn(XSSFSheet sheet, Integer columnForFormula, String headerText, String formula, Integer totalRows) {
        CellStyle formulaHeaderStyle = sheet.getWorkbook().createCellStyle();
        formulaHeaderStyle.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
        formulaHeaderStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        sheet.getRow(0).createCell(columnForFormula).setCellValue(headerText);
        sheet.getRow(0).getCell(columnForFormula).setCellStyle(formulaHeaderStyle);
        for (int row = 1; row <= totalRows; row++) {
            Cell formulaCell = sheet.getRow(row).createCell(columnForFormula);
            formulaCell.setCellFormula(formula);
        }
        return sheet;

    }

    private static List<String> getHeaders() {
        List<String> headers = new ArrayList<String>();
        headers.add("?RECORD_STATUS__C");
        headers.add("UNIQUE_CHPL_ID__C");
        headers.add("URL");
        headers.add("ACB_NAME");
        headers.add("CERTIFICATION_STATUS");
        headers.add("DEVELOPER_NAME");
        headers.add("PRODUCT_NAME");
        headers.add("PRODUCT_VERSION");
        headers.add("SURVEILLANCE_ID");
        headers.add("SURVEILLANCE_BEGAN");
        headers.add("SURVEILLANCE_ENDED");
        headers.add("SURVEILLANCE_TYPE");
        headers.add("RANDOMIZED_SITES_USED");
        headers.add("SURVEILLED_REQUIREMENT_TYPE");
        headers.add("SURVEILLED_REQUIREMENT");
        headers.add("SURVEILLANCE_RESULT");
        headers.add("NON_CONFORMITY_TYPE");
        headers.add("NON_CONFORMITY_STATUS");
        headers.add("DATE_OF_DETERMINATION");
        headers.add("CAP_APPROVAL_DATE");
        headers.add("ACTION_BEGAN_DATE");
        headers.add("MUST_COMPLETE_DATE");
        headers.add("WAS_COMPLETE_DATE");
        headers.add("NON_CONFORMITY_SUMMARY");
        headers.add("NON_CONFORMITY_FINDINGS");
        headers.add("SITES_PASSED");
        headers.add("TOTAL_SITES");
        headers.add("DEVELOPER_EXPLANATION");
        headers.add("RESOLUTION_DESCRIPTION");

        return headers;
    }
}
