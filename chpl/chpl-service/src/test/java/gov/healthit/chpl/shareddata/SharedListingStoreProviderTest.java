package gov.healthit.chpl.shareddata;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.sharedstore.SharedStore;
import gov.healthit.chpl.sharedstore.SharedStoreDAO;
import gov.healthit.chpl.sharedstore.listing.SharedListingStoreProvider;

public class SharedListingStoreProviderTest {
    private static final Long ALT_ID = 5L;

    private SharedStoreDAO sharedStoreDAO;
    private SharedListingStoreProvider sharedListingStoreProvider;

    @Before
    public void setup() {
        sharedStoreDAO = Mockito.mock(SharedStoreDAO.class);

        sharedListingStoreProvider = new SharedListingStoreProvider(sharedStoreDAO);
    }

    @Test
    public void get_ObjectNotInSharedData_ObjectReturned() {
        Mockito.when(sharedStoreDAO.get(ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
                .thenReturn(null);

        CertifiedProductSearchDetails listing = sharedListingStoreProvider.get(1L, () -> getListing(ALT_ID));

        assertEquals(ALT_ID, listing.getId());
    }

    @Test
    public void get_ObjectInSharedData_ObjectReturned() {
        Mockito.when(sharedStoreDAO.get(ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
                .thenReturn(SharedStore.builder()
                        .domain("gov.healthit.chpl.domain.CertifiedProductSearchDetails")
                        .key("1")
                        .value("{\"id\": 1}")
                        .putDate(LocalDateTime.now().minusMinutes(1))
                        .build());

        CertifiedProductSearchDetails listing = sharedListingStoreProvider.get(1L, () -> getListing(1L));

        assertEquals(1L, listing.getId());
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
