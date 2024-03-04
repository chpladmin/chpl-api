package gov.healthit.chpl.surveillance.report.builder;

import java.io.IOException;

import gov.healthit.chpl.surveillance.report.domain.QuarterlyReport;

public interface QuarterlyReportBuilderXlsx {

    SurveillanceReportWorkbookWrapper buildXlsx(QuarterlyReport report) throws IOException;
}
