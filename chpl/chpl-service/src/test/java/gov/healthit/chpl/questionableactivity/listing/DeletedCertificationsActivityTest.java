package gov.healthit.chpl.questionableactivity.listing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.questionableactivity.domain.QuestionableActivityListing;
import gov.healthit.chpl.service.CertificationCriterionService;
import gov.healthit.chpl.util.Util;

public class DeletedCertificationsActivityTest {

    private CertificationCriterionService criteriaService;
    private DeletedCertificationsActivity activityChecker;
    private CertificationCriterion a1, b1, b1Cures, g8, g10Cures;

    @Before
    public void setup() {
        a1 = buildCriterion(1L, "170.315 (a)(1)", "a1");
        b1 = buildCriterion(20L, "170.315 (b)(1)", "b1");
        b1Cures = buildCriterion(180L, "170.315(b)(1)", "b1 " + Util.CURES_SUFFIX);
        g8 = buildCriterion(40L, "170.315 (g)(8)", "g8");
        g10Cures = buildCriterion(181L, "170.315 (g)(10)", "g10 " + Util.CURES_SUFFIX);

        criteriaService = Mockito.mock(CertificationCriterionService.class);
        Mockito.when(criteriaService.getOriginalToCuresCriteriaMap())
            .thenReturn(buildOriginalToCuresCriteriaMap());
        Mockito.when(criteriaService.get(ArgumentMatchers.eq(1L))).thenReturn(a1);
        Mockito.when(criteriaService.get(ArgumentMatchers.eq(20L))).thenReturn(b1);
        Mockito.when(criteriaService.get(ArgumentMatchers.eq(180L))).thenReturn(b1Cures);
        Mockito.when(criteriaService.get(ArgumentMatchers.eq(40L))).thenReturn(g8);
        Mockito.when(criteriaService.get(ArgumentMatchers.eq(181L))).thenReturn(g10Cures);

        activityChecker = new DeletedCertificationsActivity(criteriaService);
    }

    @Test
    public void check_noCertsInOriginalOrNew_noActivitiesReturned() {
        CertifiedProductSearchDetails originalListing = CertifiedProductSearchDetails.builder()
                .build();
        CertifiedProductSearchDetails updatedListing = CertifiedProductSearchDetails.builder()
                .build();
        List<QuestionableActivityListing> activities = activityChecker.check(originalListing, updatedListing);
        assertNotNull(activities);
        assertEquals(0, activities.size());
    }

    @Test
    public void check_sameCertsInOriginalAndNew_noActivitiesReturned() {
        CertifiedProductSearchDetails originalListing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .criterion(a1)
                        .success(true)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(b1)
                        .success(true)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(b1Cures)
                        .success(false)
                        .build())
                .build();
        CertifiedProductSearchDetails updatedListing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .criterion(a1)
                        .success(true)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(b1)
                        .success(true)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(b1Cures)
                        .success(false)
                        .build())
                .build();
        List<QuestionableActivityListing> activities = activityChecker.check(originalListing, updatedListing);
        assertNotNull(activities);
        assertEquals(0, activities.size());
    }

    @Test
    public void check_b1CuresAddedAndB1Removed_noActivitiesReturned() {
        CertifiedProductSearchDetails originalListing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .criterion(a1)
                        .success(true)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(b1)
                        .success(true)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(b1Cures)
                        .success(false)
                        .build())
                .build();
        CertifiedProductSearchDetails updatedListing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .criterion(a1)
                        .success(true)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(b1)
                        .success(false)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(b1Cures)
                        .success(true)
                        .build())
                .build();
        List<QuestionableActivityListing> activities = activityChecker.check(originalListing, updatedListing);
        assertNotNull(activities);
        assertEquals(0, activities.size());
    }

    @Test
    public void check_g10CuresAddedAndG8Removed_noActivitiesReturned() {
        CertifiedProductSearchDetails originalListing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .criterion(a1)
                        .success(true)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(g8)
                        .success(true)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(g10Cures)
                        .success(false)
                        .build())
                .build();
        CertifiedProductSearchDetails updatedListing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .criterion(a1)
                        .success(true)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(g8)
                        .success(false)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(g10Cures)
                        .success(true)
                        .build())
                .build();
        List<QuestionableActivityListing> activities = activityChecker.check(originalListing, updatedListing);
        assertNotNull(activities);
        assertEquals(0, activities.size());
    }

    @Test
    public void check_a1RemovedAndNothingAdded_activityReturned() {
        CertifiedProductSearchDetails originalListing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .criterion(a1)
                        .success(true)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(g8)
                        .success(true)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(g10Cures)
                        .success(false)
                        .build())
                .build();
        CertifiedProductSearchDetails updatedListing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .criterion(a1)
                        .success(false)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(g8)
                        .success(true)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(g10Cures)
                        .success(false)
                        .build())
                .build();
        List<QuestionableActivityListing> activities = activityChecker.check(originalListing, updatedListing);
        assertNotNull(activities);
        assertEquals(1, activities.size());
        assertEquals(Util.formatCriteriaNumber(a1), activities.get(0).getBefore());
    }

    @Test
    public void check_a1RemovedAndG10CuresAddedAndG8Removed_activityForA1Returned() {
        CertifiedProductSearchDetails originalListing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .criterion(a1)
                        .success(true)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(g8)
                        .success(true)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(g10Cures)
                        .success(false)
                        .build())
                .build();
        CertifiedProductSearchDetails updatedListing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .criterion(a1)
                        .success(false)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(g8)
                        .success(false)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(g10Cures)
                        .success(true)
                        .build())
                .build();
        List<QuestionableActivityListing> activities = activityChecker.check(originalListing, updatedListing);
        assertNotNull(activities);
        assertEquals(1, activities.size());
        assertEquals(Util.formatCriteriaNumber(a1), activities.get(0).getBefore());
    }

    @Test
    public void check_a1RemovedAndB1CuresRemovedAndG8Added_activityForA1AndB1CuresReturned() {
        CertifiedProductSearchDetails originalListing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .criterion(a1)
                        .success(true)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(g8)
                        .success(false)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(b1Cures)
                        .success(true)
                        .build())
                .build();
        CertifiedProductSearchDetails updatedListing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .criterion(a1)
                        .success(false)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(g8)
                        .success(true)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(b1Cures)
                        .success(false)
                        .build())
                .build();
        List<QuestionableActivityListing> activities = activityChecker.check(originalListing, updatedListing);
        assertNotNull(activities);
        assertEquals(2, activities.size());
        assertEquals(Util.formatCriteriaNumber(a1), activities.get(0).getBefore());
        assertEquals(Util.formatCriteriaNumber(b1Cures), activities.get(1).getBefore());
    }

    private Map<CertificationCriterion, CertificationCriterion> buildOriginalToCuresCriteriaMap() {
        Map<CertificationCriterion, CertificationCriterion> map = new LinkedHashMap<CertificationCriterion, CertificationCriterion>();
        map.put(b1, b1Cures);
        map.put(g8, g10Cures);
        return map;
    }

    private CertificationCriterion buildCriterion(Long id, String number, String title) {
        return CertificationCriterion.builder()
                .certificationEdition("2015")
                .certificationEditionId(3L)
                .id(id)
                .number(number)
                .removed(false)
                .title(title)
                .build();
    }
}
