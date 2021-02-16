package gov.healthit.chpl.surveillance.report.builder2021;

import java.util.List;

import org.apache.poi.ss.usermodel.Sheet;

import gov.healthit.chpl.surveillance.report.builder.ReportInfoWorksheetBuilder;
import gov.healthit.chpl.surveillance.report.builder.SurveillanceReportWorkbookWrapper;
import gov.healthit.chpl.surveillance.report.dto.QuarterlyReportDTO;

public class ReportInfoWorksheetBuilder2021 extends ReportInfoWorksheetBuilder {

    public ReportInfoWorksheetBuilder2021() {
        super();
    }

    @Override
    protected String getReportingAcbDescription() {
        return "This report is submitted by the below ONC-ACB in accordance with "
                + "45 CFR § 170.523(i)(2), 45 CFR § 170.523(n), and 45 CFR § 170.556(e).";
    }

    @Override
    protected String getSurveillanceActivitiesAndOutcomesDescription() {
        return "The ONC-ACB used the following selection method to make its "
                + "random selection of certified Health IT Modules for surveillance "
                + "initiated during the reporting period.";
    }

    @Override
    protected String getReactiveSummaryDescription() {
        return "In order to meet its obligation to conduct reactive surveillance, "
                + "the ONC-ACB undertook the following activities and implemented the following "
                + "measures to ensure that it was able to systematically obtain, synthesize and "
                + "act on all facts and circumstances that would cause a reasonable person to "
                + "question the ongoing compliance of any certified Health IT Module.";
    }

    @Override
    protected String getDisclosureSummaryTitle() {
        return "Disclosure Requirements";
    }

    @Override
    protected String getDisclosureSummaryDescription() {
        return "The ONC-ACB undertook the following activities and implemented the following measures "
                + "to ensure adherence by developers to disclosure requirements, as required of "
                + "the ONC-ACB under 45 CFR § 170.523(k):";
    }

    @Override
    protected String getReactiveSummaryTitle() {
        return "Reactive Surveillance Summary";
    }

    @Override
    protected int addExclusionAndExhaustionSection(SurveillanceReportWorkbookWrapper workbook,
            Sheet sheet, List<QuarterlyReportDTO> reports, int beginRow) {
        return beginRow;
    }
}
