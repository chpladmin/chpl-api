package gov.healthit.chpl.report.criteriaattribute;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class PrivacyAndSecurityFrameworkReportService {
    private PrivacyAndSecurityFrameworkReportDao privacyAndSecurityFrameworkReportDao;

    @Autowired
    public PrivacyAndSecurityFrameworkReportService(PrivacyAndSecurityFrameworkReportDao privacyAndSecurityFrameworkReportDao) {
        this.privacyAndSecurityFrameworkReportDao = privacyAndSecurityFrameworkReportDao;
    }

    @Transactional
    public List<PrivacyAndSecurityFrameworkReport> getPrivacyAndSecurityFrameworkReports() {
        return privacyAndSecurityFrameworkReportDao.getPrivacyAndSecurityFrameworkReports();
    }

    @Transactional
    public List<PrivacyAndSecurityFrameworkListingReport> getPrivacyAndSecurityFrameworkListingReports() {
        return privacyAndSecurityFrameworkReportDao.getPrivacyAndSecurityFrameworkListingReports();
    }

}

