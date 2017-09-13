package gov.healthit.chpl.domain;

import java.io.Serializable;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

public class CertificationStatus implements Serializable{
	
	private static final long serialVersionUID = 818896721132619130L;
	
	private String name;
	
	public CertificationStatus(String name){
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	

}
