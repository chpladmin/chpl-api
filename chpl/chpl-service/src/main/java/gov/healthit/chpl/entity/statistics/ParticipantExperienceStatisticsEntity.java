package gov.healthit.chpl.entity.statistics;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

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
@Table(name = "participant_experience_statistics")
public class ParticipantExperienceStatisticsEntity extends EntityAudit {
    private static final long serialVersionUID = 1094674270161664550L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id", nullable = false)
    private Long id;

    @Basic(optional = false)
    @Column(name = "experience_type_id", nullable = false)
    private Long experienceTypeId;

    @Basic(optional = false)
    @Column(name = "participant_count", nullable = false)
    private Long participantCount;

    @Basic(optional = false)
    @Column(name = "experience_months", nullable = false)
    private Integer experienceMonths;

    public ParticipantExperienceStatisticsEntity(final Long id) {
        this.id = id;
    }

    @Transient
    public Class<?> getClassType() {
        return ParticipantExperienceStatisticsEntity.class;
    }

}
