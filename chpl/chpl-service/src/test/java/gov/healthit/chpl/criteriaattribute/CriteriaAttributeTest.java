package gov.healthit.chpl.criteriaattribute;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;

import org.junit.Test;

public class CriteriaAttributeTest {

    @Test
    public void isRetired_EndDayNull_ReturnFalse() {
        CriteriaAttribute criteriaAttribute = CriteriaAttribute.builder().build();
        assertFalse(criteriaAttribute.isRetired());
    }

    @Test
    public void isRetired_EndDayAfterNow_ReturnFalse() {
        CriteriaAttribute criteriaAttribute = CriteriaAttribute.builder()
                .endDay(LocalDate.MAX)
                .build();
        assertFalse(criteriaAttribute.isRetired());
    }

    @Test
    public void isRetired_NowAfterEndDay_ReturnTrue() {
        CriteriaAttribute criteriaAttribute = CriteriaAttribute.builder()
                .endDay(LocalDate.MIN)
                .build();
        assertTrue(criteriaAttribute.isRetired());
    }

    @Test
    public void isRetired_NowEqualsEndDay_ReturnFalse() {
        CriteriaAttribute criteriaAttribute = CriteriaAttribute.builder()
                .endDay(LocalDate.now())
                .build();
        assertFalse(criteriaAttribute.isRetired());
    }
}
