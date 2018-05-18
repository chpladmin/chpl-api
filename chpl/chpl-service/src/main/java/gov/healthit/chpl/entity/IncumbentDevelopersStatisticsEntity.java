package gov.healthit.chpl.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

/**
 * Entity object representing the incumbent_developers_statistics table.
 * @author alarned
 *
 */
@Entity
@Table(name = "incumbent_developers_statistics")
public class IncumbentDevelopersStatisticsEntity implements Serializable {
    private static final long serialVersionUID = 1313677047965534572L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id", nullable = false)
    private Long id;

    @Basic(optional = false)
    @Column(name = "new_2011_to_2014", nullable = false)
    private Long new2011To2014;

    @Basic(optional = false)
    @Column(name = "new_2011_to_2015", nullable = false)
    private Long new2011To2015;

    @Basic(optional = false)
    @Column(name = "new_2014_to_2015", nullable = false)
    private Long new2014To2015;

    @Basic(optional = false)
    @Column(name = "incumbent_2011_to_2014", nullable = false)
    private Long incumbent2011To2014;

    @Basic(optional = false)
    @Column(name = "incumbent_2011_to_2015", nullable = false)
    private Long incumbent2011To2015;

    @Basic(optional = false)
    @Column(name = "incumbent_2014_to_2015", nullable = false)
    private Long incumbent2014To2015;

    @Basic(optional = false)
    @Column(name = "creation_date", nullable = false)
    private Date creationDate;

    @Basic(optional = false)
    @Column(name = "deleted", nullable = false)
    private Boolean deleted;

    @Basic(optional = false)
    @Column(name = "last_modified_date", nullable = false)
    private Date lastModifiedDate;

    @Basic(optional = false)
    @Column(name = "last_modified_user", nullable = false)
    private Long lastModifiedUser;

    /**
     * Default constructor.
     */
    public IncumbentDevelopersStatisticsEntity() {
        this.new2011To2014 = 0L;
        this.new2011To2015 = 0L;
        this.new2014To2015 = 0L;
        this.incumbent2011To2014 = 0L;
        this.incumbent2011To2015 = 0L;
        this.incumbent2014To2015 = 0L;
    }

    /**
     * Sets the id field upon creation.
     * @param id The value to set object's id equal to
     */
    public IncumbentDevelopersStatisticsEntity(final Long id) {
        this.id = id;
    }

    @Transient
    public Class<?> getClassType() {
        return IncumbentDevelopersStatisticsEntity.class;
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public Long getNew2011To2014() {
        return new2011To2014;
    }

    public void setNew2011To2014(final Long new2011To2014) {
        this.new2011To2014 = new2011To2014;
    }

    public Long getNew2011To2015() {
        return new2011To2015;
    }

    public void setNew2011To2015(final Long new2011To2015) {
        this.new2011To2015 = new2011To2015;
    }

    public Long getNew2014To2015() {
        return new2014To2015;
    }

    public void setNew2014To2015(final Long new2014To2015) {
        this.new2014To2015 = new2014To2015;
    }

    public Long getIncumbent2011To2014() {
        return incumbent2011To2014;
    }

    public void setIncumbent2011To2014(final Long incumbent2011To2014) {
        this.incumbent2011To2014 = incumbent2011To2014;
    }

    public Long getIncumbent2011To2015() {
        return incumbent2011To2015;
    }

    public void setIncumbent2011To2015(final Long incumbent2011To2015) {
        this.incumbent2011To2015 = incumbent2011To2015;
    }

    public Long getIncumbent2014To2015() {
        return incumbent2014To2015;
    }

    public void setIncumbent2014To2015(final Long incumbent2014To2015) {
        this.incumbent2014To2015 = incumbent2014To2015;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(final Date creationDate) {
        this.creationDate = creationDate;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(final Boolean deleted) {
        this.deleted = deleted;
    }

    public Date getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(final Date lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public Long getLastModifiedUser() {
        return lastModifiedUser;
    }

    public void setLastModifiedUser(final Long lastModifiedUser) {
        this.lastModifiedUser = lastModifiedUser;
    }

    @Override
    public String toString() {
        return "Incumbent Developers Statistics Entity ["
                + "[New 2011 to 2014: " + this.new2011To2014 + "]"
                + "[New 2011 to 2015: " + this.new2011To2015 + "]"
                + "[New 2014 to 2015: " + this.new2014To2015 + "]"
                + "[Incumbent 2011 to 2014: " + this.incumbent2011To2014 + "]"
                + "[Incumbent 2011 to 2015: " + this.incumbent2011To2015 + "]"
                + "[Incumbent 2014 to 2015: " + this.incumbent2014To2015 + "]"
                + "]";
    }
}
