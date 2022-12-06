package gov.healthit.chpl.complaint.rules;

import gov.healthit.chpl.complaint.domain.Complaint;
import gov.healthit.chpl.manager.rules.ValidationRule;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class ComplaintAcbChanged extends ValidationRule<ComplaintValidationContext> {
    public ComplaintAcbChanged() {
        super();
    }

    @Override
    public boolean isValid(ComplaintValidationContext context) {
        try {
            // Get the original complaint
            Complaint original = context.getComplaintDAO().getComplaint(context.getComplaint().getId());
            if (context.getComplaint().getCertificationBody().getId()
                    .equals(original.getCertificationBody().getId())) {
                return true;
            } else {
                getMessages().add(getErrorMessage("complaints.update.acbChange"));
                return false;
            }
        } catch (Exception e) {
            String error = getErrorMessage("complaints.error");
            LOGGER.error(error, e);
            getMessages().add(error);
            return false;
        }
    }

}
