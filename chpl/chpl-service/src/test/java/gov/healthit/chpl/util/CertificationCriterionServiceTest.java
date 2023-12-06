package gov.healthit.chpl.util;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.core.env.Environment;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.dao.CertificationCriterionDAO;
import gov.healthit.chpl.service.CertificationCriterionService;

public class CertificationCriterionServiceTest {

    private CertificationCriterion criterion1;
    private CertificationCriterion criterion;
    private CertificationCriterion criterion2;

    private CertificationCriterionService service;
    private Environment env;
    private CertificationCriterionDAO certificationCriterionDAO;

    @Before
    public void setup() {
        criterion = new CertificationCriterion();
        criterion1 = new CertificationCriterion();
        criterion2 = new CertificationCriterion();

        env = Mockito.mock(Environment.class);
        Mockito.when(env.getProperty("criteria.sortOrder")).thenReturn(sortOrderFromProperty());

        certificationCriterionDAO = Mockito.mock(CertificationCriterionDAO.class);
        Mockito.when(certificationCriterionDAO.findAll()).thenReturn(new ArrayList<CertificationCriterion>());

        service = new CertificationCriterionService(certificationCriterionDAO, env);
        service.postConstruct();
    }

    @Test
    public void sortCriteria_BetweenEditions_SortCorrectly() {
        criterion1.setId(137L);
        criterion2.setId(71L);
        assertTrue("2011 should be earlier than 2014", service.sortCriteria(criterion1, criterion2) < 0);
        criterion1.setId(10L);
        assertTrue("2015 should be later than 2014", service.sortCriteria(criterion1, criterion2) > 0);
        criterion2.setId(137L);
        assertTrue("2015 should be later than 2011", service.sortCriteria(criterion1, criterion2) > 0);
    }

    @Test
    public void sortCriteria_WithOneParagraph_SortCorrectly() {
        criterion1.setId(129L);
        criterion2.setId(130L);
        assertTrue("h should be earlier than i", service.sortCriteria(criterion1, criterion2) < 0);
    }

    @Test
    public void sortCriteria_WithTwoParagraphs_SortCorrectly() {
        criterion1.setId(63L);
        criterion2.setId(62L);
        assertTrue("3 should be after 2", service.sortCriteria(criterion1, criterion2) > 0);
    }

    @Test
    public void formatCriteriaNumber_CertificationCriterionWithoutCuresSuffixInTitle_formattedCorrectly() {
        final String expectedResult = "170.315(b)(1)";
        criterion.setNumber(expectedResult);
        criterion.setTitle(expectedResult);
        final String formattedResult = CertificationCriterionService.formatCriteriaNumber(criterion);
        assertEquals(expectedResult, formattedResult);
    }

    @Test
    public void formatCriteriaNumber_WantToFormatForRemovedAndCriteriaIsRemoved_ResultStartsWithRemoved() {
        criterion = CertificationCriterion.builder()
                .number("170.315(a)(6)")
                .startDay(LocalDate.parse("2023-01-01"))
                .endDay(LocalDate.parse("2023-01-02"))
                .build();

        String result = CertificationCriterionService.formatCriteriaNumber(criterion, true);

        assertEquals(true, result.startsWith("Removed | "));
    }

    @Test
    public void formatCriteriaNumber_WantToFormatForRemovedAndCriteriaIsNotRemoved_ResultDoesNotStartWithRemoved() {
        criterion = CertificationCriterion.builder()
                .number("170.315(a)(6)")
                .startDay(LocalDate.parse("2023-01-01"))
                .build();

        String result = CertificationCriterionService.formatCriteriaNumber(criterion, true);

        assertEquals(false, result.startsWith("Removed | "));
    }

    @Test
    public void formatCriteriaNumber_DoNotWantToFormatForRemovedAndCriteriaIsRemoved_ResultDoesNotStartWithRemoved() {
        criterion = CertificationCriterion.builder()
                .number("170.315(a)(6)")
                .startDay(LocalDate.parse("2023-01-01"))
                .endDay(LocalDate.parse("2023-01-02"))
                .build();

        String result = CertificationCriterionService.formatCriteriaNumber(criterion, false);

        assertEquals(false, result.startsWith("Removed | "));
    }

    @Test
    public void formatCriteriaNumber_DoNotWantToFormatForRemovedAndCriteriaIsNotRemoved_ResultDoesNotStartWithRemoved() {
        criterion = CertificationCriterion.builder()
                .number("170.315(a)(6)")
                .startDay(LocalDate.parse("2023-01-01"))
                .build();

        String result = CertificationCriterionService.formatCriteriaNumber(criterion, false);

        assertEquals(false, result.startsWith("Removed | "));
    }

    @Test
    public void coerceToCriteriaNumber_ValidCriteriaNumber_DoesNotChange() {
        String criterionNumber = "170.315 (a)(6)";
        String result = service.coerceToCriterionNumberFormat(criterionNumber);
        assertEquals(criterionNumber, result);
    }

    @Test
    public void coerceToCriteriaNumber_MissingSpaceBeforeParen_AddsSpace() {
        String criterionNumber = "170.315(a)(6)";
        String result = service.coerceToCriterionNumberFormat(criterionNumber);
        assertEquals("170.315 (a)(6)", result);
    }

    @Test
    public void coerceToCriteriaNumber_ExtraSpacesBeforeParen_RemovesSpace() {
        String criterionNumber = "170.315   (a)(6)";
        String result = service.coerceToCriterionNumberFormat(criterionNumber);
        assertEquals("170.315 (a)(6)", result);
    }

    public static String sortOrderFromProperty() {
        return "120, 122, 123, 124, 125, 126, 127, 128, 129, 130, 131, 132, 133, 134, 135, 136, 137, 138, 139, 140, "
                + "141, 142, 143, 144, 145, 146, 147, 148, 149, 150, 151, 152, 153, 154, 155, 156, 157, 158, 159, "
                + "160, 161, 162, 163, 164, 61, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 62, 80, 63, 64, 65, 66, 67, "
                + "68, 69, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 91, 92, 93, 94, 95, 96, 97, 98, 99, 100, 101, 102, "
                + "103, 104, 105, 106, 107, 108, 109, 110, 111, 112, 113, 114, 115, 116, 117, 118, 119, 1, 2, 3, 4, "
                + "5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 16, 165, 17, 17, 166, 18, 18, 167, 19, 20, 21, 22, 22, "
                + "168, 23, 23, 169, 24, 24, 170, 171, 210, 25, 26, 27, 27, 172, 28, 29, 30, 30, 173, 31, 31, 174, 32, "
                + "33, 34, 35, 36, 37, 38, 38, 175, 39, 176, 177, 211, 40, 40, 178, 41, 42, 43, 44, 45, 46, 47, 47, 179, "
                + "48, 49, 50, 51, 52, 53, 54, 55, 55, 180, 56, 57, 58, 58, 181, 182, 59, 60";

    }
}
