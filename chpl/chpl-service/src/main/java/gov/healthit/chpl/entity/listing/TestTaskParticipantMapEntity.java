package gov.healthit.chpl.entity.listing;

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

import org.hibernate.annotations.Where;

import gov.healthit.chpl.entity.EntityAudit;
import gov.healthit.chpl.entity.TestParticipantEntity;
import gov.healthit.chpl.entity.TestTaskEntity;
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
@Table(name = "test_task_participant_map")
public class TestTaskParticipantMapEntity extends EntityAudit {
    private static final long serialVersionUID = -1114257207589782550L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Long id;

    @Basic(optional = false)
    @Column(name = "test_task_id", nullable = false)
    private Long testTaskId;

    @Basic(optional = true)
    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "test_task_id", unique = true, nullable = true, insertable = false, updatable = false)
    @Where(clause = "deleted <> 'true'")
    private TestTaskEntity testTask;

    @Basic(optional = false)
    @Column(name = "test_participant_id", nullable = false)
    private Long testParticipantId;

    @Basic(optional = true)
    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "test_participant_id", unique = true, nullable = true, insertable = false, updatable = false)
    @Where(clause = "deleted <> 'true'")
    private TestParticipantEntity testParticipant;
}
