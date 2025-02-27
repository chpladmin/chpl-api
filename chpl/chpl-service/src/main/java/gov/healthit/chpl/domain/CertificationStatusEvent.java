package gov.healthit.chpl.domain;

import java.io.Serializable;
import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import gov.healthit.chpl.util.DateUtil;
import gov.healthit.chpl.util.LocalDateDeserializer;
import gov.healthit.chpl.util.LocalDateSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CertificationStatusEvent implements Serializable {
    private static final long serialVersionUID = -2498656549844148886L;

    @Schema(description = "Internal ID")
    private Long id;

    @Schema(description = "The day on which a change of certification status occurred.")
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate eventDay;

    @Schema(description = "The certification status for the listing on the eventDate.")
    private CertificationStatus status;

    @Schema(description = "The user-provided reason that a change of certification status occurred.")
    private String reason;
    private Long lastModifiedUser;
    private Long lastModifiedDate;

    //this setter remains for parsing old activity which did not always have the "eventDay" field
    @Deprecated
    public void setEventDate(Long eventDate) {
        this.eventDay = DateUtil.toLocalDate(eventDate);
    }

    public boolean matches(CertificationStatusEvent other) {
        boolean result = false;

        if (this.getId() != null && other.getId() != null
                && this.getId().longValue() == other.getId().longValue()) {
            result = true;
        }
        return result;
    }
}
