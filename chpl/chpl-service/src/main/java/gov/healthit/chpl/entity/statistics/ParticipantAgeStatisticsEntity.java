package gov.healthit.chpl.entity.statistics;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

import gov.healthit.chpl.entity.EntityAudit;
import gov.healthit.chpl.entity.lastmodifieduserstrategy.CurrentUserThenSystemUserStrategy;
import gov.healthit.chpl.entity.lastmodifieduserstrategy.LastModifiedUserStrategy;
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
@Table(name = "participant_age_statistics")
public class ParticipantAgeStatisticsEntity extends EntityAudit {
    private static final long serialVersionUID = -3608777880397004236L;

    @Override
    public LastModifiedUserStrategy getLastModifiedUserStrategy() {
        return new CurrentUserThenSystemUserStrategy();
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id", nullable = false)
    private Long id;

    @Basic(optional = false)
    @Column(name = "age_count", nullable = false)
    private Long ageCount;

    @Basic(optional = false)
    @Column(name = "test_participant_age_id", nullable = false)
    private Long testParticipantAgeId;

    public ParticipantAgeStatisticsEntity(final Long id) {
        this.id = id;
    }

    @Transient
    public Class<?> getClassType() {
        return ParticipantAgeStatisticsEntity.class;
    }

}
