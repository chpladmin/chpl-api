package gov.healthit.chpl.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.Before;
import org.junit.Test;

import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.dto.CertificationCriterionDTO;

public class UtilTest {
    private CertificationCriterionDTO criterionDto;
    private CertificationCriterion criterion;
    private CertificationCriterionDTO criterionDto2;

    @Before
    public void setup() {
        criterionDto = new CertificationCriterionDTO();
        criterion = new CertificationCriterion();
        criterionDto2 = new CertificationCriterionDTO();
    }

    @Test
    public void sortCriteria_BetweenEditions_SortCorrectly() {
        criterionDto.setNumber("170.302 (h)");
        criterionDto2.setNumber("170.314 (a)(1)");
        assertTrue("2011 should be earlier than 2014", Util.sortCriteria(criterionDto, criterionDto2) < 0);
        criterionDto.setNumber("170.315 (b)(2)");
        assertTrue("2015 should be later than 2014", Util.sortCriteria(criterionDto, criterionDto2) > 0);
        criterionDto2.setNumber("170.304 (d)");
        assertTrue("2015 should be later than 2011", Util.sortCriteria(criterionDto, criterionDto2) > 0);
    }

    @Test
    public void sortCriteria_WithOneParagraph_SortCorrectly() {
        criterionDto.setNumber("170.302 (h)");
        criterionDto2.setNumber("170.302 (i)");
        assertTrue("h should be earlier than i", Util.sortCriteria(criterionDto, criterionDto2) < 0);
    }

    @Test
    public void sortCriteria_WithTwoParagraphs_SortCorrectly() {
        criterionDto.setNumber("170.314 (a)(3)");
        criterionDto2.setNumber("170.314 (a)(2)");
        assertTrue("3 should be after 2", Util.sortCriteria(criterionDto, criterionDto2) > 0);
    }

    @Test
    public void sortCriteria_WithThreeParagraphs_SortCorrectly() {
        criterionDto.setNumber("170.314 (d)(3)(B)");
        criterionDto2.setNumber("170.314 (d)(3)(A)");
        assertTrue("B should be after A", Util.sortCriteria(criterionDto, criterionDto2) > 0);
    }

    @Test
    public void sortCriteria_WithDifferingParagraphComponentCount_SortCorrectly() {
        criterionDto.setNumber("170.314 (d)(3)");
        criterionDto2.setNumber("170.314 (d)(3)(A)");
        assertTrue("fewer paragraphs should be before ones with more", Util.sortCriteria(criterionDto, criterionDto2) < 0);
    }

    @Test
    public void sortCriteria_WithMatchingNumbers_SortCorrectly() {
        criterionDto.setNumber("170.314 (a)(3)");
        criterionDto.setTitle("This is a title");
        criterionDto2.setNumber("170.314 (a)(3)");
        criterionDto2.setTitle("This is a title (Cures Update)");
        assertTrue("should sort by title if paragraphs match", Util.sortCriteria(criterionDto, criterionDto2) < 0);
    }

    @Test
    public void isCures_CertificationCriterionWithoutCuresSuffixInTitle_IsNotCures() {
        criterion.setTitle("170.315(b)(7)");
        assertFalse("The criterion Should be identified as NOT CURES but was instead identfied as CURES",
                Util.isCures(criterion));
    }

    @Test
    public void isCures_CertificationCriterionWithCuresSuffixInTitle_IsCures() {
        criterion.setTitle("170.315(b)(7)" + Util.CURES_SUFFIX);
        assertTrue("The criterion Should be identified as CURES but was instead identfied as NOT CURES", Util.isCures(criterion));
    }

    @Test
    public void isCures_CertificationCriterionDtoWithoutCuresSuffixInTitle_IsNotCures() {
        criterion.setTitle("170.315(c)(3)");
        assertFalse("The criterion Should be identified as NOT CURES but was instead identfied as CURES",
                Util.isCures(criterion));
    }

    @Test
    public void isCures_CertificationResultDtoWithCuresSuffixInTitle_IsCures() {
        criterion.setTitle("170.315(c)(3)" + Util.CURES_SUFFIX);
        assertTrue("The criterion Should be identified as CURES but was instead identfied as NOT CURES", Util.isCures(criterion));
    }

    @Test
    public void formatCriteriaNumber_CertificationCriterionWithCuresSuffixInTitle_StringFormattedAsCures() {
        final String num = "170.315(b)(1)";
        criterion.setNumber(num);
        final String expectedResult = num + Util.CURES_SUFFIX;
        criterion.setTitle(expectedResult);
        final String formattedResult = Util.formatCriteriaNumber(criterion);
        assertEquals(expectedResult, formattedResult);
    }

    @Test
    public void formatCriteriaNumber_CertificationCriterionWithoutCuresSuffixInTitle_StringFormattedAsNotCures() {
        final String expectedResult = "170.315(b)(1)";
        criterion.setNumber(expectedResult);
        criterion.setTitle(expectedResult);
        final String formattedResult = Util.formatCriteriaNumber(criterion);
        assertEquals(expectedResult, formattedResult);
    }

    @Test
    public void formatCriteriaNumber_CertificationCriterionDtoWithCuresSuffixInTitle_StringFormattedAsCures() {
        final String num = "170.315(b)(2)";
        criterion.setNumber(num);
        final String expectedResult = num + Util.CURES_SUFFIX;
        criterion.setTitle(expectedResult);
        final String formattedResult = Util.formatCriteriaNumber(criterion);
        assertEquals(expectedResult, formattedResult);
    }

    @Test
    public void formatCriteriaNumber_CertificationCriterionDtoWithoutCuresSuffixInTitle_StringFormattedAsNotCures() {
        final String expectedResult = "170.315(b)(2)";
        criterion.setNumber(expectedResult);
        criterion.setTitle(expectedResult);
        final String formattedResult = Util.formatCriteriaNumber(criterion);
        assertEquals(expectedResult, formattedResult);
    }
}
