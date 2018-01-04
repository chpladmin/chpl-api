package gov.healthit.chpl.domain;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

@XmlType(namespace = "http://chpl.healthit.gov/listings")
@XmlAccessorType(XmlAccessType.FIELD)
public class CertificationStatusEvent implements Serializable {
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
    @XmlElement(required = true)
    private String reason;
    
    @XmlTransient
    private Long lastModifiedUser;

    @XmlTransient
    private Long lastModifiedDate;

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

    public void setStatus(CertificationStatus status) {
        this.status = status;
    }
    
    public boolean matches(CertificationStatusEvent other) {
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

    public void setReason(String reason) {
        this.reason = reason;
    }
}
