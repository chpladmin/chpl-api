package gov.healthit.chpl.surveillance.report.builder;

import java.io.IOException;

import org.apache.poi.ss.usermodel.Workbook;

import gov.healthit.chpl.surveillance.report.dto.AnnualReportDTO;

public interface AnnualReportBuilderXlsx {

    Workbook buildXlsx(AnnualReportDTO annualReport) throws IOException;
}
