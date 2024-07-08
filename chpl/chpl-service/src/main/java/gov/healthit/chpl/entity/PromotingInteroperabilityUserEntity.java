package gov.healthit.chpl.entity;

import java.time.LocalDate;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import gov.healthit.chpl.domain.PromotingInteroperabilityUser;
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
@Table(name = "promoting_interoperability_user")
public class PromotingInteroperabilityUserEntity extends EntityAudit {
    private static final long serialVersionUID = -1463562876433665214L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id", nullable = false)
    private Long id;

    @Basic(optional = false)
    @Column(name = "certified_product_id")
    private Long listingId;

    @Basic(optional = false)
    @Column(name = "user_count")
    private Long userCount;

    @Basic(optional = false)
    @Column(name = "user_count_date")
    private LocalDate userCountDate;

    public PromotingInteroperabilityUser toDomain() {
        return PromotingInteroperabilityUser.builder()
                .id(this.getId())
                .userCount(this.getUserCount())
                .userCountDate(this.getUserCountDate())
        .build();
    }
}
