package gov.healthit.chpl.domain;

public class KeyValueModelStatuses extends KeyValueModel {
	Statuses statuses;
	
	public KeyValueModelStatuses() {}
	public KeyValueModelStatuses(Statuses statuses) {
		this.statuses = statuses;
	}
	
	public KeyValueModelStatuses(Long id, String name, Statuses statuses) {
		super.setId(id);
		super.setName(name);
		this.statuses = statuses;
	}
	
	public Statuses getStatuses(){
		return this.statuses;
	}
	
	public void setStatuses(Statuses statuses){
		this.statuses = statuses;
	}
	
}