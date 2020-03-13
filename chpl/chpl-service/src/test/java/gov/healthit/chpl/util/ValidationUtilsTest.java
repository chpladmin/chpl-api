package gov.healthit.chpl.util;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import gov.healthit.chpl.dto.listing.pending.PendingCertifiedProductDTO;

public class ValidationUtilsTest {

    private ErrorMessageUtil errorMessageUtil;

    @Before
    public void before() {

    }

    @Test
    public void checkSubordinateCriteriaAllRequired_AllRequiredCriteriaAttestedTo_NoMessages() {
        errorMessageUtil = Mockito.mock(ErrorMessageUtil.class);
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.anyString(), ArgumentMatchers.anyString(),
                ArgumentMatchers.anyString()))
                .thenAnswer(i -> String.format("Attesting to Criteria %s requires that Criteria %s must also be attested to.",
                        i.getArgument(1), i.getArgument(2)));
        
        PendingCertifiedProductDTO listing = PendingCertifiedProductDTO.builder()

    }

    @Test
    public void checkSubordinateCriteriaAllRequired_NoCriteriaAttestedTo_NoMessages() {
        errorMessageUtil = Mockito.mock(ErrorMessageUtil.class);
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.anyString(), ArgumentMatchers.anyString(),
                ArgumentMatchers.anyString()))
                .thenAnswer(i -> String.format("Attesting to Criteria %s requires that Criteria %s must also be attested to.",
                        i.getArgument(1), i.getArgument(2)));

    }

    @Test
    public void checkSubordinateCriteriaAllRequired_MissingRequiredCriteriaAttestedTo_ErrorMessagesExist() {
        errorMessageUtil = Mockito.mock(ErrorMessageUtil.class);
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.anyString(), ArgumentMatchers.anyString(),
                ArgumentMatchers.anyString()))
                .thenAnswer(i -> String.format("Attesting to Criteria %s requires that Criteria %s must also be attested to.",
                        i.getArgument(1), i.getArgument(2)));

    }
}
