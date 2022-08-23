package gov.healthit.chpl.questionableactivity.listing;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.concept.QuestionableActivityTriggerConcept;
import gov.healthit.chpl.dto.questionableActivity.QuestionableActivityListingDTO;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class RwtPlansUpadatedOutsideNormalPeriod extends RwtUpdatedOutsideNormalPeriod implements ListingActivity {
    private String rwtPlanStartDayOfYear;
    private String rwtPlanDueDate;

    @Autowired
    public RwtPlansUpadatedOutsideNormalPeriod(@Value("${rwtPlanStartDayOfYear}") String rwtPlanStartDayOfYear,
            @Value("${rwtPlanDueDate}") String rwtPlanDueDate) {
        this.rwtPlanStartDayOfYear = rwtPlanStartDayOfYear;
        this.rwtPlanDueDate = rwtPlanDueDate;
    }

    @Override
    String getStartMonthAndDay() {
        return rwtPlanStartDayOfYear;
    }

    @Override
    String getEndMonthAndDay() {
        return rwtPlanDueDate;
    }

    @Override
    public List<QuestionableActivityListingDTO> check(CertifiedProductSearchDetails origListing, CertifiedProductSearchDetails newListing) {
        return getQuestionableActivity(origListing, newListing);
    }

    @Override
    public QuestionableActivityTriggerConcept getTriggerType() {
        return QuestionableActivityTriggerConcept.RWT_PLANS_UPDATED_OUTSIDE_NORMAL_PERIOD;
    }
}
