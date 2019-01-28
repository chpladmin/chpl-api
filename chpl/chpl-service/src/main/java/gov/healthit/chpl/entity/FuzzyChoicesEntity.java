package gov.healthit.chpl.entity;

import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Type;

import gov.healthit.chpl.util.Util;

@Entity
@Table(name = "fuzzy_choices")
public class FuzzyChoicesEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "fuzzy_choices_id", nullable = false)
    private Long id;

    @Column(name = "fuzzy_type")
    @Type(type = "gov.healthit.chpl.entity.PostgresFuzzyType", parameters = {
            @org.hibernate.annotations.Parameter(name = "enumClassName",
                                                 value = "gov.healthit.chpl.entity.FuzzyType")
        })
    private FuzzyType fuzzyType;

    @Basic(optional = false)
    @Column(name = "choices", nullable = false)
    private String choices;

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
    @Column(nullable = false, name = "deleted")
    private Boolean deleted;

    public Long getId() {
        return id;
    }

    public void setId(final Long long1) {
        this.id = long1;
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

    public FuzzyType getFuzzyType() {
        return fuzzyType;
    }

    public void setFuzzyType(final FuzzyType fuzzyType) {
        this.fuzzyType = fuzzyType;
    }

    public String getChoices() {
        return choices;
    }

    public void setChoices(String choices) {
        this.choices = choices;
    }
}
