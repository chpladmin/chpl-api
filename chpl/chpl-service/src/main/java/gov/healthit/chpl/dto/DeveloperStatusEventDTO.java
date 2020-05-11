package gov.healthit.chpl.dto;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import gov.healthit.chpl.entity.developer.DeveloperStatusEventEntity;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@NoArgsConstructor
public class DeveloperStatusEventDTO implements Serializable {
    private static final long serialVersionUID = -2492374479266782228L;

    private Long id;
    private Long developerId;
    private DeveloperStatusDTO status;
    private Date statusDate;
    private String reason;
    private Boolean deleted;

    public DeveloperStatusEventDTO(final DeveloperStatusEventEntity entity) {
        this();
        this.id = entity.getId();
        this.developerId = entity.getDeveloperId();
        this.status = new DeveloperStatusDTO(entity.getDeveloperStatus());
        this.statusDate = entity.getStatusDate();
        this.reason = entity.getReason();
        this.setDeleted(entity.getDeleted());
    }

    public DeveloperStatusEventDTO(final DeveloperStatusEventDTO dto) {
        this.id = dto.getId();
        this.developerId = dto.getDeveloperId();
        this.status = dto.getStatus(); // Shallow copy
        this.statusDate = dto.getStatusDate();
        this.reason = dto.getReason();
    }

    /**
     * Return true iff this DTO matches a different on.
     * 
     * @param anotherStatusEvent
     *            the different one
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
        DeveloperStatusEventDTO other = (DeveloperStatusEventDTO) obj;
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

}
