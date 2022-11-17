package gov.healthit.chpl.ucdProcess;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

/**
 * The user-centered design (UCD) process applied for the corresponding
 * certification criteria
 *
 */
@XmlType(namespace = "http://chpl.healthit.gov/listings")
@XmlAccessorType(XmlAccessType.FIELD)
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UcdProcess implements Serializable {
    private static final long serialVersionUID = 7248865611086710891L;

    /**
     * UCD process to certification result internal mapping ID
     */
    @XmlElement(required = true)
    private Long id;

    /**
     * The user-centered design (UCD) process applied for the corresponding
     * certification criteria
     */
    @XmlElement(required = true)
    private String name;

    @Override
    public boolean equals(Object other) {
        if (other == null) {
            return false;
        }
        if (!(other instanceof UcdProcess)) {
            return false;
        }
        UcdProcess otherUcdProcess = (UcdProcess) other;
        if (this.getId() == null) {
            if (otherUcdProcess.getId() != null) {
                return false;
            }
        } else if (!this.getId().equals(otherUcdProcess.getId())) {
            return false;
        }

        if (StringUtils.isEmpty(this.getName())) {
            if (!StringUtils.isEmpty(otherUcdProcess.getName())) {
                return false;
            }
        } else if (!this.getName().equals(otherUcdProcess.getName())) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
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

    public void setName(String ucdProcessName) {
        this.name = ucdProcessName;
    }
}
