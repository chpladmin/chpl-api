package gov.healthit.chpl.domain.compliance;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@XmlType(namespace = "http://chpl.healthit.gov/listings")
@XmlAccessorType(XmlAccessType.FIELD)
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class DeveloperAssociatedListing implements Serializable {
    private static final long serialVersionUID = 5321764022740308740L;

    @XmlElement(required = false, nillable = true)
    private Long id;

    @XmlElement(required = false, nillable = true)
    private String chplProductNumber;
}
