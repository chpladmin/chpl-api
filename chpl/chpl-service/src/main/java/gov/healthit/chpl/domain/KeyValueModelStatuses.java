package gov.healthit.chpl.domain;

import org.springframework.util.StringUtils;

public class KeyValueModelStatuses extends KeyValueModel {
	private Long id;
	private String name;
	private String description;
	Statuses statuses;
	
	public KeyValueModelStatuses() {}
	public KeyValueModelStatuses(Long id, String name, Statuses statuses) {
		this.id = id;
		this.name = name;
		this.statuses = statuses;
	}
	
	public KeyValueModelStatuses(Long id, String name, String description, Statuses statuses) {
		this.id = id;
		this.name = name;
		this.description = description;
		this.statuses = statuses;
	}
	
	public Long getId(){
		return this.id;
	}
	
	public void setId(Long id){
		this.id = id;
	}
	
	public String getName(){
		return this.name;
	}
	
	public void setName(String name){
		this.name = name;
	}
	
	public String getDescription(){
		return this.description;
	}
	
	public void setDescription(String description){
		this.description = description;
	}
	
	public Statuses getStatuses(){
		return this.statuses;
	}
	
	public void setStatuses(Statuses statuses){
		this.statuses = statuses;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof DescriptiveModel))
            return false;
        if (obj == this)
            return true;

        DescriptiveModel rhs = (DescriptiveModel) obj;
        
        if(StringUtils.isEmpty(rhs.getName()) != StringUtils.isEmpty(this.getName())) {
        	return false;
        }
        
        return rhs.getName().equals(this.getName());
	}
	
	@Override 
	public int hashCode() {
		if(StringUtils.isEmpty(this.getName())) {
			return 0;
		}
		return this.getName().hashCode();
	}
}