package gov.healthit.chpl.domain;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import gov.healthit.chpl.dto.CertificationResultTestFunctionalityDTO;


@XmlType(namespace = "http://chpl.healthit.gov/listings")
@XmlAccessorType(XmlAccessType.FIELD)
public class CertificationResultTestFunctionality implements Serializable {
	private static final long serialVersionUID = -1647645050538126758L;
	
	@XmlElement(required = true)
	private Long id;
	
	@XmlElement(required = true)
	private Long testFunctionalityId;
	
	@XmlElement(required = false, nillable=true)
	private String description;
	
	@XmlElement(required = true)
	private String name;
	
	@XmlElement(required = true)
	private String year;
	
	public CertificationResultTestFunctionality() {
		super();
	}
	
	public CertificationResultTestFunctionality(CertificationResultTestFunctionalityDTO dto) {
		this.id = dto.getId();
		this.testFunctionalityId = dto.getTestFunctionalityId();
		this.description = dto.getTestFunctionalityName();
		this.name = dto.getTestFunctionalityNumber();
		this.year = dto.getTestFunctionalityEdition();
	}
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getTestFunctionalityId() {
		return testFunctionalityId;
	}

	public void setTestFunctionalityId(Long testFunctionalityId) {
		this.testFunctionalityId = testFunctionalityId;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String name) {
		this.description = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getYear() {
		return year;
	}

	public void setYear(String year) {
		this.year = year;
	}

}
