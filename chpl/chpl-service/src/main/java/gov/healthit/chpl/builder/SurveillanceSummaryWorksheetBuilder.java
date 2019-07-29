package gov.healthit.chpl.builder;

import java.awt.Color;
import java.io.IOException;

import org.apache.poi.ss.usermodel.BorderExtent;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.PropertyTemplate;
import org.springframework.stereotype.Component;

@Component
public class SurveillanceSummaryWorksheetBuilder {
    private static final int LAST_DATA_COLUMN = 10;
    private static final int LAST_DATA_ROW = 60;

    private PropertyTemplate pt;

    public SurveillanceSummaryWorksheetBuilder() {
    }

    public int getLastDataColumn() {
        return LAST_DATA_COLUMN;
    }

    public int getLastDataRow() {
        return LAST_DATA_ROW;
    }

    /**
     * Creates a formatted Excel worksheet with the information in the report.
     * @param report
     * @return
     */
    public Sheet buildWorksheet(final SurveillanceReportWorkbookWrapper workbook) throws IOException {
        pt = new PropertyTemplate();

        //create sheet
        Sheet sheet = workbook.getSheet("Surveillance Summary", new Color(196, 215, 155), getLastDataColumn());

        //set some styling that applies to the whole sheet
        sheet.setDisplayGridlines(false);
        sheet.setDisplayRowColHeadings(false);

        //columns B, C, D, and E need a certain width to match the document format
        sheet.setColumnWidth(1, workbook.getColumnWidth(65));
        int colWidth = workbook.getColumnWidth(12);
        sheet.setColumnWidth(2,  colWidth);
        sheet.setColumnWidth(3, colWidth);
        sheet.setColumnWidth(4,  colWidth);

        //column G needs a certain width to match the document format
        sheet.setColumnWidth(6, workbook.getColumnWidth(40));

        addSurveillanceCounts(workbook, sheet);
        addComplaintsCounts(workbook, sheet);

        //apply the borders after the sheet has been created
        pt.applyBorders(sheet);
        return sheet;
    }

    private void addSurveillanceCounts(final SurveillanceReportWorkbookWrapper workbook, final Sheet sheet) {
        Row row = workbook.getRow(sheet, 1);
        Cell cell = workbook.createCell(row, 1, workbook.getRightAlignedTableHeadingStyle());
        cell.setCellValue("");
        cell = workbook.createCell(row, 2, workbook.getRightAlignedTableHeadingStyle());
        cell.setCellValue("Reactive");
        cell = workbook.createCell(row, 3, workbook.getRightAlignedTableHeadingStyle());
        cell.setCellValue("Randomized");
        cell = workbook.createCell(row, 4, workbook.getRightAlignedTableHeadingStyle());
        cell.setCellValue("Total");

        createSurveillanceCountsSubheadingRow(workbook, sheet, "Surveillance Counts", 2);
        createSurveillanceCountsDataRow(workbook, sheet, "Number of Certificates Surveilled",
                -1, -1, -1, 3);

        createSurveillanceCountsSubheadingRow(workbook, sheet, "Primary Surveillance Processes", 4);
        createSurveillanceCountsDataRow(workbook, sheet, "In-the-Field",
                -1, -1, -1, 5);
        createSurveillanceCountsDataRow(workbook, sheet, "Controlled/Test Environment",
                -1, -1, -1, 6);
        createSurveillanceCountsDataRow(workbook, sheet, "Correspondence with Complainant/Developer",
                -1, -1, -1, 7);
        createSurveillanceCountsDataRow(workbook, sheet, "Review of Websites/Written Documentation",
                -1, -1, -1, 8);
        createSurveillanceCountsDataRow(workbook, sheet, "Other",
                -1, -1, -1, 9);

        createSurveillanceCountsSubheadingRow(workbook, sheet, "Outcome of the Surveillance", 10);
        createSurveillanceCountsDataRow(workbook, sheet, "Number of Certificates with No Non-Conformities Found",
                -1, -1, -1, 11);
        createSurveillanceCountsDataRow(workbook, sheet, "Number of Certificates with Non-Conformities Found",
                -1, -1, -1, 12);
        createSurveillanceCountsDataRow(workbook, sheet, "Number of Corrective Action Plans",
                -1, -1, -1, 13);

        createSurveillanceCountsSubheadingRow(workbook, sheet, "Certification Status Resultant of Surveillance", 14);
        createSurveillanceCountsDataRow(workbook, sheet, "Number of Certificates with a Corrected Non-conformity",
                -1, -1, -1, 15);
        createSurveillanceCountsDataRow(workbook, sheet, "Number of Certificates Suspended",
                -1, -1, -1, 16);
        createSurveillanceCountsDataRow(workbook, sheet, "Number of Certificates Withdrawn by Developer",
                -1, -1, -1, 17);
        createSurveillanceCountsDataRow(workbook, sheet, "Number of Certificates Withdrawn by ONC-ACB",
                -1, -1, -1, 18);

        pt.drawBorders(new CellRangeAddress(1, 18, 1, 4),
                BorderStyle.MEDIUM, BorderExtent.OUTSIDE);
    }

    private void addComplaintsCounts(final SurveillanceReportWorkbookWrapper workbook, final Sheet sheet) {
        Row row = workbook.getRow(sheet, 1);
        Cell cell = workbook.createCell(row, 6, workbook.getRightAlignedTableHeadingStyle());
        cell.setCellValue("");
        cell = workbook.createCell(row, 7, workbook.getRightAlignedTableHeadingStyle());
        cell.setCellValue("Total");

        createComplaintCountsSubheadingRow(workbook, sheet, "Complaint Counts", 2);
        createComplainCountsDataRow(workbook, sheet, "Total Number Received", -1, 3);
        createComplainCountsDataRow(workbook, sheet, "Total Number Referred by ONC", -1, 4);

        createComplaintCountsSubheadingRow(workbook, sheet, "Surveillance Activities Trigged by Complaints", 5);
        createComplainCountsDataRow(workbook, sheet, "Number that Led to Surveillance Activities", -1, 6);
        createComplainCountsDataRow(workbook, sheet, "Number of Surveillance Activities", -1, 7);

        createComplaintCountsSubheadingRow(workbook, sheet, "Non-Conformities Found by Complaints", 8);
        createComplainCountsDataRow(workbook, sheet, "Number that Led to Non-conformities", -1, 9);
        createComplainCountsDataRow(workbook, sheet, "Number of Non-conformities", -1, 10);

        //dark outside border around the whole table
        pt.drawBorders(new CellRangeAddress(1, 10, 6, 7),
                BorderStyle.MEDIUM, BorderExtent.OUTSIDE);
    }

    private void createSurveillanceCountsSubheadingRow(final SurveillanceReportWorkbookWrapper workbook,
            final Sheet sheet, final String text, final int rowNum) {
        Row row = workbook.getRow(sheet, rowNum);
        Cell cell = workbook.createCell(row, 1, workbook.getTableSubheadingStyle());
        cell.setCellValue(text);
        cell = workbook.createCell(row, 2, workbook.getTableSubheadingStyle());
        cell = workbook.createCell(row, 3, workbook.getTableSubheadingStyle());
        cell = workbook.createCell(row, 4, workbook.getTableSubheadingStyle());
        //light top and bottom borders around each subheading
        pt.drawBorders(new CellRangeAddress(rowNum, rowNum, 1, 4),
                BorderStyle.THIN, BorderExtent.OUTSIDE_HORIZONTAL);
    }

    private void createSurveillanceCountsDataRow(final SurveillanceReportWorkbookWrapper workbook,
            final Sheet sheet, final String name,
            final int reactiveValue, final int randomziedValue, final int totalValue, final int rowNum) {
        Row row = workbook.getRow(sheet, rowNum);
        Cell cell = workbook.createCell(row, 1);
        cell.setCellValue(name);
        cell = workbook.createCell(row, 2);
        cell.setCellValue(reactiveValue);
        cell = workbook.createCell(row, 3);
        cell.setCellValue(randomziedValue);
        cell = workbook.createCell(row, 4);
        cell.setCellValue(totalValue);
        //dotted top and bottom borders around each data field
        pt.drawBorders(new CellRangeAddress(rowNum, rowNum, 1, 4),
                BorderStyle.HAIR, BorderExtent.OUTSIDE_HORIZONTAL);
    }

    private void createComplaintCountsSubheadingRow(final SurveillanceReportWorkbookWrapper workbook,
            final Sheet sheet, final String text, final int rowNum) {
        Row row = workbook.getRow(sheet, rowNum);
        Cell cell = workbook.createCell(row, 6, workbook.getTableSubheadingStyle());
        cell.setCellValue(text);
        cell = workbook.createCell(row, 7, workbook.getTableSubheadingStyle());
       //light top and bottom borders around each subheading
        pt.drawBorders(new CellRangeAddress(rowNum, rowNum, 6, 7),
                BorderStyle.THIN, BorderExtent.OUTSIDE_HORIZONTAL);
    }

    private void createComplainCountsDataRow(final SurveillanceReportWorkbookWrapper workbook,
            final Sheet sheet, final String name, final int value, final int rowNum) {
        Row row = workbook.getRow(sheet, rowNum);
        Cell cell = workbook.createCell(row, 6);
        cell.setCellValue(name);
        cell = workbook.createCell(row, 7);
        cell.setCellValue(value);
        //dotted top and bottom borders around each data field
        pt.drawBorders(new CellRangeAddress(rowNum, rowNum, 6, 7),
                BorderStyle.HAIR, BorderExtent.OUTSIDE_HORIZONTAL);
    }
}
