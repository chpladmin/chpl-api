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
@Table(name = "participant_education_statistics")
public class ParticipantEducationStatisticsEntity extends EntityAudit {
    private static final long serialVersionUID = -4258273713908999510L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id", nullable = false)
    private Long id;

    @Basic(optional = false)
    @Column(name = "education_count", nullable = false)
    private Long educationCount;

    @Basic(optional = false)
    @Column(name = "education_type_id", nullable = false)
    private Long educationTypeId;

    public ParticipantEducationStatisticsEntity(final Long id) {
        this.id = id;
    }

    @Transient
    public Class<?> getClassType() {
        return ParticipantEducationStatisticsEntity.class;
    }

}
