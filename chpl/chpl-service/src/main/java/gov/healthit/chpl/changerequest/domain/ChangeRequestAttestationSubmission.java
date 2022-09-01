package gov.healthit.chpl.changerequest.domain;

import java.io.Serializable;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import gov.healthit.chpl.attestation.domain.AttestationPeriod;
import gov.healthit.chpl.form.Form;
import gov.healthit.chpl.form.FormItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChangeRequestAttestationSubmission implements Serializable, ChangeRequestDetails {
    private static final long serialVersionUID = 2150025150434933303L;

    private Long id;
    private AttestationPeriod attestationPeriod;
    private Form form;
    private String signature;
    private String signatureEmail;

    public Boolean isEqual(ChangeRequestAttestationSubmission check) {
        return signature.equals(check.getSignature())
                && CollectionUtils.isEqualCollection(
                        form.extractFlatFormItems(),
                        check.getForm().extractFlatFormItems(),
                        new FormItem.FormItemByIdEquator());
    }

    public String formatResponse(Long conditionId) {
        String attestationResponse = getForm().getSectionHeadings().stream()
                .filter(section -> conditionId.equals(section.getId()))
                .flatMap(section -> section.getFormItems().get(0).getSubmittedResponses().stream())
                .map(submittedResponse -> submittedResponse.getResponse())
                .collect(Collectors.joining("; "));
        if (attestationResponse == null) {
            return "";
        }
        return attestationResponse;
    }

    public String formatResponse(String conditionName) {
        String attestationResponse = getForm().getSectionHeadings().stream()
                .filter(section -> conditionName.startsWith(section.getName()))
                .flatMap(section -> section.getFormItems().get(0).getSubmittedResponses().stream())
                .map(submittedResponse -> submittedResponse.getResponse())
                .collect(Collectors.joining("; "));
        if (attestationResponse == null) {
            return "";
        }
        return attestationResponse;
    }

    public String formatOptionalResponsesForCondition(Long conditionId) {
        String optionalResponse = getForm().getSectionHeadings().stream()
                .filter(section -> conditionId.equals(section.getId()))
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

    public String formatOptionalResponsesForCondition(String conditionName) {
        String optionalResponse = getForm().getSectionHeadings().stream()
                .filter(section -> conditionName.startsWith(section.getName()))
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
}
