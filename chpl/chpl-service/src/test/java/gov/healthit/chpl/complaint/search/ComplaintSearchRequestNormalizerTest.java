package gov.healthit.chpl.complaint.search;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.HashSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import gov.healthit.chpl.dao.CertificationBodyDAO;
import gov.healthit.chpl.dto.CertificationBodyDTO;

public class ComplaintSearchRequestNormalizerTest {

    private CertificationBodyDAO acbDao;
    private ComplaintSearchRequestNormalizer normalizer;

    @Before
    public void setup() {
        acbDao = Mockito.mock(CertificationBodyDAO.class);
        normalizer = new ComplaintSearchRequestNormalizer(acbDao);
    }

    @Test
    public void normalize_emptySearchRequest_doesNotFail() {
        ComplaintSearchRequest searchRequest = ComplaintSearchRequest.builder().build();
        normalizer.normalize(searchRequest);
        assertNotNull(searchRequest);
    }

    @Test
    public void normalize_searchTerm_normalizesCorrectly() {
        ComplaintSearchRequest searchRequest = ComplaintSearchRequest.builder()
                .searchTerm("   te   st ")
                .build();
        normalizer.normalize(searchRequest);

        assertEquals("te st", searchRequest.getSearchTerm());
    }

    @Test
    public void normalize_informedOncTrue_normalizesToTrue() {
        ComplaintSearchRequest searchRequest = ComplaintSearchRequest.builder()
                .informedOncStrings(Stream.of("TRUE").collect(Collectors.toSet()))
                .build();
        normalizer.normalize(searchRequest);

        assertEquals(1, searchRequest.getInformedOnc().size());
        assertTrue(searchRequest.getInformedOnc().iterator().next());
    }

    @Test
    public void normalize_informedOncYes_normalizesToTrue() {
        ComplaintSearchRequest searchRequest = ComplaintSearchRequest.builder()
                .informedOncStrings(Stream.of(" yes ").collect(Collectors.toSet()))
                .build();
        normalizer.normalize(searchRequest);

        assertEquals(1, searchRequest.getInformedOnc().size());
        assertTrue(searchRequest.getInformedOnc().iterator().next());
    }

    @Test
    public void normalize_informedOncFalse_normalizesToFalse() {
        ComplaintSearchRequest searchRequest = ComplaintSearchRequest.builder()
                .informedOncStrings(Stream.of("FALSE").collect(Collectors.toSet()))
                .build();
        normalizer.normalize(searchRequest);

        assertEquals(1, searchRequest.getInformedOnc().size());
        assertFalse(searchRequest.getInformedOnc().iterator().next());
    }

    @Test
    public void normalize_informedOncNo_normalizesToFalse() {
        ComplaintSearchRequest searchRequest = ComplaintSearchRequest.builder()
                .informedOncStrings(Stream.of(" no ").collect(Collectors.toSet()))
                .build();
        normalizer.normalize(searchRequest);

        assertEquals(1, searchRequest.getInformedOnc().size());
        assertFalse(searchRequest.getInformedOnc().iterator().next());
    }

    @Test
    public void normalize_informedOncBlank_normalizesToEmptySet() {
        ComplaintSearchRequest searchRequest = ComplaintSearchRequest.builder()
                .informedOncStrings(Stream.of("  ").collect(Collectors.toSet()))
                .build();
        normalizer.normalize(searchRequest);

        assertEquals(0, searchRequest.getInformedOnc().size());
    }

    @Test
    public void normalize_informedOncNull_normalizesToEmptySet() {
        ComplaintSearchRequest searchRequest = ComplaintSearchRequest.builder()
                .informedOncStrings(null)
                .build();
        normalizer.normalize(searchRequest);

        assertEquals(0, searchRequest.getInformedOnc().size());
    }

    @Test
    public void normalize_informedOncSomeValidSomeInvalid_normalizesToValidBooleans() {
        ComplaintSearchRequest searchRequest = ComplaintSearchRequest.builder()
                .informedOncStrings(Stream.of("  ", "yes", "FALSE", "123").collect(Collectors.toSet()))
                .build();
        normalizer.normalize(searchRequest);

        assertEquals(2, searchRequest.getInformedOnc().size());
    }

    @Test
    public void normalize_informedOncDuplicateTrueStrings_normalizesToValidBooleans() {
        ComplaintSearchRequest searchRequest = ComplaintSearchRequest.builder()
                .informedOncStrings(Stream.of("  ", "yes", "TRUE", "true", " yes ").collect(Collectors.toSet()))
                .build();
        normalizer.normalize(searchRequest);

        assertEquals(1, searchRequest.getInformedOnc().size());
        assertTrue(searchRequest.getInformedOnc().iterator().next());
    }

    @Test
    public void normalize_atlContactedTrue_normalizesToTrue() {
        ComplaintSearchRequest searchRequest = ComplaintSearchRequest.builder()
                .oncAtlContactedStrings(Stream.of("TRUE").collect(Collectors.toSet()))
                .build();
        normalizer.normalize(searchRequest);

        assertEquals(1, searchRequest.getOncAtlContacted().size());
        assertTrue(searchRequest.getOncAtlContacted().iterator().next());
    }

    @Test
    public void normalize_atlContactedYes_normalizesToTrue() {
        ComplaintSearchRequest searchRequest = ComplaintSearchRequest.builder()
                .oncAtlContactedStrings(Stream.of(" yes ").collect(Collectors.toSet()))
                .build();
        normalizer.normalize(searchRequest);

        assertEquals(1, searchRequest.getOncAtlContacted().size());
        assertTrue(searchRequest.getOncAtlContacted().iterator().next());
    }

    @Test
    public void normalize_atlContactedFalse_normalizesToFalse() {
        ComplaintSearchRequest searchRequest = ComplaintSearchRequest.builder()
                .oncAtlContactedStrings(Stream.of("FALSE").collect(Collectors.toSet()))
                .build();
        normalizer.normalize(searchRequest);

        assertEquals(1, searchRequest.getOncAtlContacted().size());
        assertFalse(searchRequest.getOncAtlContacted().iterator().next());
    }

    @Test
    public void normalize_atlContactedNo_normalizesToFalse() {
        ComplaintSearchRequest searchRequest = ComplaintSearchRequest.builder()
                .oncAtlContactedStrings(Stream.of(" no ").collect(Collectors.toSet()))
                .build();
        normalizer.normalize(searchRequest);

        assertEquals(1, searchRequest.getOncAtlContacted().size());
        assertFalse(searchRequest.getOncAtlContacted().iterator().next());
    }

    @Test
    public void normalize_atlContactedBlank_normalizesToEmptySet() {
        ComplaintSearchRequest searchRequest = ComplaintSearchRequest.builder()
                .oncAtlContactedStrings(Stream.of("  ").collect(Collectors.toSet()))
                .build();
        normalizer.normalize(searchRequest);

        assertEquals(0, searchRequest.getOncAtlContacted().size());
    }

    @Test
    public void normalize_atlContactedNull_normalizesToEmptySet() {
        ComplaintSearchRequest searchRequest = ComplaintSearchRequest.builder()
                .oncAtlContactedStrings(null)
                .build();
        normalizer.normalize(searchRequest);

        assertEquals(0, searchRequest.getOncAtlContacted().size());
    }

    @Test
    public void normalize_atlContactedSomeValidSomeInvalid_normalizesToValidBooleans() {
        ComplaintSearchRequest searchRequest = ComplaintSearchRequest.builder()
                .oncAtlContactedStrings(Stream.of("  ", "yes", "FALSE", "123").collect(Collectors.toSet()))
                .build();
        normalizer.normalize(searchRequest);

        assertEquals(2, searchRequest.getOncAtlContacted().size());
    }

    @Test
    public void normalize_complainantContactedTrue_normalizesToTrue() {
        ComplaintSearchRequest searchRequest = ComplaintSearchRequest.builder()
                .complainantContactedStrings(Stream.of("TRUE").collect(Collectors.toSet()))
                .build();
        normalizer.normalize(searchRequest);

        assertEquals(1, searchRequest.getComplainantContacted().size());
        assertTrue(searchRequest.getComplainantContacted().iterator().next());
    }

    @Test
    public void normalize_complainantContactedYes_normalizesToTrue() {
        ComplaintSearchRequest searchRequest = ComplaintSearchRequest.builder()
                .complainantContactedStrings(Stream.of(" yes ").collect(Collectors.toSet()))
                .build();
        normalizer.normalize(searchRequest);

        assertEquals(1, searchRequest.getComplainantContacted().size());
        assertTrue(searchRequest.getComplainantContacted().iterator().next());
    }

    @Test
    public void normalize_complainantContactedFalse_normalizesToFalse() {
        ComplaintSearchRequest searchRequest = ComplaintSearchRequest.builder()
                .complainantContactedStrings(Stream.of("FALSE").collect(Collectors.toSet()))
                .build();
        normalizer.normalize(searchRequest);

        assertEquals(1, searchRequest.getComplainantContacted().size());
        assertFalse(searchRequest.getComplainantContacted().iterator().next());
    }

    @Test
    public void normalize_complainantContactedNo_normalizesToFalse() {
        ComplaintSearchRequest searchRequest = ComplaintSearchRequest.builder()
                .complainantContactedStrings(Stream.of(" no ").collect(Collectors.toSet()))
                .build();
        normalizer.normalize(searchRequest);

        assertEquals(1, searchRequest.getComplainantContacted().size());
        assertFalse(searchRequest.getComplainantContacted().iterator().next());
    }

    @Test
    public void normalize_complainantContactedBlank_normalizesToEmptySet() {
        ComplaintSearchRequest searchRequest = ComplaintSearchRequest.builder()
                .complainantContactedStrings(Stream.of("  ").collect(Collectors.toSet()))
                .build();
        normalizer.normalize(searchRequest);

        assertEquals(0, searchRequest.getComplainantContacted().size());
    }

    @Test
    public void normalize_complainantContactedNull_normalizesToEmptySet() {
        ComplaintSearchRequest searchRequest = ComplaintSearchRequest.builder()
                .complainantContactedStrings(null)
                .build();
        normalizer.normalize(searchRequest);

        assertEquals(0, searchRequest.getComplainantContacted().size());
    }

    @Test
    public void normalize_complainantContactedSomeValidSomeInvalid_normalizesToValidBooleans() {
        ComplaintSearchRequest searchRequest = ComplaintSearchRequest.builder()
                .complainantContactedStrings(Stream.of("  ", "yes", "FALSE", "123").collect(Collectors.toSet()))
                .build();
        normalizer.normalize(searchRequest);

        assertEquals(2, searchRequest.getComplainantContacted().size());
    }

    @Test
    public void normalize_developerContactedTrue_normalizesToTrue() {
        ComplaintSearchRequest searchRequest = ComplaintSearchRequest.builder()
                .developerContactedStrings(Stream.of("TRUE").collect(Collectors.toSet()))
                .build();
        normalizer.normalize(searchRequest);

        assertEquals(1, searchRequest.getDeveloperContacted().size());
        assertTrue(searchRequest.getDeveloperContacted().iterator().next());
    }

    @Test
    public void normalize_developerContactedYes_normalizesToTrue() {
        ComplaintSearchRequest searchRequest = ComplaintSearchRequest.builder()
                .developerContactedStrings(Stream.of(" yes ").collect(Collectors.toSet()))
                .build();
        normalizer.normalize(searchRequest);

        assertEquals(1, searchRequest.getDeveloperContacted().size());
        assertTrue(searchRequest.getDeveloperContacted().iterator().next());
    }

    @Test
    public void normalize_developerContactedFalse_normalizesToFalse() {
        ComplaintSearchRequest searchRequest = ComplaintSearchRequest.builder()
                .developerContactedStrings(Stream.of("FALSE").collect(Collectors.toSet()))
                .build();
        normalizer.normalize(searchRequest);

        assertEquals(1, searchRequest.getDeveloperContacted().size());
        assertFalse(searchRequest.getDeveloperContacted().iterator().next());
    }

    @Test
    public void normalize_developerContactedNo_normalizesToFalse() {
        ComplaintSearchRequest searchRequest = ComplaintSearchRequest.builder()
                .developerContactedStrings(Stream.of(" no ").collect(Collectors.toSet()))
                .build();
        normalizer.normalize(searchRequest);

        assertEquals(1, searchRequest.getDeveloperContacted().size());
        assertFalse(searchRequest.getDeveloperContacted().iterator().next());
    }

    @Test
    public void normalize_developerContactedBlank_normalizesToEmptySet() {
        ComplaintSearchRequest searchRequest = ComplaintSearchRequest.builder()
                .developerContactedStrings(Stream.of("  ").collect(Collectors.toSet()))
                .build();
        normalizer.normalize(searchRequest);

        assertEquals(0, searchRequest.getDeveloperContacted().size());
    }

    @Test
    public void normalize_developerContactedNull_normalizesToEmptySet() {
        ComplaintSearchRequest searchRequest = ComplaintSearchRequest.builder()
                .developerContactedStrings(null)
                .build();
        normalizer.normalize(searchRequest);

        assertEquals(0, searchRequest.getDeveloperContacted().size());
    }

    @Test
    public void normalize_developerContactedSomeValidSomeInvalid_normalizesToValidBooleans() {
        ComplaintSearchRequest searchRequest = ComplaintSearchRequest.builder()
                .developerContactedStrings(Stream.of("  ", "yes", "FALSE", "123").collect(Collectors.toSet()))
                .build();
        normalizer.normalize(searchRequest);

        assertEquals(2, searchRequest.getDeveloperContacted().size());
    }

    @Test
    public void normalize_certificationBodies_trimsNamesAndGetsAcbIds() {
        Mockito.when(acbDao.getByName(ArgumentMatchers.eq("ICSA Labs")))
            .thenReturn(CertificationBodyDTO.builder()
                    .id(1L)
                    .name("ICSA Labs")
                    .build());
        Mockito.when(acbDao.getByName(ArgumentMatchers.eq("Drummond")))
            .thenReturn(CertificationBodyDTO.builder()
                .id(2L)
                .name("Drummond")
                .build());
        Mockito.when(acbDao.getByName(ArgumentMatchers.eq("test")))
            .thenReturn(null);

        ComplaintSearchRequest searchRequest = ComplaintSearchRequest.builder()
                .certificationBodyNames(Stream.of("ICSA Labs ", " Drummond ", "", " ", null, "test").collect(Collectors.toSet()))
                .build();
        normalizer.normalize(searchRequest);

        assertEquals(3, searchRequest.getCertificationBodyNames().size());
        assertTrue(searchRequest.getCertificationBodyNames().contains("ICSA Labs"));
        assertTrue(searchRequest.getCertificationBodyNames().contains("Drummond"));
        assertTrue(searchRequest.getCertificationBodyNames().contains("test"));

        assertEquals(2, searchRequest.getAcbIds().size());
        assertTrue(searchRequest.getAcbIds().contains(1L));
        assertTrue(searchRequest.getAcbIds().contains(2L));
    }

    @Test
    public void normalize_certificationBodyNamesAndAcbIds_trimsNamesAndUnionsAcbIds() {
        Mockito.when(acbDao.getByName(ArgumentMatchers.eq("ICSA Labs")))
            .thenReturn(CertificationBodyDTO.builder()
                    .id(1L)
                    .name("ICSA Labs")
                    .build());
        Mockito.when(acbDao.getByName(ArgumentMatchers.eq("Drummond")))
            .thenReturn(CertificationBodyDTO.builder()
                .id(2L)
                .name("Drummond")
                .build());
        Mockito.when(acbDao.getByName(ArgumentMatchers.eq("test")))
            .thenReturn(null);

        ComplaintSearchRequest searchRequest = ComplaintSearchRequest.builder()
                .certificationBodyNames(Stream.of("ICSA Labs ", " Drummond ", "", " ", null, "test").collect(Collectors.toSet()))
                .acbIds(Stream.of(1L, 3L).collect(Collectors.toCollection(HashSet::new)))
                .build();
        normalizer.normalize(searchRequest);

        assertEquals(3, searchRequest.getCertificationBodyNames().size());
        assertTrue(searchRequest.getCertificationBodyNames().contains("ICSA Labs"));
        assertTrue(searchRequest.getCertificationBodyNames().contains("Drummond"));
        assertTrue(searchRequest.getCertificationBodyNames().contains("test"));

        assertEquals(3, searchRequest.getAcbIds().size());
        assertTrue(searchRequest.getAcbIds().contains(1L));
        assertTrue(searchRequest.getAcbIds().contains(2L));
        assertTrue(searchRequest.getAcbIds().contains(3L));
    }

    @Test
    public void normalize_currentStatusNames_normalizesCorrectly() {
        ComplaintSearchRequest searchRequest = ComplaintSearchRequest.builder()
                .currentStatusNames(Stream.of("Open ", " Closed   ", "", " ", null, "name").collect(Collectors.toSet()))
                .build();
        normalizer.normalize(searchRequest);

        assertEquals(3, searchRequest.getCurrentStatusNames().size());
        assertTrue(searchRequest.getCurrentStatusNames().contains("Open"));
        assertTrue(searchRequest.getCurrentStatusNames().contains("Closed"));
        assertTrue(searchRequest.getCurrentStatusNames().contains("name"));
    }

    @Test
    public void normalize_closedDateStartNull_noEffect() {
        ComplaintSearchRequest searchRequest = ComplaintSearchRequest.builder()
                .closedDateStart(null)
                .build();
        normalizer.normalize(searchRequest);

        assertNull(null, searchRequest.getClosedDateStart());
    }

    @Test
    public void normalize_closedDateStartWhitespace_normalizesCorrectly() {
        ComplaintSearchRequest searchRequest = ComplaintSearchRequest.builder()
                .closedDateStart(" ")
                .build();
        normalizer.normalize(searchRequest);

        assertEquals("", searchRequest.getClosedDateStart());
    }

    @Test
    public void normalize_closedDateStartWhitespaceAround_normalizesCorrectly() {
        ComplaintSearchRequest searchRequest = ComplaintSearchRequest.builder()
                .closedDateStart(" 2015-01-01 ")
                .build();
        normalizer.normalize(searchRequest);

        assertEquals("2015-01-01", searchRequest.getClosedDateStart());
    }

    @Test
    public void normalize_closedDateEndNull_noEffect() {
        ComplaintSearchRequest searchRequest = ComplaintSearchRequest.builder()
                .closedDateEnd(null)
                .build();
        normalizer.normalize(searchRequest);

        assertNull(null, searchRequest.getClosedDateEnd());
    }

    @Test
    public void normalize_closedDateEndWhitespace_normalizesCorrectly() {
        ComplaintSearchRequest searchRequest = ComplaintSearchRequest.builder()
                .closedDateEnd(" ")
                .build();
        normalizer.normalize(searchRequest);

        assertEquals("", searchRequest.getClosedDateEnd());
    }

    @Test
    public void normalize_closedDateEndWhitespaceAround_normalizesCorrectly() {
        ComplaintSearchRequest searchRequest = ComplaintSearchRequest.builder()
                .closedDateEnd(" 2015-01-01 ")
                .build();
        normalizer.normalize(searchRequest);

        assertEquals("2015-01-01", searchRequest.getClosedDateEnd());
    }

    @Test
    public void normalize_receivedDateStartNull_noEffect() {
        ComplaintSearchRequest searchRequest = ComplaintSearchRequest.builder()
                .receivedDateStart(null)
                .build();
        normalizer.normalize(searchRequest);

        assertNull(null, searchRequest.getReceivedDateStart());
    }

    @Test
    public void normalize_receivedDateStartWhitespace_normalizesCorrectly() {
        ComplaintSearchRequest searchRequest = ComplaintSearchRequest.builder()
                .receivedDateStart(" ")
                .build();
        normalizer.normalize(searchRequest);

        assertEquals("", searchRequest.getReceivedDateStart());
    }

    @Test
    public void normalize_receivedDateStartWhitespaceAround_normalizesCorrectly() {
        ComplaintSearchRequest searchRequest = ComplaintSearchRequest.builder()
                .receivedDateStart(" 2015-01-01 ")
                .build();
        normalizer.normalize(searchRequest);

        assertEquals("2015-01-01", searchRequest.getReceivedDateStart());
    }

    @Test
    public void normalize_receivedDateEndNull_noEffect() {
        ComplaintSearchRequest searchRequest = ComplaintSearchRequest.builder()
                .receivedDateEnd(null)
                .build();
        normalizer.normalize(searchRequest);

        assertNull(null, searchRequest.getReceivedDateEnd());
    }

    @Test
    public void normalize_receivedDateEndWhitespace_normalizesCorrectly() {
        ComplaintSearchRequest searchRequest = ComplaintSearchRequest.builder()
                .receivedDateEnd(" ")
                .build();
        normalizer.normalize(searchRequest);

        assertEquals("", searchRequest.getReceivedDateEnd());
    }

    @Test
    public void normalize_receivedDateEndWhitespaceAround_normalizesCorrectly() {
        ComplaintSearchRequest searchRequest = ComplaintSearchRequest.builder()
                .receivedDateEnd(" 2015-01-01 ")
                .build();
        normalizer.normalize(searchRequest);

        assertEquals("2015-01-01", searchRequest.getReceivedDateEnd());
    }

    @Test
    public void normalize_openDuringStartNull_noEffect() {
        ComplaintSearchRequest searchRequest = ComplaintSearchRequest.builder()
                .openDuringRangeStart(null)
                .build();
        normalizer.normalize(searchRequest);

        assertNull(null, searchRequest.getOpenDuringRangeStart());
    }

    @Test
    public void normalize_openDuringStartWhitespace_normalizesCorrectly() {
        ComplaintSearchRequest searchRequest = ComplaintSearchRequest.builder()
                .openDuringRangeStart(" ")
                .build();
        normalizer.normalize(searchRequest);

        assertEquals("", searchRequest.getOpenDuringRangeStart());
    }

    @Test
    public void normalize_openDuringStartWhitespaceAround_normalizesCorrectly() {
        ComplaintSearchRequest searchRequest = ComplaintSearchRequest.builder()
                .openDuringRangeStart(" 2015-01-01 ")
                .build();
        normalizer.normalize(searchRequest);

        assertEquals("2015-01-01", searchRequest.getOpenDuringRangeStart());
    }

    @Test
    public void normalize_openDuringEndNull_noEffect() {
        ComplaintSearchRequest searchRequest = ComplaintSearchRequest.builder()
                .openDuringRangeEnd(null)
                .build();
        normalizer.normalize(searchRequest);

        assertNull(null, searchRequest.getOpenDuringRangeEnd());
    }

    @Test
    public void normalize_openDuringEndWhitespace_normalizesCorrectly() {
        ComplaintSearchRequest searchRequest = ComplaintSearchRequest.builder()
                .openDuringRangeEnd(" ")
                .build();
        normalizer.normalize(searchRequest);

        assertEquals("", searchRequest.getOpenDuringRangeEnd());
    }

    @Test
    public void normalize_openDuringEndWhitespaceAround_normalizesCorrectly() {
        ComplaintSearchRequest searchRequest = ComplaintSearchRequest.builder()
                .openDuringRangeEnd(" 2015-01-01 ")
                .build();
        normalizer.normalize(searchRequest);

        assertEquals("2015-01-01", searchRequest.getOpenDuringRangeEnd());
    }

    @Test
    public void normalize_orderByStringValid_resolvesCorrectly() {
        ComplaintSearchRequest searchRequest = ComplaintSearchRequest.builder()
                .orderByString("ACB_COMPLAINT_ID")
                .build();
        normalizer.normalize(searchRequest);

        assertEquals(OrderByOption.ACB_COMPLAINT_ID, searchRequest.getOrderBy());
    }

    @Test
    public void normalize_orderByStringLowercase_resolvesCorrectly() {
        ComplaintSearchRequest searchRequest = ComplaintSearchRequest.builder()
                .orderByString("acb_complaint_id")
                .build();
        normalizer.normalize(searchRequest);

        assertEquals(OrderByOption.ACB_COMPLAINT_ID, searchRequest.getOrderBy());
    }

    @Test
    public void normalize_orderByAcbComplaintId_noChanges() {
        ComplaintSearchRequest searchRequest = ComplaintSearchRequest.builder()
                .orderBy(OrderByOption.ACB_COMPLAINT_ID)
                .build();
        normalizer.normalize(searchRequest);

        assertEquals(OrderByOption.ACB_COMPLAINT_ID, searchRequest.getOrderBy());
    }

    @Test
    public void normalize_orderByStringInvalid_setsFieldNull() {
        ComplaintSearchRequest searchRequest = ComplaintSearchRequest.builder()
                .orderByString("NOTVALID")
                .build();
        normalizer.normalize(searchRequest);

        assertNull(searchRequest.getOrderBy());
    }

}
