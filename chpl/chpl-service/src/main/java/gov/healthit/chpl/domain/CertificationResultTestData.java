package gov.healthit.chpl.domain;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.springframework.util.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import gov.healthit.chpl.dto.CertificationResultTestDataDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * The version of the test data being used for a given certification criteria.
 *
 */
@XmlType(namespace = "http://chpl.healthit.gov/listings")
@XmlAccessorType(XmlAccessType.FIELD)
@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
@Builder
public class CertificationResultTestData implements Serializable {
    private static final long serialVersionUID = -7272525145274770518L;

    /**
     * Certification result to test data mapping internal ID
     */
    @XmlElement(required = true)
    private Long id;

    /**
     * This variable explains the test data being used to test
     * the associated criteria. It is applicable for 2015 Edition.
     */
    @XmlElement(required = true)
    private TestData testData;

    /**
     * This variable explains the version of the test data being used for a
     * given certification criteria. It is applicable for 2014 and 2015 Edition
     * and a string variable that does not take any restrictions on formatting
     * or values.
     */
    @XmlElement(required = true)
    private String version;

    /**
     * This variable indicates if test data alterations are made, a description
     * of all modifications made. It is applicable for 2014 and 2015 Edition and
     * a string variable that does not take any restrictions on formatting or
     * values.
     */
    @XmlElement(required = false, nillable = true)
    private String alteration;

    @XmlTransient
    @JsonIgnore
    private String userEnteredName;

    public CertificationResultTestData() {
        super();
    }

    public CertificationResultTestData(CertificationResultTestDataDTO dto) {
        this.id = dto.getId();
        TestData td = new TestData();
        if (dto.getTestData() == null) {
            td.setId(dto.getTestDataId());
        } else {
            td.setId(dto.getTestData().getId());
            td.setName(dto.getTestData().getName());
        }
        this.testData = td;
        this.version = dto.getVersion();
        this.alteration = dto.getAlteration();
    }

    public boolean matches(final CertificationResultTestData anotherTestData) {
        boolean result = false;
        if (this.getTestData() != null && anotherTestData.getTestData() != null
                && this.getTestData().getId() != null && anotherTestData.getTestData().getId() != null
                && this.getTestData().getId().longValue() == anotherTestData.getTestData().getId().longValue()
                && !StringUtils.isEmpty(this.getVersion()) && !StringUtils.isEmpty(anotherTestData.getVersion())
                && this.getVersion().equals(anotherTestData.getVersion())
                && ((StringUtils.isEmpty(this.getAlteration()) && StringUtils.isEmpty(anotherTestData.getAlteration()))
                        || this.getAlteration().equals(anotherTestData.getAlteration()))) {
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

    public String getVersion() {
        return version;
    }

    public void setVersion(final String version) {
        this.version = version;
    }

    public String getAlteration() {
        return alteration;
    }

    public void setAlteration(final String alteration) {
        this.alteration = alteration;
    }

    public TestData getTestData() {
        return testData;
    }

    public void setTestData(TestData testData) {
        this.testData = testData;
    }

    public String getUserEnteredName() {
        return userEnteredName;
    }

    public void setUserEnteredName(String userEnteredName) {
        this.userEnteredName = userEnteredName;
    }
}
