package gov.healthit.chpl.domain;

import gov.healthit.chpl.dto.ChartDataStatTypeDTO;
import gov.healthit.chpl.entity.ChartDataStatTypeEntity;

import java.io.Serializable;
import java.util.Date;

public class ChartDataStatType implements Serializable{
	private Long id;
	private String dataType;
	private Date lastModifiedDate;
	private Long lastModifiedUser;
	
	public ChartDataStatType(ChartDataStatTypeDTO dto){
		this.id = dto.getId();
		this.dataType = dto.getDataType();
		this.lastModifiedDate = dto.getLastModifiedDate();
		this.lastModifiedUser = dto.getLastModifiedUser();
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
