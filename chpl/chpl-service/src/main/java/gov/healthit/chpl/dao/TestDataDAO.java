package gov.healthit.chpl.dao;

import java.util.List;

import gov.healthit.chpl.dto.TestDataCriteriaMapDTO;
import gov.healthit.chpl.dto.TestDataDTO;

/**
 * Interface for database access to test data.
 * @author kekey
 *
 */
public interface TestDataDAO {

    List<TestDataCriteriaMapDTO> findAllWithMappedCriteria();
    TestDataDTO getByCriteriaNumberAndValue(String criteriaNumber, String value);
    List<TestDataDTO> getByCriteriaNumber(String criteriaNumber);
}
