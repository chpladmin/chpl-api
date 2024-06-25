package gov.healthit.chpl.entity;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import gov.healthit.chpl.domain.TestParticipant.TestParticipantAge;
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
@Table(name = "test_participant_age")
public class AgeRangeEntity extends EntityAudit {
    private static final long serialVersionUID = 505964592485391589L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "test_participant_age_id", nullable = false)
    private Long id;

    @Column(name = "age")
    private String age;
    public TestParticipantAge toDomain() {
        return TestParticipantAge.builder()
                .id(id)
                .name(age)
                .build();
    }
}
