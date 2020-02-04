package gov.healthit.chpl.validation.listing.reviewer;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import gov.healthit.chpl.dao.CertificationEditionDAO;
import gov.healthit.chpl.dao.ListingGraphDAO;
import gov.healthit.chpl.dao.search.CertifiedProductSearchDAO;
import gov.healthit.chpl.domain.CertifiedProduct;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.InheritedCertificationStatus;
import gov.healthit.chpl.dto.CertificationEditionDTO;
import gov.healthit.chpl.util.ChplProductNumberUtil;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.ListingMockUtil;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        gov.healthit.chpl.CHPLTestConfig.class
})
public class InheritedCertificationStatusReviewerTest {
    private static final String ICS_TRUE_NO_PARENTS_ERROR = "The ICS value is 'true' which means this "
            + "listing has inherited properties. It is required that at least one parent from which "
            + "the listing inherits be provided.";
    private static final String ICS_UNIQUE_ID_NOT_FOUND_ERROR = "No listing was found with the unique ID "
            + "'" + ListingMockUtil.CHPL_ID_2015 + "'. "
            + "ICS parent listings must reference existing listings in the CHPL.";
    private static final String ICS_SELF_INHERITANCE_ERROR = "A parent listing was found with the same ID "
            + "as this listing. A listing cannot inherit from itself.";
    private static final String ICS_EDITION_MISMATCH_ERROR = "A parent was found with certification "
            + "edition '2011'. Parent certification edition must match that of this listing.";
    private static final String ICS_NOT_LARGEST_CODE_ERROR = "The ICS Code for this listing was "
            + "given as '1' but it was expected to be one more than the largest inherited ICS code '2'.";

    private ListingMockUtil mockUtil = new ListingMockUtil();

    @Autowired
    private MessageSource messageSource;

    @Spy
    private ChplProductNumberUtil productNumUtil;
    @Spy
    private CertifiedProductSearchDAO searchDao;
    @Spy
    private ListingGraphDAO inheritanceDao;
    @Spy
    private CertificationEditionDAO certEditionDao;
    @Spy
    private ErrorMessageUtil msgUtil = new ErrorMessageUtil(messageSource);

    private InheritedCertificationStatusReviewer icsReviewer;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        icsReviewer = new InheritedCertificationStatusReviewer(searchDao, inheritanceDao, certEditionDao, productNumUtil,
                msgUtil);

        Mockito.doReturn(ICS_TRUE_NO_PARENTS_ERROR)
                .when(msgUtil).getMessage(ArgumentMatchers.eq("listing.icsTrueAndNoParentsFound"));
        Mockito.doReturn(ICS_UNIQUE_ID_NOT_FOUND_ERROR)
                .when(msgUtil).getMessage(ArgumentMatchers.eq("listing.icsUniqueIdNotFound"), ArgumentMatchers.anyString());
        Mockito.doReturn(ICS_SELF_INHERITANCE_ERROR)
                .when(msgUtil).getMessage(ArgumentMatchers.eq("listing.icsSelfInheritance"));
        Mockito.doReturn(ICS_EDITION_MISMATCH_ERROR)
                .when(msgUtil).getMessage(ArgumentMatchers.eq("listing.icsEditionMismatch"), ArgumentMatchers.anyString());
        Mockito.doReturn(ICS_NOT_LARGEST_CODE_ERROR)
                .when(msgUtil).getMessage(ArgumentMatchers.eq("listing.icsNotLargestCode"),
                        ArgumentMatchers.anyInt(),
                        ArgumentMatchers.anyInt());
    }

    @Test
    public void testNoIcs_NoErrors() {
        CertifiedProductSearchDetails listing = mockUtil.createValid2015Listing();
        icsReviewer.review(listing);
        assertFalse(listing.getErrorMessages().contains(ICS_TRUE_NO_PARENTS_ERROR));
        assertFalse(listing.getErrorMessages().contains(ICS_UNIQUE_ID_NOT_FOUND_ERROR));
        assertFalse(listing.getErrorMessages().contains(ICS_SELF_INHERITANCE_ERROR));
        assertFalse(listing.getErrorMessages().contains(ICS_EDITION_MISMATCH_ERROR));
        assertFalse(listing.getErrorMessages().contains(ICS_NOT_LARGEST_CODE_ERROR));
    }

    @Test
    public void testValidIcs_NoErrors() {
        CertifiedProductSearchDetails listing = mockUtil.createValid2015Listing();
        String changedChplId = mockUtil.getChangedListingId(
                listing.getChplProductNumber(), ChplProductNumberUtil.ICS_CODE_INDEX, "01");
        listing.setChplProductNumber(changedChplId);
        InheritedCertificationStatus ics = new InheritedCertificationStatus();
        ics.setInherits(Boolean.TRUE);
        CertifiedProduct parent = new CertifiedProduct();
        parent.setCertificationDate(listing.getCertificationDate() - 1);
        parent.setEdition(listing.getCertificationEdition().get("name").toString());
        parent.setId(listing.getId() - 1);
        String parentChplId = mockUtil.getChangedListingId(
                listing.getChplProductNumber(), ChplProductNumberUtil.ICS_CODE_INDEX, "00");
        parent.setChplProductNumber(parentChplId);
        ics.getParents().add(parent);
        listing.setIcs(ics);
        icsReviewer.review(listing);
        assertFalse(listing.getErrorMessages().contains(ICS_TRUE_NO_PARENTS_ERROR));
        assertFalse(listing.getErrorMessages().contains(ICS_UNIQUE_ID_NOT_FOUND_ERROR));
        assertFalse(listing.getErrorMessages().contains(ICS_SELF_INHERITANCE_ERROR));
        assertFalse(listing.getErrorMessages().contains(ICS_EDITION_MISMATCH_ERROR));
        assertFalse(listing.getErrorMessages().contains(ICS_NOT_LARGEST_CODE_ERROR));
    }

    @Test
    public void testHasIcsNoParents_HasErrors() {
        CertifiedProductSearchDetails listing = mockUtil.createValid2015Listing();
        String changedChplId = mockUtil.getChangedListingId(
                listing.getChplProductNumber(), ChplProductNumberUtil.ICS_CODE_INDEX, "01");
        listing.setChplProductNumber(changedChplId);
        InheritedCertificationStatus ics = new InheritedCertificationStatus();
        ics.setInherits(Boolean.TRUE);
        listing.setIcs(ics);
        icsReviewer.review(listing);
        assertTrue(listing.getErrorMessages().contains(ICS_TRUE_NO_PARENTS_ERROR));
        assertFalse(listing.getErrorMessages().contains(ICS_UNIQUE_ID_NOT_FOUND_ERROR));
        assertFalse(listing.getErrorMessages().contains(ICS_SELF_INHERITANCE_ERROR));
        assertFalse(listing.getErrorMessages().contains(ICS_EDITION_MISMATCH_ERROR));
        assertFalse(listing.getErrorMessages().contains(ICS_NOT_LARGEST_CODE_ERROR));
    }

    @Test
    public void testIcsWithBadParentId_HasErrors() {
        Mockito.when(searchDao.getByChplProductNumber(ArgumentMatchers.anyString()))
                .thenReturn(null);

        CertifiedProductSearchDetails listing = mockUtil.createValid2015Listing();
        String changedChplId = mockUtil.getChangedListingId(
                listing.getChplProductNumber(), ChplProductNumberUtil.ICS_CODE_INDEX, "01");
        listing.setChplProductNumber(changedChplId);
        InheritedCertificationStatus ics = new InheritedCertificationStatus();
        ics.setInherits(Boolean.TRUE);
        CertifiedProduct parent = new CertifiedProduct();
        parent.setCertificationDate(listing.getCertificationDate() - 1);
        parent.setEdition(listing.getCertificationEdition().get("name").toString());
        parent.setId(null);
        String parentChplId = mockUtil.getChangedListingId(
                listing.getChplProductNumber(), ChplProductNumberUtil.ICS_CODE_INDEX, "00");
        parent.setChplProductNumber(parentChplId);
        ics.getParents().add(parent);
        listing.setIcs(ics);
        icsReviewer.review(listing);
        assertFalse(listing.getErrorMessages().contains(ICS_TRUE_NO_PARENTS_ERROR));
        assertTrue(listing.getErrorMessages().contains(ICS_UNIQUE_ID_NOT_FOUND_ERROR));
        assertFalse(listing.getErrorMessages().contains(ICS_SELF_INHERITANCE_ERROR));
        assertFalse(listing.getErrorMessages().contains(ICS_EDITION_MISMATCH_ERROR));
        assertFalse(listing.getErrorMessages().contains(ICS_NOT_LARGEST_CODE_ERROR));
    }

    @Test
    public void testIcsWithSelfParentId_HasErrors() {
        CertifiedProductSearchDetails listing = mockUtil.createValid2015Listing();
        String changedChplId = mockUtil.getChangedListingId(
                listing.getChplProductNumber(), ChplProductNumberUtil.ICS_CODE_INDEX, "01");
        listing.setChplProductNumber(changedChplId);

        CertifiedProduct self = new CertifiedProduct();
        self.setId(listing.getId());
        self.setChplProductNumber(listing.getChplProductNumber());
        Mockito.when(searchDao.getByChplProductNumber(ArgumentMatchers.anyString()))
                .thenReturn(self);

        InheritedCertificationStatus ics = new InheritedCertificationStatus();
        ics.setInherits(Boolean.TRUE);
        CertifiedProduct parent = new CertifiedProduct();
        parent.setCertificationDate(listing.getCertificationDate());
        parent.setEdition(listing.getCertificationEdition().get("name").toString());
        parent.setId(listing.getId());
        parent.setChplProductNumber(listing.getChplProductNumber());
        ics.getParents().add(parent);
        listing.setIcs(ics);
        icsReviewer.review(listing);
        assertFalse(listing.getErrorMessages().contains(ICS_TRUE_NO_PARENTS_ERROR));
        assertFalse(listing.getErrorMessages().contains(ICS_UNIQUE_ID_NOT_FOUND_ERROR));
        assertTrue(listing.getErrorMessages().contains(ICS_SELF_INHERITANCE_ERROR));
        assertFalse(listing.getErrorMessages().contains(ICS_EDITION_MISMATCH_ERROR));
        assertFalse(listing.getErrorMessages().contains(ICS_NOT_LARGEST_CODE_ERROR));
    }

    @Test
    public void testIcsWithBadParentEdition_HasErrors() {
        CertifiedProductSearchDetails listing = mockUtil.createValid2015Listing();
        String changedChplId = mockUtil.getChangedListingId(
                listing.getChplProductNumber(), ChplProductNumberUtil.ICS_CODE_INDEX, "01");
        listing.setChplProductNumber(changedChplId);

        InheritedCertificationStatus ics = new InheritedCertificationStatus();
        ics.setInherits(Boolean.TRUE);
        CertifiedProduct parent = new CertifiedProduct();
        parent.setCertificationDate(listing.getCertificationDate() - 1);
        parent.setEdition(listing.getCertificationEdition().get("name").toString());
        parent.setId(listing.getId() - 1);
        String parentChplId = mockUtil.getChangedListingId(
                listing.getChplProductNumber(), ChplProductNumberUtil.ICS_CODE_INDEX, "00");
        parentChplId = mockUtil.getChangedListingId(
                parentChplId, ChplProductNumberUtil.EDITION_CODE_INDEX, "14");
        parent.setChplProductNumber(parentChplId);
        ics.getParents().add(parent);
        listing.setIcs(ics);

        Mockito.when(searchDao.getByChplProductNumber(ArgumentMatchers.anyString()))
                .thenReturn(parent);
        List<CertificationEditionDTO> parentEditions = new ArrayList<CertificationEditionDTO>();
        CertificationEditionDTO edition = new CertificationEditionDTO();
        edition.setId(1L);
        edition.setRetired(Boolean.FALSE);
        edition.setYear("2011");
        parentEditions.add(edition);
        Mockito.when(certEditionDao.getEditions(ArgumentMatchers.anyList()))
                .thenReturn(parentEditions);

        icsReviewer.review(listing);
        assertFalse(listing.getErrorMessages().contains(ICS_TRUE_NO_PARENTS_ERROR));
        assertFalse(listing.getErrorMessages().contains(ICS_UNIQUE_ID_NOT_FOUND_ERROR));
        assertFalse(listing.getErrorMessages().contains(ICS_SELF_INHERITANCE_ERROR));
        assertTrue(listing.getErrorMessages().contains(ICS_EDITION_MISMATCH_ERROR));
        assertFalse(listing.getErrorMessages().contains(ICS_NOT_LARGEST_CODE_ERROR));
    }

    @Test
    public void testIcsWithTooSmallCode_HasErrors() {
        CertifiedProductSearchDetails listing = mockUtil.createValid2015Listing();
        String changedChplId = mockUtil.getChangedListingId(
                listing.getChplProductNumber(), ChplProductNumberUtil.ICS_CODE_INDEX, "01");
        listing.setChplProductNumber(changedChplId);

        InheritedCertificationStatus ics = new InheritedCertificationStatus();
        ics.setInherits(Boolean.TRUE);
        CertifiedProduct parent = new CertifiedProduct();
        parent.setCertificationDate(listing.getCertificationDate() - 1);
        parent.setEdition(listing.getCertificationEdition().get("name").toString());
        parent.setId(listing.getId() - 1);
        String parentChplId = mockUtil.getChangedListingId(
                listing.getChplProductNumber(), ChplProductNumberUtil.ICS_CODE_INDEX, "02");
        parent.setChplProductNumber(parentChplId);
        ics.getParents().add(parent);
        listing.setIcs(ics);

        Mockito.when(searchDao.getByChplProductNumber(ArgumentMatchers.anyString()))
                .thenReturn(parent);
        List<CertificationEditionDTO> parentEditions = new ArrayList<CertificationEditionDTO>();
        CertificationEditionDTO edition = new CertificationEditionDTO();
        edition.setId(3L);
        edition.setRetired(Boolean.FALSE);
        edition.setYear("2015");
        parentEditions.add(edition);
        Mockito.when(certEditionDao.getEditions(ArgumentMatchers.anyList()))
                .thenReturn(parentEditions);
        Mockito.when(inheritanceDao.getLargestIcs(ArgumentMatchers.anyList()))
                .thenReturn(2);

        icsReviewer.review(listing);
        assertFalse(listing.getErrorMessages().contains(ICS_TRUE_NO_PARENTS_ERROR));
        assertFalse(listing.getErrorMessages().contains(ICS_UNIQUE_ID_NOT_FOUND_ERROR));
        assertFalse(listing.getErrorMessages().contains(ICS_SELF_INHERITANCE_ERROR));
        assertFalse(listing.getErrorMessages().contains(ICS_EDITION_MISMATCH_ERROR));
        assertTrue(listing.getErrorMessages().contains(ICS_NOT_LARGEST_CODE_ERROR));
    }
}
