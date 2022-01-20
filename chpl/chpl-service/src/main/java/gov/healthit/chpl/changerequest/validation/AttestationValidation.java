package gov.healthit.chpl.changerequest.validation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import gov.healthit.chpl.attestation.domain.AttestationForm;
import gov.healthit.chpl.attestation.domain.AttestationPeriod;
import gov.healthit.chpl.attestation.domain.AttestationQuestion;
import gov.healthit.chpl.attestation.domain.AttestationResponse;
import gov.healthit.chpl.changerequest.domain.ChangeRequestAttestation;
import gov.healthit.chpl.manager.rules.ValidationRule;
import gov.healthit.chpl.util.AuthUtil;

public class AttestationValidation extends ValidationRule<ChangeRequestValidationContext> {
    private ObjectMapper mapper;


    @Override
    public boolean isValid(ChangeRequestValidationContext context) {
        AttestationForm attestationForm = context.getDomainManagers().getAttestationManager().getAttestationForm();

        this.mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        ChangeRequestAttestation attestation = getChangeRequestAttestationFromMap((HashMap) context.getNewChangeRequest().getDetails());

        if (isChangeRequestNew(context)) {
            getMessages().addAll(validateSignature(context, attestation));
        }

        //getMessages().addAll(validateAttestationPeriod(context, attestation));

        getMessages().addAll(getMissingQuestions(attestation, attestationForm).stream()
                .map(question -> String.format(getErrorMessage("changeRequest.attestation.questionNotAnswered"), question.getQuestion()))
                .collect(Collectors.toList()));

        getMessages().addAll(getInvalidResponses(attestation, attestationForm).stream()
                .map(response -> String.format(getErrorMessage("changeRequest.attestation.invalidResponse"),
                        getAnswerText(response.getAnswer().getId(), attestationForm),
                        getQuestionText(response.getQuestion().getId(), attestationForm)))
                .collect(Collectors.toList()));

        return getMessages().size() == 0;
    }

    private List<String> validateSignature(ChangeRequestValidationContext context, ChangeRequestAttestation attestation) {
        List<String> errors = new ArrayList<String>();
        if (attestation.getSignature() == null || !AuthUtil.getCurrentUser().getFullName().equals(attestation.getSignature())) {
            errors.add(getErrorMessage("changeRequest.attestation.invalidSignature"));
        }
        return errors;
    }

    private List<String> validateAttestationPeriod(ChangeRequestValidationContext context, ChangeRequestAttestation attestation) {
        List<String> errors = new ArrayList<String>();
        List<AttestationPeriod> attestationPeriods = context.getDomainManagers().getAttestationManager().getAllPeriods();

        if (attestation == null || attestation.getAttestationPeriod() == null || attestation.getAttestationPeriod().getId() == null) {
            errors.add(String.format(getErrorMessage("changeRequest.attestation.invalidPeriod")));
        } else {
            Optional<AttestationPeriod> period = attestationPeriods.stream()
                    .filter(p -> p.getId().equals(attestation.getAttestationPeriod().getId()))
                    .findAny();

            if (!period.isPresent()) {
                errors.add(String.format(getErrorMessage("changeRequest.attestation.invalidPeriodId"), attestation.getAttestationPeriod().getId()));
            }
        }
        return errors;
    }

    private List<AttestationQuestion> getMissingQuestions(ChangeRequestAttestation attestation, AttestationForm attestationForm) {

        List<AttestationQuestion> allQuestions = attestationForm.getCategories().stream()
                .flatMap(cat -> cat.getQuestions().stream())
                .map(ques -> ques)
                .collect(Collectors.toList());

        List<AttestationQuestion> submittedQuestions = attestation.getResponses().stream()
                .map(resp -> resp.getQuestion())
                .collect(Collectors.toList());

        return subtractListsOfAttestationQuestions(allQuestions, submittedQuestions);
    }

    private List<AttestationResponse> getInvalidResponses(ChangeRequestAttestation attestation, AttestationForm attestationForm) {
        return attestation.getResponses().stream()
                .filter(response -> !isResponseValid(response, attestationForm))
                .collect(Collectors.toList());
    }

    private Boolean isResponseValid(AttestationResponse response, AttestationForm attestationForm) {
        return attestationForm.getCategories().stream()
            .flatMap(cat -> cat.getQuestions().stream())
            .filter(ques -> ques.getId().equals(response.getQuestion().getId()))
            .flatMap(ques -> ques.getAnswers().stream())
            .filter(answer -> answer.getId().equals(response.getAnswer().getId()))
            .findAny()
            .isPresent();
    }

    private String getQuestionText(Long questionId, AttestationForm attestationForm) {
        return attestationForm.getCategories().stream()
                .flatMap(cat -> cat.getQuestions().stream())
                .filter(ques -> ques.getId().equals(questionId))
                .map(ques -> ques.getQuestion())
                .findAny()
                .orElse("Not Found");
    }

    private String getAnswerText(Long answerId, AttestationForm attestationForm) {
        return attestationForm.getCategories().stream()
                .flatMap(cat -> cat.getQuestions().stream())
                .flatMap(ques -> ques.getAnswers().stream())
                .filter(ans -> ans.getId().equals(answerId))
                .map(ans -> ans.getAnswer())
                .findAny()
                .orElse("Not Found");
    }

    private ChangeRequestAttestation getChangeRequestAttestationFromMap(HashMap<String, Object> map) {
        return  mapper.convertValue(map, ChangeRequestAttestation.class);
    }

    private List<AttestationQuestion> subtractListsOfAttestationQuestions(List<AttestationQuestion> listA, List<AttestationQuestion> listB) {
        Predicate<AttestationQuestion> notInListB = questionFromA -> !listB.stream()
                .anyMatch(question -> questionFromA.getId().equals(question.getId()));

        return listA.stream()
                .filter(notInListB)
                .collect(Collectors.toList());
    }

    private boolean isChangeRequestNew(ChangeRequestValidationContext context) {
        return context.getOrigChangeRequest() == null;
    }
}
