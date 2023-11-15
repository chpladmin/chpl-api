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
@Table(name = "sed_participants_statistics_count")
public class SedParticipantStatisticsCountEntity extends EntityAudit {
    private static final long serialVersionUID = -1724804164709332747L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id", nullable = false)
    private Long id;

    @Basic(optional = false)
    @Column(name = "sed_count", nullable = false)
    private Long sedCount;

    @Basic(optional = false)
    @Column(name = "participant_count", nullable = false)
    private Long participantCount;

    public SedParticipantStatisticsCountEntity(final Long id) {
        this.id = id;
    }

    @Transient
    public Class<?> getClassType() {
        return SedParticipantStatisticsCountEntity.class;
    }

}
