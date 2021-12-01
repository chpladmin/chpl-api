package gov.healthit.chpl.upload.listing.normalizer;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.svap.dao.SvapDAO;
import gov.healthit.chpl.svap.domain.CertificationResultSvap;
import gov.healthit.chpl.svap.domain.Svap;
import gov.healthit.chpl.svap.domain.SvapCriteriaMap;

public class SvapNormalizerTest {
    private SvapDAO svapDao;
    private SvapNormalizer normalizer;

    @Before
    public void before() {
        svapDao = Mockito.mock(SvapDAO.class);
        List<SvapCriteriaMap> allowedSvaps = new ArrayList<SvapCriteriaMap>();
        allowedSvaps.add(SvapCriteriaMap.builder()
                .criterion(CertificationCriterion.builder()
                        .id(1L)
                        .number("170.315 (a)(1)")
                        .build())
                .svap(Svap.builder()
                        .svapId(1L)
                        .regulatoryTextCitation("CITATION1")
                        .approvedStandardVersion("stdver1")
                        .replaced(false)
                        .build())
                .build());
        allowedSvaps.add(SvapCriteriaMap.builder()
                .criterion(CertificationCriterion.builder()
                        .id(1L)
                        .number("170.315 (a)(1)")
                        .build())
                .svap(Svap.builder()
                        .svapId(2L)
                        .regulatoryTextCitation("CITATION2")
                        .approvedStandardVersion("stdver2")
                        .replaced(true)
                        .build())
                .build());

        try {
            Mockito.when(svapDao.getAllSvapCriteriaMap()).thenReturn(allowedSvaps);
        } catch (EntityRetrievalException e) {
        }

        normalizer = new SvapNormalizer(svapDao);
    }

    @Test
    public void normalize_nullSvap_noChanges() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("170.315 (a)(1)")
                                .build())
                        .build())
                .build();
        listing.getCertificationResults().get(0).setSvaps(null);
        normalizer.normalize(listing);
        assertNull(listing.getCertificationResults().get(0).getSvaps());
    }

    @Test
    public void normalize_emptySvap_noChanges() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("170.315 (a)(1)")
                                .build())
                        .svaps(new ArrayList<CertificationResultSvap>())
                        .build())
                .build();
        normalizer.normalize(listing);
        assertEquals(0, listing.getCertificationResults().get(0).getSvaps().size());
    }

    @Test
    public void normalize_svapNotInDatabase_idIsNull() {
        List<CertificationResultSvap> svaps = new ArrayList<CertificationResultSvap>();
        svaps.add(CertificationResultSvap.builder()
                .svapId(null)
                .regulatoryTextCitation("notindb")
                .build());

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationEdition(create2015EditionMap())
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("170.315 (a)(1)")
                                .build())
                        .svaps(svaps)
                        .build())
                .build();
        normalizer.normalize(listing);
        assertEquals(1, listing.getCertificationResults().get(0).getSvaps().size());
        assertNull(listing.getCertificationResults().get(0).getSvaps().get(0).getSvapId());
        assertEquals("notindb", listing.getCertificationResults().get(0).getSvaps().get(0).getRegulatoryTextCitation());
    }

    @Test
    public void normalize_svapInDatabase_setsAllFields() {
        List<CertificationResultSvap> svaps = new ArrayList<CertificationResultSvap>();
        svaps.add(CertificationResultSvap.builder()
                .svapId(null)
                .regulatoryTextCitation("CITATION1")
                .build());

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationEdition(create2015EditionMap())
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("170.315 (a)(1)")
                                .build())
                        .svaps(svaps)
                        .build())
                .build();
        normalizer.normalize(listing);
        assertEquals(1, listing.getCertificationResults().get(0).getSvaps().size());
        assertEquals(1L, listing.getCertificationResults().get(0).getSvaps().get(0).getSvapId());
        assertEquals("stdver1", listing.getCertificationResults().get(0).getSvaps().get(0).getApprovedStandardVersion());
        assertEquals(false, listing.getCertificationResults().get(0).getSvaps().get(0).getReplaced());
        assertEquals("CITATION1", listing.getCertificationResults().get(0).getSvaps().get(0).getRegulatoryTextCitation());
    }

    @Test
    public void normalize_criterionHasAllowedSvaps_addsAllowedSvapsToCertificationResult() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                    .id(1L)
                                    .number("170.315 (a)(1)")
                                    .build())
                        .svaps(new ArrayList<CertificationResultSvap>())
                        .build())
                .build();
        normalizer.normalize(listing);
        assertEquals(2, listing.getCertificationResults().get(0).getAllowedSvaps().size());
    }

    @Test
    public void normalize_criterionHasNoAllowedSvaps_noAllowedSvapsAdded() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                    .id(2L)
                                    .number("170.315 (a)(2)")
                                    .build())
                        .svaps(new ArrayList<CertificationResultSvap>())
                        .build())
                .build();
        normalizer.normalize(listing);
        assertEquals(0, listing.getCertificationResults().get(0).getAllowedSvaps().size());
    }

    private Map<String, Object> create2015EditionMap() {
        Map<String, Object> editionMap = new HashMap<String, Object>();
        editionMap.put(CertifiedProductSearchDetails.EDITION_ID_KEY, 3L);
        editionMap.put(CertifiedProductSearchDetails.EDITION_NAME_KEY, "2015");
        return editionMap;
    }
}
