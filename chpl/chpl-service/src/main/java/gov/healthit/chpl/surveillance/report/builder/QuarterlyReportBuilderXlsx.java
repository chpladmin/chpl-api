package gov.healthit.chpl.surveillance.report.builder;

import java.io.IOException;

import org.apache.logging.log4j.Logger;

import gov.healthit.chpl.surveillance.report.domain.QuarterlyReport;

public interface QuarterlyReportBuilderXlsx {

    SurveillanceReportWorkbookWrapper buildXlsx(QuarterlyReport report, Logger logger) throws IOException;
}
