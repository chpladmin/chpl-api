package gov.healthit.chpl.manager.rules.complaints;

import gov.healthit.chpl.manager.rules.ValidationRule;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@NoArgsConstructor
public class ComplaintStatusOpen extends ValidationRule<ComplaintValidationContext> {
    private ComplaintValidationContext context;

    @Override
    public boolean isValid(ComplaintValidationContext context) {
        try {
            this.context = context;

            // Is the complaint status Open?
            if (context.getComplaint().getClosedDate() == null) {
                return true;
            } else {
                getMessages().add(getErrorMessage("complaints.create.statusOpen"));
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
