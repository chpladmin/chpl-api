package gov.healthit.chpl.conformanceMethod.domain;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang3.StringUtils;

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
     * The method used to evaluate compliance with a certification criterion. It is applicable for 2015 Edition.
     * For the Test Procedure method, this also includes the version used during testing of the certification
     * criterion functionality.
     */
    @XmlElement(required = true)
    private ConformanceMethod conformanceMethod;

    /**
     * The conformance method version used for a given certification criteria.
     * It is valid for Test Procedure method only.
     * This variable is a string variable that does not take any restrictions on
     * formatting or values and is applicable for 2015 Edition.
     */
    @XmlElement(required = false)
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
            cm.setRemovalDate(entity.getConformanceMethod().getRemovalDate());
        }
        this.conformanceMethod = cm;
        this.conformanceMethodVersion = entity.getVersion();
    }

    public boolean matches(CertificationResultConformanceMethod anotherMethod) {
        boolean result = false;
        if (this.getConformanceMethod() != null && anotherMethod.getConformanceMethod() != null
                && this.getConformanceMethod().getId() != null && anotherMethod.getConformanceMethod().getId() != null
                && this.getConformanceMethod().getId().longValue() == anotherMethod.getConformanceMethod().getId().longValue()
                && StringUtils.equals(this.getConformanceMethodVersion(), anotherMethod.getConformanceMethodVersion())) {
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
