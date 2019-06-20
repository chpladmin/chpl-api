package gov.healthit.chpl.builder;

import java.awt.Color;
import java.io.IOException;

import org.apache.poi.ss.usermodel.BorderExtent;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.PropertyTemplate;

import gov.healthit.chpl.dto.surveillance.report.AnnualReportDTO;

public class SurveillanceSummaryWorksheetBuilder extends XlsxWorksheetBuilder {
    private static final int LAST_DATA_COLUMN = 10;
    private static final int LAST_DATA_ROW = 60;

    private PropertyTemplate pt;

    public SurveillanceSummaryWorksheetBuilder(final Workbook workbook) {
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
    public Sheet buildWorksheet() throws IOException {
        //create sheet
        Sheet sheet = getSheet("Surveillance Summary", new Color(196, 215, 155));

        //set some styling that applies to the whole sheet
        sheet.setDisplayGridlines(false);
        sheet.setDisplayRowColHeadings(false);

        //columns B, C, D, and E need a certain width to match the document format
        sheet.setColumnWidth(1, getColumnWidth(65));
        int colWidth = getColumnWidth(12);
        sheet.setColumnWidth(2,  colWidth);
        sheet.setColumnWidth(3, colWidth);
        sheet.setColumnWidth(4,  colWidth);

        //column G needs a certain width to match the document format
        sheet.setColumnWidth(6, getColumnWidth(40));

        addSurveillanceCounts(sheet);
        addComplaintsCounts(sheet);

        //apply the borders after the sheet has been created
        pt.applyBorders(sheet);
        return sheet;
    }

    private void addSurveillanceCounts(final Sheet sheet) {
        Row row = getRow(sheet, 1);
        Cell cell = createCell(row, 1);
        cell.setCellStyle(rightAlignedTableHeadingStyle);
        cell.setCellValue("");
        cell = createCell(row, 2);
        cell.setCellStyle(rightAlignedTableHeadingStyle);
        cell.setCellValue("Reactive");
        cell = createCell(row, 3);
        cell.setCellStyle(rightAlignedTableHeadingStyle);
        cell.setCellValue("Randomized");
        cell = createCell(row, 4);
        cell.setCellStyle(rightAlignedTableHeadingStyle);
        cell.setCellValue("Total");

        createSurveillanceCountsSubheadingRow(sheet, "Surveillance Counts", 2);
        createSurveillanceCountsDataRow(sheet, "Number of Certificates Surveilled",
                -1, -1, -1, 3);

        createSurveillanceCountsSubheadingRow(sheet, "Primary Surveillance Processes", 4);
        createSurveillanceCountsDataRow(sheet, "In-the-Field",
                -1, -1, -1, 5);
        createSurveillanceCountsDataRow(sheet, "Controlled/Test Environment",
                -1, -1, -1, 6);
        createSurveillanceCountsDataRow(sheet, "Correspondence with Complainant/Developer",
                -1, -1, -1, 7);
        createSurveillanceCountsDataRow(sheet, "Review of Websites/Written Documentation",
                -1, -1, -1, 8);
        createSurveillanceCountsDataRow(sheet, "Other",
                -1, -1, -1, 9);

        createSurveillanceCountsSubheadingRow(sheet, "Outcome of the Surveillance", 10);
        createSurveillanceCountsDataRow(sheet, "Number of Certificates with No Non-Conformities Found",
                -1, -1, -1, 11);
        createSurveillanceCountsDataRow(sheet, "Number of Certificates with Non-Conformities Found",
                -1, -1, -1, 12);
        createSurveillanceCountsDataRow(sheet, "Number of Corrective Action Plans",
                -1, -1, -1, 13);

        createSurveillanceCountsSubheadingRow(sheet, "Certification Status Resultant of Surveillance", 14);
        createSurveillanceCountsDataRow(sheet, "Number of Certificates with a Corrected Non-conformity",
                -1, -1, -1, 15);
        createSurveillanceCountsDataRow(sheet, "Number of Certificates Suspended",
                -1, -1, -1, 16);
        createSurveillanceCountsDataRow(sheet, "Number of Certificates Withdrawn by Developer",
                -1, -1, -1, 17);
        createSurveillanceCountsDataRow(sheet, "Number of Certificates Withdrawn by ONC-ACB",
                -1, -1, -1, 18);

        pt.drawBorders(new CellRangeAddress(1, 18, 1, 4),
                BorderStyle.MEDIUM, BorderExtent.OUTSIDE);
    }

    private void addComplaintsCounts(final Sheet sheet) {
        Row row = getRow(sheet, 1);
        Cell cell = createCell(row, 6);
        cell.setCellStyle(rightAlignedTableHeadingStyle);
        cell.setCellValue("");
        cell = createCell(row, 7);
        cell.setCellStyle(rightAlignedTableHeadingStyle);
        cell.setCellValue("Total");

        createComplaintCountsSubheadingRow(sheet, "Complaint Counts", 2);
        createComplainCountsDataRow(sheet, "Total Number Received", -1, 3);
        createComplainCountsDataRow(sheet, "Total Number Referred by ONC", -1, 4);

        createComplaintCountsSubheadingRow(sheet, "Surveillance Activities Trigged by Complaints", 5);
        createComplainCountsDataRow(sheet, "Number that Led to Surveillance Activities", -1, 6);
        createComplainCountsDataRow(sheet, "Number of Surveillance Activities", -1, 7);

        createComplaintCountsSubheadingRow(sheet, "Non-Conformities Found by Complaints", 8);
        createComplainCountsDataRow(sheet, "Number that Led to Non-conformities", -1, 9);
        createComplainCountsDataRow(sheet, "Number of Non-conformities", -1, 10);

        //dark outside border around the whole table
        pt.drawBorders(new CellRangeAddress(1, 10, 6, 7),
                BorderStyle.MEDIUM, BorderExtent.OUTSIDE);
    }

    private void createSurveillanceCountsSubheadingRow(final Sheet sheet, final String text, final int rowNum) {
        Row row = getRow(sheet, rowNum);
        Cell cell = createCell(row, 1);
        cell.setCellStyle(tableSubheadingStyle);
        cell.setCellValue(text);
        cell = createCell(row, 2);
        cell.setCellStyle(tableSubheadingStyle);
        cell = createCell(row, 3);
        cell.setCellStyle(tableSubheadingStyle);
        cell = createCell(row, 4);
        cell.setCellStyle(tableSubheadingStyle);
        //light top and bottom borders around each subheading
        pt.drawBorders(new CellRangeAddress(rowNum, rowNum, 1, 4),
                BorderStyle.THIN, BorderExtent.OUTSIDE_HORIZONTAL);
    }

    private void createSurveillanceCountsDataRow(final Sheet sheet, final String name,
            final int reactiveValue, final int randomziedValue, final int totalValue, final int rowNum) {
        Row row = getRow(sheet, rowNum);
        Cell cell = createCell(row, 1);
        cell.setCellValue(name);
        cell = createCell(row, 2);
        cell.setCellValue(reactiveValue);
        cell = createCell(row, 3);
        cell.setCellValue(randomziedValue);
        cell = createCell(row, 4);
        cell.setCellValue(totalValue);
        //dotted top and bottom borders around each data field
        pt.drawBorders(new CellRangeAddress(rowNum, rowNum, 1, 4),
                BorderStyle.HAIR, BorderExtent.OUTSIDE_HORIZONTAL);
    }

    private void createComplaintCountsSubheadingRow(final Sheet sheet, final String text, final int rowNum) {
        Row row = getRow(sheet, rowNum);
        Cell cell = createCell(row, 6);
        cell.setCellStyle(tableSubheadingStyle);
        cell.setCellValue(text);
        cell = createCell(row, 7);
        cell.setCellStyle(tableSubheadingStyle);
       //light top and bottom borders around each subheading
        pt.drawBorders(new CellRangeAddress(rowNum, rowNum, 6, 7),
                BorderStyle.THIN, BorderExtent.OUTSIDE_HORIZONTAL);
    }

    private void createComplainCountsDataRow(final Sheet sheet, final String name, final int value, final int rowNum) {
        Row row = getRow(sheet, rowNum);
        Cell cell = createCell(row, 6);
        cell.setCellValue(name);
        cell = createCell(row, 7);
        cell.setCellValue(value);
        //dotted top and bottom borders around each data field
        pt.drawBorders(new CellRangeAddress(rowNum, rowNum, 6, 7),
                BorderStyle.HAIR, BorderExtent.OUTSIDE_HORIZONTAL);
    }
}
