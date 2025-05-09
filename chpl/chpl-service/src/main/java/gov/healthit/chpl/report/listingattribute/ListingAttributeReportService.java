package gov.healthit.chpl.report.listingattribute;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ListingAttributeReportService {
    private QmsStandardReportDao qmsStandardReportDao;
    private AccessibilityStandardReportDao accessibilityStandardReportDao;
    private MeasureReportDao measureReportDao;

    @Autowired
    public ListingAttributeReportService(QmsStandardReportDao qmsStandardReportDao,
            AccessibilityStandardReportDao accessibilityStandardReportDao,
            MeasureReportDao measureReportDao) {
        this.qmsStandardReportDao = qmsStandardReportDao;
        this.accessibilityStandardReportDao = accessibilityStandardReportDao;
        this.measureReportDao = measureReportDao;
    }

    @Transactional
    public List<QmsStandardReport> getQmsStandardReports() {
        return qmsStandardReportDao.getQmsStandardReports();
    }

    @Transactional
    public List<QmsStandardListingReport> getQmsStandardListingReports() {
        return qmsStandardReportDao.getQmsStandardListingReports();
    }

    @Transactional
    public List<AccessibilityStandardReport> getAccessibilityStandardReports() {
        return accessibilityStandardReportDao.getAccessibilityStandardReports();
    }

    @Transactional
    public List<AccessibilityStandardListingReport> getAccessibilityStandardListingReports() {
        return accessibilityStandardReportDao.getAccessibilityStandardListingReports();
    }

    @Transactional
    public List<MeasureReport> getMeasureReports() {
        return measureReportDao.getMeasureReports();
    }

    @Transactional
    public List<MeasureListingReport> getMeasureListingReports() {
        return measureReportDao.getMeasureListingReports();
    }
}
