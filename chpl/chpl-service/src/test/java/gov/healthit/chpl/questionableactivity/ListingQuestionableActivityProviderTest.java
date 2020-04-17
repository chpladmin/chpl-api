package gov.healthit.chpl.questionableactivity;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.apache.commons.lang3.StringUtils;
import org.ff4j.FF4j;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.core.env.Environment;

import gov.healthit.chpl.FeatureList;
import gov.healthit.chpl.SpecialProperties;
import gov.healthit.chpl.dao.CertificationCriterionDAO;
import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.InheritedCertificationStatus;
import gov.healthit.chpl.dto.CertificationCriterionDTO;
import gov.healthit.chpl.dto.questionableActivity.QuestionableActivityDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.service.CertificationCriterionService;

public class ListingQuestionableActivityProviderTest {

    private SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");

    private FF4j ff4j;
    private CertificationCriterionDAO certificationCriterionDAO;
    private Environment env;
    private SpecialProperties specialProperties;
    private CertificationCriterionService certificationCriterionService;

    @Before
    public void before() throws EntityRetrievalException, ParseException {
        ff4j = Mockito.mock(FF4j.class);
        Mockito.when(ff4j.check(FeatureList.EFFECTIVE_RULE_DATE)).thenReturn(true);

        env = Mockito.mock(Environment.class);
        Mockito.when(env.getProperty("criterion.170_315_d_2")).thenReturn("30");
        Mockito.when(env.getProperty("criterion.170_315_d_3")).thenReturn("31");
        Mockito.when(env.getProperty("criterion.170_315_d_10")).thenReturn("38");

        certificationCriterionDAO = Mockito.mock(CertificationCriterionDAO.class);
        Mockito.when(certificationCriterionDAO.getById(30L)).thenReturn(
                CertificationCriterionDTO.builder()
                        .id(30L)
                        .number("170.315 (d)(2)")
                        .build());
        Mockito.when(certificationCriterionDAO.getById(31L)).thenReturn(
                CertificationCriterionDTO.builder()
                        .id(31L)
                        .number("170.315 (d)(2)")
                        .build());
        Mockito.when(certificationCriterionDAO.getById(38L)).thenReturn(
                CertificationCriterionDTO.builder()
                        .id(38L)
                        .number("170.315 (d)(10)")
                        .build());

        specialProperties = Mockito.mock(SpecialProperties.class);
        Mockito.when(specialProperties.getEffectiveRuleDate()).thenReturn(sdf.parse("03/01/2020"));
    }

    @Test
    public void checkNonCuresAuditCriteriaOnCreate_CertDateBeforeERD_ReturnObjectNull() throws ParseException {
        Mockito.when(ff4j.check(FeatureList.EFFECTIVE_RULE_DATE)).thenReturn(false);

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationDate(sdf.parse("02/01/2020").getTime())
                .build();

        ListingQuestionableActivityProvider provider = new ListingQuestionableActivityProvider(null, ff4j, null,
                specialProperties, certificationCriterionService);
        QuestionableActivityDTO dto = provider.checkNonCuresAuditCriteriaOnCreate(listing);

        assertNull(dto);
    }

    @Test
    public void checkNonCuresAuditCriteriaOnCreate_ListingWithICS_ReturnObjectNull() throws ParseException {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationDate(sdf.parse("03/02/2020").getTime())
                .ics(InheritedCertificationStatus.builder()
                        .inherits(true)
                        .build())
                .build();

        ListingQuestionableActivityProvider provider = new ListingQuestionableActivityProvider(null, ff4j, null,
                specialProperties, certificationCriterionService);
        QuestionableActivityDTO dto = provider.checkNonCuresAuditCriteriaOnCreate(listing);

        assertNull(dto);
    }

    @Test
    public void checkNonCuresAuditCriteriaOnCreate_QuestionableActivityIsNotPresent_ReturnObjectNull()
            throws EntityRetrievalException, ParseException {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationDate(sdf.parse("03/02/2020").getTime())
                .ics(InheritedCertificationStatus.builder()
                        .inherits(false)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("170.315 (a)(1)")
                                .build())
                        .criterion(CertificationCriterion.builder()
                                .id(2L)
                                .number("170.315 (a)(2)")
                                .build())
                        .build())
                .build();

        ListingQuestionableActivityProvider provider = new ListingQuestionableActivityProvider(
                certificationCriterionDAO, ff4j, env, specialProperties, certificationCriterionService);
        provider.postConstruct();
        QuestionableActivityDTO dto = provider.checkNonCuresAuditCriteriaOnCreate(listing);

        assertNull(dto);
    }

    @Test
    public void checkNonCuresAuditCriteriaOnCreate_QuestionableActivityIsPresent_ReturnObjectNotNull()
            throws EntityRetrievalException, ParseException {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationDate(sdf.parse("03/02/2020").getTime())
                .ics(InheritedCertificationStatus.builder()
                        .inherits(false)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(CertificationCriterion.builder()
                                .id(30L)
                                .number("170.315 (d)(1)")
                                .build())
                        .criterion(CertificationCriterion.builder()
                                .id(31L)
                                .number("170.315 (d)(2)")
                                .build())
                        .build())
                .build();

        ListingQuestionableActivityProvider provider = new ListingQuestionableActivityProvider(
                certificationCriterionDAO, ff4j, env, specialProperties, certificationCriterionService);
        provider.postConstruct();
        QuestionableActivityDTO dto = provider.checkNonCuresAuditCriteriaOnCreate(listing);

        assertNotNull(dto);
        assertTrue(StringUtils.isNotBlank(dto.getAfter()));
    }

    @Test
    public void checkNonCuresAuditCriteriaOnEdit_ListingEditedBeforeERD_ReturnObjectNull() {
        Mockito.when(ff4j.check(FeatureList.EFFECTIVE_RULE_DATE)).thenReturn(false);

        ListingQuestionableActivityProvider provider = new ListingQuestionableActivityProvider(null, ff4j, null,
                specialProperties, certificationCriterionService);
        QuestionableActivityDTO dto = provider.checkNonCuresAuditCriteriaOnEdit(null, null);

        assertNull(dto);
    }

    @Test
    public void checkNonCuresAuditCriteriaOnEdit_CriterionD2DidNotChange_ReturnObjectNotNull() throws EntityRetrievalException {
        CertifiedProductSearchDetails origListing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .success(false)
                        .criterion(CertificationCriterion.builder()
                                .id(30L)
                                .number("170.315 (d)(2)")
                                .build())
                        .build())
                .build();

        CertifiedProductSearchDetails newListing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .success(false)
                        .criterion(CertificationCriterion.builder()
                                .id(30L)
                                .number("170.315 (d)(2)")
                                .build())
                        .build())
                .build();

        ListingQuestionableActivityProvider provider = new ListingQuestionableActivityProvider(
                certificationCriterionDAO, ff4j, env, specialProperties, certificationCriterionService);
        provider.postConstruct();
        QuestionableActivityDTO dto = provider.checkNonCuresAuditCriteriaOnEdit(origListing, newListing);

        assertNull(dto);
    }

    @Test
    public void checkNonCuresAuditCriteriaOnEdit_CriterionD2ChangedToAttestedTo_ReturnObjectNotNull()
            throws EntityRetrievalException {
        CertifiedProductSearchDetails origListing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .success(false)
                        .criterion(CertificationCriterion.builder()
                                .id(30L)
                                .number("170.315 (d)(2)")
                                .build())
                        .build())
                .build();

        CertifiedProductSearchDetails newListing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(CertificationCriterion.builder()
                                .id(30L)
                                .number("170.315 (d)(2)")
                                .build())
                        .build())
                .build();

        ListingQuestionableActivityProvider provider = new ListingQuestionableActivityProvider(
                certificationCriterionDAO, ff4j, env, specialProperties, certificationCriterionService);
        provider.postConstruct();
        QuestionableActivityDTO dto = provider.checkNonCuresAuditCriteriaOnEdit(origListing, newListing);

        assertNotNull(dto);
        assertTrue(StringUtils.isNotBlank(dto.getAfter()));
    }

    public void checkNonCuresAuditCriteriaOnEdit_CriterionD3DidNotChange_ReturnObjectNotNull() throws EntityRetrievalException {
        CertifiedProductSearchDetails origListing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .success(false)
                        .criterion(CertificationCriterion.builder()
                                .id(31L)
                                .number("170.315 (d)(3)")
                                .build())
                        .build())
                .build();

        CertifiedProductSearchDetails newListing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .success(false)
                        .criterion(CertificationCriterion.builder()
                                .id(31L)
                                .number("170.315 (d)(3)")
                                .build())
                        .build())
                .build();

        ListingQuestionableActivityProvider provider = new ListingQuestionableActivityProvider(
                certificationCriterionDAO, ff4j, env, specialProperties, certificationCriterionService);
        provider.postConstruct();
        QuestionableActivityDTO dto = provider.checkNonCuresAuditCriteriaOnEdit(origListing, newListing);

        assertNull(dto);
    }

    @Test
    public void checkNonCuresAuditCriteriaOnEdit_CriterionD3ChangedToAttestedTo_ReturnObjectNotNull()
            throws EntityRetrievalException {
        CertifiedProductSearchDetails origListing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .success(false)
                        .criterion(CertificationCriterion.builder()
                                .id(31L)
                                .number("170.315 (d)(3)")
                                .build())
                        .build())
                .build();

        CertifiedProductSearchDetails newListing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(CertificationCriterion.builder()
                                .id(31L)
                                .number("170.315 (d)(3)")
                                .build())
                        .build())
                .build();

        ListingQuestionableActivityProvider provider = new ListingQuestionableActivityProvider(
                certificationCriterionDAO, ff4j, env, specialProperties, certificationCriterionService);
        provider.postConstruct();
        QuestionableActivityDTO dto = provider.checkNonCuresAuditCriteriaOnEdit(origListing, newListing);

        assertNotNull(dto);
        assertTrue(StringUtils.isNotBlank(dto.getAfter()));

    }

    public void checkNonCuresAuditCriteriaOnEdit_CriterionD10DidNotChange_ReturnObjectNotNull() throws EntityRetrievalException {
        CertifiedProductSearchDetails origListing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .success(false)
                        .criterion(CertificationCriterion.builder()
                                .id(38L)
                                .number("170.315 (d)(10)")
                                .build())
                        .build())
                .build();

        CertifiedProductSearchDetails newListing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .success(false)
                        .criterion(CertificationCriterion.builder()
                                .id(38L)
                                .number("170.315 (d)(10)")
                                .build())
                        .build())
                .build();

        ListingQuestionableActivityProvider provider = new ListingQuestionableActivityProvider(
                certificationCriterionDAO, ff4j, env, specialProperties, certificationCriterionService);
        provider.postConstruct();
        QuestionableActivityDTO dto = provider.checkNonCuresAuditCriteriaOnEdit(origListing, newListing);

        assertNull(dto);
    }

    @Test
    public void checkNonCuresAuditCriteriaOnEdit_CriterionD10ChangedToAttestedTo_ReturnObjectNotNull()
            throws EntityRetrievalException {
        CertifiedProductSearchDetails origListing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .success(false)
                        .criterion(CertificationCriterion.builder()
                                .id(38L)
                                .number("170.315 (d)(10)")
                                .build())
                        .build())
                .build();

        CertifiedProductSearchDetails newListing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(CertificationCriterion.builder()
                                .id(38L)
                                .number("170.315 (d)(10)")
                                .build())
                        .build())
                .build();

        ListingQuestionableActivityProvider provider = new ListingQuestionableActivityProvider(
                certificationCriterionDAO, ff4j, env, specialProperties, certificationCriterionService);
        provider.postConstruct();
        QuestionableActivityDTO dto = provider.checkNonCuresAuditCriteriaOnEdit(origListing, newListing);

        assertNotNull(dto);
        assertTrue(StringUtils.isNotBlank(dto.getAfter()));
    }

    @Test
    public void checkNonCuresAuditCriteriaAndAddedIcsOnEdit_ListingEditedBeforeERD_ReturnObjectNull() {
        Mockito.when(ff4j.check(FeatureList.EFFECTIVE_RULE_DATE)).thenReturn(false);

        ListingQuestionableActivityProvider provider = new ListingQuestionableActivityProvider(null, ff4j, null,
                specialProperties, certificationCriterionService);
        QuestionableActivityDTO dto = provider.checkNonCuresAuditCriteriaAndAddedIcsOnEdit(null, null);

        assertNull(dto);
    }

    @Test
    public void checkNonCuresAuditCriteriaAndAddedIcsOnEdit_IcsAddedAndNoCriteriaChange_ReturnObjectNull()
            throws EntityRetrievalException {
        CertifiedProductSearchDetails origListing = CertifiedProductSearchDetails.builder()
                .ics(InheritedCertificationStatus.builder()
                        .inherits(false)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(CertificationCriterion.builder()
                                .id(38L)
                                .number("170.315 (d)(10)")
                                .build())
                        .build())
                .build();

        CertifiedProductSearchDetails newListing = CertifiedProductSearchDetails.builder()
                .ics(InheritedCertificationStatus.builder()
                        .inherits(true)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(CertificationCriterion.builder()
                                .id(38L)
                                .number("170.315 (d)(10)")
                                .build())
                        .build())
                .build();

        ListingQuestionableActivityProvider provider = new ListingQuestionableActivityProvider(
                certificationCriterionDAO, ff4j, env, specialProperties, certificationCriterionService);
        provider.postConstruct();
        QuestionableActivityDTO dto = provider.checkNonCuresAuditCriteriaOnEdit(origListing, newListing);

        assertNull(dto);
    }

    @Test
    public void checkNonCuresAuditCriteriaAndAddedIcsOnEdit_IcsAddedAndAuditCriteriaChange_ReturnObjectNotNull()
            throws EntityRetrievalException {
        CertifiedProductSearchDetails origListing = CertifiedProductSearchDetails.builder()
                .ics(InheritedCertificationStatus.builder()
                        .inherits(false)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .success(false)
                        .criterion(CertificationCriterion.builder()
                                .id(38L)
                                .number("170.315 (d)(10)")
                                .build())
                        .build())
                .build();

        CertifiedProductSearchDetails newListing = CertifiedProductSearchDetails.builder()
                .ics(InheritedCertificationStatus.builder()
                        .inherits(true)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(CertificationCriterion.builder()
                                .id(38L)
                                .number("170.315 (d)(10)")
                                .build())
                        .build())
                .build();

        ListingQuestionableActivityProvider provider = new ListingQuestionableActivityProvider(
                certificationCriterionDAO, ff4j, env, specialProperties, certificationCriterionService);
        provider.postConstruct();
        QuestionableActivityDTO dto = provider.checkNonCuresAuditCriteriaOnEdit(origListing, newListing);

        assertNotNull(dto);
        assertTrue(StringUtils.isNotBlank(dto.getAfter()));
    }

}
