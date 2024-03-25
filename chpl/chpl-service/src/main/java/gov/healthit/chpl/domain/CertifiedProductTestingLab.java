package gov.healthit.chpl.domain;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
@Data
@AllArgsConstructor
public class CertifiedProductTestingLab implements Serializable {
    private static final long serialVersionUID = -2078691100124619582L;

    @Schema(description = "Testing Lab to listing mapping internal ID")
    private Long id;


    @Schema(description = "Testing Lab")
    private TestingLab testingLab;

    public CertifiedProductTestingLab() {
        super();
    }

    public boolean matches(final CertifiedProductTestingLab other) {
        boolean result = false;

        if (other == null || other.getTestingLab() == null || this.getTestingLab() == null) {
            result = false;
        } else if (this.getTestingLab().getId() != null && other.getTestingLab().getId() != null
                && this.getTestingLab().getId().longValue() == other.getTestingLab().getId().longValue()) {
            result = true;
        } else if (this.getTestingLab().getName() != null && other.getTestingLab().getName() != null
                && this.getTestingLab().getName().equalsIgnoreCase(other.getTestingLab().getName())) {
            result = true;
        }
        return result;
    }
}
