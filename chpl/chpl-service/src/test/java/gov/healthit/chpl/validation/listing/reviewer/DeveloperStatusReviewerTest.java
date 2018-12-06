package gov.healthit.chpl.validation.listing.reviewer;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import gov.healthit.chpl.dao.DeveloperDAO;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.dto.DeveloperDTO;
import gov.healthit.chpl.dto.DeveloperStatusDTO;
import gov.healthit.chpl.dto.DeveloperStatusEventDTO;
import gov.healthit.chpl.entity.developer.DeveloperStatusType;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.listing.ListingMockUtil;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { gov.healthit.chpl.CHPLTestConfig.class })
public class DeveloperStatusReviewerTest {
    private static final String DEV_NAME = "Test Developer";
    private static final String NO_DEV_STATUS_ERROR =
            "The current status of the developer " + DEV_NAME + " cannot be determined. "
                    + "A developer must be listed as Active in order to update certified products belongong to it.";
    private static final String DEV_SUSPENDED_ERROR =
            "The developer " + DEV_NAME + " has a status of "
                    + DeveloperStatusType.SuspendedByOnc.toString()
                    + ". Certified products belonging to this developer cannot be updated until "
                    + "its status returns to Active.";
    private static final String DEV_BANNED_ERROR =
            "The developer " + DEV_NAME + " has a status of "
                    + DeveloperStatusType.UnderCertificationBanByOnc.toString()
                    + ". Certified products belonging to this developer cannot be updated until "
                    + "its status returns to Active.";
    private static final String NO_DEV_FOUND_ERROR = "Could not find developer with id 1";

    @Autowired private ListingMockUtil mockUtil;

    @Spy
    private DeveloperDAO devDao;

    @InjectMocks
    private DeveloperStatusReviewer devStatusReviewer;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testActiveDeveloper_NoErrors() {
        try {
            Mockito.when(devDao.getById(ArgumentMatchers.anyLong()))
        .thenReturn(createDeveloperDTO(DeveloperStatusType.Active));
        } catch (EntityRetrievalException ex) { }

        CertifiedProductSearchDetails listing = mockUtil.createValid2015Listing();
        devStatusReviewer.review(listing);
        assertFalse(listing.getErrorMessages().contains(NO_DEV_STATUS_ERROR));
        assertFalse(listing.getErrorMessages().contains(DEV_SUSPENDED_ERROR));
        assertFalse(listing.getErrorMessages().contains(DEV_BANNED_ERROR));
        assertFalse(listing.getErrorMessages().contains(NO_DEV_FOUND_ERROR));
    }

    @Test
    public void testSuspendedDeveloper_HasError() {
        try {
            Mockito.when(devDao.getById(ArgumentMatchers.anyLong()))
        .thenReturn(createDeveloperDTO(DeveloperStatusType.SuspendedByOnc));
        } catch (EntityRetrievalException ex) { }

        CertifiedProductSearchDetails listing = mockUtil.createValid2015Listing();
        devStatusReviewer.review(listing);
        assertFalse(listing.getErrorMessages().contains(NO_DEV_STATUS_ERROR));
        assertTrue(listing.getErrorMessages().contains(DEV_SUSPENDED_ERROR));
        assertFalse(listing.getErrorMessages().contains(DEV_BANNED_ERROR));
        assertFalse(listing.getErrorMessages().contains(NO_DEV_FOUND_ERROR));
    }

    @Test
    public void testBannedDeveloper_HasError() {
        try {
            Mockito.when(devDao.getById(ArgumentMatchers.anyLong()))
        .thenReturn(createDeveloperDTO(DeveloperStatusType.UnderCertificationBanByOnc));
        } catch (EntityRetrievalException ex) { }

        CertifiedProductSearchDetails listing = mockUtil.createValid2015Listing();
        devStatusReviewer.review(listing);
        assertFalse(listing.getErrorMessages().contains(NO_DEV_STATUS_ERROR));
        assertFalse(listing.getErrorMessages().contains(DEV_SUSPENDED_ERROR));
        assertTrue(listing.getErrorMessages().contains(DEV_BANNED_ERROR));
        assertFalse(listing.getErrorMessages().contains(NO_DEV_FOUND_ERROR));
    }

    @Test
    public void testNoDeveloperWithIdFound_HasError() {
        try {
            Mockito.when(devDao.getById(ArgumentMatchers.anyLong()))
        .thenReturn(null);
        } catch (EntityRetrievalException ex) { }

        CertifiedProductSearchDetails listing = mockUtil.createValid2015Listing();
        devStatusReviewer.review(listing);
        assertFalse(listing.getErrorMessages().contains(NO_DEV_STATUS_ERROR));
        assertFalse(listing.getErrorMessages().contains(DEV_SUSPENDED_ERROR));
        assertFalse(listing.getErrorMessages().contains(DEV_BANNED_ERROR));
        assertTrue(listing.getErrorMessages().contains(NO_DEV_FOUND_ERROR));
    }

    private DeveloperDTO createDeveloperDTO(DeveloperStatusType currentStatus) {
        DeveloperDTO dev = new DeveloperDTO();
        dev.setId(1L);
        dev.setDeveloperCode("0001");
        dev.setName(DEV_NAME);
        DeveloperStatusEventDTO statusEvent = new DeveloperStatusEventDTO();
        statusEvent.setDeveloperId(dev.getId());
        statusEvent.setId(1L);
        statusEvent.setStatusDate(new Date());
        DeveloperStatusDTO status = new DeveloperStatusDTO();
        status.setId(1L);
        status.setStatusName(currentStatus.toString());
        statusEvent.setStatus(status);
        dev.getStatusEvents().add(statusEvent);
        return dev;
    }
}
