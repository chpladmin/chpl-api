package gov.healthit.chpl.dao;

import java.util.List;

import gov.healthit.chpl.dto.TestProcedureCriteriaMapDTO;
import gov.healthit.chpl.dto.TestProcedureDTO;

public interface TestProcedureDAO {

    public List<TestProcedureCriteriaMapDTO> findAllWithMappedCriteria();
    public TestProcedureDTO getByCriteriaNumberAndValue(String criteriaNumber, String value);
    public List<TestProcedureDTO> getByCriteriaNumber(String criteriaNumber);
}
