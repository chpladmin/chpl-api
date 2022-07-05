package gov.healthit.chpl.form;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Question {
    private Long id;

    @JsonIgnore
    private SectionHeading sectionHeading;

    private ResponseCardinalityType responseCardinalityType;
    private List<AllowedResponse> allowedResponses;
    private String question;
}
