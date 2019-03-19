package gov.healthit.chpl.domain;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import gov.healthit.chpl.dto.ProductVersionDTO;

@XmlType(namespace = "http://chpl.healthit.gov/listings")
@XmlAccessorType(XmlAccessType.FIELD)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductVersion implements Serializable {
    private static final long serialVersionUID = -447822739573816090L;

    /**
     * Product version internal ID
     */
    @XmlElement(required = true)
    private Long versionId;

    /**
     * Version name (i.e. "1.0")
     */
    @XmlElement(required = true)
    private String version;

    /**
     * Details/description of the product version.
     */
    @XmlElement(required = false, nillable = true)
    private String details;

    @XmlTransient
    private String lastModifiedDate;

    public ProductVersion() {
    }

    public ProductVersion(ProductVersionDTO dto) {
        this.versionId = dto.getId();
        this.version = dto.getVersion();
        if (dto.getLastModifiedDate() != null) {
            this.lastModifiedDate = dto.getLastModifiedDate().getTime() + "";
        }
    }

    public Long getVersionId() {
        return versionId;
    }

    public void setVersionId(final Long versionId) {
        this.versionId = versionId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(final String version) {
        this.version = version;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(final String details) {
        this.details = details;
    }

    public String getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(final String lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }
}
