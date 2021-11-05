package gov.healthit.chpl.domain;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import gov.healthit.chpl.dto.TestFunctionalityDTO;
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

    public TestFunctionality(TestFunctionalityDTO dto) {
        this.id = dto.getId();
        this.name = dto.getNumber();
        this.description = dto.getName();
        this.year = dto.getYear();
        if (dto.getPracticeType() != null) {
            this.practiceType = new PracticeType(dto.getPracticeType());
        }
    }
}
