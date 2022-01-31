package gov.healthit.chpl.surveillance.report.builder;

import java.io.IOException;

import gov.healthit.chpl.surveillance.report.dto.QuarterlyReportDTO;

public interface QuarterlyReportBuilderXlsx {

    SurveillanceReportWorkbookWrapper buildXlsx(QuarterlyReportDTO report) throws IOException;
}
