package gov.healthit.chpl.changerequest.validation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import gov.healthit.chpl.attestation.domain.Attestation;
import gov.healthit.chpl.attestation.domain.AttestationForm;
import gov.healthit.chpl.attestation.domain.AttestationSubmittedResponse;
import gov.healthit.chpl.changerequest.domain.ChangeRequestAttestationSubmission;
import gov.healthit.chpl.manager.rules.ValidationRule;
import gov.healthit.chpl.util.AuthUtil;

public class AttestationValidation extends ValidationRule<ChangeRequestValidationContext> {
    private ObjectMapper mapper;


    @Override
    public boolean isValid(ChangeRequestValidationContext context) {
        AttestationForm attestationForm = context.getDomainManagers().getAttestationManager().getAttestationForm();

        this.mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        ChangeRequestAttestationSubmission attestation = getChangeRequestAttestationFromMap((HashMap) context.getNewChangeRequest().getDetails());

        if (isChangeRequestNew(context)) {
            getMessages().addAll(validateSignature(context, attestation));
        }

        //getMessages().addAll(getMissingAttestations(attestation, attestationForm).stream()
        //        .map(question -> String.format(getErrorMessage("changeRequest.attestation.questionNotAnswered"), question.getQuestion()))
        //        .collect(Collectors.toList()));

        //getMessages().addAll(getInvalidResponses(attestation, attestationForm).stream()
        //        .map(response -> String.format(getErrorMessage("changeRequest.attestation.invalidResponse"),
        //                getValidResponseText(response.getAnswer().getId(), attestationForm),
        //                getQuestionText(response.getQuestion().getId(), attestationForm)))
        //        .collect(Collectors.toList()));

        return getMessages().size() == 0;
    }

    private List<String> validateSignature(ChangeRequestValidationContext context, ChangeRequestAttestationSubmission attestation) {
        List<String> errors = new ArrayList<String>();
        if (attestation.getSignature() == null || !AuthUtil.getCurrentUser().getFullName().equals(attestation.getSignature())) {
            errors.add(getErrorMessage("changeRequest.attestation.invalidSignature"));
        }
        return errors;
    }

    private List<Attestation> getMissingAttestations(ChangeRequestAttestationSubmission attestationSubmission, AttestationForm attestationForm) {

        List<Attestation> submittedAttestations = attestationSubmission.getResponses().stream()
                .map(resp -> resp.getAttestation())
                .collect(Collectors.toList());

        return subtractListsOfAttestationQuestions(attestationForm.getAttestations(), submittedAttestations);
    }

    private List<AttestationSubmittedResponse> getInvalidResponses(ChangeRequestAttestationSubmission attestation, AttestationForm attestationForm) {
        return attestation.getResponses().stream()
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

    private List<Attestation> subtractListsOfAttestationQuestions(List<Attestation> listA, List<Attestation> listB) {
        Predicate<Attestation> notInListB = questionFromA -> !listB.stream()
                .anyMatch(question -> questionFromA.getId().equals(question.getId()));

        return listA.stream()
                .filter(notInListB)
                .collect(Collectors.toList());
    }

    private boolean isChangeRequestNew(ChangeRequestValidationContext context) {
        return context.getOrigChangeRequest() == null;
    }
}
