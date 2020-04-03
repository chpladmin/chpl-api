package gov.healthit.chpl.entity.listing;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import gov.healthit.chpl.util.Util;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "cures_update_event")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class CuresUpdateEventEntity implements Serializable {

    /** Serial Version UID. */
    private static final long serialVersionUID = 4174889617079658144L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "certified_product_id")
    private Long certifiedProductId;

    @Column(name = "cures_update")
    private Boolean curesUpdate;

    @Column(name = "event_date")
    @Setter(AccessLevel.NONE)
    @Getter(AccessLevel.NONE)
    private Date eventDate;

    @Column(name = "deleted")
    private Boolean deleted;

    @Column(name = "last_modified_user")
    private Long lastModifiedUser;

    @Column(name = "last_modified_date", insertable = false, updatable = false)
    @Setter(AccessLevel.NONE)
    @Getter(AccessLevel.NONE)
    private Date lastModifiedDate;

    @Column(name = "creation_date", insertable = false, updatable = false)
    @Setter(AccessLevel.NONE)
    @Getter(AccessLevel.NONE)
    private Date creationDate;

    public Date getEventDate() {
        return Util.getNewDate(eventDate);
    }

    public void setEventDate(final Date eventDate) {
        this.eventDate = Util.getNewDate(eventDate);
    }

    public Date getCreationDate() {
        return Util.getNewDate(creationDate);
    }


    public Date getLastModifiedDate() {
        return Util.getNewDate(lastModifiedDate);
    }
}
