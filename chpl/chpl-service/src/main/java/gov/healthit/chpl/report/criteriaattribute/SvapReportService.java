package gov.healthit.chpl.report.criteriaattribute;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class SvapReportService {
    private SvapReportDao svapReportDao;

    @Autowired
    public SvapReportService(SvapReportDao svapReportDao) {
        this.svapReportDao = svapReportDao;
    }

    @Transactional
    public List<SvapReport> getSvapReports() {
        return svapReportDao.getSvapReports();
    }

    @Transactional
    public List<SvapListingReport> getSvapListingReports() {
        return svapReportDao.getSvapListingReports();
    }
}
