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
    @Schema(description = "Day the banned or suspended status became effective.")
    private LocalDate startDay;

    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    @Schema(description = "Day the ban or suspension ended.")
    private LocalDate endDay;

    public DeveloperStatusEvent(DeveloperStatusEvent other) {
        this();
        this.setId(other.getId());
        this.setStatus(other.getStatus());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        DeveloperStatusEvent other = (DeveloperStatusEvent) obj;
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }
        if (status == null) {
            if (other.status != null) {
                return false;
            }
        } else if (!status.equals(other.status)) {
            return false;
        }
        if (reason == null) {
            if (other.reason != null) {
                return false;
            }
        } else if (!reason.equals(other.reason)) {
            return false;
        }
        if (startDay == null) {
            if (other.startDay != null) {
                return false;
            }
        } else if (!startDay.equals(other.startDay)) {
            return false;
        }
        if (endDay == null) {
            if (other.endDay != null) {
                return false;
            }
        } else if (!endDay.equals(other.endDay)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((status == null) ? 0 : status.hashCode());
        result = prime * result + ((reason == null) ? 0 : reason.hashCode());
        result = prime * result + ((startDay == null) ? 0 : startDay.hashCode());
        result = prime * result + ((endDay == null) ? 0 : endDay.hashCode());
        return result;
    }
}
