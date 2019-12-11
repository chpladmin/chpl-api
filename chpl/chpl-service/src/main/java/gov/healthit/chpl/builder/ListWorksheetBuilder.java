package gov.healthit.chpl.builder;

import java.io.IOException;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.ComplaintDAO;
import gov.healthit.chpl.dao.surveillance.report.PrivilegedSurveillanceDAO;
import gov.healthit.chpl.dto.ComplainantTypeDTO;
import gov.healthit.chpl.dto.surveillance.report.SurveillanceOutcomeDTO;
import gov.healthit.chpl.dto.surveillance.report.SurveillanceProcessTypeDTO;
import gov.healthit.chpl.entity.CertificationStatusType;

/**
 * A hidden worksheet that contains values used to populate drop-down lists elsewhere in the workbook.
 * The workbook must be "set" to a non-null Excel workbook object before building the worksheet.
 * @author kekey
 *
 */
@Component
public class ListWorksheetBuilder {
    private static final int LAST_DATA_COLUMN = 1;
    private static final int LAST_DATA_ROW = 60;

    private static final String BOOLEAN_YES = "Yes";
    private static final String BOOLEAN_NO = "No";

    private ComplaintDAO complaintDao;
    private PrivilegedSurveillanceDAO reportMapDao;

    @Autowired
    public ListWorksheetBuilder(final ComplaintDAO complaintDao,
            final PrivilegedSurveillanceDAO reportMapDao) {
        super();
        this.complaintDao = complaintDao;
        this.reportMapDao = reportMapDao;
    }

    public int getLastDataColumn() {
        return LAST_DATA_COLUMN;
    }

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
    public Sheet buildWorksheet(final SurveillanceReportWorkbookWrapper workbook) throws IOException {
        //create sheet
        Sheet sheet = workbook.getSheet("Lists", getLastDataColumn());
        Row choicesRow = null;
        Cell choicesCell = null;

        int outcomeCol = 0;
        int outcomeRow = 0;
        List<SurveillanceOutcomeDTO> outcomes = reportMapDao.getSurveillanceOutcomes();
        for (SurveillanceOutcomeDTO outcome : outcomes) {
            choicesRow = workbook.getRow(sheet, outcomeRow++);
            choicesCell = choicesRow.createCell(outcomeCol);
            choicesCell.setCellValue(outcome.getName());
        }

        int processTypeCol = 1;
        int processTypeRow = 0;
        List<SurveillanceProcessTypeDTO> processTypes = reportMapDao.getSurveillanceProcessTypes();
        for (SurveillanceProcessTypeDTO procType : processTypes) {
            choicesRow = workbook.getRow(sheet, processTypeRow++);
            choicesCell = choicesRow.createCell(processTypeCol);
            choicesCell.setCellValue(procType.getName());
        }

        int statusCol = 2;
        int statusRow = 0;
        choicesRow = workbook.getRow(sheet, statusRow++);
        choicesCell = choicesRow.createCell(statusCol);
        choicesCell.setCellValue(CertificationStatusType.Active.getName());
        choicesRow = workbook.getRow(sheet, statusRow++);
        choicesCell = choicesRow.createCell(statusCol);
        choicesCell.setCellValue(CertificationStatusType.WithdrawnByAcb.getName());
        choicesRow = workbook.getRow(sheet, statusRow++);
        choicesCell = choicesRow.createCell(statusCol);
        choicesCell.setCellValue(CertificationStatusType.WithdrawnByDeveloper.getName());
        choicesRow = workbook.getRow(sheet, statusRow++);
        choicesCell = choicesRow.createCell(statusCol);
        choicesCell.setCellValue(CertificationStatusType.WithdrawnByDeveloperUnderReview.getName());

        int booleanCol = 3;
        int booleanRow = 0;
        choicesRow = workbook.getRow(sheet, booleanRow++);
        choicesCell = choicesRow.createCell(booleanCol);
        choicesCell.setCellValue(BOOLEAN_YES);
        choicesRow = workbook.getRow(sheet, booleanRow++);
        choicesCell = choicesRow.createCell(booleanCol);
        choicesCell.setCellValue(BOOLEAN_NO);

        int complainantTypeCol = 4;
        int complainantTypeRow = 0;
        List<ComplainantTypeDTO> complainantTypes = complaintDao.getComplainantTypes();
        for (ComplainantTypeDTO complainantType : complainantTypes) {
            choicesRow = workbook.getRow(sheet, complainantTypeRow++);
            choicesCell = choicesRow.createCell(complainantTypeCol);
            choicesCell.setCellValue(complainantType.getName());
        }

        int complaintStatusTypeCol = 5;
        int complaintStatusTypeRow = 0;
        choicesRow = workbook.getRow(sheet, complaintStatusTypeRow++);
        choicesCell = choicesRow.createCell(complaintStatusTypeCol);
        choicesCell.setCellValue("Open");
        choicesRow = workbook.getRow(sheet, complaintStatusTypeRow++);
        choicesCell = choicesRow.createCell(complaintStatusTypeCol);
        choicesCell.setCellValue("Closed");

        // unselect that sheet because we will hide it later
        sheet.setSelected(false);
        return sheet;
    }
}
