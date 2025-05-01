package gov.healthit.chpl.changerequest.entity;

import gov.healthit.chpl.entity.EntityAudit;
import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
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
@Table(name = "change_request_service_base_url_list")
public class ChangeRequestServiceBaseUrlListEntity extends EntityAudit {
    private static final long serialVersionUID = 4870820836190536546L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id", nullable = false)
    private Long id;

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "change_request_id", nullable = false, insertable = true,
            updatable = false)
    private ChangeRequestEntity changeRequest;

    @Column(name = "service_base_url_list", nullable = false)
    private String serviceBaseUrlList;

    @Column(name = "listing_id", nullable = false)
    private Long listingId;

}
