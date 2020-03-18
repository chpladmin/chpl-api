package gov.healthit.chpl.dto;

import java.io.Serializable;
import java.util.Date;

import gov.healthit.chpl.entity.listing.CertificationStatusEventEntity;
import gov.healthit.chpl.entity.listing.CuresUpdateEventEntity;
import gov.healthit.chpl.util.Util;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class CuresUpdateEventDTO implements Serializable {
    private static final long serialVersionUID = 1171613630377844762L;
    private Long id;
    private Long certifiedProductId;
    private Boolean curesUpdate;
    @Setter(AccessLevel.NONE)
    @Getter(AccessLevel.NONE)
    private Date eventDate;
    @Setter(AccessLevel.NONE)
    @Getter(AccessLevel.NONE)
    private Date creationDate;
    private Boolean deleted;
    @Setter(AccessLevel.NONE)
    @Getter(AccessLevel.NONE)
    private Date lastModifiedDate;
    private Long lastModifiedUser;

    public CuresUpdateEventDTO(CuresUpdateEventEntity entity) {
        this.id = entity.getId();
        this.certifiedProductId = entity.getCertifiedProductId();
        this.curesUpdate = entity.getCuresUpdate();
        this.eventDate = entity.getEventDate();
        this.creationDate = entity.getCreationDate();
        this.deleted = entity.getDeleted();
        this.lastModifiedDate = entity.getLastModifiedDate();
        this.lastModifiedUser = entity.getLastModifiedUser();
    }

    public Date getEventDate() {
        return Util.getNewDate(eventDate);
    }

    public void setEventDate(final Date eventDate) {
        this.eventDate = Util.getNewDate(eventDate);
    }

    public Date getCreationDate() {
        return Util.getNewDate(creationDate);
    }

    public void setCreationDate(final Date creationDate) {
        this.creationDate = Util.getNewDate(creationDate);
    }

    public Date getLastModifiedDate() {
        return Util.getNewDate(lastModifiedDate);
    }
}
