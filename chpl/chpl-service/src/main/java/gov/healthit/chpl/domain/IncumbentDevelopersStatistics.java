package gov.healthit.chpl.domain;

import java.io.Serializable;
import java.util.Date;

import gov.healthit.chpl.dto.IncumbentDevelopersStatisticsDTO;

/**
 * Domain object that represents incumbent developers statistics used for creating charts.
 * @author alarned
 *
 */
public class IncumbentDevelopersStatistics implements Serializable {
    private static final long serialVersionUID = -1648513956784683632L;

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
    public IncumbentDevelopersStatistics() {
        // Default Constructor
    }

    /**
     * Constructor that populates the new object based on the dto that was passed in as a parameter.
     * @param dto IncumbentDevelopersStatisticsDTO object
     */
    public IncumbentDevelopersStatistics(final IncumbentDevelopersStatisticsDTO dto) {
        this.id = dto.getId();
        this.setNew2011To2014(dto.getNew2011To2014());
        this.setNew2011To2015(dto.getNew2011To2015());
        this.setNew2014To2015(dto.getNew2014To2015());
        this.setIncumbent2011To2014(dto.getIncumbent2011To2014());
        this.setIncumbent2011To2015(dto.getIncumbent2011To2015());
        this.setIncumbent2014To2015(dto.getIncumbent2014To2015());
        this.deleted = dto.getDeleted();
        this.lastModifiedDate = dto.getLastModifiedDate();
        this.lastModifiedUser = dto.getLastModifiedUser();
        this.creationDate = dto.getCreationDate();
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

}
