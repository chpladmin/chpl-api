package gov.healthit.chpl.domain.concept;

import java.io.Serializable;

public enum CertificationBodyConcept implements Serializable {
	CERTIFICATION_BODY_INFOGARD(1L, "02", "InfoGard"),
	CERTIFICATION_BODY_CCHIT(2L, "03", "CCHIT"),
	CERTIFICATION_BODY_DRUMMOND_GROUP(3L, "04", "Drummond Group"),
	CERTIFICATION_BODY_SLI_GLOBAL(4L, "05", "SLI Global"),
	CERTIFICATION_BODY_SURESCRIPTS_LLC(5L, "06", "Surescripts LLC"),
	CERTIFICATION_BODY_ICSA_LABS(6L, "07", "ICSA Labs"),
	CERTIFICATION_BODY_PENDING(7L, "08", "Pending");
	
	private final Long id;
	private final String code;
	private final String name;
	
	private CertificationBodyConcept(Long id, String code, String name){
		this.id = id;
		this.code = code;
		this.name = name;
	}

	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getCode() {
		return code;
	}
}
