package gov.healthit.chpl.svap.domain;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import gov.healthit.chpl.svap.entity.CertificationResultSvapEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.ToString;

/**
 * ONC has established the Standards Version Advancement Process (SVAP) to enable health IT developers’
 * ability to incorporate newer versions of Secretary-adopted standards and implementation specifications,
 * as part of the "Real World Testing" Condition and Maintenance of Certification requirement (§170.405)
 * of the 21st Century Cures Act
 *
 */
@XmlType(namespace = "http://chpl.healthit.gov/listings")
@XmlAccessorType(XmlAccessType.FIELD)
@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
@Builder
@ToString
public class CertificationResultSvap implements Serializable {
    private static final long serialVersionUID = -1935940788953178006L;

    /**
     * SVAP to certification result mapping internal ID
     */
    @XmlElement(required = true)
    private Long id;

    /**
     * SVAP internal ID
     */
    @XmlElement(required = true)
    private Long svapId;

    /**
     * Regulatory Text Citation for Standard / Implementation Specification Adopted
     */
    @XmlElement(required = true)
    private String regulatoryTextCitation;

    /**
     *  National Coordinator Approved Advanced Version(s)
     */
    @XmlElement(required = true)
    private String approvedStandardVersion;

    /**
     *  Indicates if the SVAP has been replaced
     */
    @XmlElement(required = true)
    private boolean replaced;

    public CertificationResultSvap() { }

    public CertificationResultSvap(CertificationResultSvapEntity entity) {
        this.id = entity.getId();
        this.svapId = entity.getSvapId();
        this.regulatoryTextCitation = entity.getSvap().getRegulatoryTextCitation();
        this.approvedStandardVersion = entity.getSvap().getApprovedStandardVersion();
        this.replaced = entity.getSvap().getReplaced();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSvapId() {
        return svapId;
    }

    public void setSvapId(Long svapId) {
        this.svapId = svapId;
    }

    public String getRegulatoryTextCitation() {
        return regulatoryTextCitation;
    }

    public void setRegulatoryTextCitation(String regulatoryTextCitation) {
        this.regulatoryTextCitation = regulatoryTextCitation;
    }

    public String getApprovedStandardVersion() {
        return approvedStandardVersion;
    }

    public void setApprovedStandardVersion(String approvedStandardVersion) {
        this.approvedStandardVersion = approvedStandardVersion;
    }

    public boolean getReplaced() {
        return replaced;
    }

    public void setReplaced(boolean replaced) {
        this.replaced = replaced;
    }
 }
