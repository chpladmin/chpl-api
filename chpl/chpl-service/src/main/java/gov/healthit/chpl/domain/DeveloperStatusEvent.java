package gov.healthit.chpl.domain;

import java.io.Serializable;
import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import gov.healthit.chpl.util.LocalDateDeserializer;
import gov.healthit.chpl.util.LocalDateSerializer;
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
public class DeveloperStatusEvent implements Serializable {
    private static final long serialVersionUID = 464621477897152179L;

    @Schema(description = "Developer status internal id.")
    private Long id;

    @Schema(description = "Developer status name. Indicates a developer ban or suspension.")
    private DeveloperStatus status;

    @Schema(description = "The reason for this status change. "
            + "It is required of the status changed to 'Under Certification Ban by ONC'")
    private String reason;

    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    @Schema(description = "Date the banned or suspended status became effective.")
    private LocalDate startDate;

    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    @Schema(description = "Date the ban or suspension ended.")
    private LocalDate endDate;

    public DeveloperStatusEvent(DeveloperStatusEvent other) {
        this();
        this.setId(other.getId());
        this.setStatus(other.getStatus());
    }
}
