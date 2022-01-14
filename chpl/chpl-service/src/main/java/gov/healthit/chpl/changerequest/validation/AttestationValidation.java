package gov.healthit.chpl.changerequest.validation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import gov.healthit.chpl.attestation.domain.AttestationForm;
import gov.healthit.chpl.attestation.domain.AttestationPeriod;
import gov.healthit.chpl.attestation.domain.AttestationQuestion;
import gov.healthit.chpl.attestation.domain.AttestationResponse;
import gov.healthit.chpl.attestation.manager.AttestationManager;
import gov.healthit.chpl.changerequest.domain.ChangeRequestAttestation;
import gov.healthit.chpl.manager.rules.ValidationRule;

@Component
public class AttestationValidation extends ValidationRule<ChangeRequestValidationContext> {
    private AttestationForm attestationForm;
    private List<AttestationPeriod> attestationPeriods;

    private ObjectMapper mapper;

    @Autowired
    public AttestationValidation(AttestationManager attestationManager) {
        this.attestationForm = attestationManager.getAttestationForm();
        this.attestationPeriods = attestationManager.getAllPeriods();

        this.mapper = new ObjectMapper();
    }

    @Override
    public List<String> getErrorMessages(ChangeRequestValidationContext context) {
        List<String> errorMessages = new ArrayList<String>();

        ChangeRequestAttestation attestation = getChangeRequestAttestationFromMap((HashMap) context.getNewChangeRequest().getDetails());

        errorMessages.addAll(validateAttestationPeriod(attestation));

        errorMessages.addAll(getMissingQuestions(attestation).stream()
                .map(question -> String.format(getErrorMessageFromResource("changeRequest.attestation.questionNotAnswered"), question.getQuestion()))
                .collect(Collectors.toList()));

        errorMessages.addAll(getInvalidResponses(attestation).stream()
                .map(response -> String.format(getErrorMessageFromResource("changeRequest.attestation.invalidResponse"),
                        getAnswerText(response.getAnswer().getId()),
                        getQuestionText(response.getQuestion().getId())))
                .collect(Collectors.toList()));

        return errorMessages;
    }

    private List<String> validateAttestationPeriod(ChangeRequestAttestation attestation) {
        List<String> errors = new ArrayList<String>();

        if (attestation == null || attestation.getAttestationPeriod() == null || attestation.getAttestationPeriod().getId() == null) {
            errors.add(String.format(getErrorMessageFromResource("changeRequest.attestation.invalidPeriod")));
        } else {
            Optional<AttestationPeriod> period = attestationPeriods.stream()
                    .filter(p -> p.getId().equals(attestation.getAttestationPeriod().getId()))
                    .findAny();

            if (!period.isPresent()) {
                errors.add(String.format(getErrorMessageFromResource("changeRequest.attestation.invalidPeriodId"), attestation.getAttestationPeriod().getId()));
            }
        }
        return errors;
    }

    private List<AttestationQuestion> getMissingQuestions(ChangeRequestAttestation attestation) {
        List<AttestationQuestion> allQuestions = attestationForm.getCategories().stream()
                .flatMap(cat -> cat.getQuestions().stream())
                .map(ques -> ques)
                .collect(Collectors.toList());

        List<AttestationQuestion> submittedQuestions = attestation.getResponses().stream()
                .map(resp -> resp.getQuestion())
                .collect(Collectors.toList());

        return subtractListsOfAttestationQuestions(allQuestions, submittedQuestions);
    }

    private List<AttestationResponse> getInvalidResponses(ChangeRequestAttestation attestation) {
        return attestation.getResponses().stream()
                .filter(response -> !isResponseValid(response))
                .collect(Collectors.toList());
    }

    private Boolean isResponseValid(AttestationResponse response) {
        return attestationForm.getCategories().stream()
            .flatMap(cat -> cat.getQuestions().stream())
            .filter(ques -> ques.getId().equals(response.getQuestion().getId()))
            .flatMap(ques -> ques.getAnswers().stream())
            .filter(answer -> answer.getId().equals(response.getAnswer().getId()))
            .findAny()
            .isPresent();
    }

    private String getQuestionText(Long questionId) {
        return attestationForm.getCategories().stream()
                .flatMap(cat -> cat.getQuestions().stream())
                .filter(ques -> ques.getId().equals(questionId))
                .map(ques -> ques.getQuestion())
                .findAny()
                .orElse("Not Found");
    }

    private String getAnswerText(Long answerId) {
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

}
