package gov.healthit.chpl.domain;

import java.io.Serializable;
import java.time.LocalDate;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import gov.healthit.chpl.api.deprecatedUsage.DeprecatedResponseField;
import gov.healthit.chpl.util.DateUtil;
import gov.healthit.chpl.util.LocalDateAdapter;
import gov.healthit.chpl.util.LocalDateDeserializer;
import gov.healthit.chpl.util.LocalDateSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@XmlType(namespace = "http://chpl.healthit.gov/listings")
@XmlAccessorType(XmlAccessType.FIELD)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CertificationStatusEvent implements Serializable {
    private static final long serialVersionUID = -2498656549844148886L;

    // This is needed because JAXB requires a default constructor and the Lombok @NoArgsConstructor
    // isn't available when JAXB runs during Maven build
    public CertificationStatusEvent() {}


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

    /**
     * Internal ID
     */
    @Schema(description = "Internal ID")
    @XmlElement(required = true)
    private Long id;

    /**
     * The date on which a change of certification status occurred.
     */
    @Deprecated
    @DeprecatedResponseField(removalDate = "2024-04-01", message = "This field is deprecated and will be removed. Please use eventDay.")
    @Schema(description = "The date on which a change of certification status occurred.")
    @XmlTransient
    private Long eventDate;

    /**
     * The day on which a change of certification status occurred.
     */
    @Schema(description = "The day on which a change of certification status occurred.")
    @XmlElement(required = true)
    @XmlJavaTypeAdapter(value = LocalDateAdapter.class)
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate eventDay;

    /**
     * The certification status for the listing on the eventDate.
     */
    @Schema(description = "The certification status for the listing on the eventDate.")
    @XmlElement(required = true)
    private CertificationStatus status;

    /**
     * The user-provided reason that a change of certification status occurred.
     */
    @Schema(description = "The user-provided reason that a change of certification status occurred.")
    @XmlTransient
    private String reason;

    @XmlTransient
    private Long lastModifiedUser;

    @XmlTransient
    private Long lastModifiedDate;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Deprecated
    public Long getEventDate() {
        return eventDate;
    }

    @Deprecated
    public void setEventDate(Long eventDate) {
        this.eventDate = eventDate;
        this.eventDay = DateUtil.toLocalDate(eventDate);
    }

    public LocalDate getEventDay() {
        return eventDay;
    }

    public void setEventDay(LocalDate eventDay) {
        this.eventDay = eventDay;
        this.eventDate = DateUtil.toEpochMillis(eventDay);
    }

    public Long getLastModifiedUser() {
        return lastModifiedUser;
    }

    public void setLastModifiedUser(Long lastModifiedUser) {
        this.lastModifiedUser = lastModifiedUser;
    }

    public Long getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(Long lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public CertificationStatus getStatus() {
        return status;
    }

    public void setStatus(CertificationStatus status) {
        this.status = status;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
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
