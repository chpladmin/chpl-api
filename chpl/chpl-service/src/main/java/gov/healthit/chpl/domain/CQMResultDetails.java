package gov.healthit.chpl.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlType;

import org.springframework.util.StringUtils;

import gov.healthit.chpl.dto.CQMResultCriteriaDTO;
import gov.healthit.chpl.dto.CQMResultDetailsDTO;

@XmlType(namespace = "http://chpl.healthit.gov/listings")
@XmlAccessorType(XmlAccessType.FIELD)
public class CQMResultDetails implements Serializable {
	private static final long serialVersionUID = -7077008682408284325L;
	
	@XmlElement(required = false, nillable=true)
	private Long id;
	
	@XmlElement(required = false, nillable=true)
	private String number;
	
	@XmlElement(required = false, nillable=true)
	private String cmsId;
	
	@XmlElement(required = false, nillable=true)
	private String title;
	
	@XmlElement(required = false, nillable=true)
	private String description;
	
	@XmlElement(required = false, nillable=true)
	private String nqfNumber;
	
	@XmlElement(required = false, nillable=true)
	private Long typeId;
	
	@XmlElement(required = false, nillable=true)
	private String domain;
	
	@XmlElement(required = false, nillable=true)
	private Boolean success;
	
	@XmlElement(required = false, nillable=true)
	private Set<String> successVersions;
	
	@XmlElement(required = false, nillable=true)
	private Set<String> allVersions;
	
	@XmlElementWrapper(name = "criteriaList", nillable = true, required = false)
	@XmlElement(name = "criteria")
	private List<CQMResultCertification> criteria;
	
	public CQMResultDetails(){
		this.successVersions = new HashSet<String>();
		this.allVersions = new HashSet<String>();
		this.criteria = new ArrayList<CQMResultCertification>();
	}
	
	public CQMResultDetails(CQMResultDetailsDTO dto){
		this();
		this.id = dto.getId();
		this.number = dto.getNumber();
		this.cmsId = dto.getCmsId();
		this.title = dto.getTitle();
		this.description = dto.getDescription();
		this.nqfNumber = dto.getNqfNumber();
		this.typeId = dto.getCqmCriterionTypeId();
		this.domain = dto.getDomain();
		
		if(!StringUtils.isEmpty(dto.getCmsId())) {
			this.getSuccessVersions().add(dto.getVersion());
		} else if(!StringUtils.isEmpty(dto.getNqfNumber())) {
			this.setSuccess(dto.getSuccess());
		}
		
		if(dto.getCriteria() != null && dto.getCriteria().size() > 0) {
			for(CQMResultCriteriaDTO criteriaDTO : dto.getCriteria()) {
				CQMResultCertification criteria = new CQMResultCertification();
				criteria.setCertificationId(criteriaDTO.getCqmResultId());
				criteria.setId(criteriaDTO.getId());
				if(criteriaDTO.getCriterion() != null) {
					criteria.setCertificationNumber(criteriaDTO.getCriterion().getNumber());
				}
				this.criteria.add(criteria);
			}
		}
	}
	
	public String getNumber() {
		return number;
	}
	public void setNumber(String number) {
		this.number = number;
	}
	public String getCmsId() {
		return cmsId;
	}
	public void setCmsId(String cmsId) {
		this.cmsId = cmsId;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getNqfNumber() {
		return nqfNumber;
	}
	public void setNqfNumber(String nqfNumber) {
		this.nqfNumber = nqfNumber;
	}

	public Long getTypeId() {
		return typeId;
	}

	public void setTypeId(Long typeId) {
		this.typeId = typeId;
	}

	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

	public Set<String> getSuccessVersions() {
		return successVersions;
	}

	public void setSuccessVersions(Set<String> successVersions) {
		this.successVersions = successVersions;
	}

	public Set<String> getAllVersions() {
		return allVersions;
	}

	public void setAllVersions(Set<String> allVersions) {
		this.allVersions = allVersions;
	}

	public Boolean isSuccess() {
		if(successVersions != null && successVersions.size() > 0) {
			return true;
		}
		return success;
	}

	public void setSuccess(Boolean success) {
		this.success = success;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public List<CQMResultCertification> getCriteria() {
		return criteria;
	}

	public void setCriteria(List<CQMResultCertification> criteria) {
		this.criteria = criteria;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
}
