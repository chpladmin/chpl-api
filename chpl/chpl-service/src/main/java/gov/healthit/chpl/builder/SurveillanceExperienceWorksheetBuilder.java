package gov.healthit.chpl.builder;

import java.awt.Color;
import java.io.IOException;

import org.apache.poi.ss.usermodel.BorderExtent;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.PropertyTemplate;

import gov.healthit.chpl.dto.surveillance.report.AnnualReportDTO;

public class SurveillanceExperienceWorksheetBuilder extends XlsxWorksheetBuilder {
    private static final int LAST_DATA_COLUMN = 8;
    private static final int LAST_DATA_ROW = 60;
    private static final int MIN_TEXT_AREA_LINES = 10;

    private PropertyTemplate pt;

    public SurveillanceExperienceWorksheetBuilder(final Workbook workbook) {
        super(workbook);
        pt = new PropertyTemplate();
    }

    @Override
    public int getLastDataColumn() {
        return LAST_DATA_COLUMN;
    }

    @Override
    public int getLastDataRow() {
        return LAST_DATA_ROW;
    }

    /**
     * Creates a formatted Excel worksheet with the information in the report.
     * @param report
     * @return
     */
    public Sheet buildWorksheet(final AnnualReportDTO report) throws IOException {
        //create sheet
        Sheet sheet = getSheet("Surveillance Experience", new Color(196, 215, 155));

        //set some styling that applies to the whole sheet
        sheet.setDisplayGridlines(false);
        sheet.setDisplayRowColHeadings(false);

        //columns B, C, and D need a certain width to match the document format
        int colWidth = getColumnWidth(35);
        sheet.setColumnWidth(1, colWidth);
        sheet.setColumnWidth(2,  colWidth);
        sheet.setColumnWidth(3, colWidth);

        //column F needs a certain width to match the document format
        sheet.setColumnWidth(5, getColumnWidth(71));

        addSurveillanceObstacles(sheet, report);
        addFindingsSummary(sheet, report);

        //apply the borders after the sheet has been created
        pt.applyBorders(sheet);
        return sheet;
    }

    private void addSurveillanceObstacles(final Sheet sheet, final AnnualReportDTO report) {
        Row row = getRow(sheet, 1);
        Cell cell = createCell(row, 1);
        cell.setCellStyle(leftAlignedTableHeadingStyle);
        cell.setCellValue("List Any Obstacles Encountered During Surveillance");
        sheet.addMergedRegion(new CellRangeAddress(1, 1, 1, 3));

        row = getRow(sheet, 2);
        cell = createCell(row, 1);
        cell.setCellStyle(topAlignedWrappedStyle);
        cell.setCellValue(report.getObstacleSummary());
        sheet.addMergedRegion(new CellRangeAddress(2, 2, 1, 3));
        //this is user-entered text that wraps so we should try to resize the height
        //of the row to show all the lines of text.
        int lineCount = calculateLineCount(cell.getStringCellValue(), sheet, 1, 3);
        row.setHeightInPoints((Math.max(MIN_TEXT_AREA_LINES, lineCount) * sheet.getDefaultRowHeightInPoints()));
        pt.drawBorders(new CellRangeAddress(1, 2, 1, 3),
                BorderStyle.MEDIUM, BorderExtent.ALL);
    }

    private void addFindingsSummary(final Sheet sheet, final AnnualReportDTO report) {
        Row row = getRow(sheet, 1);
        Cell cell = createCell(row, 5);
        cell.setCellStyle(leftAlignedTableHeadingStyle);
        cell.setCellValue("Describe How Priorities May Have Shifted in Response to Findings in the Field");

        row = getRow(sheet, 2);
        cell = createCell(row, 5);
        cell.setCellStyle(topAlignedWrappedStyle);
        cell.setCellValue(report.getFindingsSummary());
        //this is user-entered text that wraps so we should try to resize the height
        //of the row to show all the lines of text.
        int lineCount = calculateLineCount(cell.getStringCellValue(), sheet, 5, 5);
        //take the max of the height found for this row in the other section and the height
        //found for this row in this section (two sections sharing the same row)
        row.setHeightInPoints(Math.max(row.getHeightInPoints(), 
                (Math.max(MIN_TEXT_AREA_LINES, lineCount) * sheet.getDefaultRowHeightInPoints())));

        pt.drawBorders(new CellRangeAddress(1, 2, 5, 5),
                BorderStyle.MEDIUM, BorderExtent.ALL);
    }
}
