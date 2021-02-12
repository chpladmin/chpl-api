package gov.healthit.chpl.surveillance.report.builder2019;

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
 *
 */
@Component
public class QuarterlyReportInfoWorksheetBuilder extends ReportInfoWorksheetBuilder {

    @Autowired
    public QuarterlyReportInfoWorksheetBuilder(final SurveillanceReportManager reportManager) {
        super(reportManager);
    }

    protected int createHeader(SurveillanceReportWorkbookWrapper workbook,
            Sheet sheet, List<QuarterlyReportDTO> reports, int beginRow) {
        int currRow = beginRow;
        Row row = workbook.getRow(sheet, currRow++);
        Cell cell = workbook.createCell(row, 1, workbook.getBoldStyle());
        cell.setCellValue("ONC-Authorized Certification Body (ONC-ACB) "
                + determineYear(reports) + " Quarterly Surveillance Report");
        return row.getRowNum()+1;
    }
}
