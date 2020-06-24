package gov.healthit.chpl.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.core.env.Environment;

import gov.healthit.chpl.dao.CertificationCriterionDAO;
import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.dto.CertificationCriterionDTO;
import gov.healthit.chpl.service.CertificationCriterionService;

public class CertificationCriterionServiceTest {

    private CertificationCriterionDTO criterionDto;
    private CertificationCriterion criterion;
    private CertificationCriterionDTO criterionDto2;

    private CertificationCriterionService service;
    private Environment env;
    private CertificationCriterionDAO certificationCriterionDAO;

    @Before
    public void setup() {
        criterionDto = new CertificationCriterionDTO();
        criterion = new CertificationCriterion();
        criterionDto2 = new CertificationCriterionDTO();

        env = Mockito.mock(Environment.class);
        Mockito.when(env.getProperty("criteria.sortOrder")).thenReturn(sortOrderFromProperty());

        certificationCriterionDAO = Mockito.mock(CertificationCriterionDAO.class);
        Mockito.when(certificationCriterionDAO.findAll()).thenReturn(new ArrayList<CertificationCriterionDTO>());

        service = new CertificationCriterionService(certificationCriterionDAO, env);
        service.postConstruct();
    }

    @Test
    public void sortCriteria_BetweenEditions_SortCorrectly() {
        criterionDto.setNumber("170.302 (h)");
        criterionDto2.setNumber("170.314 (a)(1)");
        assertTrue("2011 should be earlier than 2014", service.sortCriteria(criterionDto, criterionDto2) < 0);
        criterionDto.setNumber("170.315 (b)(2)");
        assertTrue("2015 should be later than 2014", service.sortCriteria(criterionDto, criterionDto2) > 0);
        criterionDto2.setNumber("170.304 (d)");
        assertTrue("2015 should be later than 2011", service.sortCriteria(criterionDto, criterionDto2) > 0);
    }

    @Test
    public void sortCriteria_WithOneParagraph_SortCorrectly() {
        criterionDto.setNumber("170.302 (h)");
        criterionDto2.setNumber("170.302 (i)");
        assertTrue("h should be earlier than i", service.sortCriteria(criterionDto, criterionDto2) < 0);
    }

    @Test
    public void sortCriteria_WithTwoParagraphs_SortCorrectly() {
        criterionDto.setNumber("170.314 (a)(3)");
        criterionDto2.setNumber("170.314 (a)(2)");
        assertTrue("3 should be after 2", service.sortCriteria(criterionDto, criterionDto2) > 0);
    }

    @Test
    public void sortCriteria_WithDifferingParagraphComponentCount_SortCorrectly() {
        criterionDto.setNumber("170.314 (d)(3)");
        criterionDto2.setNumber("170.314 (d)(3)(A)");
        assertTrue("fewer paragraphs should be before ones with more", service.sortCriteria(criterionDto, criterionDto2) < 0);
    }

    @Test
    public void sortCriteria_WithMatchingNumbers_SortCorrectly() {
        criterionDto.setNumber("170.314 (a)(3)");
        criterionDto.setTitle("This is a title");
        criterionDto2.setNumber("170.314 (a)(3)");
        criterionDto2.setTitle("This is a title (Cures Update)");
        assertTrue("should sort by title if paragraphs match", service.sortCriteria(criterionDto, criterionDto2) < 0);
    }

    @Test
    public void hasCuresInTitle_CertificationCriterionWithoutCuresSuffixInTitle_IsNotCures() {
        criterion.setTitle("170.315(b)(7)");
        assertFalse("The criterion Should be identified as NOT CURES but was instead identfied as CURES",
                service.hasCuresInTitle(criterion));
    }

    @Test
    public void hasCuresInTitle_CertificationCriterionWithCuresSuffixInTitle_IsCures() {
        criterion.setTitle("170.315(b)(7)" + CertificationCriterionService.CURES_SUFFIX);
        assertTrue("The criterion Should be identified as CURES but was instead identfied as NOT CURES",
                service.hasCuresInTitle(criterion));
    }

    @Test
    public void hasCuresInTitle_CertificationCriterionDtoWithoutCuresSuffixInTitle_IsNotCures() {
        criterion.setTitle("170.315(c)(3)");
        assertFalse("The criterion Should be identified as NOT CURES but was instead identfied as CURES",
                service.hasCuresInTitle(criterion));
    }

    @Test
    public void hasCuresInTitle_CertificationResultDtoWithCuresSuffixInTitle_IsCures() {
        criterion.setTitle("170.315(c)(3)" + CertificationCriterionService.CURES_SUFFIX);
        assertTrue("The criterion Should be identified as CURES but was instead identfied as NOT CURES",
                service.hasCuresInTitle(criterion));
    }

    @Test
    public void formatCriteriaNumber_CertificationCriterionWithCuresSuffixInTitle_StringFormattedAsCures() {
        final String num = "170.315(b)(1)";
        criterion.setNumber(num);
        final String expectedResult = num + CertificationCriterionService.CURES_SUFFIX;
        criterion.setTitle(expectedResult);
        final String formattedResult = service.formatCriteriaNumber(criterion);
        assertEquals(expectedResult, formattedResult);
    }

    @Test
    public void formatCriteriaNumber_CertificationCriterionWithoutCuresSuffixInTitle_StringFormattedAsNotCures() {
        final String expectedResult = "170.315(b)(1)";
        criterion.setNumber(expectedResult);
        criterion.setTitle(expectedResult);
        final String formattedResult = service.formatCriteriaNumber(criterion);
        assertEquals(expectedResult, formattedResult);
    }

    @Test
    public void formatCriteriaNumber_CertificationCriterionDtoWithCuresSuffixInTitle_StringFormattedAsCures() {
        final String num = "170.315(b)(2)";
        criterion.setNumber(num);
        final String expectedResult = num + CertificationCriterionService.CURES_SUFFIX;
        criterion.setTitle(expectedResult);
        final String formattedResult = service.formatCriteriaNumber(criterion);
        assertEquals(expectedResult, formattedResult);
    }

    @Test
    public void formatCriteriaNumber_CertificationCriterionDtoWithoutCuresSuffixInTitle_StringFormattedAsNotCures() {
        final String expectedResult = "170.315(b)(2)";
        criterion.setNumber(expectedResult);
        criterion.setTitle(expectedResult);
        final String formattedResult = service.formatCriteriaNumber(criterion);
        assertEquals(expectedResult, formattedResult);
    }

    @Test
    public void formatCriteriaNumber_WantToFormatForRemovedAndCriteriaIsRemoved_ResultStartsWithRemoved() {
        criterion = CertificationCriterion.builder()
                .number("170.315(a)(6)")
                .removed(true)
                .build();

        String result = CertificationCriterionService.formatCriteriaNumber(criterion, true);

        assertEquals(true, result.startsWith("Removed | "));
    }

    @Test
    public void formatCriteriaNumber_WantToFormatForRemovedAndCriteriaIsNotRemoved_ResultDoesNotStartWithRemoved() {
        criterion = CertificationCriterion.builder()
                .number("170.315(a)(6)")
                .removed(false)
                .build();

        String result = CertificationCriterionService.formatCriteriaNumber(criterion, true);

        assertEquals(false, result.startsWith("Removed | "));
    }

    @Test
    public void formatCriteriaNumber_DoNotWantToFormatForRemovedAndCriteriaIsRemoved_ResultDoesNotStartWithRemoved() {
        criterion = CertificationCriterion.builder()
                .number("170.315(a)(6)")
                .removed(true)
                .build();

        String result = CertificationCriterionService.formatCriteriaNumber(criterion, false);

        assertEquals(false, result.startsWith("Removed | "));
    }

    @Test
    public void formatCriteriaNumber_DoNotWantToFormatForRemovedAndCriteriaIsNotRemoved_ResultDoesNotStartWithRemoved() {
        criterion = CertificationCriterion.builder()
                .number("170.315(a)(6)")
                .removed(false)
                .build();

        String result = CertificationCriterionService.formatCriteriaNumber(criterion, false);

        assertEquals(false, result.startsWith("Removed | "));
    }

    private String sortOrderFromProperty() {
        return "170.302 (a),170.302 (b),170.302 (c),170.302 (d),170.302 (e),170.302 (f)(1),170.302 (f)(2),170.302 (f)(3),"
                + "170.302 (g),170.302 (h),170.302 (i),170.302 (j),170.302 (k),170.302 (l),170.302 (m),170.302 (n),170.302 (o),"
                + "170.302 (p),170.302 (q),170.302 (r),170.302 (s),170.302 (t),170.302 (u),170.302 (v),170.302 (w),170.304 (a),"
                + "170.304 (b),170.304 (c),170.304 (d),170.304 (e),170.304 (f),170.304 (g),170.304 (h),170.304 (i),170.304 (j),"
                + "170.306 (a),170.306 (b),170.306 (c),170.306 (d)(1),170.306 (d)(2),170.306 (e),170.306 (f),170.306 (g),"
                + "170.306 (h),170.306 (i),170.314 (a)(1),170.314 (a)(2),170.314 (a)(3),170.314 (a)(4),170.314 (a)(5),"
                + "170.314 (a)(6),170.314 (a)(7),170.314 (a)(8),170.314 (a)(9),170.314 (a)(10),170.314 (a)(11),170.314 (a)(12),"
                + "170.314 (a)(13),170.314 (a)(14),170.314 (a)(15),170.314 (a)(16),170.314 (a)(17),170.314 (a)(18),170.314 (a)(19),"
                + "170.314 (a)(20),170.314 (b)(1),170.314 (b)(2),170.314 (b)(3),170.314 (b)(4),170.314 (b)(5)(A),170.314 (b)(5)(B),"
                + "170.314 (b)(6),170.314 (b)(7),170.314 (b)(8),170.314 (b)(9),170.314 (c)(1),170.314 (c)(2),170.314 (c)(3),"
                + "170.314 (d)(1),170.314 (d)(2),170.314 (d)(3),170.314 (d)(4),170.314 (d)(5),170.314 (d)(6),170.314 (d)(7),"
                + "170.314 (d)(8),170.314 (d)(9),170.314 (e)(1),170.314 (e)(2),170.314 (e)(3),170.314 (f)(1),170.314 (f)(2),"
                + "170.314 (f)(3),170.314 (f)(4),170.314 (f)(5),170.314 (f)(6),170.314 (f)(7),170.314 (g)(1),170.314 (g)(2),"
                + "170.314 (g)(3),170.314 (g)(4),170.314 (h)(1),170.314 (h)(2),170.314 (h)(3),170.315 (a)(1),170.315 (a)(2),"
                + "170.315 (a)(3),170.315 (a)(4),170.315 (a)(5),170.315 (a)(6),170.315 (a)(7),170.315 (a)(8),170.315 (a)(9),"
                + "170.315 (a)(10),170.315 (a)(11),170.315 (a)(12),170.315 (a)(13),170.315 (a)(14),170.315 (a)(15),"
                + "170.315 (b)(1) (Cures Update),170.315 (b)(1),170.315 (b)(2) (Cures Update),170.315 (b)(2),"
                + "170.315 (b)(3) (Cures Update),170.315 (b)(3),170.315 (b)(4),170.315 (b)(5),170.315 (b)(6),"
                + "170.315 (b)(7) (Cures Update),170.315 (b)(7),170.315 (b)(8) (Cures Update),170.315 (b)(8),"
                + "170.315 (b)(9) (Cures Update),170.315 (b)(9),170.315 (b)(10),170.315 (c)(1),170.315 (c)(2),"
                + "170.315 (c)(3) (Cures Update),170.315 (c)(3),170.315 (c)(4),170.315 (d)(1),170.315 (d)(2) (Cures Update),"
                + "170.315 (d)(2),170.315 (d)(3) (Cures Update),170.315 (d)(3),170.315 (d)(4),170.315 (d)(5),170.315 (d)(6),"
                + "170.315 (d)(7),170.315 (d)(8),170.315 (d)(9),170.315 (d)(10) (Cures Update),170.315 (d)(10),170.315 (d)(11),"
                + "170.315 (d)(12),170.315 (d)(13),170.315 (e)(1) (Cures Update),170.315 (e)(1),170.315 (e)(2),170.315 (e)(3),"
                + "170.315 (f)(1),170.315 (f)(2),170.315 (f)(3),170.315 (f)(4),170.315 (f)(5) (Cures Update),170.315 (f)(5),"
                + "170.315 (f)(6),170.315 (f)(7),170.315 (g)(1),170.315 (g)(2),170.315 (g)(3),170.315 (g)(4),170.315 (g)(5),"
                + "170.315 (g)(6) (Cures Update),170.315 (g)(6),170.315 (g)(7),170.315 (g)(8),170.315 (g)(9) (Cures Update),"
                + "170.315 (g)(9),170.315 (g)(10),170.315 (h)(1),170.315 (h)(2),170.523 (k)(1),170.523 (k)(2)";
    }
}
