package gov.healthit.chpl.domain;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import gov.healthit.chpl.api.deprecatedUsage.DeprecatedResponseField;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * The Accredited Testing Labs used to test the the Listing.
 *
 */
@XmlType(namespace = "http://chpl.healthit.gov/listings")
@XmlAccessorType(XmlAccessType.FIELD)
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
@AllArgsConstructor
public class CertifiedProductTestingLab implements Serializable {
    private static final long serialVersionUID = -2078691100124619582L;

    /**
     * Testing Lab to listing mapping internal ID
     */
    @Schema(description = "Testing Lab to listing mapping internal ID")
    @XmlElement(required = true)
    private Long id;


    /**
     * Testing Lab
     */
    @Schema(description = "Testing Lab")
    @XmlElement(required = true)
    private TestingLab testingLab;


    /**
     * Testing Lab internal ID
     */
    @Deprecated
    @DeprecatedResponseField(removalDate = "2024-04-01", message = "Replaced by testingLab.id")
    @Schema(description = "Testing Lab internal ID")
    @XmlElement(required = true)
    private Long testingLabId;

    /**
     * The Testing Lab's public name
     */
    @Deprecated
    @DeprecatedResponseField(removalDate = "2024-04-01", message = "Replaced by testingLab.name")
    @Schema(description = "The Testing Lab's public name")
    @XmlElement(required = true)
    private String testingLabName;

    /**
     * The Testing Lab's Code
     */
    @Deprecated
    @DeprecatedResponseField(removalDate = "2024-04-01", message = "Replaced by testingLab.atlCode")
    @Schema(description = "The Testing Lab's Code")
    @XmlElement(required = true)
    private String testingLabCode;

    /**
     * Default constructor.
     */
    public CertifiedProductTestingLab() {
        super();
    }


    /**
     * Does this match another one.
     * @param other the other one
     * @return true iff it is the same as the other one
     */
    public boolean matches(final CertifiedProductTestingLab other) {
        boolean result = false;
        if (this.getTestingLab().getId() != null && other.getTestingLab().getId() != null
                && this.getTestingLab().getId().longValue() == other.getTestingLab().getId().longValue()) {
            result = true;
        } else if (this.getTestingLab().getName() != null && other.getTestingLab().getName() != null
                && this.getTestingLab().getName().equalsIgnoreCase(other.getTestingLab().getName())) {
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

    public TestingLab getTestingLab() {
        return testingLab;
    }

    public void setTestingLab(TestingLab testingLab) {
        this.testingLab = testingLab;
    }

    @Deprecated
    public Long getTestingLabId() {
        return testingLabId;
    }

    @Deprecated
    public void setTestingLabId(final Long testingLabId) {
        this.testingLabId = testingLabId;
    }

    @Deprecated
    public String getTestingLabName() {
        return testingLabName;
    }

    @Deprecated
    public void setTestingLabName(final String testingLabName) {
        this.testingLabName = testingLabName;
    }

    @Deprecated
    public String getTestingLabCode() {
        return testingLabCode;
    }

    @Deprecated
    public void setTestingLabCode(final String testingLabCode) {
        this.testingLabCode = testingLabCode;
    }
}
