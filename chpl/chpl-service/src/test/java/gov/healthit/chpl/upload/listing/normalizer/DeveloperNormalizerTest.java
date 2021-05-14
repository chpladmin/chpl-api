package gov.healthit.chpl.upload.listing.normalizer;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import gov.healthit.chpl.dao.DeveloperDAO;
import gov.healthit.chpl.domain.Address;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.domain.DeveloperStatus;
import gov.healthit.chpl.domain.contact.PointOfContact;
import gov.healthit.chpl.dto.AddressDTO;
import gov.healthit.chpl.dto.ContactDTO;
import gov.healthit.chpl.dto.DeveloperDTO;
import gov.healthit.chpl.dto.DeveloperStatusDTO;
import gov.healthit.chpl.dto.DeveloperStatusEventDTO;
import gov.healthit.chpl.entity.developer.DeveloperStatusType;
import gov.healthit.chpl.upload.listing.ListingUploadHandlerUtil;
import gov.healthit.chpl.util.ChplProductNumberUtil;
import gov.healthit.chpl.util.ErrorMessageUtil;

public class DeveloperNormalizerTest {

    private DeveloperDAO developerDao;
    private DeveloperDetailsNormalizer normalizer;

    @Before
    public void setup() {
        developerDao = Mockito.mock(DeveloperDAO.class);
        ErrorMessageUtil msgUtil = Mockito.mock(ErrorMessageUtil.class);
        normalizer = new DeveloperDetailsNormalizer(developerDao, new ChplProductNumberUtil(),
                new ListingUploadHandlerUtil(msgUtil));
    }

    @Test
    public void normalize_nullDeveloperNullChplProductNumber_noChanges() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber(null)
                .developer(null)
                .build();
        normalizer.normalize(listing);
        assertNull(listing.getDeveloper());
    }

    @Test
    public void normalize_nullDeveloperEmptyChplProductNumber_noChanges() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber("")
                .developer(null)
                .build();
        normalizer.normalize(listing);
        assertNull(listing.getDeveloper());
    }

    @Test
    public void normalize_nullDeveloperInvalidChplProductNumber_noChanges() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber("junk")
                .developer(null)
                .build();
        normalizer.normalize(listing);
        assertNull(listing.getDeveloper());
    }

    @Test
    public void normalize_nullDeveloperChplProductNumberMissingDeveloperCode_noChanges() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber("15.04.04..WEBe.06.00.1.210101")
                .developer(null)
                .build();
        normalizer.normalize(listing);
        assertNull(listing.getDeveloper());
    }

    @Test
    public void normalize_nullDeveloperNoDeveloperExistsByCode_noChanges() {
        Mockito.when(developerDao.getByCode(ArgumentMatchers.eq("2526")))
            .thenReturn(null);
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber("15.04.04.2526.WEBe.06.00.1.210101")
                .developer(null)
                .build();
        normalizer.normalize(listing);
        assertNull(listing.getDeveloper());
    }

    @Test
    public void normalize_nullDeveloperDeveloperExistsByCode_getsSystemInfo() {
        Mockito.when(developerDao.getByCode(ArgumentMatchers.eq("2526")))
            .thenReturn(buildSystemDeveloperDto());
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber("15.04.04.2526.WEBe.06.00.1.210101")
                .developer(null)
                .build();
        normalizer.normalize(listing);

        assertNotNull(listing.getDeveloper());
        assertEquals(1L, listing.getDeveloper().getDeveloperId());
        assertEquals("Test Name", listing.getDeveloper().getName());
    }

    @Test
    public void normalize_developerExistsByCode_getsSystemInfo() {
        Mockito.when(developerDao.getByCode(ArgumentMatchers.eq("2526")))
            .thenReturn(buildSystemDeveloperDto());
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber("15.04.04.2526.WEBe.06.00.1.210101")
                .developer(new Developer())
                .build();
        normalizer.normalize(listing);

        assertNotNull(listing.getDeveloper());
        assertEquals(1L, listing.getDeveloper().getDeveloperId());
        assertEquals("Test Name", listing.getDeveloper().getName());
    }

    @Test
    public void normalize_developerNotExistsByCode_copiesUserEnteredInfo() {
        Developer developer = Developer.builder()
                .userEnteredWebsite("http://www.website.com")
                .userEnteredSelfDeveloper("true")
                .build();
        Mockito.when(developerDao.getByCode(ArgumentMatchers.eq("2526")))
            .thenReturn(null);
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber("15.04.04.2526.WEBe.06.00.1.210101")
                .developer(developer)
                .build();
        normalizer.normalize(listing);

        assertNotNull(listing.getDeveloper());
        assertNull(listing.getDeveloper().getDeveloperId());
        assertNull(listing.getDeveloper().getName());
        assertEquals("http://www.website.com", listing.getDeveloper().getWebsite());
        assertTrue(listing.getDeveloper().getSelfDeveloper());
    }

    @Test
    public void normalize_userEnteredDeveloperNameInSystem_getsSystemInfo() {
        Developer userEnteredDeveloper = Developer.builder()
                .userEnteredName("Test Name")
                .build();
        Mockito.when(developerDao.getByName(ArgumentMatchers.eq("Test Name")))
            .thenReturn(buildSystemDeveloperDto());

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber("15.04.04.1234.WEBe.06.00.1.210101")
                .developer(userEnteredDeveloper)
                .build();
        normalizer.normalize(listing);
        assertNotNull(listing.getDeveloper());
        assertEquals("Test Name", listing.getDeveloper().getName());
        assertEquals(1L, listing.getDeveloper().getDeveloperId());
    }

    @Test
    public void normalize_userEnteredDeveloperNameNotInSystem_copiesUserEnteredInfo() {
        Developer userEnteredDeveloper = Developer.builder()
                .userEnteredName("Test Name")
                .build();
        Mockito.when(developerDao.getByName(ArgumentMatchers.eq("Test Name")))
            .thenReturn(null);

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber("15.04.04.1234.WEBe.06.00.1.210101")
                .developer(userEnteredDeveloper)
                .build();
        normalizer.normalize(listing);
        assertNotNull(listing.getDeveloper());
        assertNull(listing.getDeveloper().getDeveloperId());
        assertEquals("Test Name", listing.getDeveloper().getName());
        assertEquals("Test Name", listing.getDeveloper().getUserEnteredName());
    }

    @Test
    public void normalize_userEnteredDeveloperNameNewDeveloper_copiesUserEnteredInfo() {
        Developer userEnteredDeveloper = Developer.builder()
                .userEnteredName("Test Name")
                .build();

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber("15.04.04.XXXX.WEBe.06.00.1.210101")
                .developer(userEnteredDeveloper)
                .build();
        normalizer.normalize(listing);
        assertNotNull(listing.getDeveloper());
        assertNull(listing.getDeveloper().getDeveloperId());
        assertEquals("Test Name", listing.getDeveloper().getName());
        assertEquals("Test Name", listing.getDeveloper().getUserEnteredName());
    }

    private DeveloperDTO buildSystemDeveloperDto() {
        return buildDeveloperDto(1L);
    }

    private DeveloperDTO buildDeveloperDto(Long id) {
        return DeveloperDTO.builder()
                .id(id)
                .name("Test Name")
                .selfDeveloper(true)
                .website("http://www.test.com")
                .address(AddressDTO.builder()
                        .streetLineOne("test")
                        .city("test")
                        .state("test")
                        .zipcode("12345")
                        .build())
                .contact(ContactDTO.builder()
                        .fullName("test")
                        .email("test@test.com")
                        .phoneNumber("123-456-7890")
                        .build())
                .statusEvent(DeveloperStatusEventDTO.builder()
                        .status(DeveloperStatusDTO.builder()
                                .statusName(DeveloperStatusType.Active.getName())
                                .build())
                        .statusDate(new Date())
                        .build())
                .build();
    }

    private Developer buildSystemDeveloper() {
        return buildDeveloper(1L);
    }

    private Developer buildDeveloper(Long id) {
        return Developer.builder()
                .developerId(id)
                .name("Test Name")
                .selfDeveloper(true)
                .userEnteredSelfDeveloper("1")
                .website("http://www.test.com")
                .address(Address.builder()
                        .line1("test")
                        .city("test")
                        .state("test")
                        .zipcode("12345")
                        .build())
                .contact(PointOfContact.builder()
                        .fullName("test")
                        .email("test@test.com")
                        .phoneNumber("123-456-7890")
                        .build())
                .status(DeveloperStatus.builder()
                        .status(DeveloperStatusType.Active.getName())
                        .build())
                .build();
    }
}
