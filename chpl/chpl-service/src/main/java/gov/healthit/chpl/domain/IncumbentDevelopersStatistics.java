package gov.healthit.chpl.domain;

import java.io.Serializable;

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
    private CertificationEdition oldCertificationEdition;
    private CertificationEdition newCertificationEdition;

    /**
     * Default constructor.
     */
    public IncumbentDevelopersStatistics() {
        // Default Constructor
    }

    /**
     * Constructor that populates the new object based on the DTO that was passed in as a parameter.
     * @param dto IncumbentDevelopersStatisticsDTO object
     */
    public IncumbentDevelopersStatistics(final IncumbentDevelopersStatisticsDTO dto) {
        this.id = dto.getId();
        this.setNewCount(dto.getNewCount());
        this.setIncumbentCount(dto.getIncumbentCount());
        if (dto.getOldCertificationEdition() != null) {
            this.oldCertificationEdition = dto.getOldCertificationEdition();
        }
        if (dto.getNewCertificationEdition() != null) {
            this.newCertificationEdition = dto.getNewCertificationEdition();
        }

    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
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

    public CertificationEdition getOldCertificationEdition() {
        return oldCertificationEdition;
    }

    public void setOldCertificationEdition(final CertificationEdition oldCertificationEdition) {
        this.oldCertificationEdition = oldCertificationEdition;
    }

    public CertificationEdition getNewCertificationEdition() {
        return newCertificationEdition;
    }

    public void setNewCertificationEdition(final CertificationEdition newCertificationEdition) {
        this.newCertificationEdition = newCertificationEdition;
    }

    @Override
    public String toString() {
        return "Incumbent Developers Statistics Domain object ["
                + "[New: " + this.newCount + "]"
                + "[Incumbent: " + this.incumbentCount + "]"
                + "[Old Edition: " + this.oldCertificationEdition.getName() + "]"
                + "[New Edition: " + this.newCertificationEdition.getName() + "]"
                + "]";
    }
}
