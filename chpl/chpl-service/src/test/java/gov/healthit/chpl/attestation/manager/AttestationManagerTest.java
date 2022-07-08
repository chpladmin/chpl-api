package gov.healthit.chpl.attestation.manager;

import gov.healthit.chpl.attestation.dao.AttestationDAO;

public class AttestationManagerTest {
    private static final Integer DEFAULT_EXCEPTION_WINDOW = 3;
    private AttestationManager manager;
    private AttestationDAO attestationDAO;
    private AttestationPeriodService attestationPeriodService;

    /*
    @Before
    public void setup() throws EntityRetrievalException {
        attestationDAO = Mockito.mock(AttestationDAO.class);

        Mockito.when(attestationDAO.getAttestationForm()).thenReturn(
                Arrays.asList(Attestation.builder().build(),
                        Attestation.builder().build()));

        Mockito.when(attestationDAO.getDeveloperAttestationSubmissionsByDeveloper(ArgumentMatchers.anyLong())).thenReturn(
                Arrays.asList(DeveloperAttestationSubmission.builder().build(),
                        DeveloperAttestationSubmission.builder().build()));

        Mockito.when(attestationDAO.createDeveloperAttestationSubmission(ArgumentMatchers.any())).thenReturn(
                DeveloperAttestationSubmission.builder().build());

        attestationPeriodService = Mockito.mock(AttestationPeriodService.class);
        Mockito.when(attestationPeriodService.getMostRecentPastAttestationPeriod()).thenReturn(
                AttestationPeriod.builder()
                        .id(1L)
                        .periodStart(LocalDate.of(2021, 1, 1))
                        .periodEnd(LocalDate.of(2022, 1, 31))
                        .submissionStart(LocalDate.of(2022, 2, 1))
                        .submissionEnd(LocalDate.of(2022, 2, 28))
                        .description("First Period")
                        .build());

        Mockito.when(attestationPeriodService.getAllPeriods()).thenReturn(
                Arrays.asList(AttestationPeriod.builder()
                        .id(1L)
                        .periodStart(LocalDate.of(2021, 1, 1))
                        .periodEnd(LocalDate.of(2022, 1, 31))
                        .submissionStart(LocalDate.of(2022, 2, 1))
                        .submissionEnd(LocalDate.of(2022, 2, 28))
                        .description("First Period")
                        .build(),
                        AttestationPeriod.builder()
                        .id(2L)
                        .periodStart(LocalDate.of(2022, 2, 1))
                        .periodEnd(LocalDate.of(2022, 9, 30))
                        .submissionStart(LocalDate.of(2022, 10, 1))
                        .submissionEnd(LocalDate.of(2022, 10, 30))
                        .description("First Period")
                        .build()));

        manager = new AttestationManager(attestationDAO, attestationPeriodService, null, null, DEFAULT_EXCEPTION_WINDOW);
    }

    @Test
    public void getAllPeriods_Success_Returns2Periods() {
        List<AttestationPeriod> periods = manager.getAllPeriods();

        assertEquals(2, periods.size());
    }

    @Test
    public void getAttestationForm_Success_ReturnsAttestationForm() {
        AttestationPeriodForm form = manager.getAttestationForm();

        assertNotNull(form.getAttestations());
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
                        .id(1L)
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
                        .id(1L)
                        .build())
                .period(AttestationPeriod.builder()
                        .id(1L)
                        .build())
                .build()));

        manager.saveDeveloperAttestation(DeveloperAttestationSubmission.builder()
                .developer(Developer.builder()
                        .id(1L)
                        .build())
                .period(AttestationPeriod.builder()
                        .id(1L)
                        .build())
                .build());

        Mockito.verify(attestationDAO, Mockito.times(1)).deleteDeveloperAttestationSubmission(1L);
    }
    */
}
