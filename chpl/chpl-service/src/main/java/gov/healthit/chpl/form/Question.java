package gov.healthit.chpl.form;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Singular;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Question {
    private Long id;

    @JsonIgnore
    private SectionHeading sectionHeading;

    private ResponseCardinalityType responseCardinalityType;

    @Singular
    private List<AllowedResponse> allowedResponses;
    private String question;
}
