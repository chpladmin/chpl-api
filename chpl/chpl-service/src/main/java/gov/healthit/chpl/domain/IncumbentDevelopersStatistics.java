package gov.healthit.chpl.domain;

import java.io.Serializable;
import java.util.Date;

import gov.healthit.chpl.domain.concept.CertificationEditionConcept;
import gov.healthit.chpl.dto.IncumbentDevelopersStatisticsDTO;

/**
 * Domain object that represents incumbent developers statistics used for creating charts.
 * @author alarned
 *
 */
public class IncumbentDevelopersStatistics implements Serializable {
    private static final long serialVersionUID = -1648513956784683632L;

    private Long id;
    private Long newCount;
    private Long incumbentCount;
    private Long oldCertificationEditionId;
    private Long newCertificationEditionId;
    private CertificationEditionConcept oldCertificationEdition;
    private CertificationEditionConcept newCertificationEdition;
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
        this.setNewCount(dto.getNewCount());
        this.setIncumbentCount(dto.getIncumbentCount());
        this.setOldCertificationEditionId(dto.getOldCertificationEditionId());
        this.setNewCertificationEditionId(dto.getNewCertificationEditionId());
//        this.setOldCertificationEdition(dto.getOldCertificationEdition());
//        this.setNewCertificationEdition(dto.getNewCertificationEdition());
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

    public Long getNewCount() {
        return newCount;
    }

    public void setNewCount(final Long newCount) {
        this.newCount = newCount;
    }

    public Long getIncumbentCount() {
        return incumbentCount;
    }

    public void setIncumbentCount(final Long incumbentCount) {
        this.incumbentCount = incumbentCount;
    }

    public Long getOldCertificationEditionId() {
        return oldCertificationEditionId;
    }

    public void setOldCertificationEditionId(final Long oldCertificationEditionId) {
        this.oldCertificationEditionId = oldCertificationEditionId;
    }

    public Long getNewCertificationEditionId() {
        return newCertificationEditionId;
    }

    public void setNewCertificationEditionId(final Long newCertificationEditionId) {
        this.newCertificationEditionId = newCertificationEditionId;
    }

    public CertificationEditionConcept getOldCertificationEdition() {
        return oldCertificationEdition;
    }

    public void setOldCertificationEdition(final CertificationEditionConcept oldCertificationEdition) {
        this.oldCertificationEdition = oldCertificationEdition;
    }

    public CertificationEditionConcept getNewCertificationEdition() {
        return newCertificationEdition;
    }

    public void setNewCertificationEdition(final CertificationEditionConcept newCertificationEdition) {
        this.newCertificationEdition = newCertificationEdition;
    }

}
