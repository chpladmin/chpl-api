package gov.healthit.chpl.dao;

import java.util.List;

import gov.healthit.chpl.dto.MacraMeasureDTO;

public interface MacraMeasureDAO {
	public List<MacraMeasureDTO> findAll();
	public MacraMeasureDTO getById(Long id);
	public List<MacraMeasureDTO> getByCriteriaNumber(String criteriaNumber);
	public MacraMeasureDTO getByCriteriaNumberAndValue(String criteriaNumber, String value);
}
