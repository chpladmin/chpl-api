package gov.healthit.chpl.upload.listing.handler;

import org.junit.Before;
import org.mockito.Mockito;

import gov.healthit.chpl.upload.listing.ListingUploadHandlerUtil;
import gov.healthit.chpl.util.ErrorMessageUtil;

public class SedUploadHandlerTest {

    private SedUploadHandler handler;

    @Before
    public void setup() {
        ErrorMessageUtil msgUtil = Mockito.mock(ErrorMessageUtil.class);
        ListingUploadHandlerUtil handlerUtil = new ListingUploadHandlerUtil(msgUtil);
        handler = new SedUploadHandler(Mockito.mock(CertificationCriterionUploadHandler.class),
                Mockito.mock(TestTaskUploadHandler.class),
                Mockito.mock(TestParticipantsUploadHandler.class),
                Mockito.mock(UcdProcessUploadHandler.class),
                handlerUtil);
    }
}
