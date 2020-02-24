package gov.healthit.chpl.upload.certifiedProduct.template;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.Test;

public class TemplateColumnIndexMapTest {
    TemplateColumnIndexMap2015Version4 map = new TemplateColumnIndexMap2015Version4();

    @Test
    public void parseCriteriaHeading_CuresHeading_ReturnsCorrectNumber() {
        String expectedResult = "170.315 (d)(12)";
        String d12Cures = "CRITERIA_170_315_D_12_Cures__C";

        String parsedNumber = map.parseCriteriaNumberFromHeading(d12Cures);
        assertEquals(expectedResult, parsedNumber);
    }

    @Test
    public void parseCriteriaHeading_ValidHeading_ReturnsCorrectNumber() {
        String expectedResult = "170.315 (b)(3)";
        String b3 = "CRITERIA_170_315_B_3__C";

        String parsedNumber = map.parseCriteriaNumberFromHeading(b3);
        assertEquals(expectedResult, parsedNumber);
    }

    @Test
    public void parseCriteriaHeading_B5AHeading_ReturnsCorrectNumber() {
        String expectedResult = "170.314 (b)(5)(A)";
        String b5A = "CRITERIA_170_314_B_5A__C";

        String parsedNumber = map.parseCriteriaNumberFromHeading(b5A);
        assertEquals(expectedResult, parsedNumber);
    }

    @Test
    public void parseCriteriaHeading_InvalidHeading_ReturnsNull() {
        String badHeading = "DOES NOT MATCH";

        String parsedNumber = map.parseCriteriaNumberFromHeading(badHeading);
        assertNull(parsedNumber);
    }

    @Test
    public void parseCriteriaHeading_CuresHeading_ReturnsIsCures() {
        String d12Cures = "CRITERIA_170_315_D_12_Cures__C";
        assertTrue(map.isCriteriaNumberHeadingCures(d12Cures));

    }

    @Test
    public void parseCriteriaHeading_NonCuresHeading_ReturnsNotCures() {
        String d11 = "CRITERIA_170_315_D_11__C";
        assertFalse(map.isCriteriaNumberHeadingCures(d11));

    }

    @Test
    public void parseCriteriaHeading_BadHeading_ReturnsNotCures() {
        String badHeading = "DOES NOT MATCH";
        assertFalse(map.isCriteriaNumberHeadingCures(badHeading));

    }
}
