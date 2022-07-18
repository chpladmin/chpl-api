package gov.healthit.chpl.attestation.manager;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import gov.healthit.chpl.attestation.dao.AttestationDAO;
import gov.healthit.chpl.attestation.domain.AttestationPeriod;
import gov.healthit.chpl.attestation.domain.AttestationPeriodDeveloperException;
import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.form.Form;

public class AttestationPeriodServiceTest {
    private AttestationPeriodService service;
    private AttestationDAO attestationDAO;

    @Before
    public void setup() {
        attestationDAO = Mockito.mock(AttestationDAO.class);

        service = new AttestationPeriodService(attestationDAO);
    }

    @Test
    public void getAllPeriods_2PeriodsExist_2PeriodsReturned() {
        Mockito.when(attestationDAO.getAllPeriods()).thenReturn(List.of(getFirstAttestationPeriod(), getSecondAttestationPeriod()));

        List<AttestationPeriod> periods = service.getAllPeriods();

        assertNotNull(periods);
        assertEquals(2, periods.size());
    }

    @Test
    public void getAllPeriods_0PeriodsExist_0PeriodsReturned() {
        Mockito.when(attestationDAO.getAllPeriods()).thenReturn(List.of());

        List<AttestationPeriod> periods = service.getAllPeriods();

        assertNotNull(periods);
        assertEquals(0, periods.size());
    }

    @Test
    public void getMostRecentPastAttestationPeriod_NoPeriodsExist_ReturnNull() {
        Mockito.when(attestationDAO.getAllPeriods()).thenReturn(List.of());

        assertNull(service.getMostRecentPastAttestationPeriod());
    }

    @Test
    public void getMostRecentPastAttestationPeriod_NowFallsIntoSecondPeriod_ReturnFirstPeriod() {
        LocalDate nowDate = LocalDate.of(2022, 7, 15);
        AttestationPeriod firstPeriod = getFirstAttestationPeriod();
        AttestationPeriod secondPeriod = getSecondAttestationPeriod();

        try (MockedStatic<LocalDate> mockedLocalDate = Mockito.mockStatic(LocalDate.class)) {
            mockedLocalDate.when(LocalDate::now).thenReturn(nowDate);

            Mockito.when(attestationDAO.getAllPeriods()).thenReturn(List.of(firstPeriod, secondPeriod));

            AttestationPeriod period = service.getMostRecentPastAttestationPeriod();

            assertNotNull(period);
            assertEquals("First Period", period.getDescription());
        }
    }

    @Test
    public void getCurrentExceptionEndDateForDeveloper_ExceptionExists_ReturnNonNullLocalDate() {
        Mockito.when(attestationDAO.getCurrentAttestationPeriodDeveloperException(ArgumentMatchers.anyLong())).thenReturn(
                AttestationPeriodDeveloperException.builder()
                        .id(1L)
                        .developer(Developer.builder()
                                .id(1L)
                                .build())
                        .period(getFirstAttestationPeriod())
                        .exceptionEnd(LocalDate.of(2022, 7, 1))
                        .build());

        LocalDate exceptionDate = service.getCurrentExceptionEndDateForDeveloper(1L);

        assertNotNull(exceptionDate);
        assertEquals(LocalDate.of(2022, 7, 1), exceptionDate);
    }

    @Test
    public void getCurrentExceptionEndDateForDeveloper_ExceptionDoesNotExist_ReturnNull() {
        Mockito.when(attestationDAO.getCurrentAttestationPeriodDeveloperException(ArgumentMatchers.anyLong())).thenReturn(null);

        LocalDate exceptionDate = service.getCurrentExceptionEndDateForDeveloper(1L);

        assertNull(exceptionDate);
    }

    @Test
    public void getSubmittableAttestationPeriod_ExceptionExistsAndIsBeforeNow_ReturnPeriodRelatedToException() {
        LocalDate nowDate = LocalDate.of(2022, 7, 15);
        LocalDate exceptionEndDate = LocalDate.of(2022, 7, 16);
        AttestationPeriod secondPeriod = getSecondAttestationPeriod();

        try (MockedStatic<LocalDate> mockedLocalDate = Mockito.mockStatic(LocalDate.class)){
            mockedLocalDate.when(LocalDate::now).thenReturn(nowDate);

            Mockito.when(attestationDAO.getCurrentAttestationPeriodDeveloperException(ArgumentMatchers.anyLong())).thenReturn(
                    AttestationPeriodDeveloperException.builder()
                            .id(1L)
                            .developer(Developer.builder()
                                    .id(1L)
                                    .build())
                            .period(secondPeriod)
                            .exceptionEnd(exceptionEndDate)
                            .build());

            AttestationPeriod period = service.getSubmittableAttestationPeriod(1L);

            assertNotNull(period);
            assertEquals("Second Period", period.getDescription());
        }
    }

    @Test
    public void getSubmittableAttestationPeriod_ExceptionDoesNotExistsAndNowIsWithinSecondPeriod_ReturnFirstPeriod() {
        LocalDate nowDate = LocalDate.of(2022, 7, 15);
        AttestationPeriod firstPeriod = getFirstAttestationPeriod();
        AttestationPeriod secondPeriod = getSecondAttestationPeriod();

        try (MockedStatic<LocalDate> mockedLocalDate = Mockito.mockStatic(LocalDate.class)) {
            mockedLocalDate.when(LocalDate::now).thenReturn(nowDate);

            Mockito.when(attestationDAO.getCurrentAttestationPeriodDeveloperException(ArgumentMatchers.anyLong())).thenReturn(null);
            Mockito.when(attestationDAO.getAllPeriods()).thenReturn(List.of(firstPeriod, secondPeriod));

            AttestationPeriod period = service.getSubmittableAttestationPeriod(1L);

            assertNotNull(period);
            assertEquals("First Period", period.getDescription());
        }
    }

    @Test
    public void getSubmittableAttestationPeriod_ExceptionDoesNotExistsAndNowIsAfterAllPeriods_ReturnLast() {
        LocalDate nowDate = LocalDate.of(2022, 12, 1);
        AttestationPeriod firstPeriod = getFirstAttestationPeriod();
        AttestationPeriod secondPeriod = getSecondAttestationPeriod();

        try (MockedStatic<LocalDate> mockedLocalDate = Mockito.mockStatic(LocalDate.class)) {
            mockedLocalDate.when(LocalDate::now).thenReturn(nowDate);

            Mockito.when(attestationDAO.getCurrentAttestationPeriodDeveloperException(ArgumentMatchers.anyLong())).thenReturn(null);
            Mockito.when(attestationDAO.getAllPeriods()).thenReturn(List.of(firstPeriod, secondPeriod));

            AttestationPeriod period = service.getSubmittableAttestationPeriod(1L);

            assertNotNull(period);
            assertEquals(secondPeriod, period);
        }
    }

    private AttestationPeriod getFirstAttestationPeriod() {
        return AttestationPeriod.builder()
                .id(1L)
                .periodStart(LocalDate.of(2021, 1, 1))
                .periodEnd(LocalDate.of(2022, 1, 31))
                .submissionStart(LocalDate.of(2022, 2, 1))
                .submissionEnd(LocalDate.of(2022, 2, 28))
                .description("First Period")
                .form(Form.builder()
                        .id(1L)
                        .build())
                .build();
    }

    private AttestationPeriod getSecondAttestationPeriod() {
        return AttestationPeriod.builder()
                .id(2L)
                .periodStart(LocalDate.of(2022, 2, 1))
                .periodEnd(LocalDate.of(2022, 9, 30))
                .submissionStart(LocalDate.of(2022, 10, 1))
                .submissionEnd(LocalDate.of(2022, 10, 30))
                .description("Second Period")
                .form(Form.builder()
                        .id(2L)
                        .build())
                .build();
    }

}
