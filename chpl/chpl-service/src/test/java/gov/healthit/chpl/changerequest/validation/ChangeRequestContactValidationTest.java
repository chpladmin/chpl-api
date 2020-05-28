package gov.healthit.chpl.changerequest.validation;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashMap;

import org.junit.Test;
import org.mockito.Mockito;

import gov.healthit.chpl.changerequest.domain.ChangeRequest;
import gov.healthit.chpl.changerequest.domain.ChangeRequestStatus;
import gov.healthit.chpl.changerequest.domain.ChangeRequestStatusType;
import gov.healthit.chpl.changerequest.domain.ChangeRequestType;
import gov.healthit.chpl.changerequest.validation.ChangeRequestValidationContext;
import gov.healthit.chpl.changerequest.validation.ContactValidation;
import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.domain.Contact;
import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.permissions.ResourcePermissions;

public class ChangeRequestContactValidationTest {
    @Test
    public void validateContact_AllData_ReturnsTrue() throws EntityRetrievalException {
        ResourcePermissions resourcePermissions = Mockito.mock(ResourcePermissions.class);
        Mockito.when(resourcePermissions.isUserRoleDeveloperAdmin()).thenReturn(true);
        Contact contact = buildContact(1L, "First M. Last", "Mr.", "444-444-4444", "first@gmail.com");

        ContactValidation crContactValidator = new ContactValidation(resourcePermissions);
        ChangeRequestValidationContext context =
                new ChangeRequestValidationContext(getChangeRequestContact(contact), null);

        boolean result = crContactValidator.isValid(context);
        assertTrue(result);
        assertEquals(0, crContactValidator.getMessages().size());
    }

    @Test
    public void validateContact_ValidDataNoTitle_ReturnsTrue() throws EntityRetrievalException {
        ResourcePermissions resourcePermissions = Mockito.mock(ResourcePermissions.class);
        Mockito.when(resourcePermissions.isUserRoleDeveloperAdmin()).thenReturn(true);
        Contact contact = buildContact(1L, "First M. Last", null, "444-444-4444", "first@gmail.com");

        ContactValidation crContactValidator = new ContactValidation(resourcePermissions);
        ChangeRequestValidationContext context =
                new ChangeRequestValidationContext(getChangeRequestContact(contact), null);

        boolean result = crContactValidator.isValid(context);
        assertTrue(result);
        assertEquals(0, crContactValidator.getMessages().size());
    }

    @Test
    public void validateContact_MissingName_ReturnsFalse() throws EntityRetrievalException {
        ResourcePermissions resourcePermissions = Mockito.mock(ResourcePermissions.class);
        Mockito.when(resourcePermissions.isUserRoleDeveloperAdmin()).thenReturn(true);
        Contact contact = buildContact(1L, null, null, "444-444-4444", "first@gmail.com");

        ContactValidation crContactValidator = new ContactValidation(resourcePermissions);
        ChangeRequestValidationContext context =
                new ChangeRequestValidationContext(getChangeRequestContact(contact), null);

        boolean result = crContactValidator.isValid(context);
        assertFalse(result);
    }

    @Test
    public void validateContact_EmptyName_ReturnsFalse() throws EntityRetrievalException {
        ResourcePermissions resourcePermissions = Mockito.mock(ResourcePermissions.class);
        Mockito.when(resourcePermissions.isUserRoleDeveloperAdmin()).thenReturn(true);
        Contact contact = buildContact(1L, "", null, "444-444-4444", "first@gmail.com");

        ContactValidation crContactValidator = new ContactValidation(resourcePermissions);
        ChangeRequestValidationContext context =
                new ChangeRequestValidationContext(getChangeRequestContact(contact), null);

        boolean result = crContactValidator.isValid(context);
        assertFalse(result);
    }

    @Test
    public void validateContact_MissingName_ReturnsExpectedError() throws EntityRetrievalException {
        ResourcePermissions resourcePermissions = Mockito.mock(ResourcePermissions.class);
        Mockito.when(resourcePermissions.isUserRoleDeveloperAdmin()).thenReturn(true);
        Contact contact = buildContact(1L, null, null, "444-444-4444", "first@gmail.com");

        ContactValidation crContactValidator = new ContactValidation(resourcePermissions);
        ChangeRequestValidationContext context =
                new ChangeRequestValidationContext(getChangeRequestContact(contact), null);

        crContactValidator.isValid(context);
        assertEquals(1, crContactValidator.getMessages().size());
        assertTrue(crContactValidator.getMessages().contains("Developer contact name is required."));
    }

    @Test
    public void validateContact_MissingPhoneNumber_ReturnsFalse() throws EntityRetrievalException {
        ResourcePermissions resourcePermissions = Mockito.mock(ResourcePermissions.class);
        Mockito.when(resourcePermissions.isUserRoleDeveloperAdmin()).thenReturn(true);
        Contact contact = buildContact(1L, "First M. Last", null, null, "test@gmail.com");

        ContactValidation crContactValidator = new ContactValidation(resourcePermissions);
        ChangeRequestValidationContext context =
                new ChangeRequestValidationContext(getChangeRequestContact(contact), null);

        boolean result = crContactValidator.isValid(context);
        assertFalse(result);
    }

    @Test
    public void validateContact_EmptyPhoneNumber_ReturnsFalse() throws EntityRetrievalException {
        ResourcePermissions resourcePermissions = Mockito.mock(ResourcePermissions.class);
        Mockito.when(resourcePermissions.isUserRoleDeveloperAdmin()).thenReturn(true);
        Contact contact = buildContact(1L, "First M. Last", null, "", "test@gmail.com");

        ContactValidation crContactValidator = new ContactValidation(resourcePermissions);
        ChangeRequestValidationContext context =
                new ChangeRequestValidationContext(getChangeRequestContact(contact), null);

        boolean result = crContactValidator.isValid(context);
        assertFalse(result);
    }

    @Test
    public void validateContact_MissingPhoneNumber_ReturnsExpectedError() throws EntityRetrievalException {
        ResourcePermissions resourcePermissions = Mockito.mock(ResourcePermissions.class);
        Mockito.when(resourcePermissions.isUserRoleDeveloperAdmin()).thenReturn(true);
        Contact contact = buildContact(1L, "First M. Last", null, null, "test@gmail.com");

        ContactValidation crContactValidator = new ContactValidation(resourcePermissions);
        ChangeRequestValidationContext context =
                new ChangeRequestValidationContext(getChangeRequestContact(contact), null);

        crContactValidator.isValid(context);
        assertEquals(1, crContactValidator.getMessages().size());
        assertTrue(crContactValidator.getMessages().contains("Developer contact phone number is required."));
    }

    @Test
    public void validateContact_MissingEmail_ReturnsFalse() throws EntityRetrievalException {
        ResourcePermissions resourcePermissions = Mockito.mock(ResourcePermissions.class);
        Mockito.when(resourcePermissions.isUserRoleDeveloperAdmin()).thenReturn(true);
        Contact contact = buildContact(1L, "First M. Last", null, "444-444-4444", null);

        ContactValidation crContactValidator = new ContactValidation(resourcePermissions);
        ChangeRequestValidationContext context =
                new ChangeRequestValidationContext(getChangeRequestContact(contact), null);

        boolean result = crContactValidator.isValid(context);
        assertFalse(result);
    }

    @Test
    public void validateContact_EmptyEmail_ReturnsFalse() throws EntityRetrievalException {
        ResourcePermissions resourcePermissions = Mockito.mock(ResourcePermissions.class);
        Mockito.when(resourcePermissions.isUserRoleDeveloperAdmin()).thenReturn(true);
        Contact contact = buildContact(1L, "First M. Last", null, "444-444-4444", "");

        ContactValidation crContactValidator = new ContactValidation(resourcePermissions);
        ChangeRequestValidationContext context =
                new ChangeRequestValidationContext(getChangeRequestContact(contact), null);

        boolean result = crContactValidator.isValid(context);
        assertFalse(result);
    }

    @Test
    public void validateContact_MissingEmail_ReturnsExpectedError() throws EntityRetrievalException {
        ResourcePermissions resourcePermissions = Mockito.mock(ResourcePermissions.class);
        Mockito.when(resourcePermissions.isUserRoleDeveloperAdmin()).thenReturn(true);
        Contact contact = buildContact(1L, "First M. Last", null, "444-444-4444", null);

        ContactValidation crContactValidator = new ContactValidation(resourcePermissions);
        ChangeRequestValidationContext context =
                new ChangeRequestValidationContext(getChangeRequestContact(contact), null);

        crContactValidator.isValid(context);
        assertEquals(1, crContactValidator.getMessages().size());
        assertTrue(crContactValidator.getMessages().contains("Developer contact email address is required."));
    }

    private ChangeRequest getChangeRequestContact(Contact contact) {
        return ChangeRequest.builder()
                .id(1L)
                .developer(Developer.builder()
                        .developerId(Long.valueOf(20L))
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
                .details(buildChangeRequestDetailsMap(contact))
                .build();
    }

    private HashMap<String, Object> buildChangeRequestDetailsMap(Contact contact) {
        HashMap<String, Object> details = new HashMap<String, Object>();
        HashMap<String, Object> contactMap = new HashMap<String, Object>();
        contactMap.put("contactId", contact.getContactId());
        contactMap.put("fullName", contact.getFullName());
        contactMap.put("title", contact.getTitle());
        contactMap.put("phoneNumber", contact.getPhoneNumber());
        contactMap.put("email", contact.getEmail());
        details.put("contact", contactMap);
        return details;
    }

    private Contact buildContact(Long id, String fullName, String title, String phoneNumber, String email) {
        Contact contact = new Contact();
        contact.setContactId(id);
        contact.setFullName(fullName);
        contact.setTitle(title);
        contact.setPhoneNumber(phoneNumber);
        contact.setEmail(email);
        return contact;
    }
}
