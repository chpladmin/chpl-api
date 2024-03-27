package gov.healthit.chpl.upload.listing.normalizer;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.codeset.CertificationResultCodeSet;
import gov.healthit.chpl.codeset.CodeSet;
import gov.healthit.chpl.codeset.CodeSetDAO;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;

public class CodeSetNormalizerTest {

    private CodeSetDAO codeSetDao;
    private CodeSetNormalizer normalizer;
    private CertificationCriterion a1, a2, a5;
    private CodeSet a1a2CodeSet, a5CodeSet;

    @Before
    public void setup() {
        a1 = CertificationCriterion.builder()
                .id(1L)
                .number("170.315 (a)(1)")
                .certificationEdition("2015")
                .build();
        a2 = CertificationCriterion.builder()
                .id(2L)
                .number("170.315 (a)(2)")
                .certificationEdition("2015")
                .build();
        a5 = CertificationCriterion.builder()
                .id(5L)
                .number("170.315 (a)(5)")
                .certificationEdition("2015")
                .build();
        a5CodeSet = CodeSet.builder()
                .id(2L)
                .startDay(LocalDate.parse("2024-03-11"))
                .requiredDay(LocalDate.parse("2024-03-12"))
                .build();
        a1a2CodeSet = CodeSet.builder()
                .id(1L)
                .startDay(LocalDate.parse("2025-03-11"))
                //setting this way in the future so the CHPL is long gone by the time any tests would start failing
                .requiredDay(LocalDate.parse("3000-12-31"))
                .build();

        codeSetDao = Mockito.mock(CodeSetDAO.class);
        Mockito.when(codeSetDao.getCodeSetCriteriaMaps()).thenReturn(buildCodeSetCriteriaMaps());

        normalizer = new CodeSetNormalizer(codeSetDao);
    }

    private Map<Long, List<CodeSet>> buildCodeSetCriteriaMaps() {
        Map<Long, List<CodeSet>> maps = new LinkedHashMap<Long, List<CodeSet>>();
        maps.put(a1.getId(), List.of(a1a2CodeSet));
        maps.put(a2.getId(), List.of(a1a2CodeSet));
        maps.put(a5.getId(), List.of(a5CodeSet));
        return maps;
    }

    @Test
    public void normalize_nullCodeSets_noChanges() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .codeSets(null)
                        .build())
                .build();
        normalizer.normalize(listing);
        assertNull(listing.getCertificationResults().get(0).getCodeSets());
    }

    @Test
    public void normalize_emptyCodeSets_noChanges() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .codeSets(new ArrayList<CertificationResultCodeSet>())
                        .build())
                .build();
        normalizer.normalize(listing);
        assertTrue(listing.getCertificationResults().get(0).getCodeSets().size() == 0);
    }

    @Test
    public void normalize_codeSetWithUnattestedCriterion_clearsCodeSet() {
        List<CertificationResultCodeSet> userEnteredCodeSets = new ArrayList<CertificationResultCodeSet>();
        userEnteredCodeSets.add(CertificationResultCodeSet.builder()
                    .codeSet(CodeSet.builder()
                            .userEnteredName(a1a2CodeSet.getName())
                            .build())
                    .build());

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .criterion(a1)
                        .success(false)
                        .codeSets(userEnteredCodeSets)
                        .build())
                .build();

        normalizer.normalize(listing);
        assertTrue(CollectionUtils.isEmpty(listing.getCertificationResults().get(0).getCodeSets()));
    }

    @Test
    public void normalize_userEnteredNameMatchesCodeSetName_setsId() {
        List<CertificationResultCodeSet> userEnteredCodeSets = new ArrayList<CertificationResultCodeSet>();
        userEnteredCodeSets.add(CertificationResultCodeSet.builder()
                    .codeSet(CodeSet.builder()
                            .userEnteredName(a1a2CodeSet.getName())
                            .build())
                    .build());

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .criterion(a1)
                        .success(true)
                        .codeSets(userEnteredCodeSets)
                        .build())
                .build();

        normalizer.normalize(listing);
        assertEquals(a1a2CodeSet.getId(),
                listing.getCertificationResults().get(0).getCodeSets().get(0).getCodeSet().getId());
    }

    @Test
    public void normalize_userEnteredNameMatchesDateFormatMMyyyy_setsId() {
        List<CertificationResultCodeSet> userEnteredCodeSets = new ArrayList<CertificationResultCodeSet>();
        userEnteredCodeSets.add(CertificationResultCodeSet.builder()
                    .codeSet(CodeSet.builder()
                            .userEnteredName("12-3000")
                            .build())
                    .build());

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .criterion(a1)
                        .success(true)
                        .codeSets(userEnteredCodeSets)
                        .build())
                .build();

        normalizer.normalize(listing);
        assertEquals(a1a2CodeSet.getId(),
                listing.getCertificationResults().get(0).getCodeSets().get(0).getCodeSet().getId());
    }

    @Test
    public void normalize_userEnteredNameMatchesDateFormatMMMyyyy_setsId() {
        List<CertificationResultCodeSet> userEnteredCodeSets = new ArrayList<CertificationResultCodeSet>();
        userEnteredCodeSets.add(CertificationResultCodeSet.builder()
                    .codeSet(CodeSet.builder()
                            .userEnteredName("Dec 3000")
                            .build())
                    .build());

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .criterion(a1)
                        .success(true)
                        .codeSets(userEnteredCodeSets)
                        .build())
                .build();

        normalizer.normalize(listing);
        assertEquals(a1a2CodeSet.getId(),
                listing.getCertificationResults().get(0).getCodeSets().get(0).getCodeSet().getId());
    }

    @Test
    public void normalize_userEnteredNameMatchesDateFormatMMMMyyyy_setsId() {
        List<CertificationResultCodeSet> userEnteredCodeSets = new ArrayList<CertificationResultCodeSet>();
        userEnteredCodeSets.add(CertificationResultCodeSet.builder()
                    .codeSet(CodeSet.builder()
                            .userEnteredName("December 3000")
                            .build())
                    .build());

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .criterion(a1)
                        .success(true)
                        .codeSets(userEnteredCodeSets)
                        .build())
                .build();

        normalizer.normalize(listing);
        assertEquals(a1a2CodeSet.getId(),
                listing.getCertificationResults().get(0).getCodeSets().get(0).getCodeSet().getId());
    }

    @Test
    public void normalize_userEnteredNameMatchesDateFormatyyyyMM_setsId() {
        List<CertificationResultCodeSet> userEnteredCodeSets = new ArrayList<CertificationResultCodeSet>();
        userEnteredCodeSets.add(CertificationResultCodeSet.builder()
                    .codeSet(CodeSet.builder()
                            .userEnteredName("300012")
                            .build())
                    .build());

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .criterion(a1)
                        .success(true)
                        .codeSets(userEnteredCodeSets)
                        .build())
                .build();

        normalizer.normalize(listing);
        assertEquals(a1a2CodeSet.getId(),
                listing.getCertificationResults().get(0).getCodeSets().get(0).getCodeSet().getId());
    }

    @Test
    public void normalize_userEnteredNameUnknownFormat_idIsNull() {
        List<CertificationResultCodeSet> userEnteredCodeSets = new ArrayList<CertificationResultCodeSet>();
        userEnteredCodeSets.add(CertificationResultCodeSet.builder()
                    .codeSet(CodeSet.builder()
                            .userEnteredName("December 31, 3000")
                            .build())
                    .build());

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .criterion(a1)
                        .success(true)
                        .codeSets(userEnteredCodeSets)
                        .build())
                .build();

        normalizer.normalize(listing);
        assertNull(listing.getCertificationResults().get(0).getCodeSets().get(0).getId());
    }
}
