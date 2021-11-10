package gov.healthit.chpl.service.realworldtesting;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.activity.history.ListingActivityUtil;
import gov.healthit.chpl.activity.history.explorer.RealWorldTestingEligibilityActivityExplorer;
import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.service.CertificationCriterionService;

@Component
public class RealWorldTestingEligiblityServiceFactory {

    @Value("${realWorldTestingCriteriaKeys}")
    private String[] eligibleCriteriaKeys;

    @Value("${rwtProgramFirstEligibilityYear}")
    private Integer rwtProgramFirstEligibilityYear;

    @Value("#{T(java.time.LocalDate).parse('${rwtProgramStartDate}')}")
    private LocalDate rwtProgramStartDate;

    private CertificationCriterionService criterionService;
    private RealWorldTestingEligibilityActivityExplorer realWorldTestingEligibilityActivityExplorer;
    private ListingActivityUtil listingActivityUtil;
    private CertifiedProductDAO certifiedProductDAO;

    @Autowired
    public RealWorldTestingEligiblityServiceFactory(CertificationCriterionService criterionService, RealWorldTestingEligibilityActivityExplorer realWorldTestingEligibilityActivityExplorer,
            ListingActivityUtil listingActivityUtil, CertifiedProductDAO certifiedProductDAO) {
        this.criterionService = criterionService;
        this.realWorldTestingEligibilityActivityExplorer = realWorldTestingEligibilityActivityExplorer;
        this.listingActivityUtil = listingActivityUtil;
        this.certifiedProductDAO = certifiedProductDAO;
    }

    public RealWorldTestingEligiblityService getInstance() {
       return  new RealWorldTestingEligiblityService(criterionService, realWorldTestingEligibilityActivityExplorer,
               listingActivityUtil, certifiedProductDAO, eligibleCriteriaKeys, rwtProgramStartDate, rwtProgramFirstEligibilityYear);
    }

}
