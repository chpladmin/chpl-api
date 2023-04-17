package gov.healthit.chpl.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.NamedNativeQueries;
import javax.persistence.NamedNativeQuery;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.domain.activity.ActivityConcept;
import gov.healthit.chpl.dto.ActivityDTO;
import gov.healthit.chpl.entity.auth.UserEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "activity")
@NamedNativeQueries({
    @NamedNativeQuery(
            name = "getPageOfActivity",
            query = "SELECT * FROM ( "
                    + "SELECT row_number() OVER(ORDER BY a.activity_date DESC) as \"record_num\", * "
                    + "FROM " + BaseDAOImpl.SCHEMA_NAME + ".activity a "
                    + "JOIN " + BaseDAOImpl.SCHEMA_NAME + ".activity_concept ac "
                            + " ON a.activity_object_concept_id = ac.activity_concept_id "
                            + "AND ac.concept = :conceptName "
                    + "LEFT OUTER JOIN " + BaseDAOImpl.SCHEMA_NAME + ".user u "
                            + "ON a.last_modified_user = u.user_id "
                    + "WHERE a.deleted = false "
                    + "AND (a.activity_date >= :startDate) "
                    + "AND (a.activity_date <= :endDate) "
                    + "ORDER BY a.activity_date DESC "
                    + ") as \"results\" "
                + "WHERE record_num >= :firstRecord and record_num < :lastRecord",
                resultClass = ActivityEntity.class
            ),
    @NamedNativeQuery(
            name = "getPageOfActivityByObjectIds",
            query = "SELECT * FROM ( "
                    + "SELECT row_number() OVER(ORDER BY a.activity_date DESC) as \"record_num\", * "
                    + "FROM " + BaseDAOImpl.SCHEMA_NAME + ".activity a "
                    + "JOIN " + BaseDAOImpl.SCHEMA_NAME + ".activity_concept ac "
                            + " ON a.activity_object_concept_id = ac.activity_concept_id "
                            + "AND ac.concept = :conceptName "
                    + "LEFT OUTER JOIN " + BaseDAOImpl.SCHEMA_NAME + ".user u "
                            + "ON a.last_modified_user = u.user_id "
                    + "WHERE a.deleted = false "
                    + "AND (a.activity_date >= :startDate) "
                    + "AND (a.activity_date <= :endDate) "
                    + "AND a.activity_object_id IN (:objectIds) "
                    + "ORDER BY a.activity_date DESC "
                    + ") as \"results\" "
                + "WHERE record_num >= :firstRecord and record_num < :lastRecord",
                resultClass = ActivityEntity.class
            )
})
@Getter
@Setter
@ToString
@NoArgsConstructor
public class ActivityEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "activity_id", nullable = false)
    private Long id;

    @Column(name = "description", nullable = true)
    private String description;

    @Column(name = "original_data", nullable = true)
    private String originalData;

    @Column(name = "new_data", nullable = true)
    private String newData;

    @Column(name = "activity_date", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date activityDate;

    @Column(name = "activity_object_id", nullable = false)
    private Long activityObjectId;

    @Column(name = "activity_object_concept_id", nullable = false)
    private Long activityObjectConceptId;

    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "activity_object_concept_id", unique = true, nullable = true, insertable = false, updatable = false)
    private ActivityConceptEntity concept;

    @Column(name = "reason")
    private String reason;

    @Column(name = "creation_date", nullable = false, insertable = false, updatable = false)
    private Date creationDate;

    @Column(name = "last_modified_date", nullable = false, insertable = false, updatable = false)
    private Date lastModifiedDate;

    @Column(name = "deleted", nullable = false, insertable = false)
    private Boolean deleted;

    @Column(name = "last_modified_user", nullable = false)
    private Long lastModifiedUser;

    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @Fetch(FetchMode.JOIN)
    @JoinColumn(name = "last_modified_user", unique = true, nullable = true, insertable = false, updatable = false)
    private UserEntity user;

    public ActivityDTO toDomain() {
        return ActivityDTO.builder()
                .id(this.getId())
                .description(this.getDescription())
                .originalData(this.getOriginalData())
                .newData(this.getNewData())
                .activityDate(this.getActivityDate())
                .activityObjectId(this.getActivityObjectId())
                .concept(ActivityConcept.valueOf(this.getConcept().getConcept()))
                .reason(this.getReason())
                .creationDate(this.getCreationDate())
                .lastModifiedDate(this.getLastModifiedDate())
                .lastModifiedUser(this.getLastModifiedUser())
                .deleted(this.getDeleted())
                .build();
    }
}
