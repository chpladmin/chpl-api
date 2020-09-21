package gov.healthit.chpl.domain;

import java.io.Serializable;
import java.util.Date;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import gov.healthit.chpl.dto.DeveloperStatusEventDTO;
import gov.healthit.chpl.util.Util;
import lombok.AllArgsConstructor;
import lombok.Builder;

@XmlType(namespace = "http://chpl.healthit.gov/listings")
@XmlAccessorType(XmlAccessType.FIELD)
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

    public DeveloperStatusEvent(DeveloperStatusEventDTO dto) {
        this.id = dto.getId();
        this.developerId = dto.getDeveloperId();
        this.status = new DeveloperStatus(dto.getStatus());
        this.statusDate = dto.getStatusDate();
        this.reason = dto.getReason();
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
