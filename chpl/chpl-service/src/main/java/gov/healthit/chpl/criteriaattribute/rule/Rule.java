package gov.healthit.chpl.criteriaattribute.rule;

import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Rule implements Serializable {
    private static final long serialVersionUID = 1896885792278074234L;

    @Schema(description = "Rule internal ID")
    private Long id;

    @Schema(description = "A string value representing an abbreviation for the specific rule name.")
    private String name;
}
