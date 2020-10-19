package gov.healthit.chpl.domain;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang3.ObjectUtils;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@XmlType(namespace = "http://chpl.healthit.gov/listings")
@XmlAccessorType(XmlAccessType.FIELD)
@JsonIgnoreProperties(ignoreUnknown = true)
public class MipsMeasureDomain implements Serializable {
    private static final long serialVersionUID = -745253265541448011L;
    private Long id;
    private String name;

    public MipsMeasureDomain() {
    }

    public boolean matches(MipsMeasureDomain anotherDomain) {
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
