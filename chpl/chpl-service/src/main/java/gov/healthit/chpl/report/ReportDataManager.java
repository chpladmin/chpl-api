package gov.healthit.chpl.report;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.report.criteriamigrationreport.CriteriaMigrationReport;
import gov.healthit.chpl.report.criteriamigrationreport.CriteriaMigrationReportDAO;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class ReportDataManager {

    private CriteriaMigrationReportDAO criteriaMigrationReportDAO;

    @Autowired
    public ReportDataManager(CriteriaMigrationReportDAO criteriaMigrationReportDAO) {
        this.criteriaMigrationReportDAO = criteriaMigrationReportDAO;
    }

    public CriteriaMigrationReport getCriteriaMigrationReport() {
        return criteriaMigrationReportDAO.getCriteriaMigrationReport(2L);
    }
}
