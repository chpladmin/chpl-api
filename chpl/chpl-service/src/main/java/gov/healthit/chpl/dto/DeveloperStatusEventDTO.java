package gov.healthit.chpl.dto;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import gov.healthit.chpl.entity.developer.DeveloperStatusEventEntity;
import gov.healthit.chpl.util.Util;

/**
 * Developer Status Event DTO.
 * @author alarned
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DeveloperStatusEventDTO implements Serializable {
    private static final long serialVersionUID = -2492374479266782228L;

    private Long id;
    private Long developerId;
    private DeveloperStatusDTO status;
    private Date statusDate;
    private String reason;
    private Boolean deleted;

    /** Default constructor. */
    public DeveloperStatusEventDTO() {
    }

    /**
     * Constructed from entity.
     * @param entity the entity
     */
    public DeveloperStatusEventDTO(final DeveloperStatusEventEntity entity) {
        this();
        this.id = entity.getId();
        this.developerId = entity.getDeveloperId();
        this.status = new DeveloperStatusDTO(entity.getDeveloperStatus());
        this.statusDate = entity.getStatusDate();
        this.reason = entity.getReason();
        this.setDeleted(entity.getDeleted());
    }

    /**
     * Copy constructor.
     * @param dto the DeveloperStatusEventDTO to copy
     */
    public DeveloperStatusEventDTO(final DeveloperStatusEventDTO dto) {
        this.id = dto.getId();
        this.developerId = dto.getDeveloperId();
        this.status = dto.getStatus(); //Shallow copy
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

    public Date getStatusDate() {
        return Util.getNewDate(statusDate);
    }

    public void setStatusDate(final Date statusDate) {
        this.statusDate = Util.getNewDate(statusDate);
    }

    public DeveloperStatusDTO getStatus() {
        return status;
    }

    public void setStatus(final DeveloperStatusDTO status) {
        this.status = status;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(final String reason) {
        this.reason = reason;
    }

    /**
     * Return true iff this DTO matches a different on.
     * @param anotherStatusEvent the different one
     * @return true iff this matches
     */
    public boolean matches(final DeveloperStatusEventDTO anotherStatusEvent) {
        boolean result = false;
        if (this.getId() != null && anotherStatusEvent.getId() != null
                && this.getId().longValue() == anotherStatusEvent.getId().longValue()) {
            return true;
        }
        return result;
    }

    @Override
    public String toString() {
        return "Developer Status Event DTO: ["
                + "[Developer ID: " + this.developerId + "] "
                + "[Status Date: " + this.statusDate.toString() + "] "
                + "[Status: " + this.status.getStatusName() + "] "
                + "[Reason: " + this.reason + "]"
                + "]";
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        DeveloperStatusEventDTO dto = (DeveloperStatusEventDTO) obj;
        return Objects.equals(getId(), dto.getId());
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(final Boolean deleted) {
        this.deleted = deleted;
    }
}
