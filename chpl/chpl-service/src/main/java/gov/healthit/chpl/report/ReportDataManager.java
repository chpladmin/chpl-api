package gov.healthit.chpl.report;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.report.criteriamigrationreport.CriteriaMigrationReport;
import gov.healthit.chpl.report.criteriamigrationreport.CriteriaMigrationReportDAO;
import gov.healthit.chpl.report.criteriamigrationreport.CriteriaMigrationReportService;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class ReportDataManager {

    private CriteriaMigrationReportDAO criteriaMigrationReportDAO;
    private CriteriaMigrationReportService criteriaMigrationReportService;

    @Autowired
    public ReportDataManager(CriteriaMigrationReportDAO criteriaMigrationReportDAO, CriteriaMigrationReportService criteriaMigrationReportService) {
        this.criteriaMigrationReportDAO = criteriaMigrationReportDAO;
        this.criteriaMigrationReportService = criteriaMigrationReportService;
    }

    public CriteriaMigrationReport getCriteriaMigrationReport() {
        criteriaMigrationReportService.gatherDataForReport();
        return criteriaMigrationReportDAO.getCriteriaMigrationReport(1L);
    }
}
