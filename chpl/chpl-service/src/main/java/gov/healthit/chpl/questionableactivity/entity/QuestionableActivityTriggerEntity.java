package gov.healthit.chpl.questionableactivity.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import gov.healthit.chpl.questionableactivity.domain.QuestionableActivityTrigger;
import lombok.Data;

@Entity
@Data
@Table(name = "questionable_activity_trigger")
public class QuestionableActivityTriggerEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "level")
    private String level;

    @Column(name = "deleted")
    private Boolean deleted;

    @Column(name = "last_modified_user")
    private Long lastModifiedUser;

    @Column(name = "creation_date", insertable = false, updatable = false)
    private Date creationDate;

    @Column(name = "last_modified_date", insertable = false, updatable = false)
    private Date lastModifiedDate;

    public QuestionableActivityTrigger toDomain() {
        return QuestionableActivityTrigger.builder()
                .id(this.getId())
                .name(this.getName())
                .level(this.getLevel())
                .build();
    }
}

