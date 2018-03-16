package gov.healthit.chpl.domain;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import gov.healthit.chpl.dto.CertifiedProductTestingLabDTO;

/**
 * The Accredited Testing Labs used to the the Listing.
 *
 */
@XmlType(namespace = "http://chpl.healthit.gov/listings")
@XmlAccessorType(XmlAccessType.FIELD)
public class CertifiedProductTestingLab implements Serializable {
    private static final long serialVersionUID = -2078691100124619582L;

    /**
     * Targeted user to listing mapping internal ID
     */
    @XmlElement(required = true)
    private Long id;

    /**
     * Testing Lab internal ID
     */
    @XmlElement(required = true)
    private Long testingLabId;

    /**
     * Default constructor.
     */
    public CertifiedProductTestingLab() {
        super();
    }

    /**
     * Constructor from DTO.
     * @param dto the DTO
     */
    public CertifiedProductTestingLab(final CertifiedProductTestingLabDTO dto) {
        this.id = dto.getId();
        this.testingLabId = dto.getTestingLabId();
    }

    /**
     * Does this match another one.
     * @param other the other one
     * @return true iff it is the same as the other one
     */
    public boolean matches(final CertifiedProductTestingLab other) {
        boolean result = false;
        if (this.getTestingLabId() != null && other.getTestingLabId() != null) {
            result = true;
        }
        return result;
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public Long getTestingLabId() {
        return testingLabId;
    }

    public void setTestingLabId(final Long testingLabId) {
        this.testingLabId = testingLabId;
    }
}
