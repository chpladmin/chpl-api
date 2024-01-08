package gov.healthit.chpl.questionableactivity.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import gov.healthit.chpl.entity.EntityAudit;
import gov.healthit.chpl.questionableactivity.domain.QuestionableActivityTrigger;
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
@Table(name = "questionable_activity_trigger")
public class QuestionableActivityTriggerEntity extends EntityAudit {
    private static final long serialVersionUID = -3351380544418191156L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "level")
    private String level;

    public QuestionableActivityTrigger toDomain() {
        return QuestionableActivityTrigger.builder()
                .id(this.getId())
                .name(this.getName())
                .level(this.getLevel())
                .build();
    }
}

