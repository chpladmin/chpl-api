package gov.healthit.chpl.validation.listing.reviewer;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
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
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.util.ErrorMessageUtil;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { gov.healthit.chpl.CHPLTestConfig.class })
public class DeveloperStatusReviewerTest {
    private static final String DEV_NAME = "Test Developer";
    private static final String NO_DEV_STATUS_ERROR =
            "The current status of the developer " + DEV_NAME + " cannot be determined. "
                    + "A developer must be listed as Active in order to update certified products belongong to it.";
    private static final String DEV_SUSPENDED_ERROR = "The developer " + DEV_NAME + " has a status of "
                    + DeveloperStatusType.SuspendedByOnc.getName() + ". Certified products belonging "
                    + "to this developer cannot be updated until its status returns to "
                    + DeveloperStatusType.Active.getName() + " or "
                    + DeveloperStatusType.UnderCertificationBanByOnc.getName() + ".";
    private static final String DEV_BANNED_ERROR =
            "The developer " + DEV_NAME + " has a status of "
                    + DeveloperStatusType.UnderCertificationBanByOnc.toString()
                    + ". Certified products belonging to this developer cannot be updated until "
                    + "its status returns to Active.";

    @Autowired
    private ListingMockUtil mockUtil;

    @Mock
    private DeveloperDAO developerDao;

    @Mock
    private ResourcePermissions resourcePermissions;

    @Mock
    private ErrorMessageUtil msgUtil;

    @InjectMocks
    private DeveloperStatusReviewer devStatusReviewer;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        Mockito.doReturn(NO_DEV_STATUS_ERROR)
        .when(msgUtil).getMessage(
                ArgumentMatchers.eq("listing.developer.noStatusFound.noUpdate"));
        Mockito.doReturn(DEV_SUSPENDED_ERROR)
        .when(msgUtil).getMessage(
                ArgumentMatchers.eq("listing.developer.noStatusFound.noUpdate"));
        Mockito.doReturn(DEV_BANNED_ERROR)
        .when(msgUtil).getMessage(
                ArgumentMatchers.eq("listing.developer.notActive.noUpdate"));
    }

    @Test
    public void testActiveDeveloper_EditedByNonAdmin_NoErrors() throws EntityRetrievalException {
        Mockito.when(developerDao.getById(ArgumentMatchers.anyLong())).thenReturn(createDeveloperDTO(DeveloperStatusType.Active));
        Mockito.when(resourcePermissions.isUserRoleAdmin()).thenReturn(false);
        Mockito.when(resourcePermissions.isUserRoleOnc()).thenReturn(false);

        CertifiedProductSearchDetails listing = mockUtil.createValid2015Listing();
        devStatusReviewer.review(listing);
        assertFalse(listing.getErrorMessages().contains(NO_DEV_STATUS_ERROR));
        assertFalse(listing.getErrorMessages().contains(DEV_SUSPENDED_ERROR));
        assertFalse(listing.getErrorMessages().contains(DEV_BANNED_ERROR));
    }

    @Test
    public void testActiveDeveloper_EditedByAdmin_NoErrors() throws EntityRetrievalException {
        Mockito.when(developerDao.getById(ArgumentMatchers.anyLong())).thenReturn(createDeveloperDTO(DeveloperStatusType.Active));
        Mockito.when(resourcePermissions.isUserRoleAdmin()).thenReturn(true);
        Mockito.when(resourcePermissions.isUserRoleOnc()).thenReturn(false);

        CertifiedProductSearchDetails listing = mockUtil.createValid2015Listing();
        devStatusReviewer.review(listing);
        assertFalse(listing.getErrorMessages().contains(NO_DEV_STATUS_ERROR));
        assertFalse(listing.getErrorMessages().contains(DEV_SUSPENDED_ERROR));
        assertFalse(listing.getErrorMessages().contains(DEV_BANNED_ERROR));
    }

    @Test
    public void testActiveDeveloper_EditedByOnc_NoErrors() throws EntityRetrievalException {
        Mockito.when(developerDao.getById(ArgumentMatchers.anyLong())).thenReturn(createDeveloperDTO(DeveloperStatusType.Active));
        Mockito.when(resourcePermissions.isUserRoleAdmin()).thenReturn(false);
        Mockito.when(resourcePermissions.isUserRoleOnc()).thenReturn(true);

        CertifiedProductSearchDetails listing = mockUtil.createValid2015Listing();
        devStatusReviewer.review(listing);
        assertFalse(listing.getErrorMessages().contains(NO_DEV_STATUS_ERROR));
        assertFalse(listing.getErrorMessages().contains(DEV_SUSPENDED_ERROR));
        assertFalse(listing.getErrorMessages().contains(DEV_BANNED_ERROR));
    }

    @Test
    public void testSuspendedDeveloper_HasError() {
        try {
            Mockito.when(developerDao.getById(ArgumentMatchers.anyLong()))
        .thenReturn(createDeveloperDTO(DeveloperStatusType.SuspendedByOnc));
        } catch (EntityRetrievalException ex) { }

        CertifiedProductSearchDetails listing = mockUtil.createValid2015Listing();
        devStatusReviewer.review(listing);
        assertFalse(listing.getErrorMessages().contains(NO_DEV_STATUS_ERROR));
        assertTrue(listing.getErrorMessages().contains(DEV_SUSPENDED_ERROR));
        assertFalse(listing.getErrorMessages().contains(DEV_BANNED_ERROR));
    }

    @Test
    public void testBannedDeveloper_HasError() {
        try {
            Mockito.when(developerDao.getById(ArgumentMatchers.anyLong()))
        .thenReturn(createDeveloperDTO(DeveloperStatusType.UnderCertificationBanByOnc));
        } catch (EntityRetrievalException ex) { }

        CertifiedProductSearchDetails listing = mockUtil.createValid2015Listing();
        devStatusReviewer.review(listing);
        assertFalse(listing.getErrorMessages().contains(NO_DEV_STATUS_ERROR));
        assertFalse(listing.getErrorMessages().contains(DEV_SUSPENDED_ERROR));
        assertTrue(listing.getErrorMessages().contains(DEV_BANNED_ERROR));
    }

    @Test
    public void testNoDeveloperWithIdFound_HasError() {
        try {
            Mockito.when(developerDao.getById(ArgumentMatchers.anyLong()))
        .thenReturn(null);
        } catch (EntityRetrievalException ex) { }

        CertifiedProductSearchDetails listing = mockUtil.createValid2015Listing();
        devStatusReviewer.review(listing);
        assertFalse(listing.getErrorMessages().contains(NO_DEV_STATUS_ERROR));
        assertFalse(listing.getErrorMessages().contains(DEV_SUSPENDED_ERROR));
        assertFalse(listing.getErrorMessages().contains(DEV_BANNED_ERROR));
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
