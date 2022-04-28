package gov.healthit.chpl.domain.developer.hierarchy;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

import gov.healthit.chpl.domain.ProductVersion;
import gov.healthit.chpl.dto.ProductVersionDTO;
import lombok.Data;

@Data
public class VersionTree extends ProductVersion {
    private static final long serialVersionUID = 6827193932953676099L;

    private Set<SimpleListing> listings = new LinkedHashSet<SimpleListing>();

    public VersionTree() {
        super();
    }

    public VersionTree(ProductVersionDTO dto) {
        super(dto);
    }

    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof VersionTree)) {
            return false;
        }
        VersionTree otherVersion = (VersionTree) obj;
        return Objects.equals(this.getId(), otherVersion.getId());
    }

    public int hashCode() {
        if (this.getId() == null) {
            return -1;
        }

        return this.getId().hashCode();
    }
}
