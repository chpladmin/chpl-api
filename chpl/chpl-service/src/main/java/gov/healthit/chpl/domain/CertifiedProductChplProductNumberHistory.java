package gov.healthit.chpl.domain;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import gov.healthit.chpl.util.LocalDateTimeDeserializer;
import gov.healthit.chpl.util.LocalDateTimeSerializer;
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
public class CertifiedProductChplProductNumberHistory implements Serializable {
    private static final long serialVersionUID = -2085183878716253974L;

    @Schema(description = "Internal ID of the CHPL Product Number history record.")
    private Long id;

    @Schema(description = "A CHPL Product Number that could have been used to reference this listing in the past.")
    private String chplProductNumber;

    @Schema(description = "A timestamp indicating when this historical CHPL Product Number stopped being referenced")
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime endDateTime;

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof CertifiedProductChplProductNumberHistory)) {
            return false;
        }

        CertifiedProductChplProductNumberHistory other = (CertifiedProductChplProductNumberHistory) obj;
        return Objects.equals(this.getChplProductNumber(), other.getChplProductNumber())
                && Objects.equals(this.endDateTime, other.getEndDateTime());
    }

    @Override
    public int hashCode() {
        return this.getChplProductNumber().hashCode() + this.endDateTime.hashCode();
    }
}
