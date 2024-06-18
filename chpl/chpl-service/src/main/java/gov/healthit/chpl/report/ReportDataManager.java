package gov.healthit.chpl.report;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.statistics.CuresCriterionChartStatistic;
import gov.healthit.chpl.report.curesupdate.CuresUpdateReportService;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class ReportDataManager {

    private CuresUpdateReportService curesUpdateReportService;

    @Autowired
    public ReportDataManager(CuresUpdateReportService curesUpdateReportService) {
        this.curesUpdateReportService = curesUpdateReportService;
    }

    public List<CuresCriterionChartStatistic> getCuresUpdateReportData() {
        return curesUpdateReportService.getReportData();
    }

}

