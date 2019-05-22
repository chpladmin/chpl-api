package gov.healthit.chpl.entity.surveillance.report;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Immutable;

@Entity
@Immutable
@Table(name = "quarter")
public class QuarterEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "quarter_begin_month")
    private Integer quarterBeginMonth;

    @Column(name = "quarter_begin_day")
    private Integer quarterBeginDay;

    @Column(name = "quarter_end_month")
    private Integer quarterEndMonth;

    @Column(name = "quarter_end_day")
    private Integer quarterEndDay;

    @Column(name = "deleted")
    private Boolean deleted;

    @Column(name = "last_modified_user")
    private Long lastModifiedUser;

    @Column(name = "creation_date", insertable = false, updatable = false)
    private Date creationDate;

    @Column(name = "last_modified_date", insertable = false, updatable = false)
    private Date lastModifiedDate;

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public Integer getQuarterBeginMonth() {
        return quarterBeginMonth;
    }

    public void setQuarterBeginMonth(final Integer quarterBeginMonth) {
        this.quarterBeginMonth = quarterBeginMonth;
    }

    public Integer getQuarterBeginDay() {
        return quarterBeginDay;
    }

    public void setQuarterBeginDay(final Integer quarterBeginDay) {
        this.quarterBeginDay = quarterBeginDay;
    }

    public Integer getQuarterEndMonth() {
        return quarterEndMonth;
    }

    public void setQuarterEndMonth(final Integer quarterEndMonth) {
        this.quarterEndMonth = quarterEndMonth;
    }

    public Integer getQuarterEndDay() {
        return quarterEndDay;
    }

    public void setQuarterEndDay(final Integer quarterEndDay) {
        this.quarterEndDay = quarterEndDay;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(final Boolean deleted) {
        this.deleted = deleted;
    }

    public Long getLastModifiedUser() {
        return lastModifiedUser;
    }

    public void setLastModifiedUser(final Long lastModifiedUser) {
        this.lastModifiedUser = lastModifiedUser;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(final Date creationDate) {
        this.creationDate = creationDate;
    }

    public Date getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(final Date lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }
}