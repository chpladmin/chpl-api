package gov.healthit.chpl.domain;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.springframework.util.StringUtils;

import gov.healthit.chpl.dto.CertificationResultTestDataDTO;

/**
 * The version of the test data being used for a given certification criteria
 *
 */
@XmlType(namespace = "http://chpl.healthit.gov/listings")
@XmlAccessorType(XmlAccessType.FIELD)
public class CertificationResultTestData implements Serializable {
    private static final long serialVersionUID = -7272525145274770518L;

    /**
     * Certification result to test data mapping internal ID
     */
    @XmlElement(required = true)
    private Long id;

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

    public CertificationResultTestData() {
        super();
    }

    public CertificationResultTestData(CertificationResultTestDataDTO dto) {
        this.id = dto.getId();
        this.version = dto.getVersion();
        this.alteration = dto.getAlteration();
    }

    public boolean matches(CertificationResultTestData anotherTestData) {
        boolean result = false;
        if (!StringUtils.isEmpty(this.getVersion()) && !StringUtils.isEmpty(anotherTestData.getVersion())
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

    public void setId(Long id) {
        this.id = id;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getAlteration() {
        return alteration;
    }

    public void setAlteration(String alteration) {
        this.alteration = alteration;
    }
}
