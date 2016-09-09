package gov.healthit.chpl.domain;

public class Statuses {
	private Integer active;
	private Integer retired;
	private Integer withdrawn;
	private Integer terminated;
	private Integer suspended;
	
	public Statuses(){}
	
	public Statuses(Integer active, Integer retired, Integer withdrawn, Integer terminated, Integer suspended){
		this.active = active;
		this.retired = retired;
		this.withdrawn = withdrawn;
		this.terminated = terminated;
		this.suspended = suspended;
	}
	
	public Integer getActive(){
		return this.active;
	}
	
	public void setActive(Integer active){
		this.active = active;
	}
	
	public Integer getRetired(){
		return this.retired;
	}
	
	public void setRetired(Integer retired){
		this.retired = retired;
	}
	
	public Integer getWithdrawn(){
		return this.withdrawn;
	}
	
	public void setWithdrawn(Integer withdrawn){
		this.withdrawn = withdrawn;
	}
	
	public Integer getTerminated(){
		return this.terminated;
	}
	
	public void setTerminated(Integer terminated){
		this.terminated = terminated;
	}
	
	public Integer getSuspended(){
		return this.suspended;
	}
	
	public void setSuspended(Integer suspended){
		this.suspended = suspended;
	}
}