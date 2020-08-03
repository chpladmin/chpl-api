package gov.healthit.chpl.domain;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.ff4j.FF4j;

import gov.healthit.chpl.FeatureList;
import gov.healthit.chpl.SpringContext;
import gov.healthit.chpl.dto.TransparencyAttestationDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;

@XmlType(namespace = "http://chpl.healthit.gov/listings")
@XmlAccessorType(XmlAccessType.FIELD)
@Builder
@AllArgsConstructor
public class TransparencyAttestation implements Serializable {
    private static final long serialVersionUID = 4734258569082877872L;

    /**
     * Affirmative, Negative, or N/A
     */
    @XmlElement(required = true)
    private String transparencyAttestation;

    /**
     * Indicates whether this data is still part of the CHPL
     */
    @XmlElement(required = true)
    private Boolean removed;

    public TransparencyAttestation() {
        // This is a temporary bad solution, will be removed when flag is removed
        if (SpringContext.getBean(FF4j.class).check(FeatureList.EFFECTIVE_RULE_DATE)) {
            this.removed = true;
        } else {
            this.removed = false;
        }
    }

    public TransparencyAttestation(String attestation) {
        this();
        this.transparencyAttestation = attestation;
    }

    public TransparencyAttestation(TransparencyAttestationDTO dto) {
        this.transparencyAttestation = dto.getTransparencyAttestation();
        this.removed = dto.getRemoved();
    }

    public String getTransparencyAttestation() {
        return transparencyAttestation;
    }

    public void setTransparencyAttestation(String transparencyAttestation) {
        this.transparencyAttestation = transparencyAttestation;
    }

    public Boolean getRemoved() {
        return removed;
    }

    public void setRemoved(Boolean removed) {
        this.removed = removed;
    }

}
