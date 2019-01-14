package gov.healthit.chpl.dao;

import java.util.List;

import gov.healthit.chpl.dto.TestProcedureCriteriaMapDTO;
import gov.healthit.chpl.dto.TestProcedureDTO;

/**
 * Interface for database access to test procedures.
 * @author kekey
 *
 */
public interface TestProcedureDAO {

    List<TestProcedureCriteriaMapDTO> findAllWithMappedCriteria();
    TestProcedureDTO getByCriteriaNumberAndValue(String criteriaNumber, String value);
    List<TestProcedureDTO> getByCriteriaNumber(String criteriaNumber);
}
