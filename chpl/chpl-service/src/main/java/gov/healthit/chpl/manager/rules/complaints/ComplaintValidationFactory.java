package gov.healthit.chpl.manager.rules.complaints;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.manager.rules.ValidationRule;

@Component
public class ComplaintValidationFactory {
    public final static String ACB_CHANGE = "ACB_CHANGE";
    public final static String OPEN_STATUS = "OPEN_STATUS";
    public final static String COMPLAINT_TYPE = "COMPLAINT_TYPE";
    public final static String COMPLAINT_STATUS_TYPE_EXISTS = "COMPLAINT_STATUS_TYPE_EXISTS";

    public ValidationRule<ComplaintValidationContext> getRule(String name) {
        switch (name) {
        case ACB_CHANGE:
            return new ComplaintAcbChanged();
        case OPEN_STATUS:
            return new ComplaintStatusOpen();
        case COMPLAINT_TYPE:
            return new ComplaintTypeValidation();
        case COMPLAINT_STATUS_TYPE_EXISTS:
            return new ComplaintStatusTypeValid();
        default:
            return null;
        }
    }
}
