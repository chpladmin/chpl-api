package gov.healthit.chpl.criteriaattribute;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;

import org.junit.Test;

public class CriteriaAttributeTest {

    @Test
    public void isRetired_StartDayAndEndDayBothNull_ReturnFalse() {
        CriteriaAttribute criteriaAttribute = CriteriaAttribute.builder().build();
        assertFalse(criteriaAttribute.isRetired());
    }

    @Test
    public void isRetired_StartDayBeforeNowAndEndDayNull_ReturnFalse() {
        CriteriaAttribute criteriaAttribute = CriteriaAttribute.builder()
                .startDay(LocalDate.MIN)
                .build();
        assertFalse(criteriaAttribute.isRetired());
    }

    @Test
    public void isRetired_StartDayNullAndEndDayAfterNow_ReturnFalse() {
        CriteriaAttribute criteriaAttribute = CriteriaAttribute.builder()
                .endDay(LocalDate.MAX)
                .build();
        assertFalse(criteriaAttribute.isRetired());
    }

    @Test
    public void isRetired_NowBetweenStartDayAndEndDay_ReturnFalse() {
        CriteriaAttribute criteriaAttribute = CriteriaAttribute.builder()
                .startDay(LocalDate.MIN)
                .endDay(LocalDate.MAX)
                .build();
        assertFalse(criteriaAttribute.isRetired());
    }

    @Test
    public void isRetired_NowBeforeStartDay_ReturnTrue() {
        CriteriaAttribute criteriaAttribute = CriteriaAttribute.builder()
                .startDay(LocalDate.MAX)
                .build();
        assertTrue(criteriaAttribute.isRetired());
    }

    @Test
    public void isRetired_NowAfterEndDay_ReturnTrue() {
        CriteriaAttribute criteriaAttribute = CriteriaAttribute.builder()
                .endDay(LocalDate.MIN)
                .build();
        assertTrue(criteriaAttribute.isRetired());
    }

    @Test
    public void isRetired_NowAfterBothStartDayAndEndDay_ReturnTrue() {
        CriteriaAttribute criteriaAttribute = CriteriaAttribute.builder()
                .startDay(LocalDate.MIN)
                .endDay(LocalDate.MIN.plusDays(1))
                .build();
        assertTrue(criteriaAttribute.isRetired());
    }

    public void isRetired_NowBeforeBothStartDayAndEndDay_ReturnTrue() {
        CriteriaAttribute criteriaAttribute = CriteriaAttribute.builder()
                .startDay(LocalDate.MAX)
                .endDay(LocalDate.MAX.minusDays(1))
                .build();
        assertTrue(criteriaAttribute.isRetired());
    }
}
