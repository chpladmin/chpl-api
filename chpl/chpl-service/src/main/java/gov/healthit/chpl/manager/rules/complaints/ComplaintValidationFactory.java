package gov.healthit.chpl.manager.rules.complaints;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.manager.rules.ValidationRule;

@Component
public class ComplaintValidationFactory {
    public final static String ACB_CHANGE = "ACB_CHANGE";
    public final static String OPEN_STATUS = "OPEN_STATUS";
    public final static String COMPLAINT_TYPE = "COMPLAINT_TYPE";
    public final static String COMPLAINT_STATUS_TYPE = "COMPLAINT_STATUS_TYPE";
    public final static String RECEIVED_DATE = "RECEIVED_DATE";
    public final static String ACB_COMPLAINT_ID = "ACB_COMPLAINT_ID";
    public final static String SUMMARY = "SUMMARY";
    public final static String LISTINGS = "LISTINGS";

    public ValidationRule<ComplaintValidationContext> getRule(String name) {
        switch (name) {
        case ACB_CHANGE:
            return new ComplaintAcbChanged();
        case OPEN_STATUS:
            return new ComplaintStatusOpen();
        case COMPLAINT_TYPE:
            return new ComplaintTypeValidation();
        case COMPLAINT_STATUS_TYPE:
            return new ComplaintStatusTypeValidation();
        case RECEIVED_DATE:
            return new ReceivedDateValidation();
        case ACB_COMPLAINT_ID:
            return new AcbComplaintIdValidation();
        case SUMMARY:
            return new SummaryValidation();
        case LISTINGS:
            return new ComplaintListingValidation();
        default:
            return null;
        }
    }
}
