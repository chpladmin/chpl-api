package gov.healthit.chpl.domain;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import gov.healthit.chpl.dto.MacraMeasureDTO;

@XmlType(namespace = "http://chpl.healthit.gov/listings")
@XmlAccessorType(XmlAccessType.FIELD)
public class MacraMeasure implements Serializable {
	private static final long serialVersionUID = 3070401446291821552L;
	
	@XmlElement(required = false, nillable=true)
	private Long id;
	
	@XmlElement(required = false, nillable=true)
	private CertificationCriterion criteria;;
	
	@XmlElement(required = true)
	private String abbreviation;
	
	@XmlElement(required = false, nillable=true)
	private String name;
	
	@XmlElement(required = false, nillable=true)
	private String description;
	
	public MacraMeasure() {
	}

	public MacraMeasure(MacraMeasureDTO dto) {
		this.id = dto.getId();
		if(dto.getCriteria() != null) {
			this.criteria = new CertificationCriterion(dto.getCriteria());
		} else {
			this.criteria = new CertificationCriterion();
			this.criteria.setId(dto.getCriteriaId());
		}
		this.abbreviation = dto.getValue();
		this.name = dto.getName();
		this.description = dto.getDescription();
	}
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public CertificationCriterion getCriteria() {
		return criteria;
	}

	public void setCriteria(CertificationCriterion criteria) {
		this.criteria = criteria;
	}

	public String getAbbreviation() {
		return abbreviation;
	}

	public void setAbbreviation(String abbreviation) {
		this.abbreviation = abbreviation;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}
