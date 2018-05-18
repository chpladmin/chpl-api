package gov.healthit.chpl.dto;

import java.io.Serializable;
import java.util.Date;

import gov.healthit.chpl.entity.IncumbentDevelopersStatisticsEntity;

/**
 * Incumbent Developers data transfer object.
 * @author alarned
 *
 */
public class IncumbentDevelopersStatisticsDTO implements Serializable {

    private static final long serialVersionUID = -1536844909545189801L;

    private Long id;
    private Long new2011To2014;
    private Long new2011To2015;
    private Long new2014To2015;
    private Long incumbent2011To2014;
    private Long incumbent2011To2015;
    private Long incumbent2014To2015;
    private Date creationDate;
    private Boolean deleted;
    private Date lastModifiedDate;
    private Long lastModifiedUser;

    /**
     * Default constructor.
     */
    public IncumbentDevelopersStatisticsDTO() {
    }

    /**
     * Constructor that will populate the created object based on the entity
     * that is passed in as a parameter.
     * @param entity IncumbentDevelopersStatisticsEntity entity
     */
    public IncumbentDevelopersStatisticsDTO(final IncumbentDevelopersStatisticsEntity entity) {
        this.setId(entity.getId());
        this.setNew2011To2014(entity.getNew2011To2014());
        this.setNew2011To2015(entity.getNew2011To2015());
        this.setNew2014To2015(entity.getNew2014To2015());
        this.setIncumbent2011To2014(entity.getIncumbent2011To2014());
        this.setIncumbent2011To2015(entity.getIncumbent2011To2015());
        this.setIncumbent2014To2015(entity.getIncumbent2014To2015());
        this.creationDate = entity.getCreationDate();
        this.deleted = entity.getDeleted();
        this.lastModifiedDate = entity.getLastModifiedDate();
        this.lastModifiedUser = entity.getLastModifiedUser();
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
        return "Incumbent Developers Statistics DTO ["
                + "[New 2011 to 2014: " + this.new2011To2014 + "]"
                + "[New 2011 to 2015: " + this.new2011To2015 + "]"
                + "[New 2014 to 2015: " + this.new2014To2015 + "]"
                + "[Incumbent 2011 to 2014: " + this.incumbent2011To2014 + "]"
                + "[Incumbent 2011 to 2015: " + this.incumbent2011To2015 + "]"
                + "[Incumbent 2014 to 2015: " + this.incumbent2014To2015 + "]"
                + "]";
    }
}
