package gov.healthit.chpl.surveillance.report.builder;

import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.surveillance.report.SurveillanceReportManager;
import gov.healthit.chpl.surveillance.report.dto.QuarterlyReportDTO;

/**
 * Creates a worksheet with high level information about the report.
 * The workbook must be "set" to a non-null Excel workbook object before building the worksheet.
 * @author kekey
 *
 */
@Component
public class QuarterlyReportInfoWorksheetBuilder extends ReportInfoWorksheetBuilder {

    @Autowired
    public QuarterlyReportInfoWorksheetBuilder(final SurveillanceReportManager reportManager) {
        super(reportManager);
    }

    /**
     * Creates the header section and returns the row number of the last row that was added.
     * @param sheet
     * @return
     */
    protected int createHeader(final SurveillanceReportWorkbookWrapper workbook,
            final Sheet sheet, final List<QuarterlyReportDTO> reports, final int beginRow) {
        int currRow = beginRow;
        Row row = workbook.getRow(sheet, currRow++);
        Cell cell = workbook.createCell(row, 1, workbook.getBoldStyle());
        cell.setCellValue("ONC-Authorized Certification Body (ONC-ACB) "
                + determineYear(reports) + " Quarterly Surveillance Report");
        return row.getRowNum()+1;
    }
}
