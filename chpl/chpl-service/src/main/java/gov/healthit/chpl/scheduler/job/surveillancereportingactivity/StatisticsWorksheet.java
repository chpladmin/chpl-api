package gov.healthit.chpl.scheduler.job.surveillancereportingactivity;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class StatisticsWorksheet {
    private static final Integer STATISTICS_COLUMN_WIDTH = 17 * 256;
    private static final Short DEFUALT_FONT_SIZE = 11;
    private static final Integer ASSESS_CONFORMITY_COLUMN = 2;
    private static final Integer APPROVE_CAP_COLUMN = 3;
    private static final Integer DURATION_CAP_COLUMN = 4;
    private static final Integer CAP_APPROVAL_TO_SURV_CLOSE_COLUMN = 5;
    private static final Integer CAP_CLOSE_TO_SURV_CLOSE_COLUMN = 6;
    private static final Integer DURATION_SURV_CLOSE_COLUMN = 7;


    private List<String> mainHeaders = Arrays.asList(
            "Time to Assess Conformity", "Time to Approve CAP", "Duration of CAP", "Time from CAP Approval to Surveillance Close",
            "Time from CAP Close to Surveillance Close", "Duration of Closed Surveillance");



    public XSSFWorkbook generateWorksheet(XSSFWorkbook workbook) {
        XSSFSheet sheet = workbook.createSheet("Stats");
        sheet.setDisplayGridlines(false);
        sheet = generateMainHeaders(sheet);
        sheet = generateStatisticsForAcb(sheet, "Drummond Group", 1);
        sheet = generateStatisticsForAcb(sheet, "ICSA Labs", 25);
        return workbook;
    }

    private XSSFSheet generateStatisticsForAcb(XSSFSheet sheet, String acbName, Integer startingRow) {
        Row row = sheet.createRow(startingRow);
        Cell acbNameCell = row.createCell(0);
        acbNameCell.setCellStyle(getAcbNameStyle(sheet.getWorkbook()));
        acbNameCell.setCellValue(acbName);

        row = sheet.createRow(startingRow + 1);
        Cell measuresOfCentralTendencyCell = row.createCell(0);
        measuresOfCentralTendencyCell.setCellStyle(getSubheaderStyle(sheet.getWorkbook()));
        measuresOfCentralTendencyCell.setCellValue("Measures of Central Tendency (days)");

        sheet = generateMeasuresOfCentralTendency(sheet, startingRow + 2, acbName);

        return sheet;
    }

    private XSSFSheet generateMeasuresOfCentralTendency(XSSFSheet sheet, Integer startingRow, String acbName) {
        Row minRow = sheet.createRow(startingRow);
        minRow.createCell(1).setCellValue("Minimum");
        minRow.createCell(ASSESS_CONFORMITY_COLUMN).setCellFormula(getStatFormula("MINIFS", acbName, "AE", 48));
        minRow.createCell(APPROVE_CAP_COLUMN).setCellFormula(getStatFormula("MINIFS", acbName, "AF", 48));
        minRow.createCell(DURATION_CAP_COLUMN).setCellFormula(getStatFormula("MINIFS", acbName, "AH", 48));
        minRow.createCell(CAP_APPROVAL_TO_SURV_CLOSE_COLUMN).setCellFormula(getStatFormula("MINIFS", acbName, "AI", 48));
        minRow.createCell(CAP_CLOSE_TO_SURV_CLOSE_COLUMN).setCellFormula(getStatFormula("MINIFS", acbName, "AJ", 48));
        minRow.createCell(DURATION_SURV_CLOSE_COLUMN).setCellFormula(getStatFormula("MINIFS", acbName, "AD", 48));

        Row maxRow = sheet.createRow(startingRow + 1);
        maxRow.createCell(1).setCellValue("Maximum");
        maxRow.createCell(ASSESS_CONFORMITY_COLUMN).setCellFormula(getStatFormula("MAXIFS", acbName, "AE", 48));
        maxRow.createCell(APPROVE_CAP_COLUMN).setCellFormula(getStatFormula("MAXIFS", acbName, "AF", 48));
        maxRow.createCell(DURATION_CAP_COLUMN).setCellFormula(getStatFormula("MAXIFS", acbName, "AH", 48));
        maxRow.createCell(CAP_APPROVAL_TO_SURV_CLOSE_COLUMN).setCellFormula(getStatFormula("MAXIFS", acbName, "AI", 48));
        maxRow.createCell(CAP_CLOSE_TO_SURV_CLOSE_COLUMN).setCellFormula(getStatFormula("MAXIFS", acbName, "AJ", 48));
        maxRow.createCell(DURATION_SURV_CLOSE_COLUMN).setCellFormula(getStatFormula("MAXIFS", acbName, "AD", 48));

        Row avgRow = sheet.createRow(startingRow + 2);
        avgRow.createCell(1).setCellValue("Mean");
        avgRow.createCell(ASSESS_CONFORMITY_COLUMN).setCellFormula(getStatFormula("AVERAGEIFS", acbName, "AE", 48));
        avgRow.createCell(APPROVE_CAP_COLUMN).setCellFormula(getStatFormula("AVERAGEIFS", acbName, "AF", 48));
        avgRow.createCell(DURATION_CAP_COLUMN).setCellFormula(getStatFormula("AVERAGEIFS", acbName, "AH", 48));
        avgRow.createCell(CAP_APPROVAL_TO_SURV_CLOSE_COLUMN).setCellFormula(getStatFormula("AVERAGEIFS", acbName, "AI", 48));
        avgRow.createCell(CAP_CLOSE_TO_SURV_CLOSE_COLUMN).setCellFormula(getStatFormula("AVERAGEIFS", acbName, "AJ", 48));
        avgRow.createCell(DURATION_SURV_CLOSE_COLUMN).setCellFormula(getStatFormula("AVERAGEIFS", acbName, "AD", 48));

        return sheet;
    }

    private XSSFSheet generateMainHeaders(XSSFSheet sheet) {
        AtomicInteger column = new AtomicInteger(2);
        Row row = sheet.createRow(0);

        mainHeaders.stream()
                .forEach(header -> {
                    Cell cell = row.createCell(column.get());
                    sheet.setColumnWidth(column.get(), STATISTICS_COLUMN_WIDTH);
                    cell.setCellValue(header);
                    cell.setCellStyle(getMainHeaderStyle(sheet.getWorkbook()));
                    column.incrementAndGet();
                });
        return sheet;
    }

    private CellStyle getMainHeaderStyle(XSSFWorkbook workbook) {
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontName("Calibri");
        font.setFontHeightInPoints(DEFUALT_FONT_SIZE);

        CellStyle style = workbook.createCellStyle();
        style.setWrapText(true);
        style.setBorderBottom(BorderStyle.THIN);
        style.setFont(font);
        return style;
    }

    private CellStyle getAcbNameStyle(XSSFWorkbook workbook) {
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontName("Calibri");
        font.setFontHeightInPoints(DEFUALT_FONT_SIZE);

        CellStyle style = workbook.createCellStyle();
        style.setFont(font);
        return style;
    }

    private CellStyle getSubheaderStyle(XSSFWorkbook workbook) {
        Font font = workbook.createFont();
        font.setItalic(true);
        font.setFontName("Calibri");
        font.setFontHeightInPoints((short)11);

        CellStyle style = workbook.createCellStyle();
        style.setFont(font);
        return style;
    }

    private CellStyle getDefaultStyle(XSSFWorkbook workbook) {
        Font font = workbook.createFont();
        font.setFontName("Calibri");
        font.setFontHeightInPoints((short)11);

        CellStyle style = workbook.createCellStyle();
        style.setFont(font);
        return style;
    }

    private String getStatFormula(String function, String acbName, String statColumn, Integer dataRowCount) {
        // See for reason we need '_xlfn.'
        //https://stackoverflow.com/questions/62614172/apache-poi-excel-formula-entering-symbols-where-they-dont-belong
        String baseFormula = "_xlfn.%s('surveillance-all'!%s2:%s%s, 'surveillance-all'!D2:D%s, \"%s\")";
        return String.format(baseFormula, function, statColumn,  statColumn, dataRowCount, dataRowCount, acbName);
    }

}
