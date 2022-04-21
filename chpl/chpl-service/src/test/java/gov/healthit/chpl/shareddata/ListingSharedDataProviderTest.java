package gov.healthit.chpl.shareddata;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;

public class ListingSharedDataProviderTest {
    private static final Long ALT_ID = 5l;

    private SharedDataDAO sharedDataDAO;
    private ListingSharedDataProvider listingSharedDataProvider;

    @Before
    public void setup() {
        sharedDataDAO = Mockito.mock(SharedDataDAO.class);

        listingSharedDataProvider = new ListingSharedDataProvider(sharedDataDAO, 1);
    }

    @Test
    public void get_ObjectNotInSharedData_ObjectReturned() {
        Mockito.when(sharedDataDAO.get(ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
                .thenReturn(null);

        CertifiedProductSearchDetails listing = listingSharedDataProvider.get(1L, () -> getListing(ALT_ID));

        assertEquals(ALT_ID, listing.getId());
    }

    @Test
    public void get_ObjectInSharedData_ObjectReturned() {
        Mockito.when(sharedDataDAO.get(ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
                .thenReturn(SharedData.builder()
                        .domain("gov.healthit.chpl.domain.CertifiedProductSearchDetails")
                        .key("1")
                        .value("{\"id\": 1}")
                        .putDate(LocalDateTime.now().minusMinutes(1))
                        .build());

        CertifiedProductSearchDetails listing = listingSharedDataProvider.get(1L, () -> getNullListing());

        assertEquals(1L, listing.getId());
    }

    @Test
    public void get_ObjectInSharedDataIsExpired_ObjectReturned() {
        Mockito.when(sharedDataDAO.get(ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
                .thenReturn(SharedData.builder()
                        .domain("gov.healthit.chpl.domain.CertifiedProductSearchDetails")
                        .key("1")
                        .value("{\"id\": 1}")
                        .putDate(LocalDateTime.now().minusHours(5))
                        .build());

        CertifiedProductSearchDetails listing = listingSharedDataProvider.get(1L, () -> getListing(ALT_ID));

        assertEquals(ALT_ID, listing.getId());
    }

    public CertifiedProductSearchDetails getListing(Long id) {
        return CertifiedProductSearchDetails.builder()
                .id(id)
                .build();
    }

    public CertifiedProductSearchDetails getNullListing() {
        return null;
    }
}
