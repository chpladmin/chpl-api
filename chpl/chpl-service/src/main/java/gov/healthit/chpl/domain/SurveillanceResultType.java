package gov.healthit.chpl.domain;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(namespace = "http://chpl.healthit.gov/listings")
@XmlAccessorType(XmlAccessType.FIELD)
public class SurveillanceResultType implements Serializable {
	private static final long serialVersionUID = 120064764043803388L;

	/**
	 * Surveillance result type internal ID
	 */
	@XmlElement(required = true)
	private Long id;

	/**
	 * Surveillance result type name. Nonconformity or No Nonconformity
	 */
	@XmlElement(required = true)
	private String name;

	public SurveillanceResultType() {}

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
