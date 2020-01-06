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

    @XmlElement(required = true)
    private Long acbId;

    @XmlElement(required = false, nillable = true)
    private String acbName;

    @XmlElement(required = true)
    private TransparencyAttestation attestation;

    public TransparencyAttestationMap() {
    }

    public Long getAcbId() {
        return acbId;
    }

    public void setAcbId(Long acbId) {
        this.acbId = acbId;
    }

    public String getAcbName() {
        return acbName;
    }

    public void setAcbName(String acbName) {
        this.acbName = acbName;
    }

    public TransparencyAttestation getAttestation() {
        return attestation;
    }

    public void setAttestation(TransparencyAttestation attestation) {
        this.attestation = attestation;
    }

}
