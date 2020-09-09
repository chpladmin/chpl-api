package gov.healthit.chpl.domain;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import gov.healthit.chpl.dto.DeveloperStatusDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;

@XmlType(namespace = "http://chpl.healthit.gov/listings")
@XmlAccessorType(XmlAccessType.FIELD)
@Builder
@AllArgsConstructor
public class DeveloperStatus implements Serializable {
    private static final long serialVersionUID = 4646214778954081679L;

    /**
     * Developer status internal id.
     */
    @XmlElement(required = true)
    private Long id;

    /**
     * Developer status name
     */
    @XmlElement(required = true)
    private String status;

    public DeveloperStatus() {
    }

    public DeveloperStatus(DeveloperStatusDTO dto) {
        this.id = dto.getId();
        this.status = dto.getStatusName();
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(final String status) {
        this.status = status;
    }

}
