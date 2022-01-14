package gov.healthit.chpl.manager.rules.complaints;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import gov.healthit.chpl.domain.complaint.ComplainantType;
import gov.healthit.chpl.manager.rules.ValidationRule;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@NoArgsConstructor
public class ComplainantTypeValidation extends ValidationRule<ComplaintValidationContext> {
    private static final String COMPLAINANT_TYPE_OTHER = "Other - [Please Describe]";

    private ComplaintValidationContext context;
    private List<ComplainantType> complainantTypes;

    @Override
    public boolean getErrorMessages(ComplaintValidationContext context) {
        this.context = context;
        try {
            // Get all of the complainant types for validation purposes..
            this.complainantTypes = context.getComplaintDAO().getComplainantTypes();

            // Complainant type is required
            if (context.getComplaint().getComplainantType() == null
                    || context.getComplaint().getComplainantType().getId() == null) {
                getMessages().add(getErrorMessageFromResource("complaints.complainantType.required"));
                return false;
            }

            // Complainant type value must exist
            if (!doesComplainantTypeExist(context.getComplaint().getComplainantType().getId())) {
                getMessages().add(getErrorMessageFromResource("complaints.complainantType.notExists"));
                return false;
            }

            // Complainant type other must exist if complaint type = other
            if (isComplainantTypeSetToOther(context.getComplaint().getComplainantType())
                    && StringUtils.isEmpty(context.getComplaint().getComplainantTypeOther())) {
                getMessages().add(getErrorMessageFromResource("complaints.complainantType.otherMissing"));
                return false;
            }
            return true;
        } catch (Exception e) {
            String error = getErrorMessageFromResource("complaints.error");
            LOGGER.error(error, e);
            getMessages().add(error);
            return false;
        }
    }

    private Boolean doesComplainantTypeExist(long complainantTypeId) {
        for (ComplainantType complainantType : this.complainantTypes) {
            if (complainantType.getId().equals(complainantTypeId)) {
                return true;
            }
        }
        return false;
    }

    private Boolean isComplainantTypeSetToOther(ComplainantType complainant) {
        ComplainantType otherComplainantType = getComplainantTypeByName(COMPLAINANT_TYPE_OTHER);
        return complainant.getId().equals(otherComplainantType.getId());
    }

    private ComplainantType getComplainantTypeByName(String name) {
        for (ComplainantType complainantType : this.complainantTypes) {
            if (complainantType.getName().equals(name)) {
                return complainantType;
            }
        }
        return null;
    }
}
