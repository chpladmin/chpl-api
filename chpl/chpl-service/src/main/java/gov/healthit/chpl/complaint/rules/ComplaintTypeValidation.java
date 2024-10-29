package gov.healthit.chpl.complaint.rules;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import gov.healthit.chpl.complaint.domain.Complaint;
import gov.healthit.chpl.complaint.domain.ComplaintType;
import gov.healthit.chpl.manager.rules.ValidationRule;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@NoArgsConstructor
public class ComplaintTypeValidation extends ValidationRule<ComplaintValidationContext> {
    private static final String COMPLAINT_TYPE_OTHER = "Other";

    private List<ComplaintType> complaintTypes;

    @Override
    public boolean isValid(ComplaintValidationContext context) {
        try {
            // Get all of the complaint types for validation purposes..
            this.complaintTypes = context.getComplaintDAO().getComplaintTypes();

            // At least one Complaint type is required
            if (CollectionUtils.isEmpty(context.getComplaint().getComplaintTypes())) {
                getMessages().add(getErrorMessage("complaints.complaintType.required"));
                return false;
            }

            // Complaint type value must exist
            context.getComplaint().getComplaintTypes().stream()
                .forEach(complaintType -> {
                    if (!doesComplaintTypeExist(complaintType)) {
                        getMessages().add(getErrorMessage("complaints.complaintType.notExists"));
                        return;
                    }
                });

            // complaint type other must exist if any complaint type = other
            if (isAnyComplaintTypeSetToOther(context.getComplaint())
                    && StringUtils.isEmpty(context.getComplaint().getComplaintTypesOther())) {
                getMessages().add(getErrorMessage("complaints.complaintType.otherMissing"));
                return false;
            } else if (!isAnyComplaintTypeSetToOther(context.getComplaint())) {
                context.getComplaint().setComplaintTypesOther(null);
            }
            return true;
        } catch (Exception e) {
            String error = getErrorMessage("complaints.error");
            LOGGER.error(error, e);
            getMessages().add(error);
            return false;
        }
    }

    private boolean doesComplaintTypeExist(ComplaintType type) {
        if (type.getId() != null) {
            return getComplaintTypeById(type.getId()) != null;
        } else if (!StringUtils.isEmpty(type.getName())) {
            return getComplaintTypeByName(type.getName()) != null;
        }
        return false;
    }

    private ComplaintType getComplaintTypeById(Long id) {
        return this.complaintTypes.stream()
            .filter(ct -> ct.getId().equals(id))
            .findAny()
            .orElse(null);
    }

    private ComplaintType getComplaintTypeByName(String name) {
        return this.complaintTypes.stream()
            .filter(ct -> ct.getName().equals(name))
            .findAny()
            .orElse(null);
    }

    private Boolean isAnyComplaintTypeSetToOther(Complaint complaint) {
        ComplaintType otherComplaintType = getComplaintTypeByName(complaint, COMPLAINT_TYPE_OTHER);
        return otherComplaintType != null;
    }

    private ComplaintType getComplaintTypeByName(Complaint complaint, String name) {
        return complaint.getComplaintTypes().stream()
            .filter(ct -> ct.getName().equals(name))
            .findAny()
            .orElse(null);
    }
}
