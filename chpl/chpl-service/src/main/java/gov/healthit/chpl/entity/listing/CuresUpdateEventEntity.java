package gov.healthit.chpl.entity.listing;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import gov.healthit.chpl.entity.EntityAudit;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@ToString
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "cures_update_event")
public class CuresUpdateEventEntity extends EntityAudit {
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
    private Date eventDate;

}
