package gov.healthit.chpl.manager.rules.complaints;

import gov.healthit.chpl.domain.complaint.Complaint;
import gov.healthit.chpl.manager.rules.ValidationRule;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class ComplaintAcbChanged extends ValidationRule<ComplaintValidationContext> {
    public ComplaintAcbChanged() {
        super();
    }

    @Override
    public boolean getErrorMessages(ComplaintValidationContext context) {
        try {
            // Get the original complaint
            Complaint original = context.getComplaintDAO().getComplaint(context.getComplaint().getId());
            if (context.getComplaint().getCertificationBody().getId()
                    .equals(original.getCertificationBody().getId())) {
                return true;
            } else {
                getMessages().add(getErrorMessageFromResource("complaints.update.acbChange"));
                return false;
            }
        } catch (Exception e) {
            String error = getErrorMessageFromResource("complaints.error");
            LOGGER.error(error, e);
            getMessages().add(error);
            return false;
        }
    }

}
