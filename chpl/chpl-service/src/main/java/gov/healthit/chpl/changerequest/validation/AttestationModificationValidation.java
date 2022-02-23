package gov.healthit.chpl.changerequest.validation;

import java.util.HashMap;

import org.apache.commons.lang3.ObjectUtils;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import gov.healthit.chpl.changerequest.domain.ChangeRequest;
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
                context.getOrigChangeRequest().getDetails(), context.getNewChangeRequest().getDetails())) {
            ChangeRequestAttestationSubmission origChangeRequest = getDetails(context.getOrigChangeRequest());
            ChangeRequestAttestationSubmission newChangeRequest = getDetails(context.getNewChangeRequest());
            if (newChangeRequest != null && origChangeRequest != null) {
                return newChangeRequest.matches(origChangeRequest);
            }
        }
        return false;
    }

    private ChangeRequestAttestationSubmission getDetails(ChangeRequest changeRequest) {
        if (changeRequest.getDetails() instanceof ChangeRequestAttestationSubmission) {
            return (ChangeRequestAttestationSubmission) changeRequest.getDetails();
        } else if (changeRequest.getDetails() instanceof HashMap) {
            return getDetailsFromHashMap((HashMap) changeRequest.getDetails());
        }
        return null;
    }

    private ChangeRequestAttestationSubmission getDetailsFromHashMap(HashMap<String, Object> map) {
        return  mapper.convertValue(map, ChangeRequestAttestationSubmission.class);
    }
}
