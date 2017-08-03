package gov.healthit.chpl.domain;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(namespace = "http://chpl.healthit.gov/listings")
@XmlAccessorType(XmlAccessType.FIELD)
public class SurveillanceRequirementType implements Serializable {
	private static final long serialVersionUID = -5865384642096284604L;
	
	/**
	 * Surveillance requirement type internal ID
	 */
	@XmlElement(required = true)
	private Long id;
	
	/**
	 * Surveillance requirement type name
	 */
	@XmlElement(required = true)
	private String name;
	
	public SurveillanceRequirementType() {}

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
