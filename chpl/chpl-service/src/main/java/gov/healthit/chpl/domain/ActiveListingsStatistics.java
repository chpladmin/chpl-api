package gov.healthit.chpl.domain;

import java.io.Serializable;

import gov.healthit.chpl.dto.ActiveListingsStatisticsDTO;
import gov.healthit.chpl.dto.IncumbentDevelopersStatisticsDTO;

/**
 * Domain object that represents active listings statistics used for creating charts.
 * @author alarned
 *
 */
public class ActiveListingsStatistics implements Serializable {
    private static final long serialVersionUID = -1648513956784683632L;

    private Long id;
    private Long developerCount;
    private Long productCount;
    private CertificationEdition certificationEdition;

    /**
     * Default constructor.
     */
    public ActiveListingsStatistics() {
        // Default Constructor
    }

    /**
     * Constructor that populates the new object based on the DTO that was passed in as a parameter.
     * @param dto ActiveListingsStatisticsDTO object
     */
    public ActiveListingsStatistics(final ActiveListingsStatisticsDTO dto) {
        this.id = dto.getId();
        this.developerCount = dto.getDeveloperCount();
        this.productCount = dto.getProductCount();
        if (dto.getCertificationEdition() != null) {
            this.certificationEdition = new CertificationEdition(dto.getCertificationEdition());
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public Long getDeveloperCount() {
        return developerCount;
    }

    public void setDeveloperCount(final Long developerCount) {
        this.developerCount = developerCount;
    }

    public Long getProductCount() {
        return productCount;
    }

    public void setProductCount(final Long productCount) {
        this.productCount = productCount;
    }

    public CertificationEdition getCertificationEdition() {
        return certificationEdition;
    }

    public void setOldCertificationEdition(final CertificationEdition certificationEdition) {
        this.certificationEdition = certificationEdition;
    }

    @Override
    public String toString() {
        return "Active Listings Statistics Domain object ["
                + "[Developers: " + this.developerCount + "]"
                + "[Products: " + this.productCount + "]"
                + "[Edition: " + this.certificationEdition.getYear() + "]"
                + "]";
    }
}
