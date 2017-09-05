package gov.healthit.chpl.dto;

import gov.healthit.chpl.entity.ChartDataStatTypeEntity;

import java.io.Serializable;
import java.util.Date;

public class ChartDataStatTypeDTO implements Serializable{
	
	private static final long serialVersionUID = -4060995213299317291L;
	private Long id;
	private String dataType;
	private Date lastModifiedDate;
	private Long lastModifiedUser;
	
	public ChartDataStatTypeDTO(ChartDataStatTypeEntity entity){
		this.id = entity.getId();
		this.dataType = entity.getDataType();
		this.lastModifiedDate = entity.getLastModifiedDate();
		this.lastModifiedUser = entity.getLastModifiedUser();
	}
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getDataType() {
		return dataType;
	}
	public void setDataType(String dataType) {
		this.dataType = dataType;
	}
	public Date getLastModifiedDate() {
		return lastModifiedDate;
	}
	public void setLastModifiedDate(Date lastModifiedDate) {
		this.lastModifiedDate = lastModifiedDate;
	}
	public Long getLastModifiedUser() {
		return lastModifiedUser;
	}
	public void setLastModifiedUser(Long lastModifiedUser) {
		this.lastModifiedUser = lastModifiedUser;
	}
}
