package gov.healthit.chpl.service;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import com.fasterxml.jackson.databind.ObjectMapper;

import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.service.CertificationCriterionService.Criteria2015;

public class CuresUpdateServiceTest {
    private CuresUpdateService curesUpdateService;
    private ObjectMapper jsonMapper;

    private CertificationCriterion b1Old, b1Cures, d12, d13, g4, g5;

    @Before
    public void setup() {
        CertificationCriterionService ccs = Mockito.mock(CertificationCriterionService.class);
        b1Old = buildCriterion(16L, "170.315 (b)(1)", "B1", true);
        b1Cures = buildCriterion(165L, "170.315 (b)(1)", "b1 title (Cures Update)", false);
        d12 = buildCriterion(176L, "170.315 (d)(12)", "D12 (Cures Update)");
        d13 = buildCriterion(177L, "170.315 (d)(13)", "D13 (Cures Update)");
        g4 = buildCriterion(53L, "170.315 (g)(4)", "G4");
        g5 = buildCriterion(54L, "170.315 (g)(5)", "G5");

        Mockito.when(ccs.get(ArgumentMatchers.eq(Criteria2015.B_6)))
            .thenReturn(buildCriterion(21L, "170.315 (b)(6)", "B6"));
        Mockito.when(ccs.get(ArgumentMatchers.eq(Criteria2015.B_1_OLD))).thenReturn(b1Old);
        Mockito.when(ccs.get(ArgumentMatchers.eq(Criteria2015.B_2_OLD)))
            .thenReturn(buildCriterion(17L, "170.315 (b)(2)", "B2"));
        Mockito.when(ccs.get(ArgumentMatchers.eq(Criteria2015.B_3_OLD)))
            .thenReturn(buildCriterion(18L, "170.315 (b)(3)", "B3"));
        Mockito.when(ccs.get(ArgumentMatchers.eq(Criteria2015.B_7_OLD)))
            .thenReturn(buildCriterion(22L, "170.315 (b)(7)", "B7"));
        Mockito.when(ccs.get(ArgumentMatchers.eq(Criteria2015.B_8_OLD)))
            .thenReturn(buildCriterion(23L, "170.315 (b)(8)", "B8"));
        Mockito.when(ccs.get(ArgumentMatchers.eq(Criteria2015.B_9_OLD)))
            .thenReturn(buildCriterion(24L, "170.315 (b)(9)", "B9"));
        Mockito.when(ccs.get(ArgumentMatchers.eq(Criteria2015.C_3_OLD)))
            .thenReturn(buildCriterion(27L, "170.315 (c)(3)", "C3"));
        Mockito.when(ccs.get(ArgumentMatchers.eq(Criteria2015.D_2_OLD)))
            .thenReturn(buildCriterion(30L, "170.315 (d)(2)", "D2"));
        Mockito.when(ccs.get(ArgumentMatchers.eq(Criteria2015.D_3_OLD)))
            .thenReturn(buildCriterion(31L, "170.315 (d)(3)", "D3"));
        Mockito.when(ccs.get(ArgumentMatchers.eq(Criteria2015.D_10_OLD)))
            .thenReturn(buildCriterion(38L, "170.315 (d)(10)", "D10"));
        Mockito.when(ccs.get(ArgumentMatchers.eq(Criteria2015.E_1_OLD)))
            .thenReturn(buildCriterion(40L, "170.315 (e)(1)", "E1"));
        Mockito.when(ccs.get(ArgumentMatchers.eq(Criteria2015.F_5_OLD)))
            .thenReturn(buildCriterion(47L, "170.315 (f)(5)", "F5"));
        Mockito.when(ccs.get(ArgumentMatchers.eq(Criteria2015.G_6_OLD)))
            .thenReturn(buildCriterion(55L, "170.315 (g)(6)", "G6"));
        Mockito.when(ccs.get(ArgumentMatchers.eq(Criteria2015.G_8)))
            .thenReturn(buildCriterion(57L, "170.315 (g)(8)", "G8"));
        Mockito.when(ccs.get(ArgumentMatchers.eq(Criteria2015.G_9_OLD)))
            .thenReturn(buildCriterion(58L, "170.315 (g)(9)", "G9"));
        Mockito.when(ccs.get(ArgumentMatchers.eq(Criteria2015.D_12))).thenReturn(d12);
        Mockito.when(ccs.get(ArgumentMatchers.eq(Criteria2015.D_13))).thenReturn(d13);
        Mockito.when(ccs.get(ArgumentMatchers.eq(Criteria2015.A_1)))
            .thenReturn(buildCriterion(1L, "170.315 (a)(1)", "A1"));
        Mockito.when(ccs.get(ArgumentMatchers.eq(Criteria2015.A_2)))
            .thenReturn(buildCriterion(2L, "170.315 (a)(2)", "A2"));
        Mockito.when(ccs.get(ArgumentMatchers.eq(Criteria2015.A_3)))
            .thenReturn(buildCriterion(3L, "170.315 (a)(3)", "A3"));
        Mockito.when(ccs.get(ArgumentMatchers.eq(Criteria2015.A_4)))
            .thenReturn(buildCriterion(4L, "170.315 (a)(4)", "A4"));
        Mockito.when(ccs.get(ArgumentMatchers.eq(Criteria2015.A_5)))
            .thenReturn(buildCriterion(5L, "170.315 (a)(5)", "A5"));
        Mockito.when(ccs.get(ArgumentMatchers.eq(Criteria2015.A_9)))
            .thenReturn(buildCriterion(9L, "170.315 (a)(9)", "A9"));
        Mockito.when(ccs.get(ArgumentMatchers.eq(Criteria2015.A_12)))
            .thenReturn(buildCriterion(12L, "170.315 (a)(12)", "A12"));
        Mockito.when(ccs.get(ArgumentMatchers.eq(Criteria2015.A_14)))
            .thenReturn(buildCriterion(14L, "170.315 (a)(14)", "A14"));
        Mockito.when(ccs.get(ArgumentMatchers.eq(Criteria2015.A_15)))
            .thenReturn(buildCriterion(15L, "170.315 (a)(15)", "A15"));
        Mockito.when(ccs.get(ArgumentMatchers.eq(Criteria2015.B_1_CURES))).thenReturn(b1Cures);
        Mockito.when(ccs.get(ArgumentMatchers.eq(Criteria2015.B_2_CURES)))
            .thenReturn(buildCriterion(166L, "170.315 (b)(2)", "B2 (Cures Update)"));
        Mockito.when(ccs.get(ArgumentMatchers.eq(Criteria2015.B_3_CURES)))
            .thenReturn(buildCriterion(167L, "170.315 (b)(3)", "B3 (Cures Update)"));
        Mockito.when(ccs.get(ArgumentMatchers.eq(Criteria2015.B_7_CURES)))
            .thenReturn(buildCriterion(168L, "170.315 (b)(7)", "B7 (Cures Update)"));
        Mockito.when(ccs.get(ArgumentMatchers.eq(Criteria2015.B_8_CURES)))
            .thenReturn(buildCriterion(169L, "170.315 (b)(8)", "B8 (Cures Update)"));
        Mockito.when(ccs.get(ArgumentMatchers.eq(Criteria2015.B_9_CURES)))
            .thenReturn(buildCriterion(170L, "170.315 (b)(9)", "B9 (Cures Update)"));
        Mockito.when(ccs.get(ArgumentMatchers.eq(Criteria2015.C_1)))
            .thenReturn(buildCriterion(25L, "170.315 (c)(1)", "C1"));
        Mockito.when(ccs.get(ArgumentMatchers.eq(Criteria2015.C_2)))
            .thenReturn(buildCriterion(26L, "170.315 (c)(2)", "C2"));
        Mockito.when(ccs.get(ArgumentMatchers.eq(Criteria2015.C_3_CURES)))
            .thenReturn(buildCriterion(172L, "170.315 (c)(3)", "C3 (Cures Update)"));
        Mockito.when(ccs.get(ArgumentMatchers.eq(Criteria2015.C_4)))
            .thenReturn(buildCriterion(27L, "170.315 (c)(4)", "C4"));
        Mockito.when(ccs.get(ArgumentMatchers.eq(Criteria2015.E_1_CURES)))
            .thenReturn(buildCriterion(178L, "170.315 (e)(1)", "E1 (Cures Update)"));
        Mockito.when(ccs.get(ArgumentMatchers.eq(Criteria2015.E_3)))
            .thenReturn(buildCriterion(42L, "170.315 (e)(3)", "E3"));
        Mockito.when(ccs.get(ArgumentMatchers.eq(Criteria2015.F_1)))
            .thenReturn(buildCriterion(43L, "170.315 (f)(1)", "F1"));
        Mockito.when(ccs.get(ArgumentMatchers.eq(Criteria2015.F_2)))
            .thenReturn(buildCriterion(44L, "170.315 (f)(2)", "F2"));
        Mockito.when(ccs.get(ArgumentMatchers.eq(Criteria2015.F_3)))
            .thenReturn(buildCriterion(45L, "170.315 (f)(3)", "F3"));
        Mockito.when(ccs.get(ArgumentMatchers.eq(Criteria2015.F_4)))
            .thenReturn(buildCriterion(46L, "170.315 (f)(4)", "F4"));
        Mockito.when(ccs.get(ArgumentMatchers.eq(Criteria2015.F_5_CURES)))
            .thenReturn(buildCriterion(179L, "170.315 (f)(5)", "F5 (Cures Update)"));
        Mockito.when(ccs.get(ArgumentMatchers.eq(Criteria2015.F_6)))
            .thenReturn(buildCriterion(47L, "170.315 (f)(6)", "F6"));
        Mockito.when(ccs.get(ArgumentMatchers.eq(Criteria2015.F_7)))
            .thenReturn(buildCriterion(48L, "170.315 (f)(7)", "F7"));
        Mockito.when(ccs.get(ArgumentMatchers.eq(Criteria2015.G_7)))
            .thenReturn(buildCriterion(56L, "170.315 (g)(7)", "G7"));
        Mockito.when(ccs.get(ArgumentMatchers.eq(Criteria2015.G_8)))
            .thenReturn(buildCriterion(57L, "170.315 (g)(8)", "G8"));
        Mockito.when(ccs.get(ArgumentMatchers.eq(Criteria2015.G_9_CURES)))
            .thenReturn(buildCriterion(181L, "170.315 (g)(9)", "G9 (Cures Update)"));
        Mockito.when(ccs.get(ArgumentMatchers.eq(Criteria2015.G_10)))
            .thenReturn(buildCriterion(182L, "170.315 (g)(10)", "G10 (Cures Update)"));
        Mockito.when(ccs.get(ArgumentMatchers.eq(Criteria2015.H_1)))
            .thenReturn(buildCriterion(59L, "170.315 (h)(1)", "H1"));
        Mockito.when(ccs.get(ArgumentMatchers.eq(Criteria2015.H_2)))
            .thenReturn(buildCriterion(60L, "170.315 (h)(2)", "H2"));
        Mockito.when(ccs.get(ArgumentMatchers.eq(Criteria2015.B_10)))
            .thenReturn(buildCriterion(171L, "170.315 (b)(10)", "B10 (Cures Update)"));
        Mockito.when(ccs.get(ArgumentMatchers.eq(Criteria2015.D_1)))
            .thenReturn(buildCriterion(29L, "170.315 (d)(1)", "D1"));
        Mockito.when(ccs.get(ArgumentMatchers.eq(Criteria2015.D_2_CURES)))
            .thenReturn(buildCriterion(173L, "170.315 (d)(2)", "D2 (Cures Update)"));
        Mockito.when(ccs.get(ArgumentMatchers.eq(Criteria2015.D_3_CURES)))
            .thenReturn(buildCriterion(174L, "170.315 (d)(3)", "D3 (Cures Update)"));
        Mockito.when(ccs.get(ArgumentMatchers.eq(Criteria2015.D_4)))
            .thenReturn(buildCriterion(32L, "170.315 (d)(4)", "D4"));
        Mockito.when(ccs.get(ArgumentMatchers.eq(Criteria2015.D_5)))
            .thenReturn(buildCriterion(33L, "170.315 (d)(5)", "D5"));
        Mockito.when(ccs.get(ArgumentMatchers.eq(Criteria2015.D_6)))
            .thenReturn(buildCriterion(34L, "170.315 (d)(6)", "D6"));
        Mockito.when(ccs.get(ArgumentMatchers.eq(Criteria2015.D_7)))
            .thenReturn(buildCriterion(35L, "170.315 (d)(7)", "D7"));
        Mockito.when(ccs.get(ArgumentMatchers.eq(Criteria2015.D_8)))
            .thenReturn(buildCriterion(36L, "170.315 (d)(8)", "D8"));
        Mockito.when(ccs.get(ArgumentMatchers.eq(Criteria2015.D_9)))
            .thenReturn(buildCriterion(37L, "170.315 (d)(9)", "D9"));
        Mockito.when(ccs.get(ArgumentMatchers.eq(Criteria2015.D_10_CURES)))
            .thenReturn(buildCriterion(175L, "170.315 (d)(10)", "D10 (Cures Update)"));
        Mockito.when(ccs.get(ArgumentMatchers.eq(Criteria2015.D_11)))
            .thenReturn(buildCriterion(39L, "170.315 (d)(11)", "D11"));
        Mockito.when(ccs.get(ArgumentMatchers.eq(Criteria2015.G_1)))
            .thenReturn(buildCriterion(50L, "170.315 (g)(1)", "G1"));
        Mockito.when(ccs.get(ArgumentMatchers.eq(Criteria2015.G_2)))
            .thenReturn(buildCriterion(51L, "170.315 (g)(2)", "G2"));
        Mockito.when(ccs.get(ArgumentMatchers.eq(Criteria2015.G_3)))
            .thenReturn(buildCriterion(52L, "170.315 (g)(3)", "G3"));
        Mockito.when(ccs.get(ArgumentMatchers.eq(Criteria2015.G_4))).thenReturn(g4);
        Mockito.when(ccs.get(ArgumentMatchers.eq(Criteria2015.G_5))).thenReturn(g5);
        Mockito.when(ccs.get(ArgumentMatchers.eq(Criteria2015.G_6_CURES)))
            .thenReturn(buildCriterion(180L, "170.315 (g)(6)", "G6 (Cures Update)"));

        jsonMapper = new ObjectMapper();
        curesUpdateService = new CuresUpdateService(ccs);
        curesUpdateService.postConstruct();
    }

    @Ignore
    @Test
    public void isListingCuresUpdate() throws IOException, FileNotFoundException {
        String listingDetailsJson = null;
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("listingDetails.json");
        try {
            listingDetailsJson = IOUtils.toString(inputStream, "UTF-8");
        } finally {
            inputStream.close();
            IOUtils.closeQuietly(inputStream);
        }
        assertNotNull(listingDetailsJson);

        //convert string to cpd
        CertifiedProductSearchDetails listing = null;
        if (listingDetailsJson != null) {
            try {
                listing = jsonMapper.readValue(listingDetailsJson, CertifiedProductSearchDetails.class);
            } catch (final Exception ex) {
                fail("Could not parse string as details object", ex);
            }
        }

        boolean isCuresUpdate = curesUpdateService.isCuresUpdate(listing);
        assertTrue(isCuresUpdate);
    }

    @Test
    public void listingWithB1Removed_ReturnsNotCuresUpdate() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(b1Old)
                        .build())
                .build();
        assertFalse(curesUpdateService.isCuresUpdate(listing));
    }

    @Test
    public void listingWithB1Cures_ReturnsNotCuresUpdate() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(b1Cures)
                        .build())
                .build();
        assertFalse(curesUpdateService.isCuresUpdate(listing));
    }

    @Test
    public void listingWithB1CuresD12_ReturnsNotCuresUpdate() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(b1Cures)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(d12)
                        .build())
                .build();
        assertFalse(curesUpdateService.isCuresUpdate(listing));
    }

    @Test
    public void listingWithB1CuresD13_ReturnsNotCuresUpdate() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(b1Cures)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(d13)
                        .build())
                .build();
        assertFalse(curesUpdateService.isCuresUpdate(listing));
    }

    @Test
    public void listingWithB1CuresD12D13_ReturnsCuresUpdate() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(b1Cures)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(d12)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(d13)
                        .build())
                .build();
        assertTrue(curesUpdateService.isCuresUpdate(listing));
    }

    @Test
    public void listingHasDependentCriteriaG4G5_ReturnsCuresUpdate() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(g4)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(g5)
                        .build())
                .build();
        assertTrue(curesUpdateService.isCuresUpdate(listing));
    }

    private CertificationCriterion buildCriterion(Long id, String number, String title) {
        return CertificationCriterion.builder()
                .id(id)
                .number(number)
                .title(title)
                .removed(false)
                .build();
    }

    private CertificationCriterion buildCriterion(Long id, String number, String title, boolean removed) {
        return CertificationCriterion.builder()
                .id(id)
                .number(number)
                .title(title)
                .removed(removed)
                .build();
    }
}
