package gov.healthit.chpl.validation.pendingListing.reviewer.duplicate;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import gov.healthit.chpl.dto.listing.pending.PendingCertificationResultAdditionalSoftwareDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertificationResultDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertifiedProductDTO;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.validation.pendingListing.reviewer.edition2015.duplicate.AdditionalSoftware2015DuplicateReviewer;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { gov.healthit.chpl.CHPLTestConfig.class })
public class AdditionalSoftware2015DuplicateReviewerTest {
    @Autowired
    private MessageSource messageSource;

    @Mock
    private ErrorMessageUtil msgUtil = new ErrorMessageUtil(messageSource);

    private AdditionalSoftware2015DuplicateReviewer reviewer;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        reviewer = new AdditionalSoftware2015DuplicateReviewer(msgUtil);

        //TODO - Can this be extracted as some sort of generic method, so it can be used all error messages??
        Mockito.doAnswer(new Answer<String>() {
            @Override
            public String answer(final InvocationOnMock invocation) throws Throwable {
                String message =
                        "Certification %s contains duplicate Additional Software: CP Source '%s', Grouping '%s'.  The duplicates have been removed.";
                Object[] args = invocation.getArguments();
                return String.format(message, args[1]);
            }
        }).when(msgUtil).getMessage(ArgumentMatchers.eq("listing.criteria.duplicateAdditionalSoftwareCP.2015"),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString(), ArgumentMatchers.anyString());

        Mockito.doAnswer(new Answer<String>() {
            @Override
            public String answer(final InvocationOnMock invocation) throws Throwable {
                String message =
                        "Certification %s contains duplicate Additional Software: Non CP Source: '%s', Version '%s', Grouping '%s'.  The duplicates have been removed.";
                Object[] args = invocation.getArguments();
                return String.format(message, args[1]);
            }
        }).when(msgUtil).getMessage(ArgumentMatchers.eq("listing.criteria.duplicateAdditionalSoftwareNonCP.2015"),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString(), ArgumentMatchers.anyString());
    }

    @Test
    public void testDuplicateCPExists() {
        PendingCertifiedProductDTO listing = new PendingCertifiedProductDTO();
        PendingCertificationResultDTO cert = new PendingCertificationResultDTO();

        PendingCertificationResultAdditionalSoftwareDTO as1 = new PendingCertificationResultAdditionalSoftwareDTO();
        as1.setChplId("Chpl1");
        as1.setGrouping("a");

        PendingCertificationResultAdditionalSoftwareDTO as2 = new PendingCertificationResultAdditionalSoftwareDTO();
        as2.setChplId("Chpl1");
        as2.setGrouping("a");

        cert.getAdditionalSoftware().add(as1);
        cert.getAdditionalSoftware().add(as2);

        reviewer.review(listing, cert);

        assertEquals(1, listing.getWarningMessages().size());
        assertEquals(1, cert.getAdditionalSoftware().size());
    }

    @Test
    public void testDuplicateCPsDoNotExist() {
        PendingCertifiedProductDTO listing = new PendingCertifiedProductDTO();
        PendingCertificationResultDTO cert = new PendingCertificationResultDTO();

        PendingCertificationResultAdditionalSoftwareDTO as1 = new PendingCertificationResultAdditionalSoftwareDTO();
        as1.setChplId("Chpl1");
        as1.setGrouping("a");

        PendingCertificationResultAdditionalSoftwareDTO as2 = new PendingCertificationResultAdditionalSoftwareDTO();
        as2.setChplId("Chpl1");
        as2.setGrouping("b");

        cert.getAdditionalSoftware().add(as1);
        cert.getAdditionalSoftware().add(as2);

        reviewer.review(listing, cert);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(2, cert.getAdditionalSoftware().size());
    }

    @Test
    public void testDuplicateCPsExistInLargerSet() {
        PendingCertifiedProductDTO listing = new PendingCertifiedProductDTO();
        PendingCertificationResultDTO cert = new PendingCertificationResultDTO();

        PendingCertificationResultAdditionalSoftwareDTO as1 = new PendingCertificationResultAdditionalSoftwareDTO();
        as1.setChplId("Chpl1");
        as1.setGrouping("a");

        PendingCertificationResultAdditionalSoftwareDTO as2 = new PendingCertificationResultAdditionalSoftwareDTO();
        as2.setChplId("Chpl2");
        as2.setGrouping("a");

        PendingCertificationResultAdditionalSoftwareDTO as3 = new PendingCertificationResultAdditionalSoftwareDTO();
        as3.setChplId("Chpl1");
        as3.setGrouping("a");

        PendingCertificationResultAdditionalSoftwareDTO as4 = new PendingCertificationResultAdditionalSoftwareDTO();
        as4.setChplId("Chpl3");
        as4.setGrouping("b");

        cert.getAdditionalSoftware().add(as1);
        cert.getAdditionalSoftware().add(as2);
        cert.getAdditionalSoftware().add(as3);
        cert.getAdditionalSoftware().add(as4);

        reviewer.review(listing, cert);

        assertEquals(1, listing.getWarningMessages().size());
        assertEquals(3, cert.getAdditionalSoftware().size());
    }

    @Test
    public void testDuplicateNonCPExists() {
        PendingCertifiedProductDTO listing = new PendingCertifiedProductDTO();
        PendingCertificationResultDTO cert = new PendingCertificationResultDTO();

        PendingCertificationResultAdditionalSoftwareDTO as1 = new PendingCertificationResultAdditionalSoftwareDTO();
        as1.setName("Chpl1");
        as1.setGrouping("a");
        as1.setVersion("v1");

        PendingCertificationResultAdditionalSoftwareDTO as2 = new PendingCertificationResultAdditionalSoftwareDTO();
        as2.setName("Chpl1");
        as2.setGrouping("a");
        as2.setVersion("v1");

        cert.getAdditionalSoftware().add(as1);
        cert.getAdditionalSoftware().add(as2);

        reviewer.review(listing, cert);

        assertEquals(1, listing.getWarningMessages().size());
        assertEquals(1, cert.getAdditionalSoftware().size());
    }

    @Test
    public void testDuplicateNonCPsDoNotExist() {
        PendingCertifiedProductDTO listing = new PendingCertifiedProductDTO();
        PendingCertificationResultDTO cert = new PendingCertificationResultDTO();

        PendingCertificationResultAdditionalSoftwareDTO as1 = new PendingCertificationResultAdditionalSoftwareDTO();
        as1.setName("Chpl1");
        as1.setGrouping("a");
        as1.setVersion("v1");

        PendingCertificationResultAdditionalSoftwareDTO as2 = new PendingCertificationResultAdditionalSoftwareDTO();
        as1.setName("Chpl2");
        as1.setGrouping("a");
        as1.setVersion("v2");

        cert.getAdditionalSoftware().add(as1);
        cert.getAdditionalSoftware().add(as2);

        reviewer.review(listing, cert);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(2, cert.getAdditionalSoftware().size());
    }

    @Test
    public void testDuplicateNonCPsExistInLargerSet() {
        PendingCertifiedProductDTO listing = new PendingCertifiedProductDTO();
        PendingCertificationResultDTO cert = new PendingCertificationResultDTO();

        PendingCertificationResultAdditionalSoftwareDTO as1 = new PendingCertificationResultAdditionalSoftwareDTO();
        as1.setName("Chpl1");
        as1.setGrouping("a");
        as1.setVersion("v1");

        PendingCertificationResultAdditionalSoftwareDTO as2 = new PendingCertificationResultAdditionalSoftwareDTO();
        as2.setName("Chpl2");
        as2.setGrouping("b");
        as2.setVersion("v2");

        PendingCertificationResultAdditionalSoftwareDTO as3 = new PendingCertificationResultAdditionalSoftwareDTO();
        as3.setName("Chpl1");
        as3.setGrouping("a");
        as3.setVersion("v1");

        PendingCertificationResultAdditionalSoftwareDTO as4 = new PendingCertificationResultAdditionalSoftwareDTO();
        as4.setName("Chpl3");
        as4.setGrouping("a");
        as4.setVersion("v3");

        cert.getAdditionalSoftware().add(as1);
        cert.getAdditionalSoftware().add(as2);
        cert.getAdditionalSoftware().add(as3);
        cert.getAdditionalSoftware().add(as4);

        reviewer.review(listing, cert);

        assertEquals(1, listing.getWarningMessages().size());
        assertEquals(3, cert.getAdditionalSoftware().size());
    }
}