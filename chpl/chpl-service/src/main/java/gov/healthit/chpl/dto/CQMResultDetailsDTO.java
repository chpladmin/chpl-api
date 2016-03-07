package gov.healthit.chpl.dto;

import java.util.ArrayList;
import java.util.List;

import gov.healthit.chpl.domain.CQMResultCriteria;
import gov.healthit.chpl.entity.CQMResultDetailsEntity;

public class CQMResultDetailsDTO {

	private Long id;
	private Boolean success;
	private Long cqmCriterionId;
	private String number;
	private String cmsId;
	private String title;
	private String nqfNumber;
	private Long cqmCriterionTypeId;
	private String domain;
	private Long cqmVersionId;
	private String version;
	
	private List<CQMResultCriteriaDTO> criteria;
	
	public CQMResultDetailsDTO(){
		criteria = new ArrayList<CQMResultCriteriaDTO>();
	}
	
	public CQMResultDetailsDTO(CQMResultDetailsEntity entity){
		this();
		this.id = entity.getId();
		this.success = entity.getSuccess();
		this.cqmCriterionId = entity.getCqmCriterionId();
		this.number = entity.getNumber();
		this.cmsId = entity.getCmsId();
		this.title = entity.getTitle();
		this.nqfNumber = entity.getNqfNumber();
		this.cqmCriterionTypeId = entity.getCqmCriterionTypeId();
		this.domain = entity.getDomain();
		this.cqmVersionId = entity.getCqmVersionId();
		this.version = entity.getVersion();
		
	}
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Boolean getSuccess() {
		return success;
	}
	public void setSuccess(Boolean success) {
		this.success = success;
	}
	public Long getCqmCriterionId() {
		return cqmCriterionId;
	}
	public void setCqmCriterionId(Long cqmCriterionId) {
		this.cqmCriterionId = cqmCriterionId;
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
	public Long getCqmCriterionTypeId() {
		return cqmCriterionTypeId;
	}
	public void setCqmCriterionTypeId(Long cqmCriterionTypeId) {
		this.cqmCriterionTypeId = cqmCriterionTypeId;
	}
	public Long getCqmVersionId() {
		return cqmVersionId;
	}
	public void setCqmVersionId(Long cqmVersionId) {
		this.cqmVersionId = cqmVersionId;
	}
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}

	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

	public List<CQMResultCriteriaDTO> getCriteria() {
		return criteria;
	}

	public void setCriteria(List<CQMResultCriteriaDTO> criteria) {
		this.criteria = criteria;
	}
	
}
