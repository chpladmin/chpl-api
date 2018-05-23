package gov.healthit.chpl.domain;

import java.io.Serializable;

import gov.healthit.chpl.dto.ListingCountStatisticsDTO;

/**
 * Domain object that represents listings count statistics used for creating charts.
 * @author alarned
 *
 */
public class ListingCountStatistics implements Serializable {
    private static final long serialVersionUID = -1648513956784683632L;

    private Long id;
    private Long developerCount;
    private Long productCount;
    private CertificationEdition certificationEdition;
    private CertificationStatus certificationStatus;

    /**
     * Default constructor.
     */
    public ListingCountStatistics() {
        // Default Constructor
    }

    /**
     * Constructor that populates the new object based on the DTO that was passed in as a parameter.
     * @param dto ListingCountStatisticsDTO object
     */
    public ListingCountStatistics(final ListingCountStatisticsDTO dto) {
        this.id = dto.getId();
        this.developerCount = dto.getDeveloperCount();
        this.productCount = dto.getProductCount();
        if (dto.getCertificationEdition() != null) {
            this.certificationEdition = new CertificationEdition(dto.getCertificationEdition());
        }
        if (dto.getCertificationStatus() != null) {
            this.certificationStatus = new CertificationStatus(dto.getCertificationStatus());
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

    public void setCertificationEdition(final CertificationEdition certificationEdition) {
        this.certificationEdition = certificationEdition;
    }

    public CertificationStatus getCertificationStatus() {
        return certificationStatus;
    }

    public void setCertificationStatus(final CertificationStatus certificationStatus) {
        this.certificationStatus = certificationStatus;
    }

    @Override
    public String toString() {
        return "Active Listings Statistics Domain object ["
                + "[Developers: " + this.developerCount + "]"
                + "[Products: " + this.productCount + "]"
                + "[Edition: " + this.certificationEdition.getYear() + "]"
                + "[Status: " + this.certificationStatus.getName() + "]"
                + "]";
    }
}
