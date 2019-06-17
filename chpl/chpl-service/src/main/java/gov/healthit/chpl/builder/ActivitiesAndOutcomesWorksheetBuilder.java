package gov.healthit.chpl.builder;

import java.awt.Color;
import java.io.IOException;

import org.apache.poi.ss.usermodel.BorderExtent;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.PropertyTemplate;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;

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
        Sheet sheet = getSheet("Activities and Outcomes", new Color(141, 180, 226));

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
        //TODO: increase the border above 5 depending on how many data rows there are
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
        addHeadingCell(row, COL_SURV_BEGIN, "Surveillance Began");
        addHeadingCell(row, COL_SURV_END, "Surveillance Ended");
        addHeadingCell(row, COL_SURV_OUTCOME, "Outcome of Surveillance");
        addHeadingCell(row, COL_NONCONFORMITY_TYPES_RESULTANT,
                "Non-Conformity Type(s) Resultant of Surveillance (i.e. \"170.xxx (x)(x)\")");
        addHeadingCell(row, COL_CERT_STATUS_RESULTANT, "Certification Status Resultant of Surveillance");
        addHeadingCell(row, COL_SUSPENDED, "Suspended During Surveillance?");
        addHeadingCell(row, COL_SURV_PROCESS_TYPE, "Surveillance Process Type");

        //these headings are more complicated with various fonts throughout the cell
        String cellTitle = "Grounds for Initiating Surveillance";
        String cellSubtitle = "On what grounds did the ONC-ACB initiate surveillance "
                + "(i.e., the particular facts and circumstances from which a reasonable person would "
                + "have had grounds to question the continued conformity of the Complete EHR or "
                + "Health IT Module)? For randomized surveillance, it is acceptable to state it was chosen randomly.";
        addRichTextHeadingCell(row, COL_SURV_GROUNDS, cellTitle, cellSubtitle);

        cellTitle = "Potential Causes of Non-Conformities or Suspected Non-Conformities";
        cellSubtitle = "What were the substantial factors that, in the ONC-ACB’s assessment, "
                + "caused or contributed to the suspected non-conformity or non-conformities "
                + "(e.g., implementation problem, user error, limitations on the use of capabilities "
                + "in the field, a failure to disclose known material information, etc.)?";
        addRichTextHeadingCell(row, COL_NONCONFORMITY_CAUSES, cellTitle, cellSubtitle);

        cellTitle = "Nature of Any Substantiated Non-Conformities";
        cellSubtitle = "Did ONC-ACB substantiate any non-conformities? If so, what was the nature "
                + "of the non-conformity or non-conformities that were substantiated?\n"
                + "Please include specific criteria involved.";
        addRichTextHeadingCell(row, COL_NONCONFORMITY_NATURES, cellTitle, cellSubtitle);

        cellTitle = "Steps to Surveil and Substantiate";
        cellSubtitle = "What steps did the ONC-ACB take to surveil the Complete EHR or Health "
                + "IT Module, to analyze evidence, and to substantiate the non-conformity or non-conformities?";
        addRichTextHeadingCell(row, COL_SURV_STEPS, cellTitle, cellSubtitle);

        cellTitle = "Steps to Engage and Work with Developer and End-Users";
        cellSubtitle = "What steps were taken by ONC-ACB to engage and work with the developer and "
                + "end-users to analyze and determine the causes of any suspected non-conformities and "
                + "related deficiencies?";
        addRichTextHeadingCell(row, COL_ENGAGEMENT_STEPS, cellTitle, cellSubtitle);

        cellTitle = "Additional Costs Evaluation";
        cellSubtitle = "If a suspected non-conformity resulted from additional types of costs "
                + "that a user was required to pay in order to implement or use the Complete EHR "
                + "or Health IT Module's certified capabilities, how did ONC-ACB evaluate that "
                + "suspected non-conformity?";
        addRichTextHeadingCell(row, COL_ADDITIONAL_COSTS, cellTitle, cellSubtitle);

        cellTitle = "Limitations Evaluation";
        cellSubtitle = "If a suspected non-conformity resulted from limitations that a user "
                + "encountered in the course of implementing and using the Complete EHR or "
                + "Health IT Module's certified capabilities, how did ONC-ACB evaluate that "
                + "suspected non-conformity?";
        addRichTextHeadingCell(row, COL_LIMITATIONS_EVAL, cellTitle, cellSubtitle);

        cellTitle = "Non-Disclosure Evaluation";
        cellSubtitle = "If a suspected non-conformity resulted from the non-disclosure of material "
                + "information by the developer about limitations or additional types of costs associated "
                + "with the Complete EHR or Health IT Module, how did the ONC-ACB evaluate the suspected "
                + "non-conformity?";
        addRichTextHeadingCell(row, COL_NONDISCLOSURE_EVAL, cellTitle, cellSubtitle);

        cellTitle = "Direction for Developer Resolution";
        cellSubtitle = "If a non-conformity was substantiated, what direction was given to the developer to "
                + "resolve the non-conformity?";
        addRichTextHeadingCell(row, COL_DEV_RESOLUTION, cellTitle, cellSubtitle);

        cellTitle = "Verification of Completed CAP";
        cellSubtitle = "If an approved Corrective Action Plan was received and completed, "
                + "how did ONC-ACB verify that the developer has completed all requirements "
                + "specified in the Plan?";
        addRichTextHeadingCell(row, COL_COMPLETED_CAP, cellTitle, cellSubtitle);
    }

    private void addTableData(final Sheet sheet) {
        //TODO

        int rowNum = 2;
        //add top and bottom border to each row
        //TODO: make rowNum dynamic
        pt.drawBorders(new CellRangeAddress(rowNum, rowNum, 1, LAST_DATA_COLUMN-1),
                BorderStyle.HAIR, BorderExtent.HORIZONTAL);
    }

    private Cell addHeadingCell(final Row row, final int cellNum, final String cellText) {
        Cell cell = createCell(row, cellNum);
        cell.setCellStyle(wrappedTableHeadingStyle);
        cell.setCellValue(cellText);
        return cell;
    }

    private Cell addRichTextHeadingCell(final Row row, final int cellNum,
            final String cellHeading, final String cellSubHeading) {
        Cell cell = createCell(row, cellNum);
        cell.setCellStyle(wrappedTableHeadingStyle);
        RichTextString richTextTitle = new XSSFRichTextString(cellHeading + "\n" + cellSubHeading);
        richTextTitle.applyFont(0, cellHeading.length(), boldSmallFont);
        richTextTitle.applyFont(cellHeading.length()+1, richTextTitle.length(), italicSmallFont);
        cell.setCellValue(richTextTitle);
        return cell;
    }
}
