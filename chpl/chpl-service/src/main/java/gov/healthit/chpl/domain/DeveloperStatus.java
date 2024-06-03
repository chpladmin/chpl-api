package gov.healthit.chpl.domain;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeveloperStatus implements Serializable {
    private static final long serialVersionUID = 4646214778954081679L;

    @Schema(description = "Developer status internal id.")
    private Long id;

    @Schema(description = "Developer status name")
    private String name;

    public DeveloperStatus(DeveloperStatus other) {
        this();
        this.setId(other.getId());
        this.setName(other.getName());
    }
}
