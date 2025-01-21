package gov.healthit.chpl.report.criteriaattribute;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class CriteriaAttributeReportService {
    private TestToolReportDao testToolReportDao;
    private StandardReportDao standardReportDao;
    private FunctionalityTestedReportDao functionalityTestedReportDao;
    private OptionalStandardReportDao optionalStandardReportDao;
    private TestDataReportDao testDataReportDao;
    private SvapReportDao svapReportDao;
    private PrivacyAndSecurityFrameworkReportDao privacyAndSecurityFrameworkReportDao;
    private ConformanceMethodReportDao conformanceMethodReportDao;
    private CodeSetReportDao codeSetReportDao;

    @Autowired
    public CriteriaAttributeReportService(TestToolReportDao testToolReportDao,
            StandardReportDao standardReportDao,
            FunctionalityTestedReportDao functionalityTestedReportDao,
            OptionalStandardReportDao optionalStandardReportDao,
            TestDataReportDao testDataReportDao,
            SvapReportDao svapReportDao,
            PrivacyAndSecurityFrameworkReportDao privacyAndSecurityFrameworkReportDao,
            ConformanceMethodReportDao conformanceMethodReportDao,
            CodeSetReportDao codeSetReportDao) {

        this.testToolReportDao = testToolReportDao;
        this.standardReportDao = standardReportDao;
        this.functionalityTestedReportDao = functionalityTestedReportDao;
        this.optionalStandardReportDao = optionalStandardReportDao;
        this.testDataReportDao = testDataReportDao;
        this.svapReportDao = svapReportDao;
        this.privacyAndSecurityFrameworkReportDao = privacyAndSecurityFrameworkReportDao;
        this.conformanceMethodReportDao = conformanceMethodReportDao;
        this.codeSetReportDao = codeSetReportDao;
    }

    @Transactional
    public List<TestToolReport> getTestToolReports() {
        return testToolReportDao.getTestToolReports();
    }

    @Transactional
    public List<TestToolListingReport> getTestToolListingReports() {
        return testToolReportDao.getTestToolListingReports();
    }

    @Transactional
    public List<StandardReport> getStandardReports() {
        return standardReportDao.getStandardReports();
    }

    @Transactional
    public List<StandardListingReport> getStandardListingReports() {
        return standardReportDao.getStandardListingReports();
    }

    @Transactional
    public List<FunctionalityTestedReport> getFunctionalityTestedReports() {
        return functionalityTestedReportDao.getFunctionalityTestedReports();
    }

    @Transactional
    public List<FunctionalityTestedListingReport> getFunctionalityTestedListingReports() {
        return functionalityTestedReportDao.getFunctionalityTestedListingReports();
    }

    @Transactional
    public List<OptionalStandardReport> getOptionalStandardReports() {
        return optionalStandardReportDao.getOptionalStandardReports();
    }

    @Transactional
    public List<OptionalStandardListingReport> getOptionalStandardListingReports() {
        return optionalStandardReportDao.getOptionalStandardListingReports();
    }

    @Transactional
    public List<TestDataReport> getTestDataReports() {
        return testDataReportDao.getTestDataReports();
    }

    @Transactional
    public List<TestDataListingReport> getTestDataListingReports() {
        return testDataReportDao.getTestDataListingReports();
    }

    @Transactional
    public List<SvapReport> getSvapReports() {
        return svapReportDao.getSvapReports();
    }

    @Transactional
    public List<SvapListingReport> getSvapListingReports() {
        return svapReportDao.getSvapListingReports();
    }

    @Transactional
    public List<PrivacyAndSecurityFrameworkReport> getPrivacyAndSecurityFrameworkReports() {
        return privacyAndSecurityFrameworkReportDao.getPrivacyAndSecurityFrameworkReports();
    }

    @Transactional
    public List<PrivacyAndSecurityFrameworkListingReport> getPrivacyAndSecurityFrameworkListingReports() {
        return privacyAndSecurityFrameworkReportDao.getPrivacyAndSecurityFrameworkListingReports();
    }

    @Transactional
    public List<ConformanceMethodReport> getConformnceMethodReports() {
        return conformanceMethodReportDao.getConformanceMethodReports();
    }

    @Transactional
    public List<ConformanceMethodListingReport> getConformanceMethodListingReports() {
        return conformanceMethodReportDao.getConformanceMethodListingReports();
    }

    @Transactional
    public List<CodeSetReport> getCodeSetReports() {
        return codeSetReportDao.getCodeSetReports();
    }

    @Transactional
    public List<CodeSetListingReport> getCodeSetListingReports() {
        return codeSetReportDao.getCodeSetListingReports();
    }

}
