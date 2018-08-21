package gov.healthit.chpl.listing;

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
import gov.healthit.chpl.util.CertificationResultRules;

@Component
public class ListingMockUtil {
    public static String CHPL_ID_2015 = "15.02.02.3007.A056.01.00.0.180214";
    @Autowired private CertificationResultRules certRules;

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
        listing.getCertifyingBody().put("name", "InfoGard");
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
        contact.setFirstName("First");
        contact.setLastName("Last");
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

    private CertifiedProductTestingLab createTestingLab() {
        CertifiedProductTestingLab atl = new CertifiedProductTestingLab();
        atl.setId(1L);
        atl.setTestingLabCode("02");
        atl.setTestingLabId(1L);
        atl.setTestingLabName("InfoGard");
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

        if(!certRules.hasCertOption(number, CertificationResultRules.ADDITIONAL_SOFTWARE)) {
            certResult.setAdditionalSoftware(null);
        }
        if(!certRules.hasCertOption(number, CertificationResultRules.API_DOCUMENTATION)) {
            certResult.setApiDocumentation(null);
        }
        if(!certRules.hasCertOption(number, CertificationResultRules.G1_SUCCESS)) {
            certResult.setG1Success(null);
        }
        if(!certRules.hasCertOption(number, CertificationResultRules.G1_MACRA)) {
            certResult.setG1MacraMeasures(null);
        }        
        if(!certRules.hasCertOption(number, CertificationResultRules.G2_SUCCESS)) {
            certResult.setG2Success(null);
        }
        if(!certRules.hasCertOption(number, CertificationResultRules.G2_MACRA)) {
            certResult.setG2MacraMeasures(null);
        }

        if(!certRules.hasCertOption(number, CertificationResultRules.GAP)) {
            certResult.setGap(null);
        } else {
            certResult.setGap(Boolean.FALSE);
        }

        if(!certRules.hasCertOption(number, CertificationResultRules.PRIVACY_SECURITY)) {
            certResult.setPrivacySecurityFramework(null);
        }

        if(!certRules.hasCertOption(number, CertificationResultRules.SED)) {
            certResult.setSed(null);
        } else {
            certResult.setSed(Boolean.FALSE);
        }

        if(!certRules.hasCertOption(number, CertificationResultRules.TEST_DATA)) {
            certResult.setTestDataUsed(null);
        }
        if(!certRules.hasCertOption(number, CertificationResultRules.FUNCTIONALITY_TESTED)) {
            certResult.setTestFunctionality(null);
        }
        if(!certRules.hasCertOption(number, CertificationResultRules.TEST_PROCEDURE)) {
            certResult.setTestProcedures(null);
        }
        if(!certRules.hasCertOption(number, CertificationResultRules.STANDARDS_TESTED)) {
            certResult.setTestStandards(null);
        }
        if(!certRules.hasCertOption(number, CertificationResultRules.TEST_TOOLS_USED)) {
            certResult.setTestToolsUsed(null);
        }
        return certResult;
    }

    private CertificationResultTestData createTestData(Long id, String name, String version) {
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

    private CertificationResultTestFunctionality createTestFunctionality(Long id, String name, String year) {
        CertificationResultTestFunctionality testFunc = new CertificationResultTestFunctionality();
        testFunc.setId(id);
        testFunc.setName(name);
        testFunc.setTestFunctionalityId(1L);
        testFunc.setYear(year);
        return testFunc;
    }

    private CertificationResultTestProcedure createTestProcedure(Long id, String name, String version) {
        CertificationResultTestProcedure testProc = new CertificationResultTestProcedure();
        testProc.setId(id);
        testProc.setTestProcedureVersion(version);
        TestProcedure tp = new TestProcedure();
        tp.setId(1L);
        tp.setName(name);
        testProc.setTestProcedure(tp);
        return testProc;
    }

    private CertificationResultTestStandard createTestStandard(Long id, String name) {
        CertificationResultTestStandard testStandard = new CertificationResultTestStandard();
        testStandard.setId(id);
        testStandard.setTestStandardName(name);
        return testStandard;
    }

    private CertificationResultTestTool createTestTool(Long id, String name, String version, boolean retired) {
        CertificationResultTestTool tt = new CertificationResultTestTool();
        tt.setId(id);
        tt.setRetired(retired);
        tt.setTestToolId(1L);
        tt.setTestToolName(name);
        tt.setTestToolVersion(version);
        return tt;
    }

    public String getChangedListingId(String origValue, int fieldIndex, String newValue) {
        String[] uniqueIdParts = origValue.split("\\.");
        uniqueIdParts[fieldIndex] = newValue;
        String changedUniqueId = String.join(".", uniqueIdParts);
        return changedUniqueId;
    }
}
