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

import gov.healthit.chpl.dto.CertifiedProductQmsStandardDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * The standard or mapping used to meet the quality management system
 * certification criterion
 *
 */
@XmlType(namespace = "http://chpl.healthit.gov/listings")
@XmlAccessorType(XmlAccessType.FIELD)
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
@AllArgsConstructor
public class CertifiedProductQmsStandard implements Serializable {
    private static final long serialVersionUID = -2085183878828053974L;

    /**
     * QMS Standard to listing mapping internal ID
     */
    @XmlElement(required = true)
    private Long id;

    /**
     * QMS Standard internal ID
     */
    @XmlElement(required = true)
    private Long qmsStandardId;

    /**
     * QMS Standard name
     */
    @XmlElement(required = false, nillable = true)
    private String qmsStandardName;

    @XmlTransient
    @JsonIgnore
    private String userEnteredQmsStandardName;

    /**
     * This variable indicates if a QMS standard or mapping was modified,
     * documentation on the changes made. This variable is applicable for 2014
     * and 2015 Edition, and a string variable that does not take any
     * restrictions on formatting or values.
     */
    @XmlElement(required = false, nillable = true)
    private String qmsModification;

    /**
     * QMS Applicable criteria. This variable is applicable for 2015 Edition,
     * and a string variable that does not take any restrictions on formatting
     * or values.
     */
    @XmlElement(required = false, nillable = true)
    private String applicableCriteria;

    public CertifiedProductQmsStandard() {
        super();
    }

    public CertifiedProductQmsStandard(CertifiedProductQmsStandardDTO dto) {
        this.id = dto.getId();
        this.qmsStandardId = dto.getQmsStandardId();
        this.qmsStandardName = dto.getQmsStandardName();
        this.qmsModification = dto.getQmsModification();
        this.applicableCriteria = dto.getApplicableCriteria();
    }

    public boolean matches(CertifiedProductQmsStandard other) {
        boolean result = false;
        if (this.getQmsStandardId() != null && other.getQmsStandardId() != null
                && this.getQmsStandardId().longValue() == other.getQmsStandardId().longValue()) {
            result = true;
        } else if (!StringUtils.isEmpty(this.getQmsStandardName()) && !StringUtils.isEmpty(other.getQmsStandardName())
                && this.getQmsStandardName().equals(other.getQmsStandardName())) {
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

    public Long getQmsStandardId() {
        return qmsStandardId;
    }

    public void setQmsStandardId(Long qmsStandardId) {
        this.qmsStandardId = qmsStandardId;
    }

    public String getQmsStandardName() {
        return qmsStandardName;
    }

    public void setQmsStandardName(String qmsStandardName) {
        this.qmsStandardName = qmsStandardName;
    }

    public String getUserEnteredQmsStandardName() {
        return userEnteredQmsStandardName;
    }

    public void setUserEnteredQmsStandardName(String userEnteredQmsStandardName) {
        this.userEnteredQmsStandardName = userEnteredQmsStandardName;
    }

    public String getQmsModification() {
        return qmsModification;
    }

    public void setQmsModification(String qmsModification) {
        this.qmsModification = qmsModification;
    }

    public String getApplicableCriteria() {
        return applicableCriteria;
    }

    public void setApplicableCriteria(String applicableCriteria) {
        this.applicableCriteria = applicableCriteria;
    }
}
