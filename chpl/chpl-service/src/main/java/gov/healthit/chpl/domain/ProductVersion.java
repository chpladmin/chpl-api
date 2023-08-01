package gov.healthit.chpl.domain;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import gov.healthit.chpl.dto.ProductVersionDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;

@XmlType(namespace = "http://chpl.healthit.gov/listings")
@XmlAccessorType(XmlAccessType.FIELD)
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
@AllArgsConstructor
public class ProductVersion implements Serializable {
    private static final long serialVersionUID = -447822739573816090L;

    /**
     * Product version internal ID
     */
    @XmlElement(required = true)
    @Schema(description = "Product version internal ID")
    private Long id;

    /**
     * Version name (i.e. "1.0")
     */
    @Schema(description = "Version name (i.e. \"1.0\")")
    @XmlElement(required = true)
    private String version;

    /**
     * Details/description of the product version.
     */
    @Schema(description = "Details/description of the product version.")
    @XmlElement(required = false, nillable = true)
    private String details;


    @XmlTransient
    private String lastModifiedDate;

    public ProductVersion() {
    }

    public ProductVersion(ProductVersionDTO dto) {
        this.id = dto.getId();
        this.version = dto.getVersion();
        if (dto.getLastModifiedDate() != null) {
            this.lastModifiedDate = dto.getLastModifiedDate().getTime() + "";
        }
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

    // Not all attributes have been included. The attributes being used were selected so the ProductVersionManager could
    // determine equality when updating a version
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((version == null) ? 0 : version.hashCode());
        result = prime * result + ((details == null) ? 0 : details.hashCode());
        return result;
    }

    // Not all attributes have been included. The attributes being used were selected so the ProductVersionManager could
    // determine equality when updating a version
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ProductVersion other = (ProductVersion) obj;
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }
        if (StringUtils.isEmpty(version)) {
            if (!StringUtils.isEmpty(other.version)) {
                return false;
            }
        } else if (!version.equals(other.version)) {
            return false;
        }
        if (StringUtils.isEmpty(details)) {
            if (!StringUtils.isEmpty(other.details)) {
                return false;
            }
        } else if (!details.equals(other.details)) {
            return false;
        }
        return true;
    }

}
