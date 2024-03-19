package gov.healthit.chpl.surveillance.report.builder;

import java.io.IOException;

import org.apache.logging.log4j.Logger;

import gov.healthit.chpl.surveillance.report.domain.AnnualReport;

public interface AnnualReportBuilderXlsx {

    SurveillanceReportWorkbookWrapper buildXlsx(AnnualReport annualReport, Logger logger) throws IOException;
}
