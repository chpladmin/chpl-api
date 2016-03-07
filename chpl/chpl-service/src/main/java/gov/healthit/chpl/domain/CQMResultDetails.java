package gov.healthit.chpl.domain;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.util.StringUtils;

import gov.healthit.chpl.dto.CQMResultCriteriaDTO;
import gov.healthit.chpl.dto.CQMResultDetailsDTO;

public class CQMResultDetails {
	
	private Long id;
	private String number;
	private String cmsId;
	private String title;
	private String nqfNumber;
	private Long typeId;
	private String domain;
	private Boolean success;
	private Set<String> successVersions;
	private Set<String> allVersions;
	private List<CQMResultCriteria> criteria;
	
	public CQMResultDetails(){
		this.successVersions = new HashSet<String>();
		this.allVersions = new HashSet<String>();
		this.criteria = new ArrayList<CQMResultCriteria>();
	}
	
	public CQMResultDetails(CQMResultDetailsDTO dto){
		this();
		this.id = dto.getId();
		this.number = dto.getNumber();
		this.cmsId = dto.getCmsId();
		this.title = dto.getTitle();
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
				CQMResultCriteria criteria = new CQMResultCriteria();
				criteria.setCriteriaId(criteriaDTO.getCqmResultId());
				criteria.setId(criteriaDTO.getId());
				if(criteriaDTO.getCriterion() != null) {
					criteria.setCriteriaNumber(criteriaDTO.getCriterion().getNumber());
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

	public List<CQMResultCriteria> getCriteria() {
		return criteria;
	}

	public void setCriteria(List<CQMResultCriteria> criteria) {
		this.criteria = criteria;
	}
	
}
