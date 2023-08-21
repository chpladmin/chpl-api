package gov.healthit.chpl.domain;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.springframework.util.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import gov.healthit.chpl.dto.CertificationResultTestStandardDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * A standard used to meet a certification criterion. You can find a list of
 * potential values in the 2014 or 2015 Functionality and Standards Reference
 * Tables.
 *
 */
@XmlType(namespace = "http://chpl.healthit.gov/listings")
@XmlAccessorType(XmlAccessType.FIELD)
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
@AllArgsConstructor
public class CertificationResultTestStandard implements Serializable {
    private static final long serialVersionUID = -9182555768595891414L;

    /**
     * Test standard to certification result mapping internal ID
     */
    @Schema(description = "Test standard to certification result mapping internal ID")
    @XmlElement(required = true)
    private Long id;

    /**
     * Test standard internal ID
     */
    @Schema(description = "Test standard internal ID")
    @XmlElement(required = false, nillable = true)
    private Long testStandardId;

    /**
     * Description of test standard
     */
    @Schema(description = "Description of test standard")
    @XmlElement(required = false, nillable = true)
    private String testStandardDescription;

    /**
     * Name of test standard
     */
    @Schema(description = "Name of test standard")
    @XmlElement(required = false, nillable = true)
    private String testStandardName;

    public CertificationResultTestStandard() {
        super();
    }

    public CertificationResultTestStandard(CertificationResultTestStandardDTO dto) {
        this.id = dto.getId();
        this.testStandardId = dto.getTestStandardId();
        this.testStandardDescription = dto.getTestStandardDescription();
        this.testStandardName = dto.getTestStandardName();
    }

    public boolean matches(CertificationResultTestStandard anotherStd) {
        boolean result = false;
        if (this.getTestStandardId() != null && anotherStd.getTestStandardId() != null
                && this.getTestStandardId().longValue() == anotherStd.getTestStandardId().longValue()) {
            result = true;
        } else if (!StringUtils.isEmpty(this.getTestStandardName())
                && !StringUtils.isEmpty(anotherStd.getTestStandardName())
                && this.getTestStandardName().equalsIgnoreCase(anotherStd.getTestStandardName())) {
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

    public Long getTestStandardId() {
        return testStandardId;
    }

    public void setTestStandardId(final Long testStandardId) {
        this.testStandardId = testStandardId;
    }

    public String getTestStandardDescription() {
        return testStandardDescription;
    }

    public void setTestStandardDescription(final String testStandardDescription) {
        this.testStandardDescription = testStandardDescription;
    }

    public String getTestStandardName() {
        return testStandardName;
    }

    public void setTestStandardName(final String testStandardName) {
        this.testStandardName = testStandardName;
    }

}
