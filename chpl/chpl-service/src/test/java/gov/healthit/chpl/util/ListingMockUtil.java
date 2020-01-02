package gov.healthit.chpl.util;

import static org.junit.Assert.fail;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.Address;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertificationResultTestData;
import gov.healthit.chpl.domain.CertificationResultTestFunctionality;
import gov.healthit.chpl.domain.CertificationResultTestProcedure;
import gov.healthit.chpl.domain.CertificationResultTestStandard;
import gov.healthit.chpl.domain.CertificationResultTestTool;
import gov.healthit.chpl.domain.CertificationStatus;
import gov.healthit.chpl.domain.CertificationStatusEvent;
import gov.healthit.chpl.domain.CertifiedProduct;
import gov.healthit.chpl.domain.CertifiedProductAccessibilityStandard;
import gov.healthit.chpl.domain.CertifiedProductQmsStandard;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.CertifiedProductTestingLab;
import gov.healthit.chpl.domain.Contact;
import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.domain.DeveloperStatus;
import gov.healthit.chpl.domain.DeveloperStatusEvent;
import gov.healthit.chpl.domain.InheritedCertificationStatus;
import gov.healthit.chpl.domain.Product;
import gov.healthit.chpl.domain.ProductVersion;
import gov.healthit.chpl.domain.TestData;
import gov.healthit.chpl.domain.TestProcedure;
import gov.healthit.chpl.dto.AddressDTO;
import gov.healthit.chpl.dto.CertificationResultDetailsDTO;
import gov.healthit.chpl.dto.MacraMeasureDTO;
import gov.healthit.chpl.dto.TestDataDTO;
import gov.healthit.chpl.dto.TestProcedureDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertificationResultDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertificationResultMacraMeasureDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertificationResultTestDataDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertificationResultTestFunctionalityDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertificationResultTestProcedureDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertificationResultTestStandardDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertificationResultTestTaskDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertificationResultTestTaskParticipantDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertificationResultTestToolDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertificationResultUcdProcessDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertifiedProductDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertifiedProductQmsStandardDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertifiedProductTestingLabDTO;
import gov.healthit.chpl.dto.listing.pending.PendingTestParticipantDTO;
import gov.healthit.chpl.dto.listing.pending.PendingTestTaskDTO;
import gov.healthit.chpl.util.CertificationResultRules;

@Component
public class ListingMockUtil {
    public static final String CHPL_ID_2014 = "14.07.07.2642.IC04.36.00.1.160402";
    public static final String CHPL_ID_2015 = "15.02.02.3007.A056.01.00.0.180214";
    private static final Long EDITION_2015_ID = 3L;
    private static final Long EDITION_2014_ID = 2L;
    @Autowired private CertificationResultRules certRules;

    public CertifiedProduct createSimpleCertifiedProduct(Long id, String chplProductNumber,
            String edition, Date certificationDate) {
        CertifiedProduct listing = new CertifiedProduct();
        listing.setId(id);
        listing.setCertificationDate(certificationDate.getTime());
        listing.setChplProductNumber(chplProductNumber);
        listing.setEdition(edition);
        return listing;
    }

    public PendingCertifiedProductDTO createPending2014Listing() {
        PendingCertifiedProductDTO listing = createPendingListing("2014");
        listing.setAcbCertificationId("IG-4152-18-0007");
        listing.setAccessibilityCertified(null);
        listing.setCertificationBodyId(1L);
        listing.setCertificationBodyName("ICSA Labs");
        listing.setCertificationDate(new Date(1459555200000L));
        listing.setCertificationEdition("2014");
        listing.setCertificationEditionId(EDITION_2014_ID);
        listing.setDeveloperCity("Palo Alto");
        listing.setDeveloperContactName("Test Person");
        listing.setDeveloperEmail("test@mail.com");
        listing.setDeveloperId(1L);
        listing.setDeveloperName("Epic Systems Corporation");
        listing.setDeveloperPhoneNumber("555-444-3333");
        listing.setDeveloperState("CA");
        listing.setDeveloperStreetAddress("541 E. Charleston Rd #4");
        listing.setDeveloperWebsite("http://healthmetricssystems.com");
        listing.setDeveloperZipCode("94303");
        listing.setHasQms(Boolean.TRUE);
        listing.setIcs(Boolean.FALSE);
        listing.setId(9001L);
        listing.setLastModifiedUser(-2L);
        listing.setProductId(1L);
        listing.setProductName("(SQI) Solution For Quality Improvement");
        listing.setProductVersion("v4.6.9.25");
        listing.setProductVersionId(1L);
        listing.getQmsStandards().add(createPendingQmsStandard());
        listing.getTestingLabs().add(createPendingTestingLab());
        listing.setTransparencyAttestation("Affirmative");
        listing.setTransparencyAttestationUrl("http://www.healthmetricssystems.com/index.php/certifications/");

        //certification results
        PendingCertificationResultDTO a1 = create2014PendingCertResult(1L, "170.314 (a)(1)", true);
        a1.setG1Success(Boolean.TRUE);
        a1.setG2Success(Boolean.TRUE);
        a1.getTestProcedures().add(createPendingTestProcedure(1L, null, "1"));
        a1.getTestData().add(createPendingTestData(1L, null, "1"));
        listing.getCertificationCriterion().add(a1);

        PendingCertificationResultDTO a2 = create2014PendingCertResult(2L, "170.314 (a)(2)", true);
        a2.getTestProcedures().add(createPendingTestProcedure(1L, null, "1.9"));
        a2.setSed(Boolean.TRUE);
        a2.getUcdProcesses().add(createPendingUcdProcess(1L, "NISTIR 7741", "changed things"));
        listing.getCertificationCriterion().add(a2);

        PendingCertificationResultDTO a3 = create2014PendingCertResult(3L, "170.314 (a)(3)", true);
        a3.setG1Success(Boolean.FALSE);
        a3.setG2Success(Boolean.TRUE);
        a3.getTestProcedures().add(createPendingTestProcedure(1L, null, "1.9"));
        a3.getTestData().add(createPendingTestData(2L, null, "1.9.1"));
        listing.getCertificationCriterion().add(a3);

        PendingCertificationResultDTO a4 = create2014PendingCertResult(4L, "170.314 (a)(4)", true);
        a4.setG1Success(Boolean.FALSE);
        a4.setG2Success(Boolean.TRUE);
        a4.getTestFunctionality().add(createPendingTestFunctionality(1L, "(a)(5)(i)", "2014"));
        a4.getTestProcedures().add(createPendingTestProcedure(1L, null, "1.9"));
        a4.getTestData().add(createPendingTestData(3L, null, "2.7"));
        listing.getCertificationCriterion().add(a4);

        PendingCertificationResultDTO a5 = create2014PendingCertResult(5L, "170.314 (a)(5)", true);
        a5.setG1Success(Boolean.FALSE);
        a5.setG2Success(Boolean.TRUE);
        a5.getTestFunctionality().add(createPendingTestFunctionality(1L, "(a)(5)(i)", "2014"));
        a5.getTestProcedures().add(createPendingTestProcedure(1L, null, "1.9"));
        a5.getTestData().add(createPendingTestData(4L, null, "2.4.1"));
        listing.getCertificationCriterion().add(a5);

        PendingCertificationResultDTO a6 = create2014PendingCertResult(6L, "170.314 (a)(6)", true);
        a6.setG1Success(Boolean.FALSE);
        a6.setG2Success(Boolean.TRUE);
        a6.getTestFunctionality().add(createPendingTestFunctionality(1L, "(a)(6)(i)", "2014"));
        a6.getTestProcedures().add(createPendingTestProcedure(1L, null, "1.9"));
        a6.getTestData().add(createPendingTestData(4L, null, "2.4.1"));
        a6.setSed(Boolean.TRUE);
        a6.getUcdProcesses().add(createPendingUcdProcess(1L, "NISTIR 7741", "changed things"));
        listing.getCertificationCriterion().add(a6);

        PendingCertificationResultDTO a7 = create2014PendingCertResult(7L, "170.314 (a)(7)", true);
        a7.setG1Success(Boolean.FALSE);
        a7.setG2Success(Boolean.TRUE);
        a7.getTestFunctionality().add(createPendingTestFunctionality(1L, "(a)(7)(i)", "2014"));
        a7.getTestProcedures().add(createPendingTestProcedure(1L, null, "1.9"));
        a7.getTestData().add(createPendingTestData(4L, null, "2.4.1"));
        a7.setSed(Boolean.TRUE);
        a7.getUcdProcesses().add(createPendingUcdProcess(1L, "NISTIR 7741", "changed things"));
        listing.getCertificationCriterion().add(a7);

        PendingCertificationResultDTO a8 = create2014PendingCertResult(8L, "170.314 (a)(8)", true);
        a8.getTestStandards().add(createPendingTestStandard(1L, "170.204(b)(1)"));
        a8.getTestProcedures().add(createPendingTestProcedure(1L, null, "1.9"));
        a8.setSed(Boolean.TRUE);
        a8.getUcdProcesses().add(createPendingUcdProcess(1L, "NISTIR 7741", "changed things"));
        listing.getCertificationCriterion().add(a8);

        PendingCertificationResultDTO a9 = create2014PendingCertResult(9L, "170.314 (a)(9)", true);
        a9.setG1Success(Boolean.FALSE);
        a9.setG2Success(Boolean.TRUE);
        a9.getTestProcedures().add(createPendingTestProcedure(1L, null, "1.9"));
        a9.getTestData().add(createPendingTestData(4L, null, "2.4.1"));
        listing.getCertificationCriterion().add(a9);

        PendingCertificationResultDTO a10 = create2014PendingCertResult(10L, "170.314 (a)(10)", true);
        a10.getTestProcedures().add(createPendingTestProcedure(1L, null, "1.9"));
        a10.getTestData().add(createPendingTestData(4L, null, "2.4.1"));
        listing.getCertificationCriterion().add(a10);

        PendingCertificationResultDTO a11 = create2014PendingCertResult(11L, "170.314 (a)(11)", true);
        a11.setG1Success(Boolean.FALSE);
        a11.setG2Success(Boolean.TRUE);
        a11.getTestProcedures().add(createPendingTestProcedure(1L, null, "1.9"));
        listing.getCertificationCriterion().add(a11);

        PendingCertificationResultDTO a12 = create2014PendingCertResult(12L, "170.314 (a)(12)", true);
        a12.setG1Success(Boolean.FALSE);
        a12.setG2Success(Boolean.TRUE);
        a12.getTestProcedures().add(createPendingTestProcedure(1L, null, "1.9"));
        listing.getCertificationCriterion().add(a12);

        PendingCertificationResultDTO a13 = create2014PendingCertResult(13L, "170.314 (a)(13)", true);
        a13.setG1Success(Boolean.FALSE);
        a13.setG2Success(Boolean.TRUE);
        a13.getTestStandards().add(createPendingTestStandard(2L, "170.207(a)(3)"));
        a13.getTestProcedures().add(createPendingTestProcedure(1L, null, "1.9"));
        listing.getCertificationCriterion().add(a13);

        PendingCertificationResultDTO a14 = create2014PendingCertResult(14L, "170.314 (a)(14)", true);
        a14.setG1Success(Boolean.FALSE);
        a14.setG2Success(Boolean.TRUE);
        a14.getTestProcedures().add(createPendingTestProcedure(1L, null, "1.9"));
        listing.getCertificationCriterion().add(a14);

        PendingCertificationResultDTO a15 = create2014PendingCertResult(15L, "170.314 (a)(15)", true);
        a15.setG1Success(Boolean.FALSE);
        a15.setG2Success(Boolean.TRUE);
        a15.getTestStandards().add(createPendingTestStandard(2L, "170.204(b)(1)"));
        a15.getTestProcedures().add(createPendingTestProcedure(1L, null, "1.9"));
        listing.getCertificationCriterion().add(a15);

        PendingCertificationResultDTO a16 = create2014PendingCertResult(16L, "170.314 (a)(16)", true);
        a16.setG1Success(Boolean.TRUE);
        a16.setG2Success(Boolean.TRUE);
        a16.getTestStandards().add(createPendingTestStandard(2L, "170.207(i)"));
        a16.getTestData().add(createPendingTestData(2L, null, "2.4"));
        a16.getTestProcedures().add(createPendingTestProcedure(1L, null, "v1"));
        a16.setSed(Boolean.TRUE);
        a16.getUcdProcesses().add(createPendingUcdProcess(1L, "NISTIR 7741", "changed things"));
        listing.getCertificationCriterion().add(a16);

        PendingCertificationResultDTO a17 = create2014PendingCertResult(17L, "170.314 (a)(17)", true);
        a17.setGap(Boolean.TRUE);
        a17.setG1Success(Boolean.TRUE);
        a17.setG2Success(Boolean.TRUE);
        a17.getTestProcedures().add(createPendingTestProcedure(1L, null, "v1"));
        listing.getCertificationCriterion().add(a17);

        listing.getCertificationCriterion().add(create2014PendingCertResult(18L, "170.314 (a)(18)", false));

        PendingCertificationResultDTO a19 = create2014PendingCertResult(19L, "170.314 (a)(19)", true);
        a19.setGap(Boolean.TRUE);
        a19.setG1Success(Boolean.TRUE);
        a19.setG2Success(Boolean.TRUE);
        a19.getTestProcedures().add(createPendingTestProcedure(1L, null, "v1"));
        a19.setSed(Boolean.TRUE);
        a19.getUcdProcesses().add(createPendingUcdProcess(1L, "NISTIR 7741", "changed things"));
        listing.getCertificationCriterion().add(a19);

        listing.getCertificationCriterion().add(create2014PendingCertResult(20L, "170.314 (a)(20)", false));

        PendingCertificationResultDTO b1 = create2014PendingCertResult(21L, "170.314 (b)(1)", true);
        b1.getTestStandards().add(createPendingTestStandard(2L, "170.207(i)"));
        b1.getTestTools().add(createPendingTestTool(1L, "Direct Certificate Discovery Tool", "181", false));
        b1.getTestProcedures().add(createPendingTestProcedure(1L, null, "1.7"));
        b1.getTestData().add(createPendingTestData(1L, null, "1.4"));
        listing.getCertificationCriterion().add(b1);

        PendingCertificationResultDTO b2 = create2014PendingCertResult(22L, "170.314 (b)(2)", true);
        b2.setG1Success(Boolean.FALSE);
        b2.setG2Success(Boolean.TRUE);
        b2.getTestStandards().add(createPendingTestStandard(2L, "170.207(a)(3)"));
        b2.getTestTools().add(createPendingTestTool(1L, "Direct Certificate Discovery Tool", "3.0.4", false));
        b2.getTestProcedures().add(createPendingTestProcedure(1L, null, "1.8"));
        b2.getTestData().add(createPendingTestData(1L, null, "1.6"));
        listing.getCertificationCriterion().add(b2);

        PendingCertificationResultDTO g1 = create2014PendingCertResult(23L, "170.314 (g)(1)", true);
        g1.getTestStandards().add(createPendingTestStandard(2L, "170.207(a)(3)"));
        g1.getTestProcedures().add(createPendingTestProcedure(1L, null, "1.8"));
        listing.getCertificationCriterion().add(g1);

        PendingCertificationResultDTO g2 = create2014PendingCertResult(24L, "170.314 (g)(2)", true);
        g2.getTestTools().add(createPendingTestTool(1L, "Direct Certificate Discovery Tool", "181", false));
        g2.getTestProcedures().add(createPendingTestProcedure(1L, null, "1.8"));
        g2.getTestData().add(createPendingTestData(1L, null, "1.6"));
        listing.getCertificationCriterion().add(g2);

        PendingCertificationResultDTO f3 = create2014PendingCertResult(25L, "170.314 (f)(3)", true);
        f3.getTestStandards().add(createPendingTestStandard(2L, "170.205(d)(3)"));
        f3.getTestTools().add(createPendingTestTool(1L, "Direct Certificate Discovery Tool", "3.0.4", false));
        f3.getTestProcedures().add(createPendingTestProcedure(1L, null, "1.8"));
        f3.getTestData().add(createPendingTestData(1L, null, "1.6"));
        listing.getCertificationCriterion().add(f3);

        //TODO: cqms
        return listing;
    }

    public PendingCertifiedProductDTO createPending2015Listing() {
        PendingCertifiedProductDTO listing = createPendingListing("2015");
        listing.setAcbCertificationId("IG-4152-18-0007");
        listing.setAccessibilityCertified(null);
        listing.setCertificationBodyId(1L);
        listing.setCertificationBodyName("ICSA Labs");
        listing.setCertificationDate(new Date(1459555200000L));
        listing.setCertificationEdition("2015");
        listing.setCertificationEditionId(EDITION_2015_ID);
        listing.setDeveloperCity("Palo Alto");
        listing.setDeveloperContactName("Test Person");
        listing.setDeveloperEmail("test@mail.com");
        listing.setDeveloperId(1L);
        listing.setDeveloperName("Epic Systems Corporation");
        listing.setDeveloperPhoneNumber("555-444-3333");
        listing.setDeveloperState("CA");
        listing.setDeveloperStreetAddress("541 E. Charleston Rd #4");
        listing.setDeveloperWebsite("http://healthmetricssystems.com");
        listing.setDeveloperZipCode("94303");
        listing.setHasQms(Boolean.TRUE);
        listing.setIcs(Boolean.FALSE);
        listing.setId(9001L);
        listing.setLastModifiedUser(-2L);
        listing.setProductId(1L);
        listing.setProductName("(SQI) Solution For Quality Improvement");
        listing.setProductVersion("v4.6.9.25");
        listing.setProductVersionId(1L);
        listing.getQmsStandards().add(createPendingQmsStandard());
        listing.getTestingLabs().add(createPendingTestingLab());
        listing.setTransparencyAttestation("Affirmative");
        listing.setTransparencyAttestationUrl("http://www.healthmetricssystems.com/index.php/certifications/");

        //certification results
        PendingCertificationResultDTO a1 = create2015PendingCertResult(1L, "170.315 (a)(1)", true);
        a1.setG1Success(Boolean.TRUE);
        a1.setG2Success(Boolean.TRUE);
        a1.getG1MacraMeasures().add(createPendingMacraMeasure());
        a1.getTestProcedures().add(createPendingTestProcedure(1L, null, "1"));
        a1.getTestTasks().add(createPendingCertificationResultTestTaskDTO());
        listing.getCertificationCriterion().add(a1);

        PendingCertificationResultDTO a2 = create2015PendingCertResult(2L, "170.315 (a)(2)", true);
        a2.getTestProcedures().add(createPendingTestProcedure(1L, null, "1.9"));
        a2.setSed(Boolean.TRUE);
        a1.getG2MacraMeasures().add(createPendingMacraMeasure());
        a2.getUcdProcesses().add(createPendingUcdProcess(1L, "NISTIR 7741", "changed things"));
        listing.getCertificationCriterion().add(a2);

        PendingCertificationResultDTO a3 = create2015PendingCertResult(3L, "170.315 (a)(3)", true);
        a3.getTestProcedures().add(createPendingTestProcedure(1L, null, "1.9"));
        listing.getCertificationCriterion().add(a3);

        PendingCertificationResultDTO a4 = create2015PendingCertResult(4L, "170.315 (a)(4)", true);
        a4.getTestFunctionality().add(createPendingTestFunctionality(1L, "(a)(5)(i)", "2015"));
        a4.getTestProcedures().add(createPendingTestProcedure(1L, null, "1.9"));
        listing.getCertificationCriterion().add(a4);

        PendingCertificationResultDTO a5 = create2015PendingCertResult(5L, "170.315 (a)(5)", true);
        a5.getTestFunctionality().add(createPendingTestFunctionality(1L, "(a)(5)(i)", "2015"));
        a5.getTestProcedures().add(createPendingTestProcedure(1L, null, "1.9"));
        listing.getCertificationCriterion().add(a5);

        PendingCertificationResultDTO a6 = create2015PendingCertResult(6L, "170.315 (a)(6)", true);
        a6.getTestFunctionality().add(createPendingTestFunctionality(1L, "(a)(6)(i)", "2015"));
        a6.getTestProcedures().add(createPendingTestProcedure(1L, null, "1.9"));
        a6.setSed(Boolean.TRUE);
        a6.getUcdProcesses().add(createPendingUcdProcess(1L, "NISTIR 7741", "changed things"));
        listing.getCertificationCriterion().add(a6);

        PendingCertificationResultDTO a7 = create2015PendingCertResult(7L, "170.315 (a)(7)", true);
        a7.getTestFunctionality().add(createPendingTestFunctionality(1L, "(a)(7)(i)", "2015"));
        a7.getTestProcedures().add(createPendingTestProcedure(1L, null, "1.9"));
        a7.setSed(Boolean.TRUE);
        a7.getUcdProcesses().add(createPendingUcdProcess(1L, "NISTIR 7741", "changed things"));
        listing.getCertificationCriterion().add(a7);

        PendingCertificationResultDTO a8 = create2015PendingCertResult(8L, "170.315 (a)(8)", true);
        a8.getTestProcedures().add(createPendingTestProcedure(1L, null, "1.9"));
        a8.setSed(Boolean.TRUE);
        a8.getUcdProcesses().add(createPendingUcdProcess(1L, "NISTIR 7741", "changed things"));
        listing.getCertificationCriterion().add(a8);

        PendingCertificationResultDTO a9 = create2015PendingCertResult(9L, "170.315 (a)(9)", true);
        a9.getTestProcedures().add(createPendingTestProcedure(1L, null, "1.9"));
        listing.getCertificationCriterion().add(a9);

        PendingCertificationResultDTO a10 = create2015PendingCertResult(10L, "170.315 (a)(10)", true);
        a10.getTestProcedures().add(createPendingTestProcedure(1L, null, "1.9"));
        listing.getCertificationCriterion().add(a10);

        PendingCertificationResultDTO a11 = create2015PendingCertResult(11L, "170.315 (a)(11)", true);
        a11.getTestProcedures().add(createPendingTestProcedure(1L, null, "1.9"));
        listing.getCertificationCriterion().add(a11);

        PendingCertificationResultDTO a12 = create2015PendingCertResult(12L, "170.315 (a)(12)", true);
        a12.getTestProcedures().add(createPendingTestProcedure(1L, null, "1.9"));
        listing.getCertificationCriterion().add(a12);

        PendingCertificationResultDTO a13 = create2015PendingCertResult(13L, "170.315 (a)(13)", true);
        a13.getTestStandards().add(createPendingTestStandard(2L, "170.207(a)(3)"));
        a13.getTestProcedures().add(createPendingTestProcedure(1L, null, "1.9"));
        listing.getCertificationCriterion().add(a13);

        PendingCertificationResultDTO a14 = create2015PendingCertResult(14L, "170.315 (a)(14)", true);
        a14.getTestProcedures().add(createPendingTestProcedure(1L, null, "1.9"));
        listing.getCertificationCriterion().add(a14);

        PendingCertificationResultDTO a15 = create2015PendingCertResult(15L, "170.315 (a)(15)", true);
        a15.getTestStandards().add(createPendingTestStandard(2L, "170.204(b)(1)"));
        a15.getTestProcedures().add(createPendingTestProcedure(1L, null, "1.9"));
        listing.getCertificationCriterion().add(a15);

        PendingCertificationResultDTO a16 = create2015PendingCertResult(16L, "170.315 (a)(16)", true);
        a16.setSed(Boolean.TRUE);
        a16.getUcdProcesses().add(createPendingUcdProcess(1L, "NISTIR 7741", "changed things"));
        listing.getCertificationCriterion().add(a16);

        PendingCertificationResultDTO a17 = create2015PendingCertResult(17L, "170.315 (a)(17)", true);
        a17.setGap(Boolean.TRUE);
        listing.getCertificationCriterion().add(a17);

        listing.getCertificationCriterion().add(create2015PendingCertResult(18L, "170.315 (a)(18)", false));

        PendingCertificationResultDTO a19 = create2015PendingCertResult(19L, "170.315 (a)(19)", true);
        a19.setGap(Boolean.TRUE);
        a19.setSed(Boolean.TRUE);
        a19.getUcdProcesses().add(createPendingUcdProcess(1L, "NISTIR 7741", "changed things"));
        listing.getCertificationCriterion().add(a19);

        listing.getCertificationCriterion().add(create2015PendingCertResult(20L, "170.315 (a)(20)", false));

        PendingCertificationResultDTO b1 = create2015PendingCertResult(21L, "170.315 (b)(1)", true);
        b1.getTestStandards().add(createPendingTestStandard(2L, "170.207(i)"));
        b1.getTestTools().add(createPendingTestTool(1L, "Direct Certificate Discovery Tool", "181", false));
        b1.getTestProcedures().add(createPendingTestProcedure(1L, null, "1.7"));
        listing.getCertificationCriterion().add(b1);

        PendingCertificationResultDTO b2 = create2015PendingCertResult(22L, "170.315 (b)(2)", true);
        b2.getTestStandards().add(createPendingTestStandard(2L, "170.207(a)(3)"));
        b2.getTestTools().add(createPendingTestTool(1L, "Direct Certificate Discovery Tool", "3.0.4", false));
        b2.getTestProcedures().add(createPendingTestProcedure(1L, null, "1.8"));
        listing.getCertificationCriterion().add(b2);

        PendingCertificationResultDTO g1 = create2015PendingCertResult(23L, "170.315 (g)(1)", true);
        g1.getTestProcedures().add(createPendingTestProcedure(1L, null, "1.8"));
        listing.getCertificationCriterion().add(g1);

        PendingCertificationResultDTO g2 = create2015PendingCertResult(24L, "170.315 (g)(2)", true);
        g2.getTestProcedures().add(createPendingTestProcedure(1L, null, "1.8"));
        listing.getCertificationCriterion().add(g2);

        PendingCertificationResultDTO f3 = create2015PendingCertResult(25L, "170.315 (f)(3)", true);
        f3.getTestStandards().add(createPendingTestStandard(2L, "170.205(d)(3)"));
        f3.getTestTools().add(createPendingTestTool(1L, "Direct Certificate Discovery Tool", "3.0.4", false));
        f3.getTestProcedures().add(createPendingTestProcedure(1L, null, "1.8"));
        listing.getCertificationCriterion().add(f3);

        //TODO: cqms
        return listing;
    }

    public CertifiedProductSearchDetails createValid2015Listing() {
        CertifiedProductSearchDetails listing = new CertifiedProductSearchDetails();
        listing.setAcbCertificationId("IG-4152-18-0007");
        listing.setAccessibilityCertified(null);
        listing.getAccessibilityStandards().add(createAccessibilityStandard());
        listing.setCertificationDate(1518566400000L);
        listing.getCertificationEdition().put("id", "3");
        listing.getCertificationEdition().put("name", "2015");
        listing.getCertificationEvents().add(createActiveCertificationEvent());
        listing.getCertifyingBody().put("id", "1");
        listing.getCertifyingBody().put("code", "02");
        listing.getCertifyingBody().put("name", "UL LLC");
        listing.setChplProductNumber(CHPL_ID_2015);
        listing.setCountCerts(8);
        listing.setCountClosedNonconformities(0);
        listing.setCountClosedSurveillance(0);
        listing.setCountCqms(12);
        listing.setCountOpenNonconformities(0);
        listing.setCountOpenSurveillance(0);
        listing.setDeveloper(createDeveloper());
        listing.setIcs(createIcsNoInheritance());
        listing.setId(9261L);
        listing.setLastModifiedDate(1518807131471L);
        listing.setProduct(createProduct());
        listing.getQmsStandards().add(createQmsStandard());
        listing.getTestingLabs().add(createTestingLab());
        listing.setTransparencyAttestation("Affirmative");
        listing.setTransparencyAttestationUrl("http://www.healthmetricssystems.com/index.php/certifications/");
        listing.setVersion(createVersion());

        //certification results
        listing.getCertificationResults().add(create2015CertResult(1L, "170.315 (a)(1)", false));
        listing.getCertificationResults().add(create2015CertResult(2L, "170.315 (a)(2)", false));
        listing.getCertificationResults().add(create2015CertResult(3L, "170.315 (a)(3)", false));
        listing.getCertificationResults().add(create2015CertResult(4L, "170.315 (a)(4)", false));
        listing.getCertificationResults().add(create2015CertResult(5L, "170.315 (a)(5)", false));
        listing.getCertificationResults().add(create2015CertResult(6L, "170.315 (a)(6)", false));
        listing.getCertificationResults().add(create2015CertResult(7L, "170.315 (a)(7)", false));
        listing.getCertificationResults().add(create2015CertResult(8L, "170.315 (a)(8)", false));
        listing.getCertificationResults().add(create2015CertResult(9L, "170.315 (a)(9)", false));
        listing.getCertificationResults().add(create2015CertResult(10L, "170.315 (a)(10)", false));
        listing.getCertificationResults().add(create2015CertResult(11L, "170.315 (a)(12)", false));
        listing.getCertificationResults().add(create2015CertResult(12L, "170.315 (a)(13)", false));
        listing.getCertificationResults().add(create2015CertResult(13L, "170.315 (a)(14)", false));
        listing.getCertificationResults().add(create2015CertResult(14L, "170.315 (a)(15)", false));
        listing.getCertificationResults().add(create2015CertResult(15L, "170.315 (b)(1)", false));
        listing.getCertificationResults().add(create2015CertResult(16L, "170.315 (b)(2)", false));
        listing.getCertificationResults().add(create2015CertResult(17L, "170.315 (b)(3)", false));
        listing.getCertificationResults().add(create2015CertResult(18L, "170.315 (b)(4)", false));
        listing.getCertificationResults().add(create2015CertResult(19L, "170.315 (b)(5)", false));
        listing.getCertificationResults().add(create2015CertResult(20L, "170.315 (b)(6)", false));
        listing.getCertificationResults().add(create2015CertResult(21L, "170.315 (b)(7)", false));
        listing.getCertificationResults().add(create2015CertResult(22L, "170.315 (b)(8)", false));
        listing.getCertificationResults().add(create2015CertResult(23L, "170.315 (b)(9)", false));
        listing.getCertificationResults().add(create2015CertResult(24L, "170.315 (c)(1)", false));

        CertificationResult c2 = create2015CertResult(25L, "170.315 (c)(2)", true);
        c2.setPrivacySecurityFramework("Approach 2");
        c2.getTestDataUsed().add(createTestData(123L, "ONC Test Method", "3.2.2"));
        c2.getTestProcedures().add(createTestProcedure(234L, "ONC Test Method", "1.1.1"));
        c2.getTestStandards().add(createTestStandard(34L, "170.205(h)2; 170.205(k)1;170.205(k)2"));
        c2.getTestToolsUsed().add(createTestTool(11L, "Cypress", "3.2.2", false));
        listing.getCertificationResults().add(c2);

        CertificationResult c3 = create2015CertResult(26L, "170.315 (c)(3)", true);
        c3.setPrivacySecurityFramework("Approach 2");
        c3.getTestDataUsed().add(createTestData(111L, "ONC Test Method", "3.2.2"));
        c3.getTestProcedures().add(createTestProcedure(156L, "ONC Test Method", "1.1.1"));
        c3.getTestStandards().add(createTestStandard(250L, "170.205(h)2; 170.205(k)1;170.205(k)2"));
        c3.getTestToolsUsed().add(createTestTool(8L, "Cypress", "3.2.2", false));
        listing.getCertificationResults().add(c3);

        listing.getCertificationResults().add(create2015CertResult(27L, "170.315 (c)(4)", false));

        CertificationResult d1 = create2015CertResult(28L, "170.315 (d)(1)", true);
        d1.getTestProcedures().add(createTestProcedure(5L, "ONC Test Method", "1.1.1"));
        listing.getCertificationResults().add(d1);

        CertificationResult d2 = create2015CertResult(29L, "170.315 (d)(2)", true);
        d2.getTestProcedures().add(createTestProcedure(98L, "ONC Test Method", "1.3"));
        d2.getTestStandards().add(createTestStandard(3L, "170.210(e)(1); 170.210(e)(2); 170.210(e)(3); 170.210(g)"));
        listing.getCertificationResults().add(d2);

        CertificationResult d3 = create2015CertResult(30L, "170.315 (d)(3)", true);
        d3.getTestProcedures().add(createTestProcedure(99L, "ONC Test Method", "1.3"));
        d3.getTestStandards().add(createTestStandard(4L, "170.210(e)(1); 170.210(e)(2); 170.210(e)(3); 170.210(g)"));
        listing.getCertificationResults().add(d3);

        listing.getCertificationResults().add(create2015CertResult(31L, "170.315 (d)(4)", false));

        CertificationResult d5 = create2015CertResult(32L, "170.315 (d)(3)", true);
        d5.getTestProcedures().add(createTestProcedure(100L, "ONC Test Method", "1.1"));
        listing.getCertificationResults().add(d5);

        listing.getCertificationResults().add(create2015CertResult(33L, "170.315 (d)(6)", false));
        listing.getCertificationResults().add(create2015CertResult(34L, "170.315 (d)(7)", false));
        listing.getCertificationResults().add(create2015CertResult(35L, "170.315 (d)(8)", false));
        listing.getCertificationResults().add(create2015CertResult(36L, "170.315 (d)(9)", false));
        listing.getCertificationResults().add(create2015CertResult(37L, "170.315 (d)(10)", false));
        listing.getCertificationResults().add(create2015CertResult(38L, "170.315 (d)(11)", false));
        listing.getCertificationResults().add(create2015CertResult(39L, "170.315 (e)(1)", false));
        listing.getCertificationResults().add(create2015CertResult(40L, "170.315 (e)(2)", false));
        listing.getCertificationResults().add(create2015CertResult(41L, "170.315 (e)(3)", false));
        listing.getCertificationResults().add(create2015CertResult(42L, "170.315 (f)(1)", false));
        listing.getCertificationResults().add(create2015CertResult(43L, "170.315 (f)(2)", false));
        listing.getCertificationResults().add(create2015CertResult(44L, "170.315 (f)(3)", false));
        listing.getCertificationResults().add(create2015CertResult(45L, "170.315 (f)(4)", false));
        listing.getCertificationResults().add(create2015CertResult(46L, "170.315 (f)(5)", false));
        listing.getCertificationResults().add(create2015CertResult(47L, "170.315 (f)(6)", false));
        listing.getCertificationResults().add(create2015CertResult(48L, "170.315 (f)(7)", false));
        listing.getCertificationResults().add(create2015CertResult(49L, "170.315 (g)(1)", false));
        listing.getCertificationResults().add(create2015CertResult(50L, "170.315 (g)(2)", false));
        listing.getCertificationResults().add(create2015CertResult(51L, "170.315 (g)(3)", false));

        CertificationResult g4 = create2015CertResult(52L, "170.315 (g)(4)", true);
        g4.getTestProcedures().add(createTestProcedure(101L, "ONC Test Method", "1"));
        g4.getTestFunctionality().add(createTestFunctionality(1L, "(g)(4)(i)(B)", "2015"));
        listing.getCertificationResults().add(g4);

        CertificationResult g5 = create2015CertResult(52L, "170.315 (g)(5)", true);
        g5.getTestProcedures().add(createTestProcedure(102L, "ONC Test Method", "1"));
        g5.getTestFunctionality().add(createTestFunctionality(2L, "(g)(5)(iii))", "2015"));
        listing.getCertificationResults().add(g5);

        listing.getCertificationResults().add(create2015CertResult(54L, "170.315 (g)(6)", false));
        listing.getCertificationResults().add(create2015CertResult(55L, "170.315 (g)(7)", false));
        listing.getCertificationResults().add(create2015CertResult(56L, "170.315 (g)(8)", false));
        listing.getCertificationResults().add(create2015CertResult(57L, "170.315 (g)(9)", false));
        listing.getCertificationResults().add(create2015CertResult(58L, "170.315 (h)(1)", false));
        listing.getCertificationResults().add(create2015CertResult(59L, "170.315 (h)(2)", false));

        //TODO: cqms
        return listing;
    }

    /**
     * Create a pending Listing.
     * @param year the edition of the listing
     * @return a valid pending listing
     */
    public PendingCertifiedProductDTO createPendingListing(final String year) {
        PendingCertifiedProductDTO pendingListing = new PendingCertifiedProductDTO();
        String certDateString = "11-09-2016";
        DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        try {
            Date inputDate = dateFormat.parse(certDateString);
            pendingListing.setCertificationDate(inputDate);
        } catch (ParseException ex) {
            fail(ex.getMessage());
        }
        pendingListing.setId(1L);
        pendingListing.setIcs(false);
        pendingListing.setCertificationEdition(year);
        if (year.equals("2015")) {
            pendingListing.setCertificationEditionId(EDITION_2015_ID);
            pendingListing.setUniqueId("15.07.07.2642.IC04.36.00.1.160402");
        } else if (year.equals("2014")) {
            pendingListing.setCertificationEditionId(EDITION_2014_ID);
            pendingListing.setUniqueId(CHPL_ID_2014);
            pendingListing.setPracticeType("Ambulatory");
            pendingListing.setProductClassificationName("Modular EHR");
        }
        return pendingListing;
    }

    private CertifiedProductAccessibilityStandard createAccessibilityStandard() {
        CertifiedProductAccessibilityStandard accessibilityStandard =
                new CertifiedProductAccessibilityStandard();
        accessibilityStandard.setId(330L);
        accessibilityStandard.setAccessibilityStandardId(8L);
        accessibilityStandard.setAccessibilityStandardName("None");
        return accessibilityStandard;
    }

    private CertificationStatusEvent createActiveCertificationEvent() {
        CertificationStatusEvent activeEvent = new CertificationStatusEvent();
        activeEvent.setEventDate(1518566400000L);
        activeEvent.setId(1L);
        activeEvent.setLastModifiedDate(1518807124360L);
        activeEvent.setLastModifiedUser(-2L);
        activeEvent.setReason(null);
        CertificationStatus status = new CertificationStatus();
        status.setId(1L);
        status.setName("Active");
        activeEvent.setStatus(status);
        return activeEvent;
    }

    private AddressDTO createAddress() {
        AddressDTO addr = new AddressDTO();
        addr.setCity("Palo Alto");
        addr.setCountry("US");
        addr.setStreetLineOne("541 E. Charleston Rd #4");
        addr.setStreetLineTwo(null);
        addr.setState("CA");
        addr.setZipcode("94303");
        return addr;
    }

    private Developer createDeveloper() {
        Developer dev = new Developer();
        Address address = new Address();
        address.setAddressId(1L);
        address.setCity("Palo Alto");
        address.setCountry("US");
        address.setLine1("541 E. Charleston Rd #4");
        address.setLine2(null);
        address.setState("CA");
        address.setZipcode("94303");
        dev.setAddress(address);
        Contact contact = new Contact();
        contact.setContactId(1L);
        contact.setEmail("test@test.com");
        contact.setFullName("First Last");
        contact.setPhoneNumber("555-444-3333");
        dev.setContact(contact);
        dev.setDeveloperId(1L);
        dev.setDeveloperCode("3007");
        dev.setName("Health Metrics System, Inc");
        DeveloperStatus status = new DeveloperStatus();
        status.setId(1L);
        status.setStatus("Active");
        dev.setStatus(status);
        DeveloperStatusEvent statusEvent = new DeveloperStatusEvent();
        statusEvent.setDeveloperId(1L);
        statusEvent.setId(1L);
        statusEvent.setStatus(status);
        statusEvent.setStatusDate(new Date(1518807131474L));
        dev.getStatusEvents().add(statusEvent);
        dev.setWebsite("http://healthmetricssystems.com");
        return dev;
    }

    private InheritedCertificationStatus createIcsNoInheritance() {
        InheritedCertificationStatus ics = new InheritedCertificationStatus();
        ics.setInherits(Boolean.FALSE);
        return ics;
    }

    private Product createProduct() {
        Product product = new Product();
        product.setLastModifiedDate("1518807124360");
        product.setName("(SQI) Solution For Quality Improvement");
        product.setProductId(1L);
        product.setOwner(createDeveloper());
        return product;
    }

    private CertifiedProductQmsStandard createQmsStandard() {
        CertifiedProductQmsStandard qms = new CertifiedProductQmsStandard();
        qms.setApplicableCriteria("All");
        qms.setId(1L);
        qms.setQmsModification("None");
        qms.setQmsStandardId(21L);
        qms.setQmsStandardName("Home Grown mapped to ISO 9001");
        return qms;
    }

    private PendingCertifiedProductQmsStandardDTO createPendingQmsStandard() {
        PendingCertifiedProductQmsStandardDTO qms = new PendingCertifiedProductQmsStandardDTO();
        qms.setApplicableCriteria("All");
        qms.setId(1L);
        qms.setModification("None");
        qms.setQmsStandardId(21L);
        qms.setName("Home Grown mapped to ISO 9001");
        return qms;
    }

    private CertifiedProductTestingLab createTestingLab() {
        CertifiedProductTestingLab atl = new CertifiedProductTestingLab();
        atl.setId(1L);
        atl.setTestingLabCode("02");
        atl.setTestingLabId(1L);
        atl.setTestingLabName("UL LLC");
        return atl;
    }

    private PendingCertifiedProductTestingLabDTO createPendingTestingLab() {
        PendingCertifiedProductTestingLabDTO atl = new PendingCertifiedProductTestingLabDTO();
        atl.setId(1L);
        atl.setTestingLabId(3L);
        atl.setTestingLabName("ICSA Labs");
        return atl;
    }

    private ProductVersion createVersion() {
        ProductVersion version = new ProductVersion();
        version.setVersionId(1L);
        version.setVersion("v4.6.9.25");
        return version;
    }

    private CertificationResult create2015CertResult(final Long id, final String number,
            final Boolean success) {
        CertificationResult certResult = new CertificationResult();
        certResult.setId(id);
        certResult.setNumber(number);
        certResult.setSuccess(success);

        if (!certRules.hasCertOption(number, CertificationResultRules.ADDITIONAL_SOFTWARE)) {
            certResult.setAdditionalSoftware(null);
        }
        if (!certRules.hasCertOption(number, CertificationResultRules.API_DOCUMENTATION)) {
            certResult.setApiDocumentation(null);
        }
        if (!certRules.hasCertOption(number, CertificationResultRules.G1_SUCCESS)) {
            certResult.setG1Success(null);
        }
        if (!certRules.hasCertOption(number, CertificationResultRules.G1_MACRA)) {
            certResult.setG1MacraMeasures(null);
        }
        if (!certRules.hasCertOption(number, CertificationResultRules.G2_SUCCESS)) {
            certResult.setG2Success(null);
        }
        if (!certRules.hasCertOption(number, CertificationResultRules.G2_MACRA)) {
            certResult.setG2MacraMeasures(null);
        }

        if (!certRules.hasCertOption(number, CertificationResultRules.GAP)) {
            certResult.setGap(null);
        } else {
            certResult.setGap(Boolean.FALSE);
        }

        if (!certRules.hasCertOption(number, CertificationResultRules.PRIVACY_SECURITY)) {
            certResult.setPrivacySecurityFramework(null);
        }

        if (!certRules.hasCertOption(number, CertificationResultRules.SED)) {
            certResult.setSed(null);
        } else {
            certResult.setSed(Boolean.FALSE);
        }

        if (!certRules.hasCertOption(number, CertificationResultRules.TEST_DATA)) {
            certResult.setTestDataUsed(null);
        }
        if (!certRules.hasCertOption(number, CertificationResultRules.FUNCTIONALITY_TESTED)) {
            certResult.setTestFunctionality(null);
        }
        if (!certRules.hasCertOption(number, CertificationResultRules.TEST_PROCEDURE)) {
            certResult.setTestProcedures(null);
        }
        if (!certRules.hasCertOption(number, CertificationResultRules.STANDARDS_TESTED)) {
            certResult.setTestStandards(null);
        }
        if (!certRules.hasCertOption(number, CertificationResultRules.TEST_TOOLS_USED)) {
            certResult.setTestToolsUsed(null);
        }
        return certResult;
    }

    public CertificationResultDetailsDTO create2015CertResultDetails(
            final Long id, final String number, final Boolean success) {
        CertificationResultDetailsDTO certResult = new CertificationResultDetailsDTO();
        certResult.setId(id);
        certResult.setNumber(number);
        certResult.setSuccess(success);

        if (!certRules.hasCertOption(number, CertificationResultRules.API_DOCUMENTATION)) {
            certResult.setApiDocumentation(null);
        }
        if (!certRules.hasCertOption(number, CertificationResultRules.G1_SUCCESS)) {
            certResult.setG1Success(null);
        }
        if (!certRules.hasCertOption(number, CertificationResultRules.G2_SUCCESS)) {
            certResult.setG2Success(null);
        }
        if (!certRules.hasCertOption(number, CertificationResultRules.GAP)) {
            certResult.setGap(null);
        } else {
            certResult.setGap(Boolean.FALSE);
        }

        if (!certRules.hasCertOption(number, CertificationResultRules.PRIVACY_SECURITY)) {
            certResult.setPrivacySecurityFramework(null);
        }

        if (!certRules.hasCertOption(number, CertificationResultRules.SED)) {
            certResult.setSed(null);
        } else {
            certResult.setSed(Boolean.FALSE);
        }
        return certResult;
    }

    public PendingCertificationResultDTO create2014PendingCertResult(final Long id, final String number,
            final Boolean success) {
        PendingCertificationResultDTO certResult = new PendingCertificationResultDTO();
        certResult.setId(id);
        certResult.getCriterion().setNumber(number);
        certResult.setMeetsCriteria(success);

        if (!certRules.hasCertOption(number, CertificationResultRules.ADDITIONAL_SOFTWARE)) {
            certResult.setAdditionalSoftware(null);
        }
        if (!certRules.hasCertOption(number, CertificationResultRules.API_DOCUMENTATION)) {
            certResult.setApiDocumentation(null);
        }
        if (!certRules.hasCertOption(number, CertificationResultRules.G1_SUCCESS)) {
            certResult.setG1Success(null);
        }
        if (!certRules.hasCertOption(number, CertificationResultRules.G1_MACRA)) {
            certResult.setG1MacraMeasures(null);
        }
        if (!certRules.hasCertOption(number, CertificationResultRules.G2_SUCCESS)) {
            certResult.setG2Success(null);
        }
        if (!certRules.hasCertOption(number, CertificationResultRules.G2_MACRA)) {
            certResult.setG2MacraMeasures(null);
        }

        if (!certRules.hasCertOption(number, CertificationResultRules.GAP)) {
            certResult.setGap(null);
        } else {
            certResult.setGap(Boolean.FALSE);
        }

        if (!certRules.hasCertOption(number, CertificationResultRules.PRIVACY_SECURITY)) {
            certResult.setPrivacySecurityFramework(null);
        }

        if (!certRules.hasCertOption(number, CertificationResultRules.SED)) {
            certResult.setSed(null);
        } else {
            certResult.setSed(Boolean.FALSE);
        }

        if (!certRules.hasCertOption(number, CertificationResultRules.TEST_DATA)) {
            certResult.setTestData(null);
        }
        if (!certRules.hasCertOption(number, CertificationResultRules.FUNCTIONALITY_TESTED)) {
            certResult.setTestFunctionality(null);
        }
        if (!certRules.hasCertOption(number, CertificationResultRules.TEST_PROCEDURE)) {
            certResult.setTestProcedures(null);
        }
        if (!certRules.hasCertOption(number, CertificationResultRules.STANDARDS_TESTED)) {
            certResult.setTestStandards(null);
        }
        if (!certRules.hasCertOption(number, CertificationResultRules.TEST_TOOLS_USED)) {
            certResult.setTestTools(null);
        }
        return certResult;
    }

    public PendingCertificationResultDTO create2015PendingCertResult(final Long id, final String number,
            final Boolean success) {
        PendingCertificationResultDTO certResult = new PendingCertificationResultDTO();
        certResult.setId(id);
        certResult.getCriterion().setNumber(number);
        certResult.setMeetsCriteria(success);

        if (!certRules.hasCertOption(number, CertificationResultRules.ADDITIONAL_SOFTWARE)) {
            certResult.setAdditionalSoftware(null);
        }
        if (!certRules.hasCertOption(number, CertificationResultRules.API_DOCUMENTATION)) {
            certResult.setApiDocumentation(null);
        }
        if (!certRules.hasCertOption(number, CertificationResultRules.G1_SUCCESS)) {
            certResult.setG1Success(null);
        }
        if (!certRules.hasCertOption(number, CertificationResultRules.G1_MACRA)) {
            certResult.setG1MacraMeasures(null);
        }
        if (!certRules.hasCertOption(number, CertificationResultRules.G2_SUCCESS)) {
            certResult.setG2Success(null);
        }
        if (!certRules.hasCertOption(number, CertificationResultRules.G2_MACRA)) {
            certResult.setG2MacraMeasures(null);
        }

        if (!certRules.hasCertOption(number, CertificationResultRules.GAP)) {
            certResult.setGap(null);
        } else {
            certResult.setGap(Boolean.FALSE);
        }

        if (!certRules.hasCertOption(number, CertificationResultRules.PRIVACY_SECURITY)) {
            certResult.setPrivacySecurityFramework(null);
        }

        if (!certRules.hasCertOption(number, CertificationResultRules.SED)) {
            certResult.setSed(null);
        } else {
            certResult.setSed(Boolean.FALSE);
        }

        if (!certRules.hasCertOption(number, CertificationResultRules.TEST_DATA)) {
            certResult.setTestData(null);
        }
        if (!certRules.hasCertOption(number, CertificationResultRules.FUNCTIONALITY_TESTED)) {
            certResult.setTestFunctionality(null);
        }
        if (!certRules.hasCertOption(number, CertificationResultRules.TEST_PROCEDURE)) {
            certResult.setTestProcedures(null);
        }
        if (!certRules.hasCertOption(number, CertificationResultRules.STANDARDS_TESTED)) {
            certResult.setTestStandards(null);
        }
        if (!certRules.hasCertOption(number, CertificationResultRules.TEST_TOOLS_USED)) {
            certResult.setTestTools(null);
        }
        return certResult;
    }

    private CertificationResultTestData createTestData(final Long id, final String name, final String version) {
        CertificationResultTestData testData = new CertificationResultTestData();
        testData.setId(id);
        testData.setAlteration("");
        testData.setVersion(version);
        TestData td = new TestData();
        td.setId(1L);
        td.setName(name);
        testData.setTestData(td);
        return testData;
    }

    private PendingCertificationResultTestDataDTO createPendingTestData(final Long id, final String name, final String version) {
        PendingCertificationResultTestDataDTO testData = new PendingCertificationResultTestDataDTO();
        testData.setId(id);
        testData.setTestDataId(id);
        testData.setAlteration("");
        testData.setVersion(version);
        testData.setEnteredName(name);
        TestDataDTO td = new TestDataDTO();
        td.setId(1L);
        td.setName(name);
        testData.setTestData(td);
        return testData;
    }

    private CertificationResultTestFunctionality createTestFunctionality(final Long id,
            final String name, final String year) {
        CertificationResultTestFunctionality testFunc = new CertificationResultTestFunctionality();
        testFunc.setId(id);
        testFunc.setName(name);
        testFunc.setTestFunctionalityId(1L);
        testFunc.setYear(year);
        return testFunc;
    }

    private PendingCertificationResultTestFunctionalityDTO createPendingTestFunctionality(final Long id,
            final String name, final String year) {
        PendingCertificationResultTestFunctionalityDTO testFunc = new PendingCertificationResultTestFunctionalityDTO();
        testFunc.setId(id);
        testFunc.setTestFunctionalityId(id);
        testFunc.setNumber(name);
        testFunc.setTestFunctionalityId(1L);
        return testFunc;
    }

    private CertificationResultTestProcedure createTestProcedure(final Long id,
            final String name, final String version) {
        CertificationResultTestProcedure testProc = new CertificationResultTestProcedure();
        testProc.setId(id);
        testProc.setTestProcedureVersion(version);
        TestProcedure tp = new TestProcedure();
        tp.setId(1L);
        tp.setName(name);
        testProc.setTestProcedure(tp);
        return testProc;
    }

    private PendingCertificationResultTestProcedureDTO createPendingTestProcedure(final Long id,
            final String name, final String version) {
        PendingCertificationResultTestProcedureDTO testProc = new PendingCertificationResultTestProcedureDTO();
        testProc.setId(id);
        testProc.setTestProcedureId(id);
        testProc.setEnteredName(name);
        testProc.setVersion(version);
        TestProcedureDTO tp = new TestProcedureDTO();
        tp.setId(1L);
        tp.setName(name);
        testProc.setTestProcedure(tp);
        return testProc;
    }

    private CertificationResultTestStandard createTestStandard(final Long id, final String name) {
        CertificationResultTestStandard testStandard = new CertificationResultTestStandard();
        testStandard.setId(id);
        testStandard.setTestStandardName(name);
        return testStandard;
    }

    private PendingCertificationResultTestStandardDTO createPendingTestStandard(final Long id, final String name) {
        PendingCertificationResultTestStandardDTO testStandard = new PendingCertificationResultTestStandardDTO();
        testStandard.setId(id);
        testStandard.setName(name);
        testStandard.setTestStandardId(id);
        return testStandard;
    }

    private CertificationResultTestTool createTestTool(final Long id, final String name,
            final String version, final boolean retired) {
        CertificationResultTestTool tt = new CertificationResultTestTool();
        tt.setId(id);
        tt.setRetired(retired);
        tt.setTestToolId(1L);
        tt.setTestToolName(name);
        tt.setTestToolVersion(version);
        return tt;
    }

    private PendingCertificationResultTestToolDTO createPendingTestTool(final Long id, final String name,
            final String version, final boolean retired) {
        PendingCertificationResultTestToolDTO tt = new PendingCertificationResultTestToolDTO();
        tt.setId(id);
        tt.setTestToolId(1L);
        tt.setName(name);
        tt.setVersion(version);
        return tt;
    }

    private PendingCertificationResultUcdProcessDTO createPendingUcdProcess(final Long id, final String name,
            final String modification) {
        PendingCertificationResultUcdProcessDTO ucd = new PendingCertificationResultUcdProcessDTO();
        ucd.setId(id);
        ucd.setUcdProcessDetails(modification);
        ucd.setUcdProcessName(name);
        return ucd;
    }

    private PendingCertificationResultMacraMeasureDTO createPendingMacraMeasure() {
        PendingCertificationResultMacraMeasureDTO measure = new PendingCertificationResultMacraMeasureDTO();
        measure.setMacraMeasure(new MacraMeasureDTO());
        measure.setEnteredValue("GAP-EP");
        return measure;
    }

    private PendingCertificationResultTestTaskDTO createPendingCertificationResultTestTaskDTO() {
        PendingCertificationResultTestTaskDTO crTask = new PendingCertificationResultTestTaskDTO();
        PendingTestTaskDTO task = new PendingTestTaskDTO();
        task.setUniqueId("Task ID");
        task.setDescription("Description");
        task.setTaskErrors("1");
        task.setTaskErrorsStddev("1");
        task.setTaskPathDeviationObserved("1");
        task.setTaskPathDeviationOptimal("1");
        task.setTaskRating("3");
        task.setTaskRatingScale("scale");
        task.setTaskRatingStddev("2");
        task.setTaskSuccessAverage("3");
        task.setTaskSuccessStddev("3");
        task.setTaskTimeAvg("3");
        task.setTaskTimeDeviationObservedAvg("3");
        task.setTaskTimeDeviationOptimalAvg("23");
        task.setTaskTimeStddev("2");
        crTask.setPendingTestTask(task);
        for (int i = 0; i < 10; i++) {
            PendingCertificationResultTestTaskParticipantDTO part = new PendingCertificationResultTestTaskParticipantDTO();
            part.setTestParticipant(new PendingTestParticipantDTO());
            part.getTestParticipant().setAgeRangeId(1L);
            part.getTestParticipant().setAssistiveTechnologyNeeds("None");
            part.getTestParticipant().setComputerExperienceMonths("4");
            part.getTestParticipant().setEducationTypeId(2L);
            part.getTestParticipant().setGender("N/A");
            part.getTestParticipant().setOccupation("job");
            part.getTestParticipant().setProductExperienceMonths("3");
            part.getTestParticipant().setProfessionalExperienceMonths("98");
            crTask.getTaskParticipants().add(part);
        }
        return crTask;
    }

    public String getChangedListingId(final String origValue,
            final int fieldIndex, final String newValue) {
        String[] uniqueIdParts = origValue.split("\\.");
        uniqueIdParts[fieldIndex] = newValue;
        String changedUniqueId = String.join(".", uniqueIdParts);
        return changedUniqueId;
    }
}
