package gov.healthit.chpl.changerequest.validation;

import java.util.HashMap;

import org.apache.commons.lang3.ObjectUtils;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import gov.healthit.chpl.changerequest.domain.ChangeRequestAttestationSubmission;
import gov.healthit.chpl.manager.rules.ValidationRule;

public class AttestationModificationValidation extends ValidationRule<ChangeRequestValidationContext> {

    private ObjectMapper mapper;

    public AttestationModificationValidation() {
        this.mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }

    @Override
    public boolean isValid(ChangeRequestValidationContext context) {
        if (isCurrentStatusEqual(context) && !changeRequestDetailsEquals(context)) {
            getMessages().add(getErrorMessage("changeRequest.noChangesAllowed"));
            return false;
        } else {
            return true;
        }
    }

    private Boolean isCurrentStatusEqual(ChangeRequestValidationContext context) {
        return context.getOrigChangeRequest().getCurrentStatus().getChangeRequestStatusType().getId().equals(
                context.getNewChangeRequest().getCurrentStatus().getChangeRequestStatusType().getId());
    }

    private Boolean changeRequestDetailsEquals(ChangeRequestValidationContext context) {
        if (ObjectUtils.allNotNull(context, context.getOrigChangeRequest(), context.getNewChangeRequest(),
                context.getOrigChangeRequest().getDetails(), context.getNewChangeRequest().getDetails())
                && context.getOrigChangeRequest().getDetails() instanceof HashMap
                && context.getNewChangeRequest().getDetails() instanceof HashMap) {
            ChangeRequestAttestationSubmission newChangeRequest = getDetailsFromHashMap((HashMap) context.getNewChangeRequest().getDetails());
            ChangeRequestAttestationSubmission origChangeRequest = getDetailsFromHashMap((HashMap) context.getOrigChangeRequest().getDetails());
            if (newChangeRequest != null && origChangeRequest != null) {
                return newChangeRequest.matches(origChangeRequest);
            }
        }
        return false;
    }

    private ChangeRequestAttestationSubmission getDetailsFromHashMap(HashMap<String, Object> map) {
        return  mapper.convertValue(map, ChangeRequestAttestationSubmission.class);
    }
}
