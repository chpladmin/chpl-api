package gov.healthit.chpl.dto;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import gov.healthit.chpl.entity.developer.DeveloperStatusEventEntity;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Developer Status Event DTO.
 * 
 * @author alarned
 *
 */
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
}
