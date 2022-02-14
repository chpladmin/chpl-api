package gov.healthit.chpl.attestation.manager;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import gov.healthit.chpl.attestation.dao.AttestationDAO;
import gov.healthit.chpl.attestation.domain.Attestation;
import gov.healthit.chpl.attestation.domain.AttestationForm;
import gov.healthit.chpl.attestation.domain.AttestationPeriod;
import gov.healthit.chpl.attestation.domain.DeveloperAttestationSubmission;
import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.exception.EntityRetrievalException;

public class AttestationManagerTest {

    private AttestationManager manager;
    private AttestationDAO attestationDAO;

    @Before
    public void setup() throws EntityRetrievalException {
        attestationDAO = Mockito.mock(AttestationDAO.class);
        Mockito.when(attestationDAO.getAllPeriods()).thenReturn(
                Arrays.asList(AttestationPeriod.builder()
                        .id(1L)
                        .periodEnd(LocalDate.of(2022, 3, 31))
                        .periodStart(LocalDate.of(2020, 7, 1))
                        .submissionEnd(LocalDate.of(2022, 4, 1))
                        .submissionStart(LocalDate.of(2022, 4, 30))
                        .build(),
                        AttestationPeriod.builder()
                        .id(2L)
                        .periodEnd(LocalDate.of(2022, 4, 1))
                        .periodStart(LocalDate.of(2022, 9, 30))
                        .submissionEnd(LocalDate.of(2022, 10, 1))
                        .submissionStart(LocalDate.of(2022, 10, 31))
                        .build()));

        Mockito.when(attestationDAO.getAttestationForm()).thenReturn(
                Arrays.asList(Attestation.builder().build(),
                        Attestation.builder().build()));

        Mockito.when(attestationDAO.getDeveloperAttestationSubmissionsByDeveloper(ArgumentMatchers.anyLong())).thenReturn(
                Arrays.asList(DeveloperAttestationSubmission.builder().build(),
                        DeveloperAttestationSubmission.builder().build()));

        Mockito.when(attestationDAO.createDeveloperAttestationSubmission(ArgumentMatchers.any())).thenReturn(
                DeveloperAttestationSubmission.builder().build());

        manager = new AttestationManager(attestationDAO, null, null);
    }

    @Test
    public void getAllPeriods_Success_Returns2Periods() {
        List<AttestationPeriod> periods = manager.getAllPeriods();

        assertEquals(2, periods.size());
    }

    @Test
    public void getAttestationForm_Success_ReturnsAttestationForm() {
        AttestationForm form = manager.getAttestationForm();

        assertNotNull(form);
    }

    @Test
    public void getDeveloperAttestations_Success_Returns2DeveloperAttestations() {
        List<DeveloperAttestationSubmission> submissions = manager.getDeveloperAttestations(1L);

        assertEquals(2, submissions.size());
    }

    @Test
    public void saveDeveloperAttestation_NoPreviousAttestationForDeveloperAndPeriod_DeleteNotCalled() throws EntityRetrievalException {
        Mockito.when(attestationDAO.getDeveloperAttestationSubmissionsByDeveloperAndPeriod(ArgumentMatchers.anyLong(), ArgumentMatchers.anyLong())).thenReturn(
                new ArrayList<DeveloperAttestationSubmission>());

        manager.saveDeveloperAttestation(DeveloperAttestationSubmission.builder()
                .developer(Developer.builder()
                        .developerId(1L)
                        .build())
                .period(AttestationPeriod.builder()
                        .id(1L)
                        .build())
                .build());

        Mockito.verify(attestationDAO, Mockito.never()).deleteDeveloperAttestationSubmission(ArgumentMatchers.anyLong());
    }

    @Test
    public void saveDeveloperAttestation_1PreviousAttestationForDeveloperAndPeriod_DeleteCalled1Time() throws EntityRetrievalException {
        Mockito.doNothing().when(attestationDAO).deleteDeveloperAttestationSubmission(ArgumentMatchers.anyLong());
        Mockito.when(attestationDAO.getDeveloperAttestationSubmissionsByDeveloperAndPeriod(ArgumentMatchers.anyLong(), ArgumentMatchers.anyLong())).thenReturn(
                Arrays.asList(DeveloperAttestationSubmission.builder()
                .id(1L)
                .developer(Developer.builder()
                        .developerId(1L)
                        .build())
                .period(AttestationPeriod.builder()
                        .id(1L)
                        .build())
                .build()));

        manager.saveDeveloperAttestation(DeveloperAttestationSubmission.builder()
                .developer(Developer.builder()
                        .developerId(1L)
                        .build())
                .period(AttestationPeriod.builder()
                        .id(1L)
                        .build())
                .build());

        Mockito.verify(attestationDAO, Mockito.times(1)).deleteDeveloperAttestationSubmission(1L);
    }

}
