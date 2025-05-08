package gov.healthit.chpl.changerequest.entity;

import gov.healthit.chpl.changerequest.domain.ChangeRequestListingUrlType;
import gov.healthit.chpl.entity.EntityAudit;
import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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
@Table(name = "change_request_listing_url_type")
public class ChangeRequestListingUrlTypeEntity extends EntityAudit {
    private static final long serialVersionUID = 1582754047636676389L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id", nullable = false)
    private Long id;

    @Basic(optional = false)
    @Column(name = "name", nullable = false)
    private String name;

    public ChangeRequestListingUrlType toDomain() {
        return ChangeRequestListingUrlType.builder()
                .id(id)
                .name(name)
                .build();
    }
}
