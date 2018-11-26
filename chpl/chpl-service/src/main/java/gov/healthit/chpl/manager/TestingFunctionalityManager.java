package gov.healthit.chpl.manager;

import java.util.List;
import java.util.Map;

import gov.healthit.chpl.domain.TestFunctionality;
import gov.healthit.chpl.dto.TestFunctionalityDTO;

public interface TestingFunctionalityManager {
    List<TestFunctionality> getTestFunctionalities(
            String criteriaNumber, String certificationEdition, Long practiceTypeId);

    Map<String, List<TestFunctionalityDTO>> getTestFunctionalityCriteriaMap2015();

    Map<String, List<TestFunctionalityDTO>> getTestFunctionalityCriteriaMap2014();
}
