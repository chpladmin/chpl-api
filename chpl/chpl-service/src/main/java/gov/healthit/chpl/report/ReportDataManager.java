package gov.healthit.chpl.report;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.statistics.CuresCriterionChartStatistic;
import gov.healthit.chpl.report.criteriamigrationreport.CriteriaMigrationReport;
import gov.healthit.chpl.report.criteriamigrationreport.CriteriaMigrationReportService;
import gov.healthit.chpl.report.curesupdate.CuresUpdateReportService;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class ReportDataManager {

    private CuresUpdateReportService curesUpdateReportService;
    private CriteriaMigrationReportService criteriaMigrationReportService;

    @Autowired
    public ReportDataManager(CuresUpdateReportService curesUpdateReportService, CriteriaMigrationReportService criteriaMigrationReportService) {
        this.curesUpdateReportService = curesUpdateReportService;
        this.criteriaMigrationReportService = criteriaMigrationReportService;
    }

    public List<CuresCriterionChartStatistic> getCuresUpdateReportData() {
        return curesUpdateReportService.getReportData();
    }

    public CriteriaMigrationReport getHti1CriteriaMigrationReport() {
        return criteriaMigrationReportService.getReport(CriteriaMigrationReportService.HTI1_REPORT_ID);
    }
}

