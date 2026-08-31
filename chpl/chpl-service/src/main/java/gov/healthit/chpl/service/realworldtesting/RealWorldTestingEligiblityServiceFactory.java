package gov.healthit.chpl.service.realworldtesting;

import java.time.LocalDate;

import org.ff4j.FF4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.activity.history.ListingActivityUtil;
import gov.healthit.chpl.activity.history.explorer.RealWorldTestingEligibilityActivityExplorer;
import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.service.CertificationCriterionService;

@Component
public class RealWorldTestingEligiblityServiceFactory {

    @Value("${rwtProgramFirstEligibilityYear}")
    private Integer rwtProgramFirstEligibilityYear;

    @Value("#{T(java.time.LocalDate).parse('${rwtProgramStartDate}')}")
    private LocalDate rwtProgramStartDate;

    private CertificationCriterionService criteriaService;
    private RealWorldTestingCriteriaService realWorldTestingCriteriaService;
    private RealWorldTestingEligibilityActivityExplorer realWorldTestingEligibilityActivityExplorer;
    private ListingActivityUtil listingActivityUtil;
    private CertifiedProductDAO certifiedProductDAO;
    private FF4j ff4j;

    @Autowired
    public RealWorldTestingEligiblityServiceFactory(CertificationCriterionService criteriaService,
            RealWorldTestingCriteriaService realWorldTestingCriteriaService,
            RealWorldTestingEligibilityActivityExplorer realWorldTestingEligibilityActivityExplorer, ListingActivityUtil listingActivityUtil,
             CertifiedProductDAO certifiedProductDAO,
             FF4j ff4j) {
        this.criteriaService = criteriaService;
        this.realWorldTestingCriteriaService = realWorldTestingCriteriaService;
        this.realWorldTestingEligibilityActivityExplorer = realWorldTestingEligibilityActivityExplorer;
        this.listingActivityUtil = listingActivityUtil;
        this.certifiedProductDAO = certifiedProductDAO;
        this.ff4j = ff4j;
    }

    public RealWorldTestingEligiblityService getInstance() {
       return  new RealWorldTestingEligiblityService(realWorldTestingCriteriaService, realWorldTestingEligibilityActivityExplorer,
               listingActivityUtil, certifiedProductDAO, rwtProgramStartDate, rwtProgramFirstEligibilityYear,
               criteriaService, ff4j);
    }

}
