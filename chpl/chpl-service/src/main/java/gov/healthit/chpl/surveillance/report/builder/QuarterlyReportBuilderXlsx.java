package gov.healthit.chpl.surveillance.report.builder;

import java.io.IOException;

import org.apache.poi.ss.usermodel.Workbook;

import gov.healthit.chpl.surveillance.report.dto.QuarterlyReportDTO;

public interface QuarterlyReportBuilderXlsx {

    Workbook buildXlsx(QuarterlyReportDTO report) throws IOException;
}
