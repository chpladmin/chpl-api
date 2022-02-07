package gov.healthit.chpl.surveillance.report.builder;

import java.io.IOException;

import gov.healthit.chpl.surveillance.report.domain.AnnualReport;

public interface AnnualReportBuilderXlsx {

    SurveillanceReportWorkbookWrapper buildXlsx(AnnualReport annualReport) throws IOException;
}
