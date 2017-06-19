package gov.healthit.chpl.domain;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(namespace = "http://chpl.healthit.gov/listings")
@XmlAccessorType(XmlAccessType.FIELD)
public class SurveillanceNonconformityStatus implements Serializable {
	private static final long serialVersionUID = -411041849666278903L;
	public static final String OPEN = "Open";
	public static final String CLOSED = "Closed";
	
	/**
	 * Nonconformity status internal ID
	 */
	@XmlElement(required = true)
	private Long id;
	
	/**
	 * Nonconformity status name. Open or Closed.
	 */
	@XmlElement(required = true)
	private String name;
	
	public SurveillanceNonconformityStatus() {}

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
