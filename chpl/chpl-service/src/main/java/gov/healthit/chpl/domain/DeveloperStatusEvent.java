package gov.healthit.chpl.domain;

import java.io.Serializable;
import java.util.Date;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import gov.healthit.chpl.util.Util;
import lombok.AllArgsConstructor;
import lombok.Builder;

@XmlType(namespace = "http://chpl.healthit.gov/listings")
@XmlAccessorType(XmlAccessType.FIELD)
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
@AllArgsConstructor
public class DeveloperStatusEvent implements Serializable {
    private static final long serialVersionUID = -7303257499336378800L;

    /**
     * Developer status event internal ID
     */
    @XmlElement(required = false, nillable = true)
    private Long id;

    /**
     * Developer internal ID
     */
    @XmlElement(required = true)
    private Long developerId;

    /**
     * The status the developer changed TO with this status event.
     */
    @XmlElement(required = true)
    private DeveloperStatus status;

    /**
     * Date this status event occurred.
     */
    @XmlElement(required = true)
    private Date statusDate;

    /**
     * The reason for this status change.
     * It is required of the status changed to 'Under Certification Ban by ONC'
     */
    @XmlElement(required = false)
    private String reason;

    public DeveloperStatusEvent() {
    }

    public boolean matches(DeveloperStatusEvent anotherStatusEvent) {
        boolean result = false;
        if (this.getId() != null && anotherStatusEvent.getId() != null
                && this.getId().longValue() == anotherStatusEvent.getId().longValue()) {
            return true;
        }
        return result;
    }

    // Not all attributes have been included. The attributes being used were selected so the DeveloperManager could
    // determine equality when updating a Developer
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
        if (developerId == null) {
            if (other.developerId != null) {
                return false;
            }
        } else if (!developerId.equals(other.developerId)) {
            return false;
        }
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }
        if (reason == null) {
            if (other.reason != null) {
                return false;
            }
        } else if (!reason.equals(other.reason)) {
            return false;
        }
        if (status == null) {
            if (other.status != null) {
                return false;
            }
        } else if (!status.equals(other.status)) {
            return false;
        }
        if (statusDate == null) {
            if (other.statusDate != null) {
                return false;
            }
        } else if (!statusDate.equals(other.statusDate)) {
            return false;
        }
        return true;
    }

    // Not all attributes have been included. The attributes being used were selected so the DeveloperManager could
    // determine equality when updating a Developer
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((developerId == null) ? 0 : developerId.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((reason == null) ? 0 : reason.hashCode());
        result = prime * result + ((status == null) ? 0 : status.hashCode());
        result = prime * result + ((statusDate == null) ? 0 : statusDate.hashCode());
        return result;
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public Long getDeveloperId() {
        return developerId;
    }

    public void setDeveloperId(final Long developerId) {
        this.developerId = developerId;
    }

    public DeveloperStatus getStatus() {
        return status;
    }

    public void setStatus(final DeveloperStatus status) {
        this.status = status;
    }

    public Date getStatusDate() {
        return Util.getNewDate(statusDate);
    }

    public void setStatusDate(final Date statusDate) {
        this.statusDate = Util.getNewDate(statusDate);
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
