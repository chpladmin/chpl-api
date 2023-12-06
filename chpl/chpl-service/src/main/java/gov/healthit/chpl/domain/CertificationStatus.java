package gov.healthit.chpl.domain;

import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CertificationStatus implements Serializable {

    private static final long serialVersionUID = 818896721132619130L;

    @Schema(description = "Internal ID")
    private Long id;

    @Schema(description = "Certification status name.")
    private String name;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }
}
