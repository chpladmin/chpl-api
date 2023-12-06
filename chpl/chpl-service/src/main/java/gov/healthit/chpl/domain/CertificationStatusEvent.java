package gov.healthit.chpl.domain;

import java.io.Serializable;
import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import gov.healthit.chpl.api.deprecatedUsage.DeprecatedResponseField;
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


    // This exists so that when the builder is used, we can call the setter for eventDate.  The
    // setters keeps the attributes synchronized.  This can removed when eventDay is removed from the system.
    // By not having eventDay in the list of params, it cannot be set via a Builder.
    @Builder
    public CertificationStatusEvent(Long id, Long eventDate, CertificationStatus status,
            String reason, Long lastModifiedUser, Long lastModifiedDate) {
        super();
        this.id = id;
        setEventDate(eventDate);
        this.status = status;
        this.reason = reason;
        this.lastModifiedUser = lastModifiedUser;
        this.lastModifiedDate = lastModifiedDate;
    }

    @Schema(description = "Internal ID")
    private Long id;

    @Deprecated
    @DeprecatedResponseField(removalDate = "2024-04-01", message = "This field is deprecated and will be removed. Please use eventDay.")
    @Schema(description = "The date on which a change of certification status occurred.")
    private Long eventDate;

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

    @Deprecated
    public void setEventDate(Long eventDate) {
        this.eventDate = eventDate;
        this.eventDay = DateUtil.toLocalDate(eventDate);
    }

    public void setEventDay(LocalDate eventDay) {
        this.eventDay = eventDay;
        this.eventDate = DateUtil.toEpochMillis(eventDay);
    }

    /**
     * Check to see if this CSE matches another one.
     *
     * @param other
     *            CSE to check against
     * @return true if the IDs match
     */
    public boolean matches(CertificationStatusEvent other) {
        boolean result = false;

        if (this.getId() != null && other.getId() != null
                && this.getId().longValue() == other.getId().longValue()) {
            result = true;
        }
        return result;
    }
}
