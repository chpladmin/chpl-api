package gov.healthit.chpl.domain;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(namespace = "http://chpl.healthit.gov/listings")
@XmlAccessorType(XmlAccessType.FIELD)
public class TransparencyAttestationMap implements Serializable {
    private static final long serialVersionUID = 584097086020777727L;

    /**
     * Certification body internal ID
     */
    @XmlElement(required = true)
    private Long acbId;

    /**
     * Certification body name
     */
    @XmlElement(required = false, nillable = true)
    private String acbName;

    /**
     * Affirmative, Negative, or N/A
     */
    @XmlElement(required = true)
    private String attestation;

    public TransparencyAttestationMap() {
    }

    public Long getAcbId() {
        return acbId;
    }

    public void setAcbId(final Long acbId) {
        this.acbId = acbId;
    }

    public String getAcbName() {
        return acbName;
    }

    public void setAcbName(final String acbName) {
        this.acbName = acbName;
    }

    public String getAttestation() {
        return attestation;
    }

    public void setAttestation(final String attestation) {
        this.attestation = attestation;
    }

}
