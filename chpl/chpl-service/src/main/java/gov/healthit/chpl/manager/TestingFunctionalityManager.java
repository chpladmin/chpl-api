package gov.healthit.chpl.manager;

import java.util.List;

import gov.healthit.chpl.domain.TestFunctionality;

public interface TestingFunctionalityManager {
    List<TestFunctionality> getTestFunctionalities(
            String criteriaNumber, String certificationEdition, Long practiceTypeId);
}
