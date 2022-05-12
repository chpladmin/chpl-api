package gov.healthit.chpl.domain.developer.hierarchy;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

import gov.healthit.chpl.domain.Developer;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeveloperTree extends Developer {
    private static final long serialVersionUID = -6578977483166240315L;

    private Set<ProductTree> products = new LinkedHashSet<ProductTree>();

    public DeveloperTree() {
        super();
    }

    public DeveloperTree(Developer developer) {
        this.setId(developer.getId());
        this.setDeveloperId(developer.getId());
        this.setAddress(developer.getAddress());
        this.setAttestations(developer.getAttestations());
        this.setContact(developer.getContact());
        this.setDeleted(developer.getDeleted());
        this.setDeveloperCode(developer.getDeveloperCode());
        this.setLastModifiedDate(developer.getLastModifiedDate());
        this.setName(developer.getName());
        this.setSelfDeveloper(developer.getSelfDeveloper());
        this.setStatusEvents(developer.getStatusEvents());
        this.setWebsite(developer.getWebsite());
    }

    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof DeveloperTree)) {
            return false;
        }
        DeveloperTree otherDev = (DeveloperTree) obj;
        return Objects.equals(this.getId(), otherDev.getId());
    }

    public int hashCode() {
        if (this.getId() == null) {
            return -1;
        }

        return this.getId().hashCode();
    }
}
