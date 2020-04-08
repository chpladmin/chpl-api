package gov.healthit.chpl.validation.pendinglisting.reviewer.edition2015;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.core.env.Environment;

import gov.healthit.chpl.SpecialProperties;
import gov.healthit.chpl.dao.CertificationCriterionDAO;
import gov.healthit.chpl.dto.CertificationCriterionDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertificationResultDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertifiedProductDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.ValidationUtils;
import gov.healthit.chpl.validation.pendingListing.reviewer.edition2015.PrivacyAndSecurityCriteriaReviewer;

public class PrivacyAndSecurityCriteriaReviewerTest {

    private CertificationCriterionDAO certificationCriterionDAO;
    private Environment env;
    private ErrorMessageUtil errorMessageUtil;
    private SpecialProperties specialProperties;
    private ValidationUtils validationUtil;

    @Before
    public void before() throws EntityRetrievalException {
        certificationCriterionDAO = Mockito.mock(CertificationCriterionDAO.class);
        Mockito.when(certificationCriterionDAO.getById(1L)).thenReturn(getCriterionDTO(1l, "170.315 (a)(1)"));
        Mockito.when(certificationCriterionDAO.getById(2L)).thenReturn(getCriterionDTO(2l, "170.315 (a)(2)"));
        Mockito.when(certificationCriterionDAO.getById(3L)).thenReturn(getCriterionDTO(3l, "170.315 (a)(3)"));
        Mockito.when(certificationCriterionDAO.getById(166L)).thenReturn(getCriterionDTO(166l, "170.315 (d)(12)"));
        Mockito.when(certificationCriterionDAO.getById(167L)).thenReturn(getCriterionDTO(167l, "170.315 (d)(13)"));

        env = Mockito.mock(Environment.class);
        Mockito.when(env.getProperty("privacyAndSecurityCriteria")).thenReturn("1,2");
        Mockito.when(env.getProperty("privacyAndSecurityRequiredCriteria")).thenReturn("166,167");

        errorMessageUtil = Mockito.mock(ErrorMessageUtil.class);
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.anyString(), ArgumentMatchers.anyString(),
                ArgumentMatchers.anyString()))
                .thenAnswer(i -> String.format("Attesting to Criteria %s requires that Criteria %s must also be attested to.",
                        i.getArgument(1), i.getArgument(2)));

        specialProperties = Mockito.mock(SpecialProperties.class);
        Mockito.when(specialProperties.getEffectiveRuleDate())
                .thenReturn(new GregorianCalendar(2020, Calendar.MARCH, 01).getTime());

        CertificationCriterionDAO criterionDao = Mockito.mock(CertificationCriterionDAO.class);
        validationUtil = new ValidationUtils(criterionDao);
    }

    @Test
    public void review_GeneratesErrorMessages_HasErrorMessages() {
        // This is a trivial test - the real work is done by ValidationUtils.checkSubordinateCriteriaAllRequired

        // Setup
        PendingCertifiedProductDTO listing = PendingCertifiedProductDTO.builder()
                .certificationDate(new Date())
                .certificationCriterionSingle(PendingCertificationResultDTO.builder()
                        .meetsCriteria(true)
                        .criterion(getCriterionDTO(1L, "170.315 (a)(1)"))
                        .build())
                .certificationCriterionSingle(PendingCertificationResultDTO.builder()
                        .meetsCriteria(true)
                        .criterion(getCriterionDTO(2L, "170.315 (a)(2)"))
                        .build())
                .certificationCriterionSingle(PendingCertificationResultDTO.builder()
                        .meetsCriteria(true)
                        .criterion(getCriterionDTO(166L, "170.315 (d)(12)"))
                        .build())
                .build();
        listing.setErrorMessages(new HashSet<String>());

        PrivacyAndSecurityCriteriaReviewer reviewer = new PrivacyAndSecurityCriteriaReviewer(certificationCriterionDAO, env,
                errorMessageUtil, specialProperties, validationUtil);
        reviewer.postConstruct();

        // Test
        reviewer.review(listing);

        // Check
        assertEquals(2, listing.getErrorMessages().size());
    }

    @Test
    public void review_DoesNotGeneratesErrorMessages_NoErrorMessages() {
        // This is a trivial test - the real work is done by ValidationUtils.checkSubordinateCriteriaAllRequired

        // Setup
        PendingCertifiedProductDTO listing = PendingCertifiedProductDTO.builder()
                .certificationDate(new Date())
                .certificationCriterionSingle(PendingCertificationResultDTO.builder()
                        .meetsCriteria(true)
                        .criterion(getCriterionDTO(1L, "170.315 (a)(1)"))
                        .build())
                .certificationCriterionSingle(PendingCertificationResultDTO.builder()
                        .meetsCriteria(true)
                        .criterion(getCriterionDTO(2L, "170.315 (a)(2)"))
                        .build())
                .certificationCriterionSingle(PendingCertificationResultDTO.builder()
                        .meetsCriteria(true)
                        .criterion(getCriterionDTO(166L, "170.315 (d)(12)"))
                        .build())
                .certificationCriterionSingle(PendingCertificationResultDTO.builder()
                        .meetsCriteria(true)
                        .criterion(getCriterionDTO(167L, "170.315 (d)(13)"))
                        .build())
                .build();
        listing.setErrorMessages(new HashSet<String>());

        PrivacyAndSecurityCriteriaReviewer reviewer = new PrivacyAndSecurityCriteriaReviewer(certificationCriterionDAO, env,
                errorMessageUtil, specialProperties, validationUtil);
        reviewer.postConstruct();

        // Test
        reviewer.review(listing);

        // Check
        assertEquals(0, listing.getErrorMessages().size());
    }

    private CertificationCriterionDTO getCriterionDTO(Long id, String number) {
        return CertificationCriterionDTO.builder()
                .id(id)
                .number(number)
                .build();
    }

}
