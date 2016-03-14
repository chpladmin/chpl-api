package gov.healthit.chpl.domain;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.util.StringUtils;

import gov.healthit.chpl.dto.CQMResultCriteriaDTO;
import gov.healthit.chpl.dto.CQMResultDetailsDTO;

public class CQMResultCriteria {
	
	private Long id;
	private Long criteriaId;
	private String criteriaNumber;

	public CQMResultCriteria(){
		
	}
	
	public CQMResultCriteria(CQMResultCriteriaDTO dto){
		this.id = dto.getId();
		this.criteriaId = dto.getCriterionId();
		if(dto.getCriterion() != null) {
			this.criteriaNumber = dto.getCriterion().getNumber();
		}
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getCriteriaId() {
		return criteriaId;
	}

	public void setCriteriaId(Long criteriaId) {
		this.criteriaId = criteriaId;
	}

	public String getCriteriaNumber() {
		return criteriaNumber;
	}

	public void setCriteriaNumber(String criteriaNumber) {
		this.criteriaNumber = criteriaNumber;
	}
}
