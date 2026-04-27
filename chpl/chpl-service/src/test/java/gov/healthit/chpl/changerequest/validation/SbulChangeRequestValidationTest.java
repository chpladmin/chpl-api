package gov.healthit.chpl.changerequest.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.certifiedproduct.CertifiedProductDetailsManager;
import gov.healthit.chpl.changerequest.domain.ChangeRequest;
import gov.healthit.chpl.changerequest.domain.ChangeRequestListingUrl;
import gov.healthit.chpl.changerequest.domain.ChangeRequestStatus;
import gov.healthit.chpl.changerequest.domain.ChangeRequestStatusType;
import gov.healthit.chpl.changerequest.domain.ChangeRequestType;
import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.permissions.ResourcePermissionsFactory;
import gov.healthit.chpl.service.CertificationCriterionService;
import gov.healthit.chpl.service.CertificationCriterionService.Criteria2015;
import gov.healthit.chpl.util.ErrorMessageUtil;

public class SbulChangeRequestValidationTest {
    private static final long G10_ID = 1L;

    private static final String NO_G10 = "The criterion 170.315 (g)(10) was not found on %s the Service Base URL List cannot be changed.";
    private static final String NO_URL_CHANGE = "No change to the Service Base URL List was found for %s.";
    private static final String SBUL_MISSING = "The Service Base URL List may not be blank for %s.";

    @Test
    public void validateValidSbulChangeRequest_returnsTrue() throws EntityRetrievalException {
        ChangeRequestValidationContext context = getValidationContext("http://www.abc.com");
        CertifiedProductDetailsManager cpdManager = Mockito.mock(CertifiedProductDetailsManager.class);
        Mockito.when(cpdManager.getCertifiedProductDetails(ArgumentMatchers.anyLong()))
            .thenReturn(getListingWithG10("http://www.def.com"));
        context.setCpdManager(cpdManager);
        CertificationCriterionService criteriaService = Mockito.mock(CertificationCriterionService.class);
        Mockito.when(criteriaService.get(ArgumentMatchers.eq(Criteria2015.G_10)))
            .thenReturn(getG10Criterion());
        context.setCriteriaService(criteriaService);
        SbulChangeRequestValidation sbulValidator = new SbulChangeRequestValidation();

        boolean result = sbulValidator.isValid(context);
        assertTrue(result);
        assertEquals(0, sbulValidator.getMessages().size());
    }

    @Test
    public void validateSbulChangeRequestWithNullDetails_returnsFalse() throws EntityRetrievalException {
        ChangeRequestValidationContext context = getValidationContext("http://www.abc.com");
        CertifiedProductDetailsManager cpdManager = Mockito.mock(CertifiedProductDetailsManager.class);
        Mockito.when(cpdManager.getCertifiedProductDetails(ArgumentMatchers.anyLong()))
            .thenReturn(getListingWithG10("http://www.def.com"));
        context.setCpdManager(cpdManager);
        CertificationCriterionService criteriaService = Mockito.mock(CertificationCriterionService.class);
        Mockito.when(criteriaService.get(ArgumentMatchers.eq(Criteria2015.G_10)))
            .thenReturn(getG10Criterion());
        context.setCriteriaService(criteriaService);
        context.getNewChangeRequest().setDetails(null);
        SbulChangeRequestValidation sbulValidator = new SbulChangeRequestValidation();

        boolean result = sbulValidator.isValid(context);
        assertFalse(result);
        assertEquals(1, sbulValidator.getMessages().size());
    }

    @Test
    public void validateSbulChangeRequestWithNullUrl_returnsFalse() throws EntityRetrievalException {
        ChangeRequestValidationContext context = getValidationContext("http://www.abc.com");
        CertifiedProductDetailsManager cpdManager = Mockito.mock(CertifiedProductDetailsManager.class);
        Mockito.when(cpdManager.getCertifiedProductDetails(ArgumentMatchers.anyLong()))
            .thenReturn(getListingWithG10("http://www.def.com"));
        context.setCpdManager(cpdManager);
        CertificationCriterionService criteriaService = Mockito.mock(CertificationCriterionService.class);
        Mockito.when(criteriaService.get(ArgumentMatchers.eq(Criteria2015.G_10)))
            .thenReturn(getG10Criterion());
        context.setCriteriaService(criteriaService);

        ChangeRequestListingUrl details = (ChangeRequestListingUrl) context.getNewChangeRequest().getDetails();
        details.setUrl(null);
        SbulChangeRequestValidation sbulValidator = new SbulChangeRequestValidation();

        boolean result = sbulValidator.isValid(context);
        assertFalse(result);
        assertEquals(1, sbulValidator.getMessages().size());
    }

    @Test
    public void validateSbulChangeRequestWithNullListing_returnsFalse() throws EntityRetrievalException {
        ChangeRequestValidationContext context = getValidationContext("http://www.abc.com");
        CertifiedProductDetailsManager cpdManager = Mockito.mock(CertifiedProductDetailsManager.class);
        Mockito.when(cpdManager.getCertifiedProductDetails(ArgumentMatchers.anyLong()))
            .thenReturn(getListingWithG10("http://www.def.com"));
        context.setCpdManager(cpdManager);
        CertificationCriterionService criteriaService = Mockito.mock(CertificationCriterionService.class);
        Mockito.when(criteriaService.get(ArgumentMatchers.eq(Criteria2015.G_10)))
            .thenReturn(getG10Criterion());
        context.setCriteriaService(criteriaService);

        ChangeRequestListingUrl details = (ChangeRequestListingUrl) context.getNewChangeRequest().getDetails();
        details.setListing(null);
        SbulChangeRequestValidation sbulValidator = new SbulChangeRequestValidation();

        boolean result = sbulValidator.isValid(context);
        assertFalse(result);
        assertEquals(1, sbulValidator.getMessages().size());
    }

    @Test
    public void validateSbulChangeRequestWithUrlNotChanged_returnsFalse() throws EntityRetrievalException {
        ChangeRequestValidationContext context = getValidationContext("http://www.abc.com");
        CertifiedProductDetailsManager cpdManager = Mockito.mock(CertifiedProductDetailsManager.class);
        Mockito.when(cpdManager.getCertifiedProductDetails(ArgumentMatchers.anyLong()))
            .thenReturn(getListingWithG10("http://www.def.com"));
        context.setCpdManager(cpdManager);
        CertificationCriterionService criteriaService = Mockito.mock(CertificationCriterionService.class);
        Mockito.when(criteriaService.get(ArgumentMatchers.eq(Criteria2015.G_10)))
            .thenReturn(getG10Criterion());
        context.setCriteriaService(criteriaService);

        ChangeRequestListingUrl details = (ChangeRequestListingUrl) context.getNewChangeRequest().getDetails();
        details.setUrl("http://www.def.com");
        SbulChangeRequestValidation sbulValidator = new SbulChangeRequestValidation();

        boolean result = sbulValidator.isValid(context);
        assertFalse(result);
        assertEquals(1, sbulValidator.getMessages().size());
    }

    private ChangeRequest getChangeRequest(String sbul) {
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
                .details(buildChangeRequestDetails(sbul))
                .build();
    }

    private ChangeRequestListingUrl buildChangeRequestDetails(String sbul) {
        return ChangeRequestListingUrl.builder()
                .listing(getListingWithG10(sbul + "test"))
                .url(sbul)
                .build();
    }

    private CertifiedProductSearchDetails getListingWithG10(String sbul) {
        return CertifiedProductSearchDetails.builder()
            .id(1L)
            .chplProductNumber("15.02.05.1026.ASPM.01.01.0.220203")
            .certificationResults(Stream.of(CertificationResult.builder()
                    .id(1L)
                    .criterion(getG10Criterion())
                    .serviceBaseUrlList(sbul)
                    .build())
                    .collect(Collectors.toList()))
            .build();
    }

    private CertificationCriterion getG10Criterion() {
        return CertificationCriterion.builder()
                .id(G10_ID)
                .number("170.315 (g)(10)")
                .build();
    }

    private ChangeRequestValidationContext getValidationContext(String sbul) {
        ResourcePermissions resourcePermissions = Mockito.mock(ResourcePermissions.class);
        Mockito.when(resourcePermissions.isUserRoleDeveloperAdmin()).thenReturn(true);
        ResourcePermissionsFactory resourcePermissionsFactory = Mockito.mock(ResourcePermissionsFactory.class);
        Mockito.when(resourcePermissionsFactory.get()).thenReturn(resourcePermissions);

        ErrorMessageUtil msgUtil = Mockito.mock(ErrorMessageUtil.class);
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("changeRequest.listingUrl.serviceBaseUrlList.noG10"),
                ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(NO_G10, i.getArgument(1), ""));
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("changeRequest.listingUrl.serviceBaseUrlList.sameUrl"),
                ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(NO_URL_CHANGE, i.getArgument(1), ""));
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("changeRequest.listingUrl.serviceBaseUrlList.missing"),
                ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(SBUL_MISSING, i.getArgument(1), ""));

        return new ChangeRequestValidationContext(null,
                        getChangeRequest(sbul),
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
