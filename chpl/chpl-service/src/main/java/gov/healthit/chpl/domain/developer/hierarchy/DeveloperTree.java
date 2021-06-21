package gov.healthit.chpl.domain.developer.hierarchy;

import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.lang3.ObjectUtils;

import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.dto.DeveloperDTO;
import lombok.Data;

@Data
public class DeveloperTree extends Developer {
    private static final long serialVersionUID = -6578977483166240315L;

    private Set<ProductTree> products = new LinkedHashSet<ProductTree>();

    public DeveloperTree() {
        super();
    }

    public DeveloperTree(DeveloperDTO dto) {
        super(dto);
    }

    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof DeveloperTree)) {
            return false;
        }
        DeveloperTree otherDev = (DeveloperTree) obj;
        return ObjectUtils.equals(this.getDeveloperId(), otherDev.getDeveloperId());
    }

    public int hashCode() {
        if (this.getDeveloperId() == null) {
            return -1;
        }

        return this.getDeveloperId().hashCode();
    }
}
