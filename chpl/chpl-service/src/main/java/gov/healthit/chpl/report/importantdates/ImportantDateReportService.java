package gov.healthit.chpl.report.importantdates;

import java.time.MonthDay;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.attestation.dao.AttestationDAO;
import gov.healthit.chpl.codeset.CodeSetDAO;
import gov.healthit.chpl.dao.CertificationCriterionDAO;
import gov.healthit.chpl.functionalitytested.FunctionalityTestedDAO;
import gov.healthit.chpl.realworldtesting.manager.RealWorldTestingReportService;
import gov.healthit.chpl.standard.StandardDAO;
import gov.healthit.chpl.surveillance.report.QuarterDAO;

@Component
public class ImportantDateReportService {

    private CertificationCriterionDAO criteriaDao;
    private StandardDAO standardDao;
    private CodeSetDAO codeSetDao;
    private FunctionalityTestedDAO functionalityTestedDao;
    private QuarterDAO quarterDao;
    private AttestationDAO attestationDao;
    private RealWorldTestingReportService rwtReportService;
    private MonthDay cmsIdCreationStart;
    private MonthDay cmsIdCreationOverlapEnd;
    private Integer attestationApprovalWindowInDays;

    public ImportantDateReportService(CertificationCriterionDAO criteriaDao, StandardDAO standardDao,
            CodeSetDAO codeSetDao, FunctionalityTestedDAO functionalityTestedDao,
            QuarterDAO quarterDao, AttestationDAO attestationDao,
            RealWorldTestingReportService rwtReportService,
            @Value("${attestationApprovalWindowInDays}") Integer attestationApprovalWindowInDays) {
        this.criteriaDao = criteriaDao;
        this.standardDao = standardDao;
        this.codeSetDao = codeSetDao;
        this.functionalityTestedDao = functionalityTestedDao;
        this.rwtReportService = rwtReportService;
        this.attestationApprovalWindowInDays = attestationApprovalWindowInDays;
    }
}
