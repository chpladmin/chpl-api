package gov.healthit.chpl.domain;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.springframework.util.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import gov.healthit.chpl.dto.CertificationResultTestToolDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * The test tool used to certify the Health IT Module to the corresponding
 * certification criteria Allowable values are based on the NIST 2014 and 2015
 * Edition Test Tools.
 *
 */
@XmlType(namespace = "http://chpl.healthit.gov/listings")
@XmlAccessorType(XmlAccessType.FIELD)
@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
@Builder
public class CertificationResultTestTool implements Serializable {
    private static final long serialVersionUID = 2785949879671019720L;

    /**
     * Test tool to certification result mapping internal ID
     */
    @XmlElement(required = true)
    private Long id;

    /**
     * Test tool internal ID
     */
    @XmlElement(required = true)
    private Long testToolId;

    /**
     * The test tool used to certify the Health IT Module to the corresponding
     * certification criteria
     */
    @XmlElement(required = true)
    private String testToolName;

    /**
     * The version of the test tool being used. This variable is applicable for
     * 2014 and 2015 Edition, and a string variable that does not take any
     * restrictions on formatting or values.
     */
    @XmlElement(required = false, nillable = true)
    private String testToolVersion;

    /**
     * Whether or not the test tool has been retired.
     */
    @XmlElement(required = false, nillable = true)
    private boolean retired;

    /**
     * Default constructor.
     */
    public CertificationResultTestTool() {
        super();
    }

    /**
     * Constructor using DTO.
     * @param dto input data transfer object
     */
    public CertificationResultTestTool(final CertificationResultTestToolDTO dto) {
        this.id = dto.getId();
        this.testToolId = dto.getTestToolId();
        this.testToolName = dto.getTestToolName();
        this.testToolVersion = dto.getTestToolVersion();
        this.retired = dto.isRetired();
    }

    /**
     * Indicate if this test tool matches another one.
     * @param anotherTool the other tool
     * @return true iff (id or name equal) and version equals
     */
    public boolean matches(final CertificationResultTestTool anotherTool) {
        boolean result = false;
        if (this.getTestToolId() != null && anotherTool.getTestToolId() != null
                && this.getTestToolId().longValue() == anotherTool.getTestToolId().longValue()
                && ((StringUtils.isEmpty(this.getTestToolVersion())
                        && StringUtils.isEmpty(anotherTool.getTestToolVersion()))
                     ||
                     (!StringUtils.isEmpty(this.getTestToolVersion())
                             && !StringUtils.isEmpty(anotherTool.getTestToolVersion())
                             && this.getTestToolVersion().equalsIgnoreCase(anotherTool.getTestToolVersion())))) {
            result = true;
        } else if (!StringUtils.isEmpty(this.getTestToolName()) && !StringUtils.isEmpty(anotherTool.getTestToolName())
                && this.getTestToolName().equalsIgnoreCase(anotherTool.getTestToolName())
                && ((StringUtils.isEmpty(this.getTestToolVersion()) && StringUtils.isEmpty(anotherTool.getTestToolVersion()))
                     ||
                     (!StringUtils.isEmpty(this.getTestToolVersion())
                             && !StringUtils.isEmpty(anotherTool.getTestToolVersion())
                             && this.getTestToolVersion().equalsIgnoreCase(anotherTool.getTestToolVersion())))) {
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

    public Long getTestToolId() {
        return testToolId;
    }

    public void setTestToolId(final Long testToolId) {
        this.testToolId = testToolId;
    }

    public String getTestToolName() {
        return testToolName;
    }

    public void setTestToolName(final String testToolName) {
        this.testToolName = testToolName;
    }

    public String getTestToolVersion() {
        return testToolVersion;
    }

    public void setTestToolVersion(final String testToolVersion) {
        this.testToolVersion = testToolVersion;
    }

    public boolean isRetired() {
        return retired;
    }

    public void setRetired(final boolean retired) {
        this.retired = retired;
    }
}
