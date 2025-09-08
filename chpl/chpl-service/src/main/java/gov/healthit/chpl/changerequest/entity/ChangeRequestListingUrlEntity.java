package gov.healthit.chpl.changerequest.entity;

import java.time.LocalDate;

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
@Table(name = "change_request_listing_url")
public class ChangeRequestListingUrlEntity extends EntityAudit {
    private static final long serialVersionUID = 4870820836190536546L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id", nullable = false)
    private Long id;

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "change_request_id", nullable = false, insertable = true, updatable = false)
    private ChangeRequestEntity changeRequest;

    @Column(name = "url", nullable = false)
    private String url;

    @Column(name = "check_date", nullable = true)
    private LocalDate checkDate;

    @Column(name = "listing_id", nullable = false)
    private Long listingId;

}
