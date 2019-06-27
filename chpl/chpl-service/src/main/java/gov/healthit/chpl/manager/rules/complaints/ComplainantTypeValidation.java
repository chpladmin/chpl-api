package gov.healthit.chpl.manager.rules.complaints;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import gov.healthit.chpl.dto.ComplainantTypeDTO;
import gov.healthit.chpl.manager.rules.ValidationRule;

public class ComplainantTypeValidation extends ValidationRule<ComplaintValidationContext> {
    private static final Logger LOGGER = LogManager.getLogger(ComplainantTypeValidation.class);
    private static final String COMPLAINANT_TYPE_OTHER = "Other - [Please Describe]";

    private ComplaintValidationContext context;
    List<ComplainantTypeDTO> complainantTypes;

    public ComplainantTypeValidation() {
        super();
    }

    @Override
    public boolean isValid(ComplaintValidationContext context) {
        this.context = context;
        try {
            // Get all of the complainant types for validation purposes..
            this.complainantTypes = context.getComplaintDAO().getComplainantTypes();

            // Complainant type is required
            if (context.getComplaintDTO().getComplainantType() == null
                    || context.getComplaintDTO().getComplainantType().getId() == null) {
                getMessages().add(getErrorMessage("complaints.complainantType.required"));
                return false;
            }

            // Complainant type value must exist
            if (!doesComplainantTypeExist(context.getComplaintDTO().getComplainantType().getId())) {
                getMessages().add(getErrorMessage("complaints.complainantType.notExists"));
                return false;
            }

            // Complainant type other must exist if complaint type = other
            if (isComplainantTypeSetToOther(context.getComplaintDTO().getComplainantType())
                    && StringUtils.isEmpty(context.getComplaintDTO().getComplainantTypeOther())) {
                getMessages().add(getErrorMessage("complaints.complainantType.otherMissing"));
                return false;
            }
            return true;
        } catch (Exception e) {
            String error = getErrorMessage("complaints.error");
            LOGGER.error(error, e);
            getMessages().add(error);
            return false;
        }
    }

    private Boolean doesComplainantTypeExist(long complainantTypeId) {
        for (ComplainantTypeDTO complainantType : this.complainantTypes) {
            if (complainantType.getId().equals(complainantTypeId)) {
                return true;
            }
        }
        return false;
    }

    private Boolean isComplainantTypeSetToOther(ComplainantTypeDTO complainant) {
        ComplainantTypeDTO otherComplainantType = getComplainantTypeByName(COMPLAINANT_TYPE_OTHER);
        return complainant.getId().equals(otherComplainantType.getId());
    }

    private ComplainantTypeDTO getComplainantTypeByName(String name) {
        for (ComplainantTypeDTO complainantType : this.complainantTypes) {
            if (complainantType.getName().equals(name)) {
                return complainantType;
            }
        }
        return null;
    }
}
