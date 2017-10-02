package gov.healthit.chpl.domain;

import java.io.Serializable;
import java.util.List;

public class UpdateVersionsRequest implements Serializable {
    private static final long serialVersionUID = 4792478364977811082L;
    private List<Long> versionIds;
    private ProductVersion version;
    private Long newProductId;

    public List<Long> getVersionIds() {
        return versionIds;
    }

    public void setVersionIds(List<Long> versionIds) {
        this.versionIds = versionIds;
    }

    public ProductVersion getVersion() {
        return version;
    }

    public void setVersion(ProductVersion version) {
        this.version = version;
    }

    public Long getNewProductId() {
        return newProductId;
    }

    public void setNewProductId(Long newProductId) {
        this.newProductId = newProductId;
    }

}
