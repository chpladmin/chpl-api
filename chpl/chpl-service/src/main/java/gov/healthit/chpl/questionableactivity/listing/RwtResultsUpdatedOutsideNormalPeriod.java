package gov.healthit.chpl.questionableactivity.listing;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.questionableactivity.QuestionableActivityTriggerConcept;
import gov.healthit.chpl.questionableactivity.dto.QuestionableActivityListingDTO;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class RwtResultsUpdatedOutsideNormalPeriod extends RwtUpdatedOutsideNormalPeriod implements ListingActivity {
    private String rwtResultsStartDayOfYear;
    private String rwtResultsDueDate;

    @Autowired
    public RwtResultsUpdatedOutsideNormalPeriod(@Value("${rwtResultsStartDayOfYear}") String rwtResultsStartDayOfYear,
            @Value("${rwtResultsDueDate}") String rwtPlanDueDate) {
        this.rwtResultsStartDayOfYear = rwtResultsStartDayOfYear;
        this.rwtResultsDueDate = rwtPlanDueDate;
    }

    @Override
    String getStartMonthAndDay() {
        return rwtResultsStartDayOfYear;
    }

    @Override
    String getEndMonthAndDay() {
        return rwtResultsDueDate;
    }

    @Override
    public List<QuestionableActivityListingDTO> check(CertifiedProductSearchDetails origListing, CertifiedProductSearchDetails newListing) {
        return getQuestionableActivity(origListing.getRwtResultsUrl(), newListing.getRwtResultsUrl(), origListing.getRwtResultsCheckDate(),  newListing.getRwtResultsCheckDate());
    }

    @Override
    public QuestionableActivityTriggerConcept getTriggerType() {
        return QuestionableActivityTriggerConcept.RWT_RESULTS_UPDATED_OUTSIDE_NORMAL_PERIOD;
    }
}
