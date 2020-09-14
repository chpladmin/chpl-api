package gov.healthit.chpl.domain;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.springframework.util.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import gov.healthit.chpl.dto.CertifiedProductAccessibilityStandardDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * The standard(s) or lack thereof used to meet the accessibility-centered
 * design certification criterion. Please see the 2015 Edition Certification
 * Companion Guide for Accessibility Centered Design for example accessibility
 * standards:
 * https://www.healthit.gov/sites/default/files/2015Ed_CCG_g5-Accessibility-
 * centered-design.pdf
 *
 */
@XmlType(namespace = "http://chpl.healthit.gov/listings")
@XmlAccessorType(XmlAccessType.FIELD)
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
@AllArgsConstructor
public class CertifiedProductAccessibilityStandard implements Serializable {
    private static final long serialVersionUID = -676179466407109456L;

    /**
     * Accessibility standard to listing mapping internal ID
     */
    @XmlElement(required = true)
    private Long id;

    /**
     * Accessibility standard internal ID
     */
    @XmlElement(required = true)
    private Long accessibilityStandardId;

    /**
     * Accessibility standard name
     */
    @XmlElement(required = true)
    private String accessibilityStandardName;

    public CertifiedProductAccessibilityStandard() {
        super();
    }

    public CertifiedProductAccessibilityStandard(CertifiedProductAccessibilityStandardDTO dto) {
        this.id = dto.getId();
        this.accessibilityStandardId = dto.getAccessibilityStandardId();
        this.accessibilityStandardName = dto.getAccessibilityStandardName();
    }

    public boolean matches(CertifiedProductAccessibilityStandard other) {
        boolean result = false;
        if (this.getAccessibilityStandardId() != null && other.getAccessibilityStandardId() != null
                && this.getAccessibilityStandardId().longValue() == other.getAccessibilityStandardId().longValue()) {
            result = true;
        } else if (!StringUtils.isEmpty(this.getAccessibilityStandardName())
                && !StringUtils.isEmpty(other.getAccessibilityStandardName())
                && this.getAccessibilityStandardName().equals(other.getAccessibilityStandardName())) {
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

    public Long getAccessibilityStandardId() {
        return accessibilityStandardId;
    }

    public void setAccessibilityStandardId(final Long accessibilityStandardId) {
        this.accessibilityStandardId = accessibilityStandardId;
    }

    public String getAccessibilityStandardName() {
        return accessibilityStandardName;
    }

    public void setAccessibilityStandardName(final String accessibilityStandardName) {
        this.accessibilityStandardName = accessibilityStandardName;
    }

}
