package gov.healthit.chpl.domain.concept;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@XmlType(namespace = "http://chpl.healthit.gov/listings")
@XmlAccessorType(XmlAccessType.FIELD)
@JsonIgnoreProperties(ignoreUnknown = true)
public enum PublicAttestationStatus implements Serializable {
    ATTESTATIONS_SUBMITTED("Attestations Submitted");

    private String name;

    PublicAttestationStatus(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
