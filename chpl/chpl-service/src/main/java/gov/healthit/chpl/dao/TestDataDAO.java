package gov.healthit.chpl.dao;

import java.util.List;

import gov.healthit.chpl.dto.TestDataCriteriaMapDTO;
import gov.healthit.chpl.dto.TestDataDTO;

public interface TestDataDAO {

    public List<TestDataCriteriaMapDTO> findAllWithMappedCriteria();
    public TestDataDTO getByCriteriaNumberAndValue(String criteriaNumber, String value);
    public List<TestDataDTO> getByCriteriaNumber(String criteriaNumber);
}
