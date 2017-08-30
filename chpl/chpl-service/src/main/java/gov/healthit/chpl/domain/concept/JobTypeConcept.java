package gov.healthit.chpl.domain.concept;

public enum JobTypeConcept {
	MUU_UPLOAD("MUU Upload");
	
	private String name;
	private JobTypeConcept(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
}
