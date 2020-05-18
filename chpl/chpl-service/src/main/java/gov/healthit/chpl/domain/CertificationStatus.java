package gov.healthit.chpl.domain;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import gov.healthit.chpl.dto.CertificationStatusDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@XmlType(namespace = "http://chpl.healthit.gov/listings")
@XmlAccessorType(XmlAccessType.FIELD)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CertificationStatus implements Serializable {

    private static final long serialVersionUID = 818896721132619130L;

    public CertificationStatus(CertificationStatusDTO dto) {
        this.id = dto.getId();
        this.name = dto.getStatus();
    }

    /**
     * Internal ID
     */
    @XmlElement(required = true)
    private Long id;

    /**
     * Certification status name.
     */
    @XmlElement(required = true)
    private String name;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }
}
