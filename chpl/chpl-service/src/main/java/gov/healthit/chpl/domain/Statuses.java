package gov.healthit.chpl.domain;

import java.io.Serializable;

public class Statuses implements Serializable {
	private static final long serialVersionUID = -7091471050071142764L;
	private Integer active;
	private Integer retired;
	private Integer withdrawnByDeveloper;
	private Integer withdrawnByAcb;
	private Integer suspendedByAcb;
	
	public Statuses(){}
	
	public Statuses(Integer active, Integer retired, Integer withdrawnByDeveloper, Integer withdrawnByAcb, Integer suspendedByAcb){
		this.active = active;
		this.retired = retired;
		this.withdrawnByDeveloper = withdrawnByDeveloper;
		this.withdrawnByAcb = withdrawnByAcb;
		this.suspendedByAcb = suspendedByAcb;
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
	
	public Integer getWithdrawnByDeveloper(){
		return this.withdrawnByDeveloper;
	}
	
	public void setWithdrawnByDeveloper(Integer withdrawnByDeveloper){
		this.withdrawnByDeveloper = withdrawnByDeveloper;
	}
	
	public Integer getWithdrawnByAcb(){
		return this.withdrawnByAcb;
	}
	
	public void setWithdrawnByAcb(Integer withdrawnByAcb){
		this.withdrawnByAcb = withdrawnByAcb;
	}
	
	public Integer getSuspendedByAcb(){
		return this.suspendedByAcb;
	}
	
	public void setSuspendedByAcb(Integer suspendedByAcb){
		this.suspendedByAcb = suspendedByAcb;
	}
}