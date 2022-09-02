package gov.healthit.chpl.form;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;

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

    public String formatResponse(Long sectionHeadingId) {
        String attestationResponse = getSectionHeadings().stream()
                .filter(section -> sectionHeadingId.equals(section.getId()))
                .flatMap(section -> section.getFormItems().get(0).getSubmittedResponses().stream())
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

    public String formatOptionalResponsesForCondition(Long sectionHeadingId) {
        String optionalResponse = getSectionHeadings().stream()
                .filter(section -> sectionHeadingId.equals(section.getId()))
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
}
