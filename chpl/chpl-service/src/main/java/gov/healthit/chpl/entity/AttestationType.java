package gov.healthit.chpl.entity;

import org.springframework.util.StringUtils;

public enum AttestationType {
	Affirmative,
	Negative,
	NA("N/A");
	
	private String name;
	private AttestationType() {
		
	}
	
	private AttestationType(String name) {
		this.name= name;
	}
	
	@Override
	public String toString() {
		if(!StringUtils.isEmpty(this.name)) {
			return this.name;
		} 
		return name();
	}
	
	public static AttestationType getValue(String value) {
		if(value == null) {
			return null;
		}
		
		AttestationType result = null;
		AttestationType[] values = AttestationType.values();
		for(int i = 0; i < values.length && result == null; i++) {
			if(value.equalsIgnoreCase(values[i].toString())) {
				result = values[i];
			}
		}
		return result;
	}
}
