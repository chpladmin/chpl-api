package gov.healthit.chpl.complaint.search;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.LinkedHashSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import gov.healthit.chpl.complaint.ComplaintDAO;
import gov.healthit.chpl.complaint.domain.ComplainantType;
import gov.healthit.chpl.dao.CertificationBodyDAO;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.util.ErrorMessageUtil;

public class ComplaintSearchRequestValidatorTest {
    private static final String INVALID_BOOLEAN = "The value '%s' for search term '%s' cannot be interpreted as true or false.";
    private static final String INVALID_COMPLAINANT_TYPE = "The complainant type name '%s' is not valid.";
    private static final String INVALID_STATUS = "The complaint status '%s' is not valid. Value must be one of %s.";
    private static final String INVALID_ACB = "There is no ONC-ACB in the system with the ID '%s'.";
    private static final String INVALID_LISTING_ID = "The listing ID '%s' is not a valid number.";
    private static final String INVALID_SURVEILLANCE_ID = "The surveillance ID '%s' is not a valid number.";
    private static final String INVALID_CRITERION_ID = "The certification criterion ID '%s' is not a valid number.";
    private static final String INVALID_CLOSED_DATE = "The closed date value '%s' is not a valid date. It must be in the format %s.";
    private static final String INVALID_RECEIVED_DATE = "The received date value '%s' is not a valid date. It must be in the format %s.";
    private static final String INVALID_OPEN_RANGE_DATE = "The open range date value '%s' is not a valid date. It must be in the format %s.";
    private static final String MISSING_OPEN_RANGE_PARAM = "The open date range must provide both start and end values.";
    private static final String PAGE_NUMBER_NAN = "Page number '%s' is not a valid number.";
    private static final String PAGE_NUMBER_OUT_OF_RANGE = "Page number must be 0 or greater. '%s' is not valid";
    private static final String PAGE_SIZE_NAN = "Page size '%s' is not a valid number.";
    private static final String PAGE_SIZE_OUT_OF_RANGE = "Page size must be between %s and %s. '%s' is not valid.";
    private static final String INVALID_ORDER_BY = "Order by parameter '%s' is invalid. Value must be one of %s.";

    private ComplaintDAO complaintDao;
    private CertificationBodyDAO acbDao;
    private ErrorMessageUtil msgUtil;
    private ComplaintSearchRequestValidator validator;

    @Before
    public void setup() {
        complaintDao = Mockito.mock(ComplaintDAO.class);
        acbDao = Mockito.mock(CertificationBodyDAO.class);

        msgUtil = Mockito.mock(ErrorMessageUtil.class);
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("search.complaint.invalidBoolean"), ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(INVALID_BOOLEAN, i.getArgument(1), i.getArgument(2)));
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("search.complaint.acbId.doesNotExist"), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(INVALID_ACB, i.getArgument(1), ""));
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("search.complaint.complainantType.invalid"), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(INVALID_COMPLAINANT_TYPE, i.getArgument(1), ""));
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("search.complaint.complaintStatus.invalid"), ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(INVALID_STATUS, i.getArgument(1), i.getArgument(2)));
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("search.complaint.listingId.invalid"), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(INVALID_LISTING_ID, i.getArgument(1), ""));
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("search.complaint.surveillanceId.invalid"), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(INVALID_SURVEILLANCE_ID, i.getArgument(1), ""));
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("search.complaint.certificationCriteriaId.invalid"), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(INVALID_CRITERION_ID, i.getArgument(1), ""));
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("search.complaint.closedDate.invalid"), ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(INVALID_CLOSED_DATE, i.getArgument(1), i.getArgument(2)));
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("search.complaint.receivedDate.invalid"), ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(INVALID_RECEIVED_DATE, i.getArgument(1), i.getArgument(2)));
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("search.complaint.openDuring.invalid"), ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(INVALID_OPEN_RANGE_DATE, i.getArgument(1), i.getArgument(2)));
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("search.complaint.openDuring.missingStartOrEnd"))).thenReturn(MISSING_OPEN_RANGE_PARAM);
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("search.complaint.pageNumber.invalid"), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(PAGE_NUMBER_NAN, i.getArgument(1), ""));
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("search.complaint.pageNumber.outOfRange"), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(PAGE_NUMBER_OUT_OF_RANGE, i.getArgument(1), ""));
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("search.complaint.pageSize.invalid"), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(PAGE_SIZE_NAN, i.getArgument(1), ""));
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("search.complaint.pageSize.outOfRange"), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(PAGE_SIZE_OUT_OF_RANGE, i.getArgument(1), ""));
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("search.complaint.orderBy.invalid"), ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(INVALID_ORDER_BY, i.getArgument(1), i.getArgument(2)));

        validator = new ComplaintSearchRequestValidator(complaintDao, acbDao, msgUtil);
    }

    @Test
    public void validate_searchTerm_noChange() {
        ComplaintSearchRequest request = ComplaintSearchRequest.builder()
            .searchTerm("some search term")
            .build();

        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            fail("Should not execute");
        }
        assertEquals("some search term", request.getSearchTerm());
    }

    @Test
    public void validate_nullInformedOncString_isValid() {
        ComplaintSearchRequest request = ComplaintSearchRequest.builder()
            .informedOncStrings(null)
            .build();

        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            fail("Should not execute");
        }
        assertEquals(0, request.getInformedOnc().size());
    }

    @Test
    public void validate_blankInformedOncString_isValid() {
        ComplaintSearchRequest request = ComplaintSearchRequest.builder()
            .informedOncStrings(Stream.of("").collect(Collectors.toSet()))
            .build();

        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            fail("Should not execute");
        }
        assertEquals(0, request.getInformedOnc().size());
    }

    @Test
    public void validate_trueInformedOncStrings_isValid() {
        ComplaintSearchRequest request = ComplaintSearchRequest.builder()
            .informedOncStrings(Stream.of("true").collect(Collectors.toSet()))
            .informedOnc(Stream.of(Boolean.TRUE).collect(Collectors.toSet()))
            .build();

        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            fail("Should not execute");
        }
        assertTrue(request.getInformedOnc().iterator().next());
    }

    @Test
    public void validate_trueInformedOnc_isValid() {
        ComplaintSearchRequest request = ComplaintSearchRequest.builder()
            .informedOnc(Stream.of(Boolean.TRUE).collect(Collectors.toSet()))
            .build();

        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            fail("Should not execute");
        }
        assertTrue(request.getInformedOnc().iterator().next());
    }

    @Test
    public void validate_falseInformedOncString_isValid() {
        ComplaintSearchRequest request = ComplaintSearchRequest.builder()
            .informedOncStrings(Stream.of("false").collect(Collectors.toSet()))
            .informedOnc(Stream.of(Boolean.FALSE).collect(Collectors.toSet()))
            .build();

        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            fail("Should not execute");
        }
        assertFalse(request.getInformedOnc().iterator().next());
    }

    @Test
    public void validate_invalidInformedOncString_hasError() {
        ComplaintSearchRequest request = ComplaintSearchRequest.builder()
            .informedOncStrings(Stream.of("junk").collect(Collectors.toSet()))
            .build();

        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            assertEquals(1, ex.getErrorMessages().size());
            assertTrue(ex.getErrorMessages().contains(String.format(INVALID_BOOLEAN, "junk", "Informed ONC")));
        }
    }

    @Test
    public void validate_someValidSomeInvalidInformedOncString_hasError() {
        ComplaintSearchRequest request = ComplaintSearchRequest.builder()
            .informedOncStrings(Stream.of("junk", "TRUE", "true", "1", "BAD", " ").collect(Collectors.toSet()))
            .build();

        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            assertEquals(2, ex.getErrorMessages().size());
            assertTrue(ex.getErrorMessages().contains(String.format(INVALID_BOOLEAN, "junk", "Informed ONC")));
            assertTrue(ex.getErrorMessages().contains(String.format(INVALID_BOOLEAN, "BAD", "Informed ONC")));
        }
    }

    @Test
    public void validate_nullAtlContactedString_isValid() {
        ComplaintSearchRequest request = ComplaintSearchRequest.builder()
            .oncAtlContactedStrings(null)
            .build();

        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            fail("Should not execute");
        }
        assertEquals(0, request.getOncAtlContacted().size());
    }

    @Test
    public void validate_blankAtlContactedString_isValid() {
        ComplaintSearchRequest request = ComplaintSearchRequest.builder()
            .oncAtlContactedStrings(Stream.of("").collect(Collectors.toSet()))
            .build();

        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            fail("Should not execute");
        }
        assertEquals(0, request.getOncAtlContacted().size());
    }

    @Test
    public void validate_trueAtlContactedString_isValid() {
        ComplaintSearchRequest request = ComplaintSearchRequest.builder()
            .oncAtlContactedStrings(Stream.of("true").collect(Collectors.toSet()))
            .oncAtlContacted(Stream.of(Boolean.TRUE).collect(Collectors.toSet()))
            .build();

        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            fail("Should not execute");
        }
        assertTrue(request.getOncAtlContacted().iterator().next());
    }

    @Test
    public void validate_falseAtlContactedString_isValid() {
        ComplaintSearchRequest request = ComplaintSearchRequest.builder()
            .oncAtlContactedStrings(Stream.of("false").collect(Collectors.toSet()))
            .oncAtlContacted(Stream.of(Boolean.FALSE).collect(Collectors.toSet()))
            .build();

        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            fail("Should not execute");
        }
        assertFalse(request.getOncAtlContacted().iterator().next());
    }

    @Test
    public void validate_invalidAtlContactedString_hasError() {
        ComplaintSearchRequest request = ComplaintSearchRequest.builder()
            .oncAtlContactedStrings(Stream.of("junk").collect(Collectors.toSet()))
            .build();

        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            assertEquals(1, ex.getErrorMessages().size());
            assertTrue(ex.getErrorMessages().contains(String.format(INVALID_BOOLEAN, "junk", "ONC-ATL Contacted")));
        }
    }

    @Test
    public void validate_someValidSomeInvalidAtlContactString_hasError() {
        ComplaintSearchRequest request = ComplaintSearchRequest.builder()
            .oncAtlContactedStrings(Stream.of("junk", "TRUE", "true", "1", "BAD", " ").collect(Collectors.toSet()))
            .build();

        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            assertEquals(2, ex.getErrorMessages().size());
            assertTrue(ex.getErrorMessages().contains(String.format(INVALID_BOOLEAN, "junk", "ONC-ATL Contacted")));
            assertTrue(ex.getErrorMessages().contains(String.format(INVALID_BOOLEAN, "BAD", "ONC-ATL Contacted")));
        }
    }

    @Test
    public void validate_nullComplainantContactedString_isValid() {
        ComplaintSearchRequest request = ComplaintSearchRequest.builder()
            .complainantContactedStrings(null)
            .build();

        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            fail("Should not execute");
        }
        assertEquals(0, request.getComplainantContacted().size());
    }

    @Test
    public void validate_blankComplainantContactedString_isValid() {
        ComplaintSearchRequest request = ComplaintSearchRequest.builder()
            .complainantContactedStrings(Stream.of("").collect(Collectors.toSet()))
            .build();

        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            fail("Should not execute");
        }
        assertEquals(0, request.getComplainantContacted().size());
    }

    @Test
    public void validate_trueComplainantContactedString_isValid() {
        ComplaintSearchRequest request = ComplaintSearchRequest.builder()
            .complainantContactedStrings(Stream.of("true").collect(Collectors.toSet()))
            .complainantContacted(Stream.of(Boolean.TRUE).collect(Collectors.toSet()))
            .build();

        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            fail("Should not execute");
        }
        assertTrue(request.getComplainantContacted().iterator().next());
    }

    @Test
    public void validate_falseComplainantContactedString_isValid() {
        ComplaintSearchRequest request = ComplaintSearchRequest.builder()
            .complainantContactedStrings(Stream.of("false").collect(Collectors.toSet()))
            .complainantContacted(Stream.of(Boolean.FALSE).collect(Collectors.toSet()))
            .build();

        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            fail("Should not execute");
        }
        assertFalse(request.getComplainantContacted().iterator().next());
    }

    @Test
    public void validate_invalidComplainantContactedString_hasError() {
        ComplaintSearchRequest request = ComplaintSearchRequest.builder()
            .complainantContactedStrings(Stream.of("junk").collect(Collectors.toSet()))
            .build();

        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            assertEquals(1, ex.getErrorMessages().size());
            assertTrue(ex.getErrorMessages().contains(String.format(INVALID_BOOLEAN, "junk", "Complainant Contacted")));
        }
    }

    @Test
    public void validate_someValidSomeInvalidComplainantContactedString_hasError() {
        ComplaintSearchRequest request = ComplaintSearchRequest.builder()
            .complainantContactedStrings(Stream.of("junk", "TRUE", "true", "1", "BAD", " ").collect(Collectors.toSet()))
            .build();

        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            assertEquals(2, ex.getErrorMessages().size());
            assertTrue(ex.getErrorMessages().contains(String.format(INVALID_BOOLEAN, "junk", "Complainant Contacted")));
            assertTrue(ex.getErrorMessages().contains(String.format(INVALID_BOOLEAN, "BAD", "Complainant Contacted")));
        }
    }

    @Test
    public void validate_nullDeveloperContactedString_isValid() {
        ComplaintSearchRequest request = ComplaintSearchRequest.builder()
            .developerContactedStrings(null)
            .build();

        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            fail("Should not execute");
        }
        assertEquals(0, request.getDeveloperContacted().size());
    }

    @Test
    public void validate_blankDeveloperContactedString_isValid() {
        ComplaintSearchRequest request = ComplaintSearchRequest.builder()
            .developerContactedStrings(Stream.of("").collect(Collectors.toSet()))
            .build();

        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            System.out.println(ex.getErrorMessages().iterator().next());
            fail("Should not execute");
        }
        assertEquals(0, request.getDeveloperContacted().size());
    }

    @Test
    public void validate_trueDeveloperContactedString_isValid() {
        ComplaintSearchRequest request = ComplaintSearchRequest.builder()
            .developerContactedStrings(Stream.of("true").collect(Collectors.toSet()))
            .developerContacted(Stream.of(Boolean.TRUE).collect(Collectors.toSet()))
            .build();

        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            fail("Should not execute");
        }
        assertTrue(request.getDeveloperContacted().iterator().next());
    }

    @Test
    public void validate_falseDeveloperContactedString_isValid() {
        ComplaintSearchRequest request = ComplaintSearchRequest.builder()
            .developerContactedStrings(Stream.of("false").collect(Collectors.toSet()))
            .developerContacted(Stream.of(Boolean.FALSE).collect(Collectors.toSet()))
            .build();

        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            fail("Should not execute");
        }
        assertFalse(request.getDeveloperContacted().iterator().next());
    }

    @Test
    public void validate_invalidDeveloperContactedString_hasError() {
        ComplaintSearchRequest request = ComplaintSearchRequest.builder()
            .developerContactedStrings(Stream.of("junk").collect(Collectors.toSet()))
            .build();

        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            assertEquals(1, ex.getErrorMessages().size());
            assertTrue(ex.getErrorMessages().contains(String.format(INVALID_BOOLEAN, "junk", "Developer Contacted")));
        }
    }

    @Test
    public void validate_someValidSomeInvalidDeveloperContactedString_hasError() {
        ComplaintSearchRequest request = ComplaintSearchRequest.builder()
            .developerContactedStrings(Stream.of("junk", "TRUE", "true", "1", "BAD", " ").collect(Collectors.toSet()))
            .build();

        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            assertEquals(2, ex.getErrorMessages().size());
            assertTrue(ex.getErrorMessages().contains(String.format(INVALID_BOOLEAN, "junk", "Developer Contacted")));
            assertTrue(ex.getErrorMessages().contains(String.format(INVALID_BOOLEAN, "BAD", "Developer Contacted")));
        }
    }

    @Test
    public void validate_nullAcbIds_noErrors() {
        ComplaintSearchRequest request = ComplaintSearchRequest.builder()
            .build();
        request.setAcbIds(null);

        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            fail("Should not execute");
        }
    }

    @Test
    public void validate_emptyAcbIds_noErrors() {
        ComplaintSearchRequest request = ComplaintSearchRequest.builder()
            .acbIds(new LinkedHashSet<Long>())
            .build();

        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            fail("Should not execute");
        }
    }

    @Test
    public void validate_singleValidAcbId_noErrors() throws EntityRetrievalException {
        Mockito.when(acbDao.getById(ArgumentMatchers.eq(1L))).thenReturn(
                CertificationBodyDTO.builder()
                .id(1L)
                .build());
        ComplaintSearchRequest request = ComplaintSearchRequest.builder()
            .acbIds(Stream.of(1L).collect(Collectors.toSet()))
            .build();

        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            fail("Should not execute");
        }
    }

    @Test
    public void validate_multipleValidAcbIds_noErrors() throws EntityRetrievalException {
        Mockito.when(acbDao.getById(ArgumentMatchers.eq(1L))).thenReturn(
                CertificationBodyDTO.builder()
                .id(1L)
                .build());
        Mockito.when(acbDao.getById(ArgumentMatchers.eq(1L))).thenReturn(
                CertificationBodyDTO.builder()
                .id(2L)
                .build());
        ComplaintSearchRequest request = ComplaintSearchRequest.builder()
            .acbIds(Stream.of(1L, 2L).collect(Collectors.toSet()))
            .build();

        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            fail("Should not execute");
        }
    }

    @Test
    public void validate_singleInvalidAcbId_hasError() throws EntityRetrievalException {
        Mockito.when(acbDao.getById(ArgumentMatchers.eq(1L))).thenThrow(EntityRetrievalException.class);

        ComplaintSearchRequest request = ComplaintSearchRequest.builder()
            .acbIds(Stream.of(1L).collect(Collectors.toSet()))
            .build();

        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            assertEquals(1, ex.getErrorMessages().size());
            assertTrue(ex.getErrorMessages().contains(String.format(INVALID_ACB, "1")));
        }
    }

    @Test
    public void validate_multipleInvalidAcbIds_hasErrors() throws EntityRetrievalException {
        Mockito.when(acbDao.getById(ArgumentMatchers.eq(1L))).thenThrow(EntityRetrievalException.class);
        Mockito.when(acbDao.getById(ArgumentMatchers.eq(2L))).thenThrow(EntityRetrievalException.class);

        ComplaintSearchRequest request = ComplaintSearchRequest.builder()
            .acbIds(Stream.of(1L, 2L).collect(Collectors.toSet()))
            .build();

        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            assertEquals(2, ex.getErrorMessages().size());
            assertTrue(ex.getErrorMessages().contains(String.format(INVALID_ACB, "1")));
            assertTrue(ex.getErrorMessages().contains(String.format(INVALID_ACB, "2")));
        }
    }

    @Test
    public void validate_oneValidAndOneInvalidAcbId_hasError() throws EntityRetrievalException {
        Mockito.when(acbDao.getById(ArgumentMatchers.eq(1L))).thenThrow(EntityRetrievalException.class);
        Mockito.when(acbDao.getById(ArgumentMatchers.eq(2L))).thenReturn(CertificationBodyDTO.builder()
                .id(2L)
                .build());

        ComplaintSearchRequest request = ComplaintSearchRequest.builder()
            .acbIds(Stream.of(1L, 2L).collect(Collectors.toSet()))
            .build();

        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            assertEquals(1, ex.getErrorMessages().size());
            assertTrue(ex.getErrorMessages().contains(String.format(INVALID_ACB, "1")));
        }
    }

    @Test
    public void validate_nullComplainantTypes_noErrors() {
        ComplaintSearchRequest request = ComplaintSearchRequest.builder()
            .build();
        request.setComplainantTypeNames(null);

        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            fail("Should not execute");
        }
    }

    @Test
    public void validate_emptyComplainantTypes_noErrors() {
        ComplaintSearchRequest request = ComplaintSearchRequest.builder()
                .complainantTypeNames(new LinkedHashSet<String>())
            .build();

        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            fail("Should not execute");
        }
    }

    @Test
    public void validate_singleValidComplainantTypes_noErrors() {
        Mockito.when(complaintDao.getComplainantTypes())
            .thenReturn(Stream.of(
                    new ComplainantType(1L, "type1"),
                    new ComplainantType(2L, "type2")).collect(Collectors.toList()));

        ComplaintSearchRequest request = ComplaintSearchRequest.builder()
                .complainantTypeNames(Stream.of("type1").collect(Collectors.toSet()))
            .build();

        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            fail("Should not execute");
        }
    }

    @Test
    public void validate_multipleValidComplainantTypes_noErrors() {
        Mockito.when(complaintDao.getComplainantTypes())
            .thenReturn(Stream.of(
                    new ComplainantType(1L, "type1"),
                    new ComplainantType(2L, "type2")).collect(Collectors.toList()));

        ComplaintSearchRequest request = ComplaintSearchRequest.builder()
                .complainantTypeNames(Stream.of("type1", "type2").collect(Collectors.toSet()))
            .build();

        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            fail("Should not execute");
        }
    }

    @Test
    public void validate_singleInvalidComplainantTypes_hasError() {
        Mockito.when(complaintDao.getComplainantTypes())
            .thenReturn(Stream.of(
                    new ComplainantType(1L, "type1"),
                    new ComplainantType(2L, "type2")).collect(Collectors.toList()));

        ComplaintSearchRequest request = ComplaintSearchRequest.builder()
                .complainantTypeNames(Stream.of("type3").collect(Collectors.toSet()))
            .build();

        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            assertEquals(1L, ex.getErrorMessages().size());
            assertTrue(ex.getErrorMessages().contains(String.format(INVALID_COMPLAINANT_TYPE, "type3")));
        }
    }

    @Test
    public void validate_multipleInvalidComplainantTypes_hasErrors() {
        Mockito.when(complaintDao.getComplainantTypes())
            .thenReturn(Stream.of(
                    new ComplainantType(1L, "type1"),
                    new ComplainantType(2L, "type2")).collect(Collectors.toList()));

        ComplaintSearchRequest request = ComplaintSearchRequest.builder()
                .complainantTypeNames(Stream.of("type3", "type4").collect(Collectors.toSet()))
            .build();

        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            assertEquals(2L, ex.getErrorMessages().size());
            assertTrue(ex.getErrorMessages().contains(String.format(INVALID_COMPLAINANT_TYPE, "type3")));
            assertTrue(ex.getErrorMessages().contains(String.format(INVALID_COMPLAINANT_TYPE, "type4")));
        }
    }

    @Test
    public void validate_oneValidOneInvalidComplainantTypes_hasError() {
        Mockito.when(complaintDao.getComplainantTypes())
            .thenReturn(Stream.of(
                    new ComplainantType(1L, "type1"),
                    new ComplainantType(2L, "type2")).collect(Collectors.toList()));

        ComplaintSearchRequest request = ComplaintSearchRequest.builder()
                .complainantTypeNames(Stream.of("type1", "type4").collect(Collectors.toSet()))
            .build();

        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            assertEquals(1L, ex.getErrorMessages().size());
            assertTrue(ex.getErrorMessages().contains(String.format(INVALID_COMPLAINANT_TYPE, "type4")));
        }
    }

    @Test
    public void validate_nullCurrentStatus_noError() {
        ComplaintSearchRequest request = ComplaintSearchRequest.builder()
            .build();
        request.setCurrentStatusNames(null);

        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            fail("Should not execute.");
        }
    }

    @Test
    public void validate_emptyCurrentStatus_noError() {
        ComplaintSearchRequest request = ComplaintSearchRequest.builder()
            .currentStatusNames(new LinkedHashSet<String>())
            .build();

        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            fail("Should not execute.");
        }
    }

    @Test
    public void validate_openCurrentStatus_noError() {
        ComplaintSearchRequest request = ComplaintSearchRequest.builder()
            .currentStatusNames(Stream.of("Open").collect(Collectors.toSet()))
            .build();

        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            fail("Should not execute.");
        }
    }

    @Test
    public void validate_closedCurrentStatus_noError() {
        ComplaintSearchRequest request = ComplaintSearchRequest.builder()
            .currentStatusNames(Stream.of("Closed").collect(Collectors.toSet()))
            .build();

        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            fail("Should not execute.");
        }
    }

    @Test
    public void validate_invalidCurrentStatus_hasError() {
        ComplaintSearchRequest request = ComplaintSearchRequest.builder()
            .currentStatusNames(Stream.of("junk").collect(Collectors.toSet()))
            .build();

        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            assertEquals(1L, ex.getErrorMessages().size());
            assertTrue(ex.getErrorMessages().contains(String.format(INVALID_STATUS, "junk", "Open, Closed")));
        }
    }

    @Test
    public void validate_invalidListingIdFormat_addsError() {
        ComplaintSearchRequest request = ComplaintSearchRequest.builder()
            .listingIdStrings(Stream.of("3 ", " 4 ", " 01", " ", "", null, "BAD").collect(Collectors.toSet()))
            .build();

        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            assertEquals(1, ex.getErrorMessages().size());
            assertTrue(ex.getErrorMessages().contains(String.format(INVALID_LISTING_ID, "BAD", "")));
            return;
        }
        fail("Should not execute.");
    }

    @Test
    public void validate_invalidSurveillanceIdFormat_addsError() {
        ComplaintSearchRequest request = ComplaintSearchRequest.builder()
            .surveillanceIdStrings(Stream.of("3 ", " 4 ", " 01", " ", "", null, "BAD").collect(Collectors.toSet()))
            .build();

        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            assertEquals(1, ex.getErrorMessages().size());
            assertTrue(ex.getErrorMessages().contains(String.format(INVALID_SURVEILLANCE_ID, "BAD", "")));
            return;
        }
        fail("Should not execute.");
    }

    @Test
    public void validate_invalidCertificationCriteriaIdFormat_addsError() {
        ComplaintSearchRequest request = ComplaintSearchRequest.builder()
            .certificationCriteriaIdStrings(Stream.of("3 ", " 4 ", " 01", " ", "", null, "BAD").collect(Collectors.toSet()))
            .build();

        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            assertEquals(1, ex.getErrorMessages().size());
            assertTrue(ex.getErrorMessages().contains(String.format(INVALID_CRITERION_ID, "BAD", "")));
            return;
        }
        fail("Should not execute.");
    }

    @Test
    public void validate_invalidClosedDateStartFormat_addsError() {
        ComplaintSearchRequest request = ComplaintSearchRequest.builder()
            .closedDateStart("12345")
            .build();

        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            assertEquals(1, ex.getErrorMessages().size());
            assertTrue(ex.getErrorMessages().contains(String.format(INVALID_CLOSED_DATE, "12345", ComplaintSearchRequest.DATE_SEARCH_FORMAT)));
        }
    }

    @Test
    public void validate_invalidClosedDateEndFormat_addsError() {
        ComplaintSearchRequest request = ComplaintSearchRequest.builder()
            .closedDateEnd("12345")
            .build();

        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            assertEquals(1, ex.getErrorMessages().size());
            assertTrue(ex.getErrorMessages().contains(String.format(INVALID_CLOSED_DATE, "12345", ComplaintSearchRequest.DATE_SEARCH_FORMAT)));
        }
    }

    @Test
    public void validate_validClosedDateStartEmptyEnd_noError() {
        ComplaintSearchRequest request = ComplaintSearchRequest.builder()
            .closedDateStart("2015-01-01")
            .closedDateEnd("")
            .build();

        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            fail(ex.getMessage());
        }
    }

    @Test
    public void validate_validClosedDateEndEmptyStart_noError() {
        ComplaintSearchRequest request = ComplaintSearchRequest.builder()
            .closedDateEnd("")
            .closedDateEnd("2015-12-31")
            .build();

        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            fail(ex.getMessage());
        }
    }

    @Test
    public void validate_validClosedDateStartAndEnd_noError() {
        ComplaintSearchRequest request = ComplaintSearchRequest.builder()
            .closedDateStart("2015-01-01")
            .closedDateEnd("2015-12-31")
            .build();

        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            fail(ex.getMessage());
        }
    }

    @Test
    public void validate_invalidReceivedDateStartFormat_addsError() {
        ComplaintSearchRequest request = ComplaintSearchRequest.builder()
            .receivedDateStart("12345")
            .build();

        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            assertEquals(1, ex.getErrorMessages().size());
            assertTrue(ex.getErrorMessages().contains(String.format(INVALID_RECEIVED_DATE, "12345", ComplaintSearchRequest.DATE_SEARCH_FORMAT)));
        }
    }

    @Test
    public void validate_invalidReceivedDateEndFormat_addsError() {
        ComplaintSearchRequest request = ComplaintSearchRequest.builder()
            .receivedDateEnd("12345")
            .build();

        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            assertEquals(1, ex.getErrorMessages().size());
            assertTrue(ex.getErrorMessages().contains(String.format(INVALID_RECEIVED_DATE, "12345", ComplaintSearchRequest.DATE_SEARCH_FORMAT)));
        }
    }

    @Test
    public void validate_validReceivedDateStartEmptyEnd_noError() {
        ComplaintSearchRequest request = ComplaintSearchRequest.builder()
            .receivedDateStart("2015-01-01")
            .receivedDateEnd("")
            .build();

        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            fail(ex.getMessage());
        }
    }

    @Test
    public void validate_validReceivedDateEndEmptyStart_noError() {
        ComplaintSearchRequest request = ComplaintSearchRequest.builder()
            .receivedDateEnd("")
            .receivedDateStart("2015-12-31")
            .build();

        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            fail(ex.getMessage());
        }
    }

    @Test
    public void validate_validReceivedDateStartAndEnd_noError() {
        ComplaintSearchRequest request = ComplaintSearchRequest.builder()
            .receivedDateStart("2015-01-01")
            .receivedDateEnd("2015-12-31")
            .build();

        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            fail(ex.getMessage());
        }
    }

    @Test
    public void validate_invalidOpenRangeStartFormat_addsError() {
        ComplaintSearchRequest request = ComplaintSearchRequest.builder()
            .openDuringRangeStart("12345")
            .openDuringRangeEnd("2022-01-10")
            .build();

        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            assertEquals(1, ex.getErrorMessages().size());
            assertTrue(ex.getErrorMessages().contains(String.format(INVALID_OPEN_RANGE_DATE, "12345", ComplaintSearchRequest.DATE_SEARCH_FORMAT)));
        }
    }

    @Test
    public void validate_invalidOpenRangeEndFormat_addsError() {
        ComplaintSearchRequest request = ComplaintSearchRequest.builder()
            .openDuringRangeEnd("12345")
            .openDuringRangeStart("2022-01-20")
            .build();

        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            assertEquals(1, ex.getErrorMessages().size());
            assertTrue(ex.getErrorMessages().contains(String.format(INVALID_OPEN_RANGE_DATE, "12345", ComplaintSearchRequest.DATE_SEARCH_FORMAT)));
        }
    }

    @Test
    public void validate_validOpenRangeStartEmptyEnd_noError() {
        ComplaintSearchRequest request = ComplaintSearchRequest.builder()
            .openDuringRangeStart("2015-01-01")
            .openDuringRangeEnd("2015-01-02")
            .build();

        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            fail(ex.getMessage());
        }
    }

    @Test
    public void validate_validOpenRangeEndEmptyStart_hasError() {
        ComplaintSearchRequest request = ComplaintSearchRequest.builder()
            .openDuringRangeEnd("")
            .openDuringRangeStart("2015-12-31")
            .build();

        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            assertEquals(1, ex.getErrorMessages().size());
            assertTrue(ex.getErrorMessages().contains(MISSING_OPEN_RANGE_PARAM));
        }
    }

    @Test
    public void validate_validOpenRangeStartEmptyEnd_hasError() {
        ComplaintSearchRequest request = ComplaintSearchRequest.builder()
            .openDuringRangeEnd("2015-12-31")
            .build();

        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            assertEquals(1, ex.getErrorMessages().size());
            assertTrue(ex.getErrorMessages().contains(MISSING_OPEN_RANGE_PARAM));
        }
    }

    @Test
    public void validate_validOpenRangeStartAndEnd_noError() {
        ComplaintSearchRequest request = ComplaintSearchRequest.builder()
            .openDuringRangeStart("2015-01-01")
            .openDuringRangeEnd("2015-12-31")
            .build();

        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            fail(ex.getMessage());
        }
    }

    @Test
    public void validate_invalidOpenDateRangeStartMissingEnd_hasErrors() {
        ComplaintSearchRequest request = ComplaintSearchRequest.builder()
            .openDuringRangeStart("12345")
            .build();

        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            assertEquals(2, ex.getErrorMessages().size());
            assertTrue(ex.getErrorMessages().contains(MISSING_OPEN_RANGE_PARAM));
            assertTrue(ex.getErrorMessages().contains(String.format(INVALID_OPEN_RANGE_DATE, "12345", ComplaintSearchRequest.DATE_SEARCH_FORMAT)));
        }
    }

    @Test
    public void validate_invalidOrderBy_addsError() {
        ComplaintSearchRequest request = ComplaintSearchRequest.builder()
            .orderBy(null)
            .orderByString("NOTVALID")
            .build();
        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            assertEquals(1, ex.getErrorMessages().size());
            assertTrue(ex.getErrorMessages().contains(String.format(INVALID_ORDER_BY, "NOTVALID",
                    Stream.of(OrderByOption.values())
                    .map(value -> value.name())
                    .collect(Collectors.joining(", ")))));
            return;
        }
        fail("Should not execute.");
    }

    @Test
    public void validate_validOrderByParsedFromString_noErrors() {
        ComplaintSearchRequest request = ComplaintSearchRequest.builder()
            .orderBy(OrderByOption.ACB_COMPLAINT_ID)
            .orderByString("ACB_COMPLAINT_ID")
            .build();
        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            fail(ex.getMessage());
        }
    }

    @Test
    public void validate_validOrderByWithoutString_noErrors() {
        ComplaintSearchRequest request = ComplaintSearchRequest.builder()
            .orderBy(OrderByOption.ACB_COMPLAINT_ID)
            .orderByString(null)
            .build();
        try {
            validator.validate(request);
        } catch (ValidationException ex) {
            fail(ex.getMessage());
        }
    }
}
