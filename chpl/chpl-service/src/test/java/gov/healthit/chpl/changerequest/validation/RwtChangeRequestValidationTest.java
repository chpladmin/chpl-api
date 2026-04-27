package gov.healthit.chpl.changerequest.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import gov.healthit.chpl.certifiedproduct.CertifiedProductDetailsManager;
import gov.healthit.chpl.changerequest.domain.ChangeRequest;
import gov.healthit.chpl.changerequest.domain.ChangeRequestListingUrl;
import gov.healthit.chpl.changerequest.domain.ChangeRequestStatus;
import gov.healthit.chpl.changerequest.domain.ChangeRequestStatusType;
import gov.healthit.chpl.changerequest.domain.ChangeRequestType;
import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.permissions.ResourcePermissionsFactory;
import gov.healthit.chpl.util.ErrorMessageUtil;

public class RwtChangeRequestValidationTest {
    private static final String URL_MISSING = "The Real World Testing URL may not be blank for %s.";

    @Test
    public void validateValidRwtChangeRequest_returnsTrue() throws EntityRetrievalException {
        ChangeRequestValidationContext context = getValidationContext("http://www.abc.com");
        CertifiedProductDetailsManager cpdManager = Mockito.mock(CertifiedProductDetailsManager.class);
        Mockito.when(cpdManager.getCertifiedProductDetails(ArgumentMatchers.anyLong()))
            .thenReturn(getListing("http://www.def.com"));
        context.setCpdManager(cpdManager);
        RwtChangeRequestValidation rwtValidator = new RwtChangeRequestValidation();

        boolean result = rwtValidator.isValid(context);
        assertTrue(result);
        assertEquals(0, rwtValidator.getMessages().size());
    }

    @Test
    public void validateRwtChangeRequestWithNullDetails_returnsFalse() throws EntityRetrievalException {
        ChangeRequestValidationContext context = getValidationContext("http://www.abc.com");
        context.getNewChangeRequest().setDetails(null);
        CertifiedProductDetailsManager cpdManager = Mockito.mock(CertifiedProductDetailsManager.class);
        Mockito.when(cpdManager.getCertifiedProductDetails(ArgumentMatchers.anyLong()))
            .thenReturn(getListing("http://www.def.com"));
        context.setCpdManager(cpdManager);
        RwtChangeRequestValidation rwtValidator = new RwtChangeRequestValidation();

        boolean result = rwtValidator.isValid(context);
        assertFalse(result);
        assertEquals(1, rwtValidator.getMessages().size());
    }

    @Test
    public void validateRwtChangeRequestWithNullUrl_returnsFalse() throws EntityRetrievalException {
        ChangeRequestValidationContext context = getValidationContext("http://www.abc.com");
        ChangeRequestListingUrl details = (ChangeRequestListingUrl) context.getNewChangeRequest().getDetails();
        details.setUrl(null);
        CertifiedProductDetailsManager cpdManager = Mockito.mock(CertifiedProductDetailsManager.class);
        Mockito.when(cpdManager.getCertifiedProductDetails(ArgumentMatchers.anyLong()))
            .thenReturn(getListing("http://www.def.com"));
        context.setCpdManager(cpdManager);
        RwtChangeRequestValidation rwtValidator = new RwtChangeRequestValidation();

        boolean result = rwtValidator.isValid(context);
        assertFalse(result);
        assertEquals(1, rwtValidator.getMessages().size());
    }

    @Test
    public void validateRwtChangeRequestWithNullListing_returnsFalse() throws EntityRetrievalException {
        ChangeRequestValidationContext context = getValidationContext("http://www.abc.com");
        ChangeRequestListingUrl details = (ChangeRequestListingUrl) context.getNewChangeRequest().getDetails();
        CertifiedProductDetailsManager cpdManager = Mockito.mock(CertifiedProductDetailsManager.class);
        Mockito.when(cpdManager.getCertifiedProductDetails(ArgumentMatchers.anyLong()))
            .thenReturn(getListing("http://www.def.com"));
        context.setCpdManager(cpdManager);
        details.setListing(null);
        RwtChangeRequestValidation rwtValidator = new RwtChangeRequestValidation();

        boolean result = rwtValidator.isValid(context);
        assertFalse(result);
        assertEquals(1, rwtValidator.getMessages().size());
    }

    private CertifiedProductSearchDetails getListing(String rwtUrl) {
        return CertifiedProductSearchDetails.builder()
            .id(1L)
            .chplProductNumber("15.02.05.1026.ASPM.01.01.0.220203")
            .rwtPlansUrl(rwtUrl)
            .rwtResultsUrl(rwtUrl)
            .build();
    }

    private ChangeRequest getChangeRequest(String rwt) {
        return ChangeRequest.builder()
                .id(1L)
                .developer(Developer.builder()
                        .id(Long.valueOf(20L))
                        .developerCode("1234")
                        .name("Dev 1")
                        .build())
                .changeRequestType(ChangeRequestType.builder()
                        .id(3L)
                        .name("Service Base URL List Change Request")
                        .build())
                .currentStatus(ChangeRequestStatus.builder()
                        .id(Long.valueOf(8L))
                        .comment("Comment")
                        .changeRequestStatusType(ChangeRequestStatusType.builder()
                                .id(1L)
                                .name("Pending ONC-ACB Action")
                                .build())
                        .build())
                .certificationBody(CertificationBody.builder()
                        .id(1L)
                        .acbCode("1234")
                        .name("ACB 1234")
                        .build())
                .details(buildChangeRequestDetails(rwt))
                .build();
    }

    private ChangeRequestListingUrl buildChangeRequestDetails(String rwt) {
        return ChangeRequestListingUrl.builder()
                .listing(getListingWithRwt(rwt))
                .url(rwt)
                .build();
    }

    private CertifiedProductSearchDetails getListingWithRwt(String rwt) {
        return CertifiedProductSearchDetails.builder()
            .id(1L)
            .rwtPlansUrl(rwt)
            .build();
    }

    private ChangeRequestValidationContext getValidationContext(String rwt) {
        ResourcePermissions resourcePermissions = Mockito.mock(ResourcePermissions.class);
        Mockito.when(resourcePermissions.isUserRoleDeveloperAdmin()).thenReturn(true);
        ResourcePermissionsFactory resourcePermissionsFactory = Mockito.mock(ResourcePermissionsFactory.class);
        Mockito.when(resourcePermissionsFactory.get()).thenReturn(resourcePermissions);

        ErrorMessageUtil msgUtil = Mockito.mock(ErrorMessageUtil.class);
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("changeRequest.listingUrl.rwtUrl.missing"),
                ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(URL_MISSING, i.getArgument(1), ""));

        return new ChangeRequestValidationContext(null,
                        getChangeRequest(rwt),
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        resourcePermissionsFactory,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        msgUtil,
                        null,
                        null,
                        null,
                        null,
                        null);
    }

}
