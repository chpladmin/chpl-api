package gov.healthit.chpl.upload.listing.normalizer;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;

import org.junit.Before;
import org.junit.Test;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;

public class RwtNormalizerTest {

    private RwtNormalizer normalizer;

    @Before
    public void setup() {
        normalizer = new RwtNormalizer();
    }

    @Test
    public void normalize_nullRwtFields_noChanges() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .rwtPlansUrl(null)
                .rwtPlansCheckDate(null)
                .rwtResultsUrl(null)
                .rwtResultsCheckDate(null)
                .build();
        normalizer.normalize(listing);
        assertNull(listing.getRwtPlansUrl());
        assertNull(listing.getRwtPlansCheckDate());
        assertNull(listing.getRwtResultsUrl());
        assertNull(listing.getRwtResultsCheckDate());
    }

    @Test
    public void normalize_emptyRwtUrls_noChanges() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .rwtPlansUrl("")
                .rwtPlansCheckDate(null)
                .rwtResultsUrl("")
                .rwtResultsCheckDate(null)
                .build();
        normalizer.normalize(listing);
        assertEquals("", listing.getRwtPlansUrl());
        assertNull(listing.getRwtPlansCheckDate());
        assertEquals("", listing.getRwtResultsUrl());
        assertNull(listing.getRwtResultsCheckDate());
    }

    @Test
    public void normalize_plansDateMatchesDateFormat1_setsDate() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .userEnteredRwtPlansCheckDate("01/01/2024")
                .build();

        normalizer.normalize(listing);
        assertEquals(LocalDate.parse("2024-01-01"), listing.getRwtPlansCheckDate());
    }

    @Test
    public void normalize_plansDateMatchesDateFormat2_setsDate() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .userEnteredRwtPlansCheckDate("01-01-2024")
                .build();

        normalizer.normalize(listing);
        assertEquals(LocalDate.parse("2024-01-01"), listing.getRwtPlansCheckDate());
    }

    @Test
    public void normalize_plansDateMatchesDateFormat3_setsDate() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .userEnteredRwtPlansCheckDate("2024-01-01")
                .build();

        normalizer.normalize(listing);
        assertEquals(LocalDate.parse("2024-01-01"), listing.getRwtPlansCheckDate());
    }

    @Test
    public void normalize_plansDateMatchesDateFormat4_setsDate() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .userEnteredRwtPlansCheckDate("Jan 1, 2024")
                .build();

        normalizer.normalize(listing);
        assertEquals(LocalDate.parse("2024-01-01"), listing.getRwtPlansCheckDate());
    }

    @Test
    public void normalize_plansDateMatchesDateFormat5_setsDate() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .userEnteredRwtPlansCheckDate("Jan 01, 2024")
                .build();

        normalizer.normalize(listing);
        assertEquals(LocalDate.parse("2024-01-01"), listing.getRwtPlansCheckDate());
    }

    @Test
    public void normalize_plansDateMatchesDateFormat6_setsDate() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .userEnteredRwtPlansCheckDate("January 1, 2024")
                .build();

        normalizer.normalize(listing);
        assertEquals(LocalDate.parse("2024-01-01"), listing.getRwtPlansCheckDate());
    }

    @Test
    public void normalize_plansDateMatchesDateFormat7_setsDate() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .userEnteredRwtPlansCheckDate("January 01, 2024")
                .build();

        normalizer.normalize(listing);
        assertEquals(LocalDate.parse("2024-01-01"), listing.getRwtPlansCheckDate());
    }

    @Test
    public void normalize_resultsDateMatchesDateFormat1_setsDate() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .userEnteredRwtResultsCheckDate("01/01/2024")
                .build();

        normalizer.normalize(listing);
        assertEquals(LocalDate.parse("2024-01-01"), listing.getRwtResultsCheckDate());
    }

    @Test
    public void normalize_resultsDateMatchesDateFormat2_setsDate() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .userEnteredRwtResultsCheckDate("01-01-2024")
                .build();

        normalizer.normalize(listing);
        assertEquals(LocalDate.parse("2024-01-01"), listing.getRwtResultsCheckDate());
    }

    @Test
    public void normalize_resultsDateMatchesDateFormat3_setsDate() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .userEnteredRwtResultsCheckDate("2024-01-01")
                .build();

        normalizer.normalize(listing);
        assertEquals(LocalDate.parse("2024-01-01"), listing.getRwtResultsCheckDate());
    }

    @Test
    public void normalize_resultsDateMatchesDateFormat4_setsDate() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .userEnteredRwtResultsCheckDate("Jan 1, 2024")
                .build();

        normalizer.normalize(listing);
        assertEquals(LocalDate.parse("2024-01-01"), listing.getRwtResultsCheckDate());
    }

    @Test
    public void normalize_resultsDateMatchesDateFormat5_setsDate() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .userEnteredRwtResultsCheckDate("Jan 01, 2024")
                .build();

        normalizer.normalize(listing);
        assertEquals(LocalDate.parse("2024-01-01"), listing.getRwtResultsCheckDate());
    }

    @Test
    public void normalize_resultsDateMatchesDateFormat6_setsDate() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .userEnteredRwtResultsCheckDate("January 1, 2024")
                .build();

        normalizer.normalize(listing);
        assertEquals(LocalDate.parse("2024-01-01"), listing.getRwtResultsCheckDate());
    }

    @Test
    public void normalize_resultsDateMatchesDateFormat7_setsDate() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .userEnteredRwtResultsCheckDate("January 01, 2024")
                .build();

        normalizer.normalize(listing);
        assertEquals(LocalDate.parse("2024-01-01"), listing.getRwtResultsCheckDate());
    }
}
