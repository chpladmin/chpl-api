package gov.healthit.chpl.manager.rules.complaints;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.manager.rules.ValidationRule;

@Component
public class ComplaintValidationFactory {
    public static final String ACB_CHANGE = "ACB_CHANGE";
    public static final String OPEN_STATUS = "OPEN_STATUS";
    public static final String COMPLAINT_TYPE = "COMPLAINT_TYPE";
    public static final String RECEIVED_DATE = "RECEIVED_DATE";
    public static final String ACB_COMPLAINT_ID = "ACB_COMPLAINT_ID";
    public static final String SUMMARY = "SUMMARY";
    public static final String LISTINGS = "LISTINGS";
    public static final String CLOSED_DATE = "CLOSED_DATE";

    public ValidationRule<ComplaintValidationContext> getRule(String name) {
        switch (name) {
        case ACB_CHANGE:
            return new ComplaintAcbChanged();
        case OPEN_STATUS:
            return new ComplaintStatusOpen();
        case COMPLAINT_TYPE:
            return new ComplainantTypeValidation();
        case RECEIVED_DATE:
            return new ReceivedDateValidation();
        case ACB_COMPLAINT_ID:
            return new AcbComplaintIdValidation();
        case SUMMARY:
            return new SummaryValidation();
        case LISTINGS:
            return new ComplaintListingValidation();
        case CLOSED_DATE:
            return new ClosedDateValidation();
        default:
            return null;
        }
    }
}
