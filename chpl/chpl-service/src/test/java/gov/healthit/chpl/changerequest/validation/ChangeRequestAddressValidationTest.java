package gov.healthit.chpl.changerequest.validation;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.Test;
import org.mockito.Mockito;

import gov.healthit.chpl.changerequest.domain.ChangeRequest;
import gov.healthit.chpl.changerequest.domain.ChangeRequestDeveloperDemographics;
import gov.healthit.chpl.changerequest.domain.ChangeRequestStatus;
import gov.healthit.chpl.changerequest.domain.ChangeRequestStatusType;
import gov.healthit.chpl.changerequest.domain.ChangeRequestType;
import gov.healthit.chpl.domain.Address;
import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.permissions.ResourcePermissionsFactory;

public class ChangeRequestAddressValidationTest {

    @Test
    public void validateAddress_AllData_ReturnsTrue() throws EntityRetrievalException {
        ResourcePermissions resourcePermissions = Mockito.mock(ResourcePermissions.class);
        Mockito.when(resourcePermissions.isUserRoleDeveloperAdmin()).thenReturn(true);
        ResourcePermissionsFactory resourcePermissionsFactory = Mockito.mock(ResourcePermissionsFactory.class);
        Mockito.when(resourcePermissionsFactory.get()).thenReturn(resourcePermissions);

        Address address = buildAddress(1L, "Street", "Suite 6", "City", "MD", "11111", "USA");

        ChangeRequestValidationContext context = getValidationContext(address, resourcePermissionsFactory);
        DemographicsValidation crAddrValidator = new DemographicsValidation();

        boolean result = crAddrValidator.isValid(context);
        assertTrue(result);
        assertEquals(0, crAddrValidator.getMessages().size());
    }

    @Test
    public void validateAddress_ValidDataNoLine2_ReturnsTrue() throws EntityRetrievalException {
        ResourcePermissions resourcePermissions = Mockito.mock(ResourcePermissions.class);
        Mockito.when(resourcePermissions.isUserRoleDeveloperAdmin()).thenReturn(true);
        ResourcePermissionsFactory resourcePermissionsFactory = Mockito.mock(ResourcePermissionsFactory.class);
        Mockito.when(resourcePermissionsFactory.get()).thenReturn(resourcePermissions);

        Address address = buildAddress(1L, "Street", null, "City", "MD", "11111", "USA");

        ChangeRequestValidationContext context = getValidationContext(address, resourcePermissionsFactory);
        DemographicsValidation crAddrValidator = new DemographicsValidation();

        boolean result = crAddrValidator.isValid(context);
        assertTrue(result);
        assertEquals(0, crAddrValidator.getMessages().size());
    }

    @Test
    public void validateAddress_ValidDataNoCountry_ReturnsTrue() throws EntityRetrievalException {
        ResourcePermissions resourcePermissions = Mockito.mock(ResourcePermissions.class);
        Mockito.when(resourcePermissions.isUserRoleDeveloperAdmin()).thenReturn(true);
        ResourcePermissionsFactory resourcePermissionsFactory = Mockito.mock(ResourcePermissionsFactory.class);
        Mockito.when(resourcePermissionsFactory.get()).thenReturn(resourcePermissions);

        Address address = buildAddress(1L, "Street", "Suite 6", "City", "MD", "11111", null);

        ChangeRequestValidationContext context = getValidationContext(address, resourcePermissionsFactory);
        DemographicsValidation crAddrValidator = new DemographicsValidation();

        boolean result = crAddrValidator.isValid(context);
        assertTrue(result);
        assertEquals(0, crAddrValidator.getMessages().size());
    }

    @Test
    public void validateAddress_MissingLine1_ReturnsFalse() throws EntityRetrievalException {
        ResourcePermissions resourcePermissions = Mockito.mock(ResourcePermissions.class);
        Mockito.when(resourcePermissions.isUserRoleDeveloperAdmin()).thenReturn(true);
        ResourcePermissionsFactory resourcePermissionsFactory = Mockito.mock(ResourcePermissionsFactory.class);
        Mockito.when(resourcePermissionsFactory.get()).thenReturn(resourcePermissions);

        Address address = buildAddress(1L, null, null, "City", "MD", "11111", "USA");

        ChangeRequestValidationContext context = getValidationContext(address, resourcePermissionsFactory);
        DemographicsValidation crAddrValidator = new DemographicsValidation();

        boolean result = crAddrValidator.isValid(context);
        assertFalse(result);
    }

    @Test
    public void validateAddress_EmptyLine1_ReturnsFalse() throws EntityRetrievalException {
        ResourcePermissions resourcePermissions = Mockito.mock(ResourcePermissions.class);
        Mockito.when(resourcePermissions.isUserRoleDeveloperAdmin()).thenReturn(true);
        ResourcePermissionsFactory resourcePermissionsFactory = Mockito.mock(ResourcePermissionsFactory.class);
        Mockito.when(resourcePermissionsFactory.get()).thenReturn(resourcePermissions);

        Address address = buildAddress(1L, "", null, "City", "MD", "11111", "USA");

        ChangeRequestValidationContext context = getValidationContext(address, resourcePermissionsFactory);
        DemographicsValidation crAddrValidator = new DemographicsValidation();

        boolean result = crAddrValidator.isValid(context);
        assertFalse(result);
    }

    @Test
    public void validateAddress_MissingLine1_ReturnsExpectedError() throws EntityRetrievalException {
        ResourcePermissions resourcePermissions = Mockito.mock(ResourcePermissions.class);
        Mockito.when(resourcePermissions.isUserRoleDeveloperAdmin()).thenReturn(true);
        ResourcePermissionsFactory resourcePermissionsFactory = Mockito.mock(ResourcePermissionsFactory.class);
        Mockito.when(resourcePermissionsFactory.get()).thenReturn(resourcePermissions);

        Address address = buildAddress(1L, null, null, "City", "MD", "11111", "USA");

        ChangeRequestValidationContext context = getValidationContext(address, resourcePermissionsFactory);
        DemographicsValidation crAddrValidator = new DemographicsValidation();

        crAddrValidator.isValid(context);

        assertEquals(1, crAddrValidator.getMessages().size());
        assertTrue(crAddrValidator.getMessages().contains("Developer street address is required."));
    }

    @Test
    public void validateAddress_MissingCity_ReturnsFalse() throws EntityRetrievalException {
        ResourcePermissions resourcePermissions = Mockito.mock(ResourcePermissions.class);
        Mockito.when(resourcePermissions.isUserRoleDeveloperAdmin()).thenReturn(true);
        ResourcePermissionsFactory resourcePermissionsFactory = Mockito.mock(ResourcePermissionsFactory.class);
        Mockito.when(resourcePermissionsFactory.get()).thenReturn(resourcePermissions);

        Address address = buildAddress(1L, "Line 1", null, null, "MD", "11111", "USA");

        ChangeRequestValidationContext context = getValidationContext(address, resourcePermissionsFactory);
        DemographicsValidation crAddrValidator = new DemographicsValidation();

        boolean result = crAddrValidator.isValid(context);
        assertFalse(result);
    }

    @Test
    public void validateAddress_EmptyCity_ReturnsFalse() throws EntityRetrievalException {
        ResourcePermissions resourcePermissions = Mockito.mock(ResourcePermissions.class);
        Mockito.when(resourcePermissions.isUserRoleDeveloperAdmin()).thenReturn(true);
        ResourcePermissionsFactory resourcePermissionsFactory = Mockito.mock(ResourcePermissionsFactory.class);
        Mockito.when(resourcePermissionsFactory.get()).thenReturn(resourcePermissions);

        Address address = buildAddress(1L, "Line 1", null, "", "MD", "11111", "USA");

        ChangeRequestValidationContext context = getValidationContext(address, resourcePermissionsFactory);
        DemographicsValidation crAddrValidator = new DemographicsValidation();

        boolean result = crAddrValidator.isValid(context);
        assertFalse(result);
    }

    @Test
    public void validateAddress_MissingCity_ReturnsExpectedError() throws EntityRetrievalException {
        ResourcePermissions resourcePermissions = Mockito.mock(ResourcePermissions.class);
        Mockito.when(resourcePermissions.isUserRoleDeveloperAdmin()).thenReturn(true);
        ResourcePermissionsFactory resourcePermissionsFactory = Mockito.mock(ResourcePermissionsFactory.class);
        Mockito.when(resourcePermissionsFactory.get()).thenReturn(resourcePermissions);

        Address address = buildAddress(1L, "Street", null, null, "MD", "11111", "USA");

        ChangeRequestValidationContext context = getValidationContext(address, resourcePermissionsFactory);
        DemographicsValidation crAddrValidator = new DemographicsValidation();

        crAddrValidator.isValid(context);

        assertEquals(1, crAddrValidator.getMessages().size());
        assertTrue(crAddrValidator.getMessages().contains("Developer city is required."));
    }

    @Test
    public void validateAddress_MissingState_ReturnsFalse() throws EntityRetrievalException {
        ResourcePermissions resourcePermissions = Mockito.mock(ResourcePermissions.class);
        Mockito.when(resourcePermissions.isUserRoleDeveloperAdmin()).thenReturn(true);
        ResourcePermissionsFactory resourcePermissionsFactory = Mockito.mock(ResourcePermissionsFactory.class);
        Mockito.when(resourcePermissionsFactory.get()).thenReturn(resourcePermissions);

        Address address = buildAddress(1L, "Line 1", null, "City", null, "11111", "USA");

        ChangeRequestValidationContext context = getValidationContext(address, resourcePermissionsFactory);
        DemographicsValidation crAddrValidator = new DemographicsValidation();

        boolean result = crAddrValidator.isValid(context);
        assertFalse(result);
    }

    @Test
    public void validateAddress_EmptyState_ReturnsFalse() throws EntityRetrievalException {
        ResourcePermissions resourcePermissions = Mockito.mock(ResourcePermissions.class);
        Mockito.when(resourcePermissions.isUserRoleDeveloperAdmin()).thenReturn(true);
        ResourcePermissionsFactory resourcePermissionsFactory = Mockito.mock(ResourcePermissionsFactory.class);
        Mockito.when(resourcePermissionsFactory.get()).thenReturn(resourcePermissions);

        Address address = buildAddress(1L, "Line 1", null, "City", "", "11111", "USA");

        ChangeRequestValidationContext context = getValidationContext(address, resourcePermissionsFactory);
        DemographicsValidation crAddrValidator = new DemographicsValidation();

        boolean result = crAddrValidator.isValid(context);
        assertFalse(result);
    }

    @Test
    public void validateAddress_MissingState_ReturnsExpectedError() throws EntityRetrievalException {
        ResourcePermissions resourcePermissions = Mockito.mock(ResourcePermissions.class);
        Mockito.when(resourcePermissions.isUserRoleDeveloperAdmin()).thenReturn(true);
        ResourcePermissionsFactory resourcePermissionsFactory = Mockito.mock(ResourcePermissionsFactory.class);
        Mockito.when(resourcePermissionsFactory.get()).thenReturn(resourcePermissions);

        Address address = buildAddress(1L, "Street", null, "City", null, "11111", "USA");

        ChangeRequestValidationContext context = getValidationContext(address, resourcePermissionsFactory);
        DemographicsValidation crAddrValidator = new DemographicsValidation();

        crAddrValidator.isValid(context);

        assertEquals(1, crAddrValidator.getMessages().size());
        assertTrue(crAddrValidator.getMessages().contains("Developer state is required."));
    }

    @Test
    public void validateAddress_MissingZipcode_ReturnsFalse() throws EntityRetrievalException {
        ResourcePermissions resourcePermissions = Mockito.mock(ResourcePermissions.class);
        Mockito.when(resourcePermissions.isUserRoleDeveloperAdmin()).thenReturn(true);
        ResourcePermissionsFactory resourcePermissionsFactory = Mockito.mock(ResourcePermissionsFactory.class);
        Mockito.when(resourcePermissionsFactory.get()).thenReturn(resourcePermissions);

        Address address = buildAddress(1L, "Line 1", null, "City", "MD", null, "USA");

        ChangeRequestValidationContext context = getValidationContext(address, resourcePermissionsFactory);
        DemographicsValidation crAddrValidator = new DemographicsValidation();

        boolean result = crAddrValidator.isValid(context);
        assertFalse(result);
    }

    @Test
    public void validateAddress_EmptyZipcode_ReturnsFalse() throws EntityRetrievalException {
        ResourcePermissions resourcePermissions = Mockito.mock(ResourcePermissions.class);
        Mockito.when(resourcePermissions.isUserRoleDeveloperAdmin()).thenReturn(true);
        ResourcePermissionsFactory resourcePermissionsFactory = Mockito.mock(ResourcePermissionsFactory.class);
        Mockito.when(resourcePermissionsFactory.get()).thenReturn(resourcePermissions);

        Address address = buildAddress(1L, "Line 1", null, "City", "MD", "", "USA");

        ChangeRequestValidationContext context = getValidationContext(address, resourcePermissionsFactory);
        DemographicsValidation crAddrValidator = new DemographicsValidation();

        boolean result = crAddrValidator.isValid(context);
        assertFalse(result);
    }

    @Test
    public void validateAddress_MissingZipcode_ReturnsExpectedError() throws EntityRetrievalException {
        ResourcePermissions resourcePermissions = Mockito.mock(ResourcePermissions.class);
        Mockito.when(resourcePermissions.isUserRoleDeveloperAdmin()).thenReturn(true);
        ResourcePermissionsFactory resourcePermissionsFactory = Mockito.mock(ResourcePermissionsFactory.class);
        Mockito.when(resourcePermissionsFactory.get()).thenReturn(resourcePermissions);

        Address address = buildAddress(1L, "Street", null, "City", "MD", null, "USA");

        ChangeRequestValidationContext context = getValidationContext(address, resourcePermissionsFactory);
        DemographicsValidation crAddrValidator = new DemographicsValidation();

        crAddrValidator.isValid(context);

        assertEquals(1, crAddrValidator.getMessages().size());
        assertTrue(crAddrValidator.getMessages().contains("Developer zipcode is required."));
    }

    private ChangeRequest getChangeRequestAddress(Address address) {
        return ChangeRequest.builder()
                .id(1L)
                .developer(Developer.builder()
                        .id(Long.valueOf(20L))
                        .developerCode("1234")
                        .name("Dev 1")
                        .build())
                .changeRequestType(ChangeRequestType.builder()
                        .id(2L)
                        .name("Developer Details Request")
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
                .details(buildChangeRequestDetails(address))
                .build();
    }

    private ChangeRequestDeveloperDemographics buildChangeRequestDetails(Address address) {
        return ChangeRequestDeveloperDemographics.builder()
                .address(address)
                .build();
    }

    private Address buildAddress(Long id, String line1, String line2, String city, String state, String zipcode, String country) {
        Address address = new Address();
        address.setAddressId(id);
        address.setLine1(line1);
        address.setLine2(line2);
        address.setCity(city);
        address.setState(state);
        address.setZipcode(zipcode);
        address.setCountry(country);

        return address;
    }

    private ChangeRequestValidationContext getValidationContext(Address address, ResourcePermissionsFactory resourcePermissionsFactory) {
        return new ChangeRequestValidationContext(null, getChangeRequestAddress(address),
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
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null);
    }
}
