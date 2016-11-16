package gov.healthit.chpl.domain;

public enum RequirementType {
	K1("170.523 (k)(1)"),
	K2("170.523 (k)(2)"),
	L("170.523 (l)");
	
	private String name;
	private RequirementType(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
}
