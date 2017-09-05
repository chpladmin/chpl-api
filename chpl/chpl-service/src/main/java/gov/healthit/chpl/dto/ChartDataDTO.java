package gov.healthit.chpl.dto;

import gov.healthit.chpl.entity.ChartDataEntity;

import java.io.Serializable;
import java.util.Date;

public class ChartDataDTO implements Serializable {
	
	private static final long serialVersionUID = 9045053072631026729L;
	private Long id;
	private Date dataDate;
	private String jsonDataObject;
	private Long typeOfStatId;
	private Date lastModifiedDate;
	private Long lastModifiedUser;
	
	public ChartDataDTO(ChartDataEntity entity){
		this.id = entity.getId();
		this.dataDate = entity.getDataDate();
		this.setJsonDataObject(entity.getJsonDataObject());
		this.typeOfStatId = entity.getTypeOfStatId();
		this.lastModifiedDate = entity.getLastModifiedDate();
		this.lastModifiedUser = entity.getLastModifiedUser();
	}
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Date getDataDate() {
		return dataDate;
	}
	public void setDataDate(Date dataDate) {
		this.dataDate = dataDate;
	}
	public Long getTypeOfStatId() {
		return typeOfStatId;
	}
	public void setTypeOfStatId(Long typeOfStatId) {
		this.typeOfStatId = typeOfStatId;
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

	public String getJsonDataObject() {
		return jsonDataObject;
	}

	public void setJsonDataObject(String jsonDataObject) {
		this.jsonDataObject = jsonDataObject;
	}
}
