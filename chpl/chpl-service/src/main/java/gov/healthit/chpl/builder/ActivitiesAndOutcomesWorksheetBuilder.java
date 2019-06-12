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

public class ActivitiesAndOutcomesWorksheetBuilder extends XlsxWorksheetBuilder {
    private static final int LAST_DATA_COLUMN = 35;
    private static final int LAST_DATA_ROW = 60;

    private static final int COL_CHPL_ID = 1;
    private static final int COL_SURV_ID = 2;
    //cols 3-8 get hidden
    private static final int COL_SURV_ACTIVITY_TRACKER = 3;
    private static final int COL_RELATED_COMPLAINT = 4;
    private static final int COL_Q1 = 5;
    private static final int COL_Q2 = 6;
    private static final int COL_Q3 = 7;
    private static final int COL_Q4 = 8;
    private static final int COL_CERT_EDITION = 9;
    private static final int COL_DEVELOPER_NAME = 10;
    private static final int COL_PRODUCT_NAME = 11;
    private static final int COL_PRODUCT_VERSION = 12;
    private static final int COL_K1_REVIEWED = 13;
    private static final int COL_SURV_TYPE = 14;
    private static final int COL_SURV_LOCATION_COUNT = 15;
    private static final int COL_SURV_BEGIN = 16;
    private static final int COL_SURV_END = 17;
    private static final int COL_SURV_OUTCOME = 18;
    private static final int COL_NONCONFORMITY_TYPES_RESULTANT = 19;
    private static final int COL_CERT_STATUS_RESULTANT = 20;
    private static final int COL_SUSPENDED = 21;
    private static final int COL_SURV_PROCESS_TYPE = 22;
    private static final int COL_SURV_GROUNDS = 23;
    private static final int COL_NONCONFORMITY_CAUSES = 24;
    private static final int COL_NONCONFORMITY_NATURES = 25;
    private static final int COL_SURV_STEPS = 26;
    private static final int COL_ENGAGEMENT_STEPS = 27;
    private static final int COL_ADDITIONAL_COSTS = 28;
    private static final int COL_LIMITATIONS_EVAL = 29;
    private static final int COL_NONDISCLOSURE_EVAL = 30;
    private static final int COL_DEV_RESOLUTION = 31;
    private static final int COL_COMPLETED_CAP = 32;

    private PropertyTemplate pt;

    public ActivitiesAndOutcomesWorksheetBuilder(final Workbook workbook) {
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
        Sheet sheet = getSheet("Activities and outcomes", new Color(141, 180, 226));

        //set some styling that applies to the whole sheet
        sheet.setDisplayGridlines(false);
        sheet.setDisplayRowColHeadings(false);

        //all columns need a certain width to match the document format
        int sharedColWidth = getColumnWidth(11.78);
        sheet.setColumnWidth(COL_CHPL_ID, sharedColWidth);
        sheet.setColumnWidth(COL_SURV_ID,  sharedColWidth);
        sheet.setColumnWidth(COL_SURV_ACTIVITY_TRACKER, sharedColWidth);
        sheet.setColumnWidth(COL_RELATED_COMPLAINT, sharedColWidth);
        int quarterColWidth = getColumnWidth(2.22);
        sheet.setColumnWidth(COL_Q1, quarterColWidth);
        sheet.setColumnWidth(COL_Q2, quarterColWidth);
        sheet.setColumnWidth(COL_Q3, quarterColWidth);
        sheet.setColumnWidth(COL_Q4, quarterColWidth);
        sheet.setColumnWidth(COL_CERT_EDITION, sharedColWidth);
        sheet.setColumnWidth(COL_DEVELOPER_NAME, sharedColWidth);
        sheet.setColumnWidth(COL_PRODUCT_NAME, sharedColWidth);
        sheet.setColumnWidth(COL_PRODUCT_VERSION, sharedColWidth);
        sheet.setColumnWidth(COL_K1_REVIEWED, sharedColWidth);
        sheet.setColumnWidth(COL_SURV_TYPE, getColumnWidth(13.67));
        sheet.setColumnWidth(COL_SURV_LOCATION_COUNT, sharedColWidth);
        sheet.setColumnWidth(COL_SURV_BEGIN, sharedColWidth);
        sheet.setColumnWidth(COL_SURV_END, sharedColWidth);
        sheet.setColumnWidth(COL_SURV_OUTCOME, getColumnWidth(51.44));
        sheet.setColumnWidth(COL_NONCONFORMITY_TYPES_RESULTANT, getColumnWidth(27));
        sheet.setColumnWidth(COL_CERT_STATUS_RESULTANT, getColumnWidth(17.78));
        sheet.setColumnWidth(COL_SUSPENDED, getColumnWidth(17.78));
        sheet.setColumnWidth(COL_SURV_PROCESS_TYPE, getColumnWidth(30.67));
        int longTextColWidth = getColumnWidth(59.44);
        sheet.setColumnWidth(COL_SURV_GROUNDS, longTextColWidth);
        sheet.setColumnWidth(COL_NONCONFORMITY_CAUSES, longTextColWidth);
        sheet.setColumnWidth(COL_NONCONFORMITY_NATURES, longTextColWidth);
        sheet.setColumnWidth(COL_SURV_STEPS, longTextColWidth);
        sheet.setColumnWidth(COL_ENGAGEMENT_STEPS, longTextColWidth);
        sheet.setColumnWidth(COL_ADDITIONAL_COSTS, longTextColWidth);
        sheet.setColumnWidth(COL_LIMITATIONS_EVAL, longTextColWidth);
        sheet.setColumnWidth(COL_NONDISCLOSURE_EVAL, longTextColWidth);
        sheet.setColumnWidth(COL_DEV_RESOLUTION, longTextColWidth);
        sheet.setColumnWidth(COL_COMPLETED_CAP, longTextColWidth);

        addHeadingRow(sheet);
        addTableData(sheet);

        //hide some rows the ACBs are not expected to fill out (columns D-I)
        for (int i = 3; i < 9; i++) {
            sheet.setColumnHidden(i, true);
        }

        //apply the borders after the sheet has been created
        pt.drawBorders(new CellRangeAddress(1, 5, 1, LAST_DATA_COLUMN-1),
                BorderStyle.MEDIUM, BorderExtent.OUTSIDE);
        pt.applyBorders(sheet);
        return sheet;
    }

    private void addHeadingRow(final Sheet sheet) {
        Row row = createRow(sheet, 1);
        //row can have 6 lines of text
        row.setHeightInPoints(6 * sheet.getDefaultRowHeightInPoints());

        addHeadingCell(row, COL_CHPL_ID, "CHPL ID");
        addHeadingCell(row, COL_SURV_ID, "Surveillance ID");
        addHeadingCell(row, COL_SURV_ACTIVITY_TRACKER, "Surveillance Activity Tracker");
        addHeadingCell(row, COL_RELATED_COMPLAINT, "Related Complaint (both if possible)");
        addHeadingCell(row, COL_Q1, "Q1");
        addHeadingCell(row, COL_Q2, "Q2");
        addHeadingCell(row, COL_Q3, "Q3");
        addHeadingCell(row, COL_Q4, "Q4");
        addHeadingCell(row, COL_CERT_EDITION, "Certification Edition");
        addHeadingCell(row, COL_DEVELOPER_NAME, "Developer Name");
        addHeadingCell(row, COL_PRODUCT_NAME, "Product Name");
        addHeadingCell(row, COL_PRODUCT_VERSION, "Product Version");
        addHeadingCell(row, COL_K1_REVIEWED, "§170.523(k)(1) Reviewed?");
        addHeadingCell(row, COL_SURV_TYPE, "Type of Surveillance");
        addHeadingCell(row, COL_SURV_LOCATION_COUNT, "Number of Locations Surveilled");
        Cell cell = addHeadingCell(row, COL_SURV_BEGIN, "Surveillance Began");
        cell.getCellStyle().setAlignment(HorizontalAlignment.RIGHT);
        cell = addHeadingCell(row, COL_SURV_END, "Surveillance Ended");
        cell.getCellStyle().setAlignment(HorizontalAlignment.RIGHT);
        addHeadingCell(row, COL_SURV_OUTCOME, "Outcome of Surveillance");
        addHeadingCell(row, COL_NONCONFORMITY_TYPES_RESULTANT, "Non-Conformity Type(s) Resultant of Surveillance (i.e. \"170.xxx (x)(x)\")");
        addHeadingCell(row, COL_CERT_STATUS_RESULTANT, "Certification Status Resultant of Surveillance");
        addHeadingCell(row, COL_SUSPENDED, "Suspended During Surveillance?");
        addHeadingCell(row, COL_SURV_PROCESS_TYPE, "Surveillance Process Type");
        addHeadingCell(row, COL_SURV_GROUNDS, "Ground for Initiating Surveillance");
        addHeadingCell(row, COL_NONCONFORMITY_CAUSES, "Potential Causes of Non-Conformities or Suspected Non-Conformities");
        addHeadingCell(row, COL_NONCONFORMITY_NATURES, "Nature of any Substantiated Non-Conformities");
        addHeadingCell(row, COL_SURV_STEPS, "Steps to Surveil and Substantiate");
        addHeadingCell(row, COL_ENGAGEMENT_STEPS, "Steps to Engage and Work with Developer and End-Users");
        addHeadingCell(row, COL_ADDITIONAL_COSTS, "Additional Costs Evaluation");
        addHeadingCell(row, COL_LIMITATIONS_EVAL, "Limitations Evaluation");
        addHeadingCell(row, COL_NONDISCLOSURE_EVAL, "Non-Disclosure Evaluation");
        addHeadingCell(row, COL_DEV_RESOLUTION, "Direction for Developer Resolution");
        addHeadingCell(row, COL_COMPLETED_CAP, "Verification of Completed CAP");
    }

    private void addTableData(final Sheet sheet) {
        //TODO
    }

    private Cell addHeadingCell(final Row row, final int cellNum, final String cellText) {
        Cell cell = createCell(row, cellNum);
        cell.setCellStyle(wrappedTableHeadingStyle);
        cell.setCellValue(cellText);
        return cell;
    }
}
