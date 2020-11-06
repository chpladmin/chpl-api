package gov.healthit.chpl.domain;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Builder;

@XmlType(namespace = "http://chpl.healthit.gov/listings")
@XmlAccessorType(XmlAccessType.FIELD)
@Builder
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class MeasureType implements Serializable {
    private static final long serialVersionUID = -8391253265541448011L;
    private Long id;
    private String name;

    public MeasureType() {
    }

    public boolean matches(MeasureType anotherType) {
        if (this.id == null && anotherType.id != null || this.id != null && anotherType.id == null) {
            return false;
        } else if (ObjectUtils.allNotNull(this.id, anotherType.id)
                && this.id.longValue() != anotherType.id.longValue()) {
            return false;
        }

        if (this.name == null && anotherType.name != null || this.name != null && anotherType.name == null) {
            return false;
        } else if (ObjectUtils.allNotNull(this.name, anotherType.name)
                && !StringUtils.equalsIgnoreCase(this.name, anotherType.name)) {
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
