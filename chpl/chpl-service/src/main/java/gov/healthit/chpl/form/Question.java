package gov.healthit.chpl.form;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class Question {
    private Long id;
    private SectionHeading sectionHeading;
    private ResponseCardinalityType responseCardinalityType;
    private List<AllowedResponse> allowedResponses;
    private String question;
}
