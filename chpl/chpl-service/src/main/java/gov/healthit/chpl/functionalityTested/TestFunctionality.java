package gov.healthit.chpl.functionalityTested;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import gov.healthit.chpl.domain.PracticeType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TestFunctionality implements Serializable {
    private static final long serialVersionUID = 620315627813874301L;
    private Long id;
    private String name;
    private String description;
    private String year;
    private PracticeType practiceType;
}
