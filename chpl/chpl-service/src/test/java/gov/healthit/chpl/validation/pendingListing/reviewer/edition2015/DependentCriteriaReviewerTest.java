package gov.healthit.chpl.validation.pendingListing.reviewer.edition2015;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashSet;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.core.env.Environment;

import gov.healthit.chpl.dao.CertificationCriterionDAO;
import gov.healthit.chpl.dto.CertificationCriterionDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertificationResultDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertifiedProductDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.util.ErrorMessageUtil;

public class DependentCriteriaReviewerTest {

    private Environment env;
    private ErrorMessageUtil errorMessageUtil;
    private CertificationCriterionDAO certificationCriterionDAO;

    private DependentCriteriaReviewer dependentCriteriaReviewer;

    @Before
    public void before() throws EntityRetrievalException {
        env = Mockito.mock(Environment.class);
        // For criteria ids 1,2,3,4,5, criteria 166, 167 are required
        Mockito.when(env.getProperty("requiredCriteriaDependencies")).thenReturn(
                "[{\"dependentCriteria\": [1,2,3,4,5], \"requiredCriteria\": [166,167]}]");

        errorMessageUtil = Mockito.mock(ErrorMessageUtil.class);
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.anyString(), ArgumentMatchers.anyString(),
                ArgumentMatchers.anyString()))
                .thenAnswer(i -> String.format("Attesting to Criteria %s requires that Criteria %s must also be attested to.",
                        i.getArgument(1), i.getArgument(2)));

        certificationCriterionDAO = Mockito.mock(CertificationCriterionDAO.class);
        Mockito.when(certificationCriterionDAO.getById(1L)).thenReturn(getCriterion(1l, "170.315 (a)(1)"));
        Mockito.when(certificationCriterionDAO.getById(166L)).thenReturn(getCriterion(166l, "170.315 (d)(12)"));
        Mockito.when(certificationCriterionDAO.getById(167L)).thenReturn(getCriterion(167l, "170.315 (d)(13)"));

        dependentCriteriaReviewer = new DependentCriteriaReviewer(env, certificationCriterionDAO, errorMessageUtil);
        // Force call the postconstruct method
        dependentCriteriaReviewer.postConstruct();
    }

    @Test
    public void review_NoDependentCriteria_NoMessages() {
        PendingCertifiedProductDTO listing = PendingCertifiedProductDTO.builder()
                .certificationCriterionSingle(PendingCertificationResultDTO.builder()
                        .meetsCriteria(true)
                        .criterion(CertificationCriterionDTO.builder()
                                .id(21L)
                                .number("170.315 (b)(6)")
                                .build())
                        .build())
                .build();
        listing.setErrorMessages(new HashSet<String>());

        dependentCriteriaReviewer.review(listing);

        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_DependentCriteriaAndNoRequiredCriteria_MultipleMesssages() {
        PendingCertifiedProductDTO listing = PendingCertifiedProductDTO.builder()
                .certificationCriterionSingle(PendingCertificationResultDTO.builder()
                        .meetsCriteria(true)
                        .criterion(CertificationCriterionDTO.builder()
                                .id(1L)
                                .number("170.315 (a)(1)")
                                .build())
                        .build())
                .build();
        listing.setErrorMessages(new HashSet<String>());

        dependentCriteriaReviewer.review(listing);

        assertEquals(2, listing.getErrorMessages().size());
    }

    @Test
    public void review_DependentCriteriaAndOneRequiredCriteria_OneErrorMessage() {
        PendingCertifiedProductDTO listing = PendingCertifiedProductDTO.builder()
                .certificationCriterionSingle(PendingCertificationResultDTO.builder()
                        .meetsCriteria(true)
                        .criterion(CertificationCriterionDTO.builder()
                                .id(1L)
                                .number("170.315 (a)(1)")
                                .build())
                        .build())
                .certificationCriterionSingle(PendingCertificationResultDTO.builder()
                        .meetsCriteria(true)
                        .criterion(CertificationCriterionDTO.builder()
                                .id(166L)
                                .number("170.315 (d)(12)")
                                .build())
                        .build())
                .build();
        listing.setErrorMessages(new HashSet<String>());

        dependentCriteriaReviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
    }

    @Test
    public void review_DependentCriteriaAndAllRequiredCriteria_NoMessages() {
        PendingCertifiedProductDTO listing = PendingCertifiedProductDTO.builder()
                .certificationCriterionSingle(PendingCertificationResultDTO.builder()
                        .meetsCriteria(true)
                        .criterion(CertificationCriterionDTO.builder()
                                .id(1L)
                                .number("170.315 (a)(1)")
                                .build())
                        .build())
                .certificationCriterionSingle(PendingCertificationResultDTO.builder()
                        .meetsCriteria(true)
                        .criterion(CertificationCriterionDTO.builder()
                                .id(166L)
                                .number("170.315 (d)(12)")
                                .build())
                        .build())
                .certificationCriterionSingle(PendingCertificationResultDTO.builder()
                        .meetsCriteria(true)
                        .criterion(CertificationCriterionDTO.builder()
                                .id(167L)
                                .number("170.315 (d)(13)")
                                .build())
                        .build())
                .build();
        listing.setErrorMessages(new HashSet<String>());

        dependentCriteriaReviewer.review(listing);

        assertEquals(0, listing.getErrorMessages().size());

    }

    private CertificationCriterionDTO getCriterion(Long id, String number) {
        return CertificationCriterionDTO.builder()
                .id(id)
                .number(number)
                .build();
    }
}
