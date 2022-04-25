package gov.healthit.chpl.sharedstore;

import java.io.Serializable;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SharedStorePrimaryKey implements Serializable {
    private static final long serialVersionUID = 8887068260992258871L;

    @Column(name = "domain")
    private String domain;

    @Column(name = "key")
    private String key;

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Objects.hash(key, domain);
        return result;
    }

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
        SharedStorePrimaryKey other = (SharedStorePrimaryKey) obj;
        return Objects.equals(key, other.key) && Objects.equals(domain, other.domain);
    }

}
