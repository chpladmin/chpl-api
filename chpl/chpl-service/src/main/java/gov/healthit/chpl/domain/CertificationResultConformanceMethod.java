package gov.healthit.chpl.domain;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.springframework.util.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import gov.healthit.chpl.entity.listing.CertificationResultConformanceMethodEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * The conformance method used for the certification criteria
 *
 */
@XmlType(namespace = "http://chpl.healthit.gov/listings")
@XmlAccessorType(XmlAccessType.FIELD)
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
@AllArgsConstructor
public class CertificationResultConformanceMethod implements Serializable {
    private static final long serialVersionUID = -8648559250833503194L;

    /**
     * Conformance Method to certification result mapping internal ID
     */
    @XmlElement(required = true)
    private Long id;

    /**
     * This variable explains the conformance method being used to test
     * the associated criteria. It is applicable for 2015 Edition.
     */
    @XmlElement(required = true)
    private ConformanceMethod conformanceMethod;

    /**
     * The conformance method version used for a given certification criteria. This
     * variable is a string variable that does not take any restrictions on
     * formatting or values and is applicable for 2014 and 2015 Edition.
     */
    @XmlElement(required = true)
    private String conformanceMethodVersion;

    public CertificationResultConformanceMethod() {
        super();
    }

    public CertificationResultConformanceMethod(CertificationResultConformanceMethodEntity entity) {
        this.id = entity.getId();
        ConformanceMethod cm = new ConformanceMethod();
        if (entity.getConformanceMethod() == null) {
            cm.setId(entity.getConformanceMethodId());
        } else {
            cm.setId(entity.getConformanceMethod().getId());
            cm.setName(entity.getConformanceMethod().getName());
        }
        this.conformanceMethod = cm;
        this.conformanceMethodVersion = entity.getVersion();
    }

    public boolean matches(CertificationResultConformanceMethod anotherMethod) {
        boolean result = false;
        if (this.getConformanceMethod() != null && anotherMethod.getConformanceMethod() != null
                && this.getConformanceMethod().getId() != null && anotherMethod.getConformanceMethod().getId() != null
                && this.getConformanceMethod().getId().longValue() == anotherMethod.getConformanceMethod().getId().longValue()
                && !StringUtils.isEmpty(this.getConformanceMethodVersion())
                && !StringUtils.isEmpty(anotherMethod.getConformanceMethodVersion())
                && this.getConformanceMethodVersion().equalsIgnoreCase(anotherMethod.getConformanceMethodVersion())) {
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

    public String getConformanceMethodVersion() {
        return conformanceMethodVersion;
    }

    public void setConformanceMethodVersion(String conformanceMethodVersion) {
        this.conformanceMethodVersion = conformanceMethodVersion;
    }

    public ConformanceMethod getConformanceMethod() {
        return conformanceMethod;
    }

    public void setConformanceMethod(ConformanceMethod conformanceMethod) {
        this.conformanceMethod = conformanceMethod;
    }
}
