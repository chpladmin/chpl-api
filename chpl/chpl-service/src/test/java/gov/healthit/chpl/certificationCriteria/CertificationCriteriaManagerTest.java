package gov.healthit.chpl.certificationCriteria;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import gov.healthit.chpl.dao.CertificationCriterionDAO;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.util.CertificationResultRules;

public class CertificationCriteriaManagerTest {
    private CertificationCriterionDAO certificationCriterionDao;
    private CertificationResultRules rules;
    private CertificationCriterionComparator criterionComparator;
    private CertificationCriteriaManager manager;

    @Before
    public void setup() throws EntityRetrievalException {
        certificationCriterionDao = Mockito.mock(CertificationCriterionDAO.class);
        rules = Mockito.mock(CertificationResultRules.class);
        criterionComparator = Mockito.mock(CertificationCriterionComparator.class);

        Mockito.when(certificationCriterionDao.findAll())
            .thenReturn(Stream.of(
                    CertificationCriterion.builder()
                        .id(1L)
                        .startDay(LocalDate.parse("2010-01-01"))
                        .endDay(LocalDate.parse("2011-01-01"))
                        .build(),
                    CertificationCriterion.builder()
                        .id(2L)
                        .startDay(LocalDate.parse("2015-01-01"))
                        .endDay(LocalDate.parse("2016-01-01"))
                        .build(),
                    CertificationCriterion.builder()
                        .id(3L)
                        .startDay(LocalDate.parse("2020-06-30"))
                        .endDay(null)
                        .build(),
                    CertificationCriterion.builder()
                        .id(4L)
                        .startDay(LocalDate.parse("2022-12-31"))
                        .endDay(null)
                        .build(),
                    CertificationCriterion.builder()
                        .id(5L)
                        .startDay(LocalDate.parse("2022-12-31"))
                        .endDay(LocalDate.parse("2024-01-01"))
                        .build())
                    .toList());
        manager = new CertificationCriteriaManager(certificationCriterionDao, rules, criterionComparator);
    }

    @Test
    public void getActiveCriteria_startAndEndNull_returnsAll() {
        List<CertificationCriterion> criteria = manager.getActiveBetween(null, null);
        assertEquals(5, criteria.size());
    }

    @Test
    public void getActiveCriteria_startEqualsCriteriaStartAndEndNull_returnsCorrectCriteria() {
        LocalDate startDate = LocalDate.parse("2015-01-01");
        List<CertificationCriterion> criteria = manager.getActiveBetween(startDate, null);
        assertEquals(4, criteria.size());
    }

    @Test
    public void getActiveCriteria_startEqualsCriteriaEndAndEndNull_returnsCorrectCriteria() {
        LocalDate startDate = LocalDate.parse("2016-01-01");
        List<CertificationCriterion> criteria = manager.getActiveBetween(startDate, null);
        assertEquals(4, criteria.size());
    }

    @Test
    public void getActiveCriteria_startAfterCriteriaStartAndEndNull_returnsCorrectCriteria() {
        LocalDate startDate = LocalDate.parse("2015-01-02");
        List<CertificationCriterion> criteria = manager.getActiveBetween(startDate, null);
        assertEquals(4, criteria.size());
    }

    @Test
    public void getActiveCriteria_startNullAndEndEqualsCriteriaStart_returnsCorrectCriteria() {
        LocalDate endDate = LocalDate.parse("2015-01-01");
        List<CertificationCriterion> criteria = manager.getActiveBetween(null, endDate);
        assertEquals(2, criteria.size());
    }

    @Test
    public void getActiveCriteria_startNullAndEndBeforeCriteriaStart_returnsCorrectCriteria() {
        LocalDate endDate = LocalDate.parse("2014-12-31");
        List<CertificationCriterion> criteria = manager.getActiveBetween(null, endDate);
        assertEquals(1, criteria.size());
    }

    @Test
    public void getActiveCriteria_startNullAndEndAfterCriteriaStart_returnsCorrectCriteria() {
        LocalDate endDate = LocalDate.parse("2015-01-02");
        List<CertificationCriterion> criteria = manager.getActiveBetween(null, endDate);
        assertEquals(2, criteria.size());
    }

    @Test
    public void getActiveCriteria_startEqualsCriteriaStartAndEndAfterCriteriaStart_returnsCorrectCriteria() {
        LocalDate startDate = LocalDate.parse("2015-01-01");
        LocalDate endDate = LocalDate.parse("2015-01-02");
        List<CertificationCriterion> criteria = manager.getActiveBetween(startDate, endDate);
        assertEquals(1, criteria.size());
    }

    @Test
    public void getActiveCriteria_startBeforeCriteriaStartAndEndAfterCriteriaStart_returnsCorrectCriteria() {
        LocalDate startDate = LocalDate.parse("2014-12-31");
        LocalDate endDate = LocalDate.parse("2015-01-02");
        List<CertificationCriterion> criteria = manager.getActiveBetween(startDate, endDate);
        assertEquals(1, criteria.size());
    }

    @Test
    public void getActiveCriteria_startAfterCriteriaStartAndEndAfterCriteriaStart_returnsCorrectCriteria() {
        LocalDate startDate = LocalDate.parse("2015-01-02");
        LocalDate endDate = LocalDate.parse("2015-01-05");
        List<CertificationCriterion> criteria = manager.getActiveBetween(startDate, endDate);
        assertEquals(1, criteria.size());
    }

    @Test
    public void getActiveCriteria_startAfterCriteriaStartAndEndAfterCriteriaEnd_returnsCorrectCriteria() {
        LocalDate startDate = LocalDate.parse("2015-01-02");
        LocalDate endDate = LocalDate.parse("2022-01-01");
        List<CertificationCriterion> criteria = manager.getActiveBetween(startDate, endDate);
        assertEquals(2, criteria.size());
    }

    @Test
    public void getActiveCriteria_startBeforeCriteriaStartAndEndBeforeCriteriaEnd_returnsNoCriteria() {
        LocalDate startDate = LocalDate.parse("2000-01-02");
        LocalDate endDate = LocalDate.parse("2000-01-05");
        List<CertificationCriterion> criteria = manager.getActiveBetween(startDate, endDate);
        assertEquals(0, criteria.size());
    }
}
