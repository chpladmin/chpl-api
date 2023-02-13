package gov.healthit.chpl.scheduler.job.ics.reviewer;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import gov.healthit.chpl.dao.ListingGraphDAO;
import gov.healthit.chpl.domain.CertifiedProduct;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.InheritedCertificationStatus;

public class IncorrectIcsIncrementReviewerTest {
    private static final String ERROR_MESSAGE = "ICS increment is %s but should be %s";

    private ListingGraphDAO listingGraphDao;
    private IncorrectIcsIncrementReviewer reviewer;

    @Before
    @SuppressWarnings("checkstyle:magicnumber")
    public void setup() {
        listingGraphDao = Mockito.mock(ListingGraphDAO.class);
        reviewer = new IncorrectIcsIncrementReviewer(listingGraphDao, ERROR_MESSAGE);
    }

    @Test
    public void review_listingWithoutIcs_noError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .id(1L)
                .chplProductNumber("15.02.05.1439.A111.01.00.1.200219")
                .ics(InheritedCertificationStatus.builder()
                        .inherits(false)
                        .build())
                .build();
        String errorMessage = reviewer.getIcsError(listing);
        assertNull(errorMessage);
    }

    @Test
    public void review_listingWithIcs_noParents_noError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .id(2L)
                .chplProductNumber("15.02.05.1439.A111.01.01.1.200219")
                .ics(InheritedCertificationStatus.builder()
                        .inherits(true)
                        .build())
                .build();
        String errorMessage = reviewer.getIcsError(listing);
        assertNull(errorMessage);
    }

    @Test
    public void review_listingWithIcsAndCorrectIncrement_hasParents_noError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .id(2L)
                .chplProductNumber("15.02.05.1439.A111.01.01.1.200219")
                .ics(InheritedCertificationStatus.builder()
                        .inherits(true)
                        .parents(Stream.of(CertifiedProduct.builder()
                                .id(1L)
                                .chplProductNumber("15.02.05.1439.A111.01.00.1.200219")
                                .build()).toList())
                        .build())
                .build();
        Mockito.when(listingGraphDao.getLargestIcs(ArgumentMatchers.anyList()))
            .thenReturn(0);
        String errorMessage = reviewer.getIcsError(listing);
        assertNull(errorMessage);
    }

    @Test
    public void review_listingWithIcsAndIncorrectIncrement_hasParents_noError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .id(2L)
                .chplProductNumber("15.02.05.1439.A111.01.02.1.200219")
                .ics(InheritedCertificationStatus.builder()
                        .inherits(true)
                        .parents(Stream.of(CertifiedProduct.builder()
                                .id(1L)
                                .chplProductNumber("15.02.05.1439.A111.01.00.1.200219")
                                .build()).toList())
                        .build())
                .build();
        Mockito.when(listingGraphDao.getLargestIcs(ArgumentMatchers.anyList()))
            .thenReturn(0);

        String errorMessage = reviewer.getIcsError(listing);
        assertNotNull(errorMessage);
        assertEquals(String.format(ERROR_MESSAGE, "02", "01"), errorMessage);
    }
}
