package gov.healthit.chpl.domain;

import java.io.Serializable;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import gov.healthit.chpl.dto.ProductVersionDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductVersion implements Serializable {
    private static final long serialVersionUID = -447822739573816090L;

    @Schema(description = "Product version internal ID")
    private Long id;

    @Schema(description = "Version name (i.e. \"1.0\")")
    private String version;

    @Schema(description = "Details/description of the product version.")
    private String details;

    private String lastModifiedDate;

    public ProductVersion(ProductVersionDTO dto) {
        this.id = dto.getId();
        this.version = dto.getVersion();
        if (dto.getLastModifiedDate() != null) {
            this.lastModifiedDate = dto.getLastModifiedDate().getTime() + "";
        }
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
