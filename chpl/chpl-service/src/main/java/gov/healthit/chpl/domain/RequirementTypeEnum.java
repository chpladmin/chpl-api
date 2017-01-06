package gov.healthit.chpl.domain;

public enum RequirementTypeEnum {
	K1("170.523 (k)(1)"),
	K2("170.523 (k)(2)"),
	L("170.523 (l)");
	
	private String name;
	private RequirementTypeEnum(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
}
