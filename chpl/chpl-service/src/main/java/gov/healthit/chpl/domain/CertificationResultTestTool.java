package gov.healthit.chpl.domain;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import gov.healthit.chpl.criteriaattribute.testtool.TestTool;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

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
@NoArgsConstructor
@Builder
public class CertificationResultTestTool implements Serializable {
    private static final long serialVersionUID = 2785949879671019720L;

    /**
     * Test tool to certification result mapping internal ID
     */
    @XmlElement(required = true)
    private Long id;

    /**
     * Test tool
     */
    @XmlElement(required = true)
    private TestTool testTool;

    /**
     * The version of the test tool being used. This variable is applicable for
     * 2014 and 2015 Edition, and a string variable that does not take any
     * restrictions on formatting or values.
     */
    @XmlElement(required = false, nillable = true)
    private String version;


    @XmlTransient
    @JsonIgnore
    private Long certificationResultId;

    @Deprecated
    @XmlTransient
    private Long testToolId;

    @Deprecated
    @XmlTransient
    private String testToolName;

    @Deprecated
    @XmlTransient
    private String testToolVersion;

    @Deprecated
    @XmlTransient
    private Boolean retired;

    public boolean matches(final CertificationResultTestTool anotherTool) {
        boolean result = false;
        if (this.getTestTool().getId() != null && anotherTool.getTestTool().getId() != null
                && this.getTestTool().getId().longValue() == anotherTool.getTestTool().getId().longValue()
                && ((StringUtils.isEmpty(this.getVersion())
                        && StringUtils.isEmpty(anotherTool.getVersion()))
                     ||
                     (!StringUtils.isEmpty(this.getVersion())
                             && !StringUtils.isEmpty(anotherTool.getVersion())
                             && this.getVersion().equalsIgnoreCase(anotherTool.getVersion())))) {
            result = true;
        } else if (!StringUtils.isEmpty(this.getTestTool().getValue()) && !StringUtils.isEmpty(anotherTool.getTestTool().getValue())
                && this.getTestTool().getValue().equalsIgnoreCase(anotherTool.getTestTool().getValue())
                && ((StringUtils.isEmpty(this.getVersion()) && StringUtils.isEmpty(anotherTool.getVersion()))
                     ||
                     (!StringUtils.isEmpty(this.getVersion())
                             && !StringUtils.isEmpty(anotherTool.getVersion())
                             && this.getVersion().equalsIgnoreCase(anotherTool.getVersion())))) {
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

    public TestTool getTestTool() {
        return testTool;
    }

    public void setTestTool(TestTool testTool) {
        this.testTool = testTool;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    //TODO OCD-4242
    @Deprecated
    public Long getTestToolId() {
        return testToolId;
    }

    //TODO OCD-4242
    @Deprecated
    public void setTestToolId(final Long testToolId) {
        this.testToolId = testToolId;
    }

    //TODO OCD-4242
    @Deprecated
    public String getTestToolName() {
        return testToolName;
    }

    //TODO OCD-4242
    @Deprecated
    public void setTestToolName(final String testToolName) {
        this.testToolName = testToolName;
    }

    //TODO OCD-4242
    @Deprecated
    public String getTestToolVersion() {
        return testToolVersion;
    }

    //TODO OCD-4242
    @Deprecated
    public void setTestToolVersion(final String testToolVersion) {
        this.testToolVersion = testToolVersion;
    }

    //TODO OCD-4242
    @Deprecated
    public boolean isRetired() {
        return testTool.isRetired();
    }

    //TODO OCD-4242
    //@Deprecated
    //public void setRetired(final boolean retired) {
    //    this.retired = retired;
    //}

    public Long getCertificationResultId() {
        return certificationResultId;
    }

    public void setCertificationResultId(Long certificationResultId) {
        this.certificationResultId = certificationResultId;
    }
}
