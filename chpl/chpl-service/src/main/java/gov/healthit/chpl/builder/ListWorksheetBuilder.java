package gov.healthit.chpl.builder;

import java.awt.Color;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.DVConstraint;
import org.apache.poi.hssf.usermodel.HSSFDataValidation;
import org.apache.poi.ss.usermodel.BorderExtent;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataValidation;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.ss.util.PropertyTemplate;
import org.apache.poi.xssf.usermodel.XSSFDataValidation;
import org.apache.poi.xssf.usermodel.XSSFDataValidationConstraint;
import org.apache.poi.xssf.usermodel.XSSFDataValidationHelper;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTDataValidation;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTDataValidations;

import gov.healthit.chpl.domain.CertificationStatusEvent;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.Surveillance;
import gov.healthit.chpl.domain.SurveillanceNonconformity;
import gov.healthit.chpl.domain.SurveillanceNonconformityStatus;
import gov.healthit.chpl.domain.SurveillanceRequirement;
import gov.healthit.chpl.dto.surveillance.report.QuarterlyReportDTO;
import gov.healthit.chpl.entity.CertificationStatusType;
import gov.healthit.chpl.validation.surveillance.SurveillanceValidator;

public class ListWorksheetBuilder extends XlsxWorksheetBuilder {
    private static final int LAST_DATA_COLUMN = 1;
    private static final int LAST_DATA_ROW = 60;

    private static final String OUTCOME_NO_NC = "No non-conformity";
    private static final String OUTCOME_NC_RESOLVED = "Non-conformity substantiated - Resolved through corrective action";
    private static final String OUTCOME_NC_UNRESOLVED_CAP = "Non-conformity substantiated - Unresolved - Corrective action ongoing";
    private static final String OUTCOME_NC_UNRESOLVED_SUSPENDED = "Non-conformity substantiated - Unresolved - Certification suspended";
    private static final String OUTCOME_NC_UNRESOLVED_WITHDRAWN = "Non-conformity substantiated - Unresolved - Certification withdrawn";
    private static final String OUTCOME_NC_UNRESOLVED_SURV = "Non-conformity substantiated - Unresolved - Surveillance in process";
    private static final String OUTCOME_NC_UNRESOLVED_REVIEW = "Non-conformity substantiated - Unresolved - Under investigation/review";
    private static final String OUTCOME_NC_UNRESOLVED_OTHER = "Non-conformity substantiated - Unresolved - Other - [Please describe]";

    private static final String PROCESS_TYPE_FIELD = "In-the-Field";
    private static final String PROCESS_TYPE_CONTROLLED = "Controlled/Test Environment";
    private static final String PROCESS_TYPE_CORRESPONDENCE = "Correspondence with Complainant/Developer";
    private static final String PROCESS_TYPE_REVIEW = "Review of Websites/Written Documentation";
    private static final String PROCESS_TYPE_OTHER = "Other - [Please describe]";

    private static final String BOOLEAN_YES = "Yes";
    private static final String BOOLEAN_NO = "No";

    public ListWorksheetBuilder(final Workbook workbook) {
        super(workbook);
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
 * Creates a hidden excel worksheet that contains a list of values
 * to be used in other worksheets within the same workbook for data validation
 * dropdown values.
 * It is needed to be done this way because if you just specify an array of strings
 * as the dropdown value then the entire list has to have less than 256 characters total.
 * It is not limited in length if specified as a formula.
 * @return
 * @throws IOException
 */
    public Sheet buildWorksheet()
            throws IOException {

        //create sheet
        Sheet sheet = getSheet("Lists");

        int outcomeCol = 0;
        int outcomeRow = 0;
        Row choicesRow = getRow(sheet, outcomeRow++);
        Cell choicesCell = choicesRow.createCell(outcomeCol);
        choicesCell.setCellValue(OUTCOME_NO_NC);
        choicesRow = getRow(sheet, outcomeRow++);
        choicesCell = choicesRow.createCell(outcomeCol);
        choicesCell.setCellValue(OUTCOME_NC_RESOLVED);
        choicesRow = getRow(sheet, outcomeRow++);
        choicesCell = choicesRow.createCell(outcomeCol);
        choicesCell.setCellValue(OUTCOME_NC_UNRESOLVED_CAP);
        choicesRow = getRow(sheet, outcomeRow++);
        choicesCell = choicesRow.createCell(outcomeCol);
        choicesCell.setCellValue(OUTCOME_NC_UNRESOLVED_SUSPENDED);
        choicesRow = getRow(sheet, outcomeRow++);
        choicesCell = choicesRow.createCell(outcomeCol);
        choicesCell.setCellValue(OUTCOME_NC_UNRESOLVED_WITHDRAWN);
        choicesRow = getRow(sheet, outcomeRow++);
        choicesCell = choicesRow.createCell(outcomeCol);
        choicesCell.setCellValue(OUTCOME_NC_UNRESOLVED_SURV);
        choicesRow = getRow(sheet, outcomeRow++);
        choicesCell = choicesRow.createCell(outcomeCol);
        choicesCell.setCellValue(OUTCOME_NC_UNRESOLVED_REVIEW);
        choicesRow = getRow(sheet, outcomeRow++);
        choicesCell = choicesRow.createCell(outcomeCol);
        choicesCell.setCellValue(OUTCOME_NC_UNRESOLVED_OTHER);

        int processTypeCol = 1;
        int processTypeRow = 0;
        choicesRow = getRow(sheet, processTypeRow++);
        choicesCell = choicesRow.createCell(processTypeCol);
        choicesCell.setCellValue(PROCESS_TYPE_FIELD);
        choicesRow = getRow(sheet, processTypeRow++);
        choicesCell = choicesRow.createCell(processTypeCol);
        choicesCell.setCellValue(PROCESS_TYPE_CORRESPONDENCE);
        choicesRow = getRow(sheet, processTypeRow++);
        choicesCell = choicesRow.createCell(processTypeCol);
        choicesCell.setCellValue(PROCESS_TYPE_CONTROLLED);
        choicesRow = getRow(sheet, processTypeRow++);
        choicesCell = choicesRow.createCell(processTypeCol);
        choicesCell.setCellValue(PROCESS_TYPE_REVIEW);
        choicesRow = getRow(sheet, processTypeRow++);
        choicesCell = choicesRow.createCell(processTypeCol);
        choicesCell.setCellValue(PROCESS_TYPE_OTHER);

        int statusCol = 2;
        int statusRow = 0;
        choicesRow = getRow(sheet, statusRow++);
        choicesCell = choicesRow.createCell(statusCol);
        choicesCell.setCellValue(CertificationStatusType.Active.getName());
        choicesRow = getRow(sheet, statusRow++);
        choicesCell = choicesRow.createCell(statusCol);
        choicesCell.setCellValue(CertificationStatusType.WithdrawnByAcb.getName());
        choicesRow = getRow(sheet, statusRow++);
        choicesCell = choicesRow.createCell(statusCol);
        choicesCell.setCellValue(CertificationStatusType.WithdrawnByDeveloper.getName());
        choicesRow = getRow(sheet, statusRow++);
        choicesCell = choicesRow.createCell(statusCol);
        choicesCell.setCellValue(CertificationStatusType.WithdrawnByDeveloperUnderReview.getName());

        int booleanCol = 3;
        int booleanRow = 0;
        choicesRow = getRow(sheet, booleanRow++);
        choicesCell = choicesRow.createCell(booleanCol);
        choicesCell.setCellValue(BOOLEAN_YES);
        choicesRow = getRow(sheet, booleanRow++);
        choicesCell = choicesRow.createCell(booleanCol);
        choicesCell.setCellValue(BOOLEAN_NO);

        // unselect that sheet because we will hide it later
        sheet.setSelected(false);
        return sheet;
    }
}
