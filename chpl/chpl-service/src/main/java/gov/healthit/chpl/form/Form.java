package gov.healthit.chpl.form;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.collections4.CollectionUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Singular;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Form implements Serializable {
    private static final long serialVersionUID = 2148616530869605769L;
    private static final String INFO_BLOCKING_HEADING = "Information Blocking";
    private static final String ASSURANCES_HEADING = "Assurances";
    private static final String COMMUNICATIONS_HEADING = "Communications";
    private static final String RWT_HEADING = "Real World Testing";
    private static final String API_HEADING = "Application Programming Interfaces";

    private static final String COMPLIANT_RESPONSE = "Compliant";
    private static final String NOT_APPLICABLE_RESPONSE = "Not Applicable";
    private static final List<String> ASSURANCES_COMPLIANT_IS_APPLICABLE_RESPONSES = Stream.of(
            "Compliant with the requirements of 45 CFR 170.402; certifies to the criterion at 45 CFR 170.315(b)(10) and provides all of its customers of certified health IT with health IT certified to the certification criterion in 45 CFR 170.315(b)(10).",
            "Compliant with the requirements of 45 CFR 170.402; 45 CFR 170.402(a)(4) is applicable because we are a developer of a Certified Health IT Module that is part of a health IT product which electronically stores EHI and therefore must certify to the certification criterion in § 170.315(b)(10)."
    ).toList();

    private static final List<String> ASSURANCES_COMPLIANT_NOT_APPLICABLE_RESPONSES = Stream.of(
            "Compliant with the requirements of 45 CFR 170.402; does not certify to the criterion at 45 CFR 170.315(b)(10) or does not provide all of its customers of certified health IT with health IT certified to the certification criterion in 45 CFR 170.315(b)(10).",
            "Compliant with the requirements of 45 CFR 170.402; 45 CFR 170.402(a)(4) is not applicable because we are not a developer of a Certified Health IT Module that is part of a health IT product which electronically stores EHI."
    ).toList();

    private Long id;
    private String description;
    private String instructions;

    @Singular
    private List<SectionHeading> sectionHeadings;

    public List<FormItem> extractFormItems() {
        return sectionHeadings.stream()
                .map(sh -> sh.getFormItems().stream())
                .flatMap(fi -> fi)
                .toList();
    }
    public List<FormItem> extractFlatFormItems() {
        return sectionHeadings.stream()
                .map(sh -> gatherAllFormItems(sh.getFormItems()).stream())
                .flatMap(fi -> fi)
                .toList();
    }

    private List<FormItem> gatherAllFormItems(List<FormItem> formItems) {
        List<FormItem> accumulatedFormItems = new ArrayList<FormItem>();
        formItems.forEach(fi -> {
            accumulatedFormItems.add(fi);
            accumulatedFormItems.addAll(gatherAllFormItems(fi.getChildFormItems()));
        });

        return accumulatedFormItems;
    }

    public String formatResponse(Long questionId) {
        String attestationResponse = getSectionHeadings().stream()
                .flatMap(section -> section.getFormItems().stream())
                .filter(formItem -> formItem.getQuestion().getId().equals(questionId))
                .flatMap(formItem -> formItem.getSubmittedResponses().stream())
                .map(submittedResponse -> submittedResponse.getResponse())
                .collect(Collectors.joining("; "));
        if (attestationResponse == null) {
            return "";
        }
        return attestationResponse;
    }

    public String formatResponse(String sectionHeadingName) {
        String attestationResponse = getSectionHeadings().stream()
                .filter(section -> sectionHeadingName.startsWith(section.getName()))
                .flatMap(section -> section.getFormItems().get(0).getSubmittedResponses().stream())
                .map(submittedResponse -> submittedResponse.getResponse())
                .collect(Collectors.joining("; "));
        if (attestationResponse == null) {
            return "";
        }
        return attestationResponse;
    }

    public String formatOptionalResponsesForCondition(Long questionId) {
        String optionalResponse = getSectionHeadings().stream()
                .flatMap(section -> section.getFormItems().stream())
                .filter(formItem -> formItem.getQuestion().getId().equals(questionId)
                        && !CollectionUtils.isEmpty(formItem.getChildFormItems()))
                .map(formItem -> formItem.getChildFormItems().get(0))
                .flatMap(childFormItem -> childFormItem.getSubmittedResponses().stream())
                .map(submittedResponse -> submittedResponse.getResponse())
                .collect(Collectors.joining("; "));
        if (optionalResponse == null) {
            return "";
        }
        return optionalResponse;
    }

    public String formatOptionalResponsesForCondition(String sectionHeadingName) {
        String optionalResponse = getSectionHeadings().stream()
                .filter(section -> sectionHeadingName.startsWith(section.getName()))
                .map(section -> section.getFormItems().get(0))
                .filter(formItem -> !CollectionUtils.isEmpty(formItem.getChildFormItems()))
                .map(formItem -> formItem.getChildFormItems().get(0))
                .flatMap(childFormItem -> childFormItem.getSubmittedResponses().stream())
                .map(submittedResponse -> submittedResponse.getResponse())
                .collect(Collectors.joining("; "));
        if (optionalResponse == null) {
            return "";
        }
        return optionalResponse;
    }

    @JsonIgnore
    @Transient
    public Long getInformationBlockingQuestionId() {
        return getQuestionIdFromHeading(INFO_BLOCKING_HEADING);
    }

    @JsonIgnore
    @Transient
    public Long getAssurancesQuestionId() {
        return getQuestionIdFromHeading(ASSURANCES_HEADING);
    }

    @JsonIgnore
    @Transient
    public Long getCommunicationQuestionId() {
        return getQuestionIdFromHeading(COMMUNICATIONS_HEADING);
    }

    @JsonIgnore
    @Transient
    public Long getRwtQuestionId() {
        return getQuestionIdFromHeading(RWT_HEADING);
    }

    @JsonIgnore
    @Transient
    public Long getApiQuestionId() {
        return getQuestionIdFromHeading(API_HEADING);
    }

    private Long getQuestionIdFromHeading(String headingText) {
        return getSectionHeadings().stream()
                .filter(heading -> heading.getName().equals(headingText))
                .flatMap(heading -> heading.getFormItems().stream())
                .filter(formItem -> formItem.getRequired())
                .map(formItem -> formItem.getQuestion().getId())
                .findAny()
                .orElse(null);
    }

    @JsonIgnore
    @Transient
    public Long getNotApplicableResponseId(Long questionId) {
        return getAllowedResponseIdFromQuestion(questionId, NOT_APPLICABLE_RESPONSE);
    }

    @JsonIgnore
    @Transient
    public Long getCompliantResponseId(Long questionId) {
        return getAllowedResponseIdFromQuestion(questionId, COMPLIANT_RESPONSE);
    }

    @JsonIgnore
    @Transient
    public Long getAssurancesCompliantIsApplicableResponseId() {
        Long questionId = getAssurancesQuestionId();
        return getAllowedResponseIdFromQuestion(questionId, ASSURANCES_COMPLIANT_IS_APPLICABLE_RESPONSES);
    }

    @JsonIgnore
    @Transient
    public Long getAssurancesCompliantNotApplicableResponseId() {
        Long questionId = getAssurancesQuestionId();
        return getAllowedResponseIdFromQuestion(questionId, ASSURANCES_COMPLIANT_NOT_APPLICABLE_RESPONSES);
    }

    private Long getAllowedResponseIdFromQuestion(Long questionId, String responseText) {
        return extractFormItems().stream()
                .filter(formItem -> formItem.getQuestion().getId().equals(questionId))
                .flatMap(formItem -> formItem.getQuestion().getAllowedResponses().stream())
                .filter(allowedResponse -> allowedResponse.getResponse().equals(responseText))
                .map(allowedResponse -> allowedResponse.getId())
                .findAny()
                .orElse(null);
    }

    private Long getAllowedResponseIdFromQuestion(Long questionId, List<String> possibleResponses) {
        return extractFormItems().stream()
                .filter(formItem -> formItem.getQuestion().getId().equals(questionId))
                .flatMap(formItem -> formItem.getQuestion().getAllowedResponses().stream())
                .filter(allowedResponse -> possibleResponses.contains(allowedResponse.getResponse()))
                .map(allowedResponse -> allowedResponse.getId())
                .findAny()
                .orElse(null);
    }
}
