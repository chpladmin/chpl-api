package gov.healthit.chpl.domain;

import java.io.Serializable;

import org.apache.commons.lang3.ObjectUtils;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Builder;

@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
@AllArgsConstructor
public class MeasureDomain implements Serializable {
    private static final long serialVersionUID = -745253265541448011L;
    private Long id;
    private String name;

    public MeasureDomain() {
    }

    public boolean matches(MeasureDomain anotherDomain) {
        if (this.id == null && anotherDomain.id != null || this.id != null && anotherDomain.id == null) {
            return false;
        } else if (ObjectUtils.allNotNull(this.id, anotherDomain.id)
                && this.id.longValue() != anotherDomain.id.longValue()) {
            return false;
        }

        return true;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
