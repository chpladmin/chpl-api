package gov.healthit.chpl.domain;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Certification Status Event domain object.
 * @author alarned
 *
 */
@XmlType(namespace = "http://chpl.healthit.gov/listings")
@XmlAccessorType(XmlAccessType.FIELD)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CertificationStatusEvent implements Comparable<CertificationStatusEvent>, Serializable {
    private static final long serialVersionUID = -2498656549844148886L;

    /**
     * Internal ID
     */
    @XmlElement(required = true)
    private Long id;

    /**
     * The date on which a change of certification status occurred.
     */
    @XmlElement(required = true)
    private Long eventDate;

    /**
     * The certification status for the listing on the eventDate.
     */
    @XmlElement(required = true)
    private CertificationStatus status;

    /**
     * The user-provided reason that a change of certification status occurred.
     */
    @XmlElement(nillable = true, required = false)
    private String reason;

    @XmlTransient
    private Long lastModifiedUser;

    @XmlTransient
    private Long lastModifiedDate;

    /**
     * Constructor.
     */
    public CertificationStatusEvent() {
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public Long getEventDate() {
        return eventDate;
    }

    public void setEventDate(final Long eventDate) {
        this.eventDate = eventDate;
    }

    public Long getLastModifiedUser() {
        return lastModifiedUser;
    }

    public void setLastModifiedUser(final Long lastModifiedUser) {
        this.lastModifiedUser = lastModifiedUser;
    }

    public Long getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(final Long lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public CertificationStatus getStatus() {
        return status;
    }

    public void setStatus(final CertificationStatus status) {
        this.status = status;
    }

    /**
     * Check to see if this CSE matches another one.
     * @param other CSE to check against
     * @return true if the IDs match
     */
    public boolean matches(final CertificationStatusEvent other) {
        boolean result = false;

        if (this.getId() != null && other.getId() != null
                && this.getId().longValue() == other.getId().longValue()) {
            result = true;
        }
        return result;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(final String reason) {
        this.reason = reason;
    }

    /**
     * Returns a negative number if this status event is "before" the other status event.
     * Returns a positive number if this status event is "after" the other status event.
     * Returns 0 if either status event has a null date or if the dates are equal
     */
    @Override
    public int compareTo(final CertificationStatusEvent other) {
        if (this.getEventDate() == null || other.getEventDate() == null
                || this.getEventDate().equals(other.getEventDate())) {
            return 0;
        }
        if (this.getEventDate() < other.getEventDate()) {
            return -1;
        }
        if (this.getEventDate() > other.getEventDate()) {
            return 1;
        }
        return 0;
    }
}
