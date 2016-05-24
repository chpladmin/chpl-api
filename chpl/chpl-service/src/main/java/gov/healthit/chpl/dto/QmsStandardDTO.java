package gov.healthit.chpl.dto;

import gov.healthit.chpl.entity.QmsStandardEntity;
import gov.healthit.chpl.entity.TestingLabEntity;

import java.util.Date;

public class QmsStandardDTO {
	private Long id;
	private String name;
	
	public QmsStandardDTO(){}
	
	public QmsStandardDTO(QmsStandardEntity entity){
		this.id = entity.getId();
		this.name = entity.getName();
	}
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
}
