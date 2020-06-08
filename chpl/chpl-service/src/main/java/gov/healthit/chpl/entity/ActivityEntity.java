package gov.healthit.chpl.entity;

import java.util.Date;

import javax.persistence.Basic;
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
import gov.healthit.chpl.entity.auth.UserEntity;
import gov.healthit.chpl.util.Util;

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
            name = "getPageOfActivityByObjectId",
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
            ),
    @NamedNativeQuery(
            name = "getPublicAnnouncementActivityByDate",
            query = "SELECT * "
                    + "FROM " + BaseDAOImpl.SCHEMA_NAME + ".activity a "
                    + "JOIN " + BaseDAOImpl.SCHEMA_NAME + ".activity_concept ac "
                    + "ON a.activity_object_concept_id = ac.activity_concept_id "
                    + "AND ac.concept = :conceptName "
                    + "LEFT OUTER JOIN " + BaseDAOImpl.SCHEMA_NAME + ".user u "
                    + "ON a.last_modified_user = u.user_id "
                    + "WHERE a.original_data IS NOT NULL AND cast(a.original_data as json)->>'isPublic'= 'true' "
                    + "AND a.new_data IS NOT NULL AND cast(a.new_data as json)->>'isPublic' = 'true' "
                    + "AND (a.activity_date >= :startDate) "
                    + "AND (a.activity_date <= :endDate)",
                    resultClass = ActivityEntity.class
            ),
    @NamedNativeQuery(
            name = "getPublicAnnouncementActivityByIdAndDate",
            query = "SELECT * "
                    + "FROM " + BaseDAOImpl.SCHEMA_NAME + ".activity a "
                    + "JOIN " + BaseDAOImpl.SCHEMA_NAME + ".activity_concept ac "
                    + "ON a.activity_object_concept_id = ac.activity_concept_id "
                    + "AND ac.concept = :conceptName "
                    + "LEFT OUTER JOIN " + BaseDAOImpl.SCHEMA_NAME + ".user u "
                    + "ON a.last_modified_user = u.user_id "
                    + "WHERE a.activity_object_id = :announcementId "
                    + "AND a.original_data IS NOT NULL AND cast(a.original_data as json)->>'isPublic'= 'true' "
                    + "AND a.new_data IS NOT NULL AND cast(a.new_data as json)->>'isPublic' = 'true' "
                    + "AND (a.activity_date >= :startDate) "
                    + "AND (a.activity_date <= :endDate)",
                    resultClass = ActivityEntity.class
            ),
    @NamedNativeQuery(
            name = "getPendingListingActivityByAcbIdsAndDate",
            query = "SELECT * "
                    + "FROM " + BaseDAOImpl.SCHEMA_NAME + ".activity a "
                    + "JOIN " + BaseDAOImpl.SCHEMA_NAME + ".activity_concept ac "
                    + "ON a.activity_object_concept_id = ac.activity_concept_id "
                    + "AND ac.concept = :conceptName "
                    + "LEFT OUTER JOIN " + BaseDAOImpl.SCHEMA_NAME + ".user u "
                    + "ON a.last_modified_user = u.user_id "
                    + "WHERE ( "
                    + "cast(a.original_data as json)->>'certificationBodyId' IN (:acbIds) "
                    + "OR cast(a.new_data as json)->>'certificationBodyId' IN (:acbIds) "
                    + ")"
                    + "AND (a.activity_date >= :startDate) "
                    + "AND (a.activity_date <= :endDate)",
                    resultClass = ActivityEntity.class
            )
})
public class ActivityEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "activity_id", nullable = false)
    private Long id;

    @Basic(optional = true)
    @Column(name = "description", nullable = true)
    private String description;

    @Basic(optional = true)
    @Column(name = "original_data", nullable = true)
    private String originalData;

    @Basic(optional = true)
    @Column(name = "new_data", nullable = true)
    private String newData;

    @Basic(optional = false)
    @Column(name = "activity_date", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date activityDate;

    @Basic(optional = false)
    @Column(name = "activity_object_id", nullable = false)
    private Long activityObjectId;

    @Basic(optional = false)
    @Column(name = "activity_object_concept_id", nullable = false)
    private Long activityObjectConceptId;

    @Basic(optional = false)
    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "activity_object_concept_id", unique = true, nullable = true, insertable = false, updatable = false)
    private ActivityConceptEntity concept;

    @Basic(optional = false)
    @Column(name = "creation_date", nullable = false)
    private Date creationDate;

    @Basic(optional = false)
    @Column(name = "last_modified_date", nullable = false)
    private Date lastModifiedDate;

    @Basic(optional = false)
    @Column(name = "last_modified_user", nullable = false)
    private Long lastModifiedUser;

    @Basic(optional = false)
    @Column(name = "deleted", nullable = false)
    private Boolean deleted;

    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @Fetch(FetchMode.JOIN)
    @JoinColumn(name = "last_modified_user", unique = true, nullable = true, insertable = false, updatable = false)
    private UserEntity user;

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public Date getActivityDate() {
        return Util.getNewDate(activityDate);
    }

    public void setActivityDate(final Date activityDate) {
        this.activityDate = Util.getNewDate(activityDate);
    }

    public Long getActivityObjectId() {
        return activityObjectId;
    }

    public void setActivityObjectId(final Long activityObjectId) {
        this.activityObjectId = activityObjectId;
    }

    public Date getCreationDate() {
        return Util.getNewDate(creationDate);
    }

    public void setCreationDate(final Date creationDate) {
        this.creationDate = Util.getNewDate(creationDate);
    }

    public Date getLastModifiedDate() {
        return Util.getNewDate(lastModifiedDate);
    }

    public void setLastModifiedDate(final Date lastModifiedDate) {
        this.lastModifiedDate = Util.getNewDate(lastModifiedDate);
    }

    public Long getLastModifiedUser() {
        return lastModifiedUser;
    }

    public void setLastModifiedUser(final Long lastModifiedUser) {
        this.lastModifiedUser = lastModifiedUser;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(final Boolean deleted) {
        this.deleted = deleted;
    }

    public String getOriginalData() {
        return originalData;
    }

    public void setOriginalData(final String originalData) {
        this.originalData = originalData;
    }

    public String getNewData() {
        return newData;
    }

    public void setNewData(final String newData) {
        this.newData = newData;
    }

    public UserEntity getUser() {
        return user;
    }

    public void setUser(final UserEntity user) {
        this.user = user;
    }

    public Long getActivityObjectConceptId() {
        return activityObjectConceptId;
    }

    public void setActivityObjectConceptId(Long activityObjectConceptId) {
        this.activityObjectConceptId = activityObjectConceptId;
    }

    public ActivityConceptEntity getConcept() {
        return concept;
    }

    public void setConcept(ActivityConceptEntity concept) {
        this.concept = concept;
    }

}
