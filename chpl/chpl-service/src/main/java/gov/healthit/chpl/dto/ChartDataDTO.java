package gov.healthit.chpl.dto;

import gov.healthit.chpl.domain.ChartDataStatType;
import gov.healthit.chpl.entity.ChartDataEntity;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Column;

public class ChartDataDTO implements Serializable {
	
	private static final long serialVersionUID = 9045053072631026729L;
	private Long id;
	private Long date;
	private String data;
	private ChartDataStatTypeDTO statisticType;
	private Date lastModifiedDate;
	private Long lastModifiedUser;
	
	public ChartDataDTO(ChartDataEntity entity) throws ParseException{
		this.id = entity.getId();
		SimpleDateFormat f = new SimpleDateFormat("yyyy-mm-dd");
		this.date = f.parse(entity.getDataDate().toString()).getTime();
		this.data = entity.getJsonDataObject();
		this.statisticType = new ChartDataStatTypeDTO(entity.getTypeOfStatId());
		this.lastModifiedDate = entity.getLastModifiedDate();
		this.lastModifiedUser = entity.getLastModifiedUser();
	}
	public ChartDataDTO(){}
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Long getDate() {
		return date;
	}
	public void setDate(Long date) {
		this.date = date;
	}
	public String getData() {
		return data;
	}
	public void setData(String data) {
		this.data = data;
	}
	public ChartDataStatTypeDTO getStatisticType() {
		return statisticType;
	}
	public void setStatisticType(ChartDataStatTypeDTO statisticType) {
		this.statisticType = statisticType;
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
