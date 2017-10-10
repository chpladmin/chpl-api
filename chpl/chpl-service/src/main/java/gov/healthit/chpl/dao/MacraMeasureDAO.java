package gov.healthit.chpl.dao;

import java.util.List;

import gov.healthit.chpl.dto.MacraMeasureDTO;

public interface MacraMeasureDAO {
    List<MacraMeasureDTO> findAll();

    MacraMeasureDTO getById(Long id);

    List<MacraMeasureDTO> getByCriteriaNumber(String criteriaNumber);

    MacraMeasureDTO getByCriteriaNumberAndValue(String criteriaNumber, String value);
}
