package gov.healthit.chpl.domain;

import java.io.Serializable;

public class TransparencyAttestationMap implements Serializable {
	private static final long serialVersionUID = 584097086020777727L;
	private Long acbId;
	private String acbName;
	private String attestation;
	
	public TransparencyAttestationMap() {}

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

	public String getAttestation() {
		return attestation;
	}

	public void setAttestation(String attestation) {
		this.attestation = attestation;
	}
	
}
