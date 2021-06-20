package gov.healthit.chpl.search;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.manager.DimensionalDataManager;
import gov.healthit.chpl.util.ErrorMessageUtil;

public class SearchRequestValidatorTest {
    private ErrorMessageUtil msgUtil;
    private DimensionalDataManager dimensionalDataManager;
    private SearchRequestValidator validator;

    @Before
    public void setup() {
        msgUtil = Mockito.mock(ErrorMessageUtil.class);
        dimensionalDataManager = Mockito.mock(DimensionalDataManager.class);
        //TODO: set up mocks
        validator = new SearchRequestValidator(dimensionalDataManager, msgUtil);
    }

    @Test(expected = ValidationException.class)
    public void validate_invalidCertificationStatus_throwsException() {
        //TODO:
    }

    @Test
    public void validate_validCertificationStatus_noException() {
        //TODO:
    }

    //TODO: more tests
}
