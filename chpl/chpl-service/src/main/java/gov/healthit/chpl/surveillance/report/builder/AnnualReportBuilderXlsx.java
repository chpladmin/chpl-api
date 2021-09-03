package gov.healthit.chpl.surveillance.report.builder;

import java.io.IOException;

import org.apache.poi.ss.usermodel.Workbook;

import gov.healthit.chpl.surveillance.report.domain.AnnualReport;

public interface AnnualReportBuilderXlsx {

    Workbook buildXlsx(AnnualReport annualReport) throws IOException;
}
