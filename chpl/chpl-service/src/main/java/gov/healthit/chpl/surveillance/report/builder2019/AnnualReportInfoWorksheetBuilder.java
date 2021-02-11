package gov.healthit.chpl.surveillance.report.builder2019;

import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.surveillance.report.SurveillanceReportManager;
import gov.healthit.chpl.surveillance.report.dto.QuarterlyReportDTO;

@Component
public class AnnualReportInfoWorksheetBuilder extends ReportInfoWorksheetBuilder {

    @Autowired
    public AnnualReportInfoWorksheetBuilder(SurveillanceReportManager reportManager) {
        super(reportManager);
    }

    protected int createHeader(SurveillanceReportWorkbookWrapper workbook, Sheet sheet, List<QuarterlyReportDTO> reports, int beginRow) {
        int currRow = beginRow;
        Row row = workbook.getRow(sheet, currRow++);
        Cell cell = workbook.createCell(row, 1, workbook.getBoldStyle());
        cell.setCellValue("ONC-Authorized Certification Body (ONC-ACB) "
                + determineYear(reports) + " Annual Surveillance Report");
        return row.getRowNum() + 1;
    }
}
