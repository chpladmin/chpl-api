package gov.healthit.chpl.domain;

import gov.healthit.chpl.dto.ChartDataStatTypeDTO;
import java.io.Serializable;

public class ChartDataStatType implements Serializable{
	private static final long serialVersionUID = -259958508932717179L;
	private Long id;
	private String dataType;
	
	public ChartDataStatType(ChartDataStatTypeDTO dto){
		this.id = dto.getId();
		this.dataType = dto.getDataType();
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
}
