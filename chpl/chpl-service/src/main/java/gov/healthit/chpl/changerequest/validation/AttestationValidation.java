package gov.healthit.chpl.changerequest.validation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.functors.DefaultEquator;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import gov.healthit.chpl.attestation.domain.Attestation;
import gov.healthit.chpl.attestation.domain.AttestationForm;
import gov.healthit.chpl.attestation.domain.AttestationSubmittedResponse;
import gov.healthit.chpl.changerequest.domain.ChangeRequestAttestationSubmission;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.manager.rules.ValidationRule;

public class AttestationValidation extends ValidationRule<ChangeRequestValidationContext> {
    private ObjectMapper mapper;


    @Override
    public boolean isValid(ChangeRequestValidationContext context) {
        AttestationForm attestationForm = context.getDomainManagers().getAttestationManager().getAttestationForm();

        this.mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        ChangeRequestAttestationSubmission attestationSubmission = getChangeRequestAttestationFromMap((HashMap) context.getNewChangeRequest().getDetails());


        if (isChangeRequestNew(context)) {
            getMessages().addAll(canDeveloperSubmitChangeRequest(context));
        } else {
            if (hasAttestationInformationChanged(context)) {
                getMessages().addAll(validateSignature(context, attestationSubmission));
            }
        }

        getMessages().addAll(getMissingAttestations(attestationSubmission, attestationForm).stream()
                .map(att -> String.format(getErrorMessage("changeRequest.attestation.attestationNotAnswered"), att.getDescription()))
                .collect(Collectors.toList()));

        getMessages().addAll(getInvalidResponses(attestationSubmission, attestationForm).stream()
                .map(resp -> String.format(getErrorMessage("changeRequest.attestation.invalidResponse"),
                        getValidResponseText(resp.getResponse().getId(), attestationForm),
                        getAttestationText(resp.getAttestation().getId(), attestationForm)))
                .collect(Collectors.toList()));


        return getMessages().size() == 0;
    }

    private List<String> canDeveloperSubmitChangeRequest(ChangeRequestValidationContext context) {
        List<String> errors = new ArrayList<String>();
        try {
            if (!context.getDomainManagers().getAttestationManager().canDeveloperSubmitChangeRequest(context.getNewChangeRequest().getDeveloper().getDeveloperId())) {
                errors.add(getErrorMessage("changeRequest.attestation.submissionWindow"));
            }
        } catch (EntityRetrievalException e) {
            errors.add(getErrorMessage("changeRequest.attestation.submissionWindow"));
        }
        return errors;
    }

    private List<String> validateSignature(ChangeRequestValidationContext context, ChangeRequestAttestationSubmission attestation) {
        List<String> errors = new ArrayList<String>();
        if (attestation.getSignature() == null || !context.getCurrentUser().getFullName().equals(attestation.getSignature())) {
            errors.add(getErrorMessage("changeRequest.attestation.invalidSignature"));
        }
        return errors;
    }

    private List<Attestation> getMissingAttestations(ChangeRequestAttestationSubmission attestationSubmission, AttestationForm attestationForm) {

        List<Attestation> submittedAttestations = attestationSubmission.getAttestationResponses().stream()
                .filter(resp -> resp.getAttestation() != null)
                .map(resp -> resp.getAttestation())
                .collect(Collectors.toList());

        return subtractListsOfAttestations(attestationForm.getAttestations(), submittedAttestations);
    }

    private List<AttestationSubmittedResponse> getInvalidResponses(ChangeRequestAttestationSubmission attestationSubmission, AttestationForm attestationForm) {
        return attestationSubmission.getAttestationResponses().stream()
                .filter(response -> !isResponseValid(response, attestationForm))
                .collect(Collectors.toList());
    }

    private Boolean isResponseValid(AttestationSubmittedResponse response, AttestationForm attestationForm) {
        return attestationForm.getAttestations().stream()
                .filter(att -> att.getId().equals(response.getAttestation().getId()))
                .flatMap(resp -> resp.getValidResponses().stream())
                .filter(resp -> resp.getId().equals(response.getResponse().getId()))
                .findAny()
                .isPresent();
    }

    private String getAttestationText(Long attestationId, AttestationForm attestationForm) {
        return attestationForm.getAttestations().stream()
                .filter(att -> att.getId().equals(attestationId))
                .map(att -> att.getDescription())
                .findAny()
                .orElse("Not Found");
    }

    private String getValidResponseText(Long responseId, AttestationForm attestationForm) {
        return attestationForm.getAttestations().stream()
                .flatMap(att -> att.getValidResponses().stream())
                .filter(resp -> resp.getId().equals(responseId))
                .map(resp -> resp.getResponse())
                .findAny()
                .orElse("Not Found");
    }

    private ChangeRequestAttestationSubmission getChangeRequestAttestationFromMap(HashMap<String, Object> map) {
        return  mapper.convertValue(map, ChangeRequestAttestationSubmission.class);
    }

    private List<Attestation> subtractListsOfAttestations(List<Attestation> listA, List<Attestation> listB) {
        Predicate<Attestation> notInListB = attestationFromA -> !listB.stream()
                .anyMatch(attestation -> attestationFromA.getId().equals(attestation.getId()));

        return listA.stream()
                .filter(notInListB)
                .collect(Collectors.toList());
    }

    private boolean isChangeRequestNew(ChangeRequestValidationContext context) {
        return context.getOrigChangeRequest() == null;
    }

    @SuppressWarnings("unchecked")
    private boolean hasAttestationInformationChanged(ChangeRequestValidationContext context) {
        ChangeRequestAttestationSubmission attestationSubmission = getChangeRequestAttestationFromMap((HashMap) context.getNewChangeRequest().getDetails());

        if (!isChangeRequestNew(context)) {
            ChangeRequestAttestationSubmission attestationOriginal = (ChangeRequestAttestationSubmission) context.getOrigChangeRequest().getDetails();
            return !CollectionUtils.isEqualCollection(
                    attestationSubmission.getAttestationResponses(),
                    attestationOriginal.getAttestationResponses(),
                    DefaultEquator.INSTANCE);
        } else {
            return false;
        }
    }
}
