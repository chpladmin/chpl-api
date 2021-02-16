package gov.healthit.chpl.surveillance.report.builder2021;

import java.util.List;

import org.apache.poi.ss.usermodel.Sheet;

import gov.healthit.chpl.surveillance.report.builder.ReportInfoWorksheetBuilder;
import gov.healthit.chpl.surveillance.report.builder.SurveillanceReportWorkbookWrapper;
import gov.healthit.chpl.surveillance.report.dto.QuarterlyReportDTO;

/**
 * Creates a worksheet with high level information about the report.
 *
 */
public abstract class ReportInfoWorksheetBuilder2021 extends ReportInfoWorksheetBuilder {

    public ReportInfoWorksheetBuilder2021() {
        super();
    }

    protected int addExclusionAndExhaustionSection(SurveillanceReportWorkbookWrapper workbook,
            Sheet sheet, List<QuarterlyReportDTO> reports, int beginRow) {
        return beginRow;
    }
}
