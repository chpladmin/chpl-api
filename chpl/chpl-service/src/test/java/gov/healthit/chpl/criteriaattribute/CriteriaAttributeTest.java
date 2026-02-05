package gov.healthit.chpl.criteriaattribute;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

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
