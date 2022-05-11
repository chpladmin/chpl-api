package gov.healthit.chpl.upload.listing.normalizer;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import gov.healthit.chpl.dao.ProductDAO;
import gov.healthit.chpl.dao.ProductVersionDAO;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.domain.Product;
import gov.healthit.chpl.domain.ProductVersion;
import gov.healthit.chpl.dto.ProductVersionDTO;

public class ProductAndVersionNormalizerTest {

    private ProductDAO productDao;
    private ProductVersionDAO versionDao;
    private ProductAndVersionNormalizer normalizer;

    @Before
    public void setup() {
        productDao = Mockito.mock(ProductDAO.class);
        versionDao = Mockito.mock(ProductVersionDAO.class);
        normalizer = new ProductAndVersionNormalizer(productDao, versionDao);
    }

    @Test
    public void normalize_nullFields_noChanges() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .developer(null)
                .product(null)
                .version(null)
                .build();
        normalizer.normalize(listing);
        assertNull(listing.getProduct());
        assertNull(listing.getVersion());
    }

    @Test
    public void normalize_nullDeveloper_noChanges() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .developer(null)
                .product(Product.builder()
                        .name("prod 1")
                        .build())
                .version(ProductVersion.builder()
                        .version("1.0")
                        .build())
                .build();
        normalizer.normalize(listing);

        assertNull(listing.getDeveloper());
        assertNotNull(listing.getProduct());
        assertNull(listing.getProduct().getId());
        assertEquals("prod 1", listing.getProduct().getName());
        assertNotNull(listing.getVersion());
        assertNull(listing.getVersion().getId());
        assertEquals("1.0", listing.getVersion().getVersion());
    }

    @Test
    public void normalize_nullDeveloperId_noChanges() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .developer(Developer.builder()
                        .name("New Developer 1")
                        .build())
                .product(Product.builder()
                        .name("prod 1")
                        .build())
                .version(ProductVersion.builder()
                        .version("1.0")
                        .build())
                .build();
        normalizer.normalize(listing);

        assertNotNull(listing.getDeveloper());
        assertNull(listing.getDeveloper().getDeveloperId());
        assertEquals("New Developer 1", listing.getDeveloper().getName());
        assertNotNull(listing.getProduct());
        assertNull(listing.getProduct().getId());
        assertEquals("prod 1", listing.getProduct().getName());
        assertNotNull(listing.getVersion());
        assertNull(listing.getVersion().getId());
        assertEquals("1.0", listing.getVersion().getVersion());
    }

    @Test
    public void normalize_nullProductAndVersion_noChanges() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .developer(Developer.builder()
                        .developerId(1L)
                        .name("New Developer 1")
                        .build())
                .product(null)
                .version(null)
                .build();
        normalizer.normalize(listing);

        assertNotNull(listing.getDeveloper());
        assertEquals(1L, listing.getDeveloper().getDeveloperId());
        assertEquals("New Developer 1", listing.getDeveloper().getName());
        assertNull(listing.getProduct());
        assertNull(listing.getVersion());
    }

    @Test
    public void normalize_emptyProductNameAndVersionName_noChanges() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .developer(Developer.builder()
                        .developerId(1L)
                        .name("New Developer 1")
                        .build())
                .product(Product.builder()
                        .name("")
                        .build())
                .version(ProductVersion.builder()
                        .version("")
                        .build())
                .build();
        normalizer.normalize(listing);

        assertNotNull(listing.getDeveloper());
        assertEquals(1L, listing.getDeveloper().getDeveloperId());
        assertEquals("New Developer 1", listing.getDeveloper().getName());
        assertNotNull(listing.getProduct());
        assertNull(listing.getProduct().getId());
        assertEquals("", listing.getProduct().getName());
        assertNotNull(listing.getVersion());
        assertNull(listing.getVersion().getId());
        assertEquals("", listing.getVersion().getVersion());
    }

    @Test
    public void normalize_hasProductNameNullVersion_findsProduct() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .developer(Developer.builder()
                        .developerId(1L)
                        .name("New Developer 1")
                        .build())
                .product(Product.builder()
                        .name("Test 1")
                        .build())
                .version(null)
                .build();
        Mockito.when(productDao.getByDeveloperAndName(ArgumentMatchers.anyLong(), ArgumentMatchers.anyString()))
        .thenReturn(Product.builder()
                .id(1L)
                .name("Test 1")
                .build());
        normalizer.normalize(listing);

        assertNotNull(listing.getDeveloper());
        assertEquals(1L, listing.getDeveloper().getDeveloperId());
        assertEquals("New Developer 1", listing.getDeveloper().getName());
        assertNotNull(listing.getProduct());
        assertEquals(1L, listing.getProduct().getId());
        assertEquals("Test 1", listing.getProduct().getName());
        assertNull(listing.getVersion());
    }

    @Test
    public void normalize_hasProductNameNullVersion_noProductFound() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .developer(Developer.builder()
                        .developerId(1L)
                        .name("New Developer 1")
                        .build())
                .product(Product.builder()
                        .name("Test 1")
                        .build())
                .version(null)
                .build();
        Mockito.when(productDao.getByDeveloperAndName(ArgumentMatchers.anyLong(), ArgumentMatchers.anyString()))
        .thenReturn(null);
        normalizer.normalize(listing);

        assertNotNull(listing.getDeveloper());
        assertEquals(1L, listing.getDeveloper().getDeveloperId());
        assertEquals("New Developer 1", listing.getDeveloper().getName());
        assertNotNull(listing.getProduct());
        assertNull(listing.getProduct().getId());
        assertEquals("Test 1", listing.getProduct().getName());
        assertNull(listing.getVersion());
    }

    @Test
    public void normalize_nullProductHasVersion_noChanges() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .developer(Developer.builder()
                        .developerId(1L)
                        .name("New Developer 1")
                        .build())
                .product(null)
                .version(ProductVersion.builder()
                        .version("1.0")
                        .build())
                .build();
        normalizer.normalize(listing);

        assertNotNull(listing.getDeveloper());
        assertEquals(1L, listing.getDeveloper().getDeveloperId());
        assertEquals("New Developer 1", listing.getDeveloper().getName());
        assertNull(listing.getProduct());
        assertNotNull(listing.getVersion());
        assertEquals("1.0", listing.getVersion().getVersion());
    }

    @Test
    public void normalize_hasProductNameHasVersion_findsProductAndVersion() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .developer(Developer.builder()
                        .developerId(1L)
                        .name("New Developer 1")
                        .build())
                .product(Product.builder()
                        .name("Test 1")
                        .build())
                .version(ProductVersion.builder()
                        .version("1.0")
                        .build())
                .build();
        Mockito.when(productDao.getByDeveloperAndName(ArgumentMatchers.anyLong(), ArgumentMatchers.anyString()))
        .thenReturn(Product.builder()
                .id(1L)
                .name("Test 1")
                .build());
        Mockito.when(versionDao.getByProductAndVersion(ArgumentMatchers.anyLong(), ArgumentMatchers.anyString()))
        .thenReturn(ProductVersionDTO.builder()
                .id(2L)
                .version("1.0")
                .build());
        normalizer.normalize(listing);

        assertNotNull(listing.getDeveloper());
        assertEquals(1L, listing.getDeveloper().getDeveloperId());
        assertEquals("New Developer 1", listing.getDeveloper().getName());
        assertNotNull(listing.getProduct());
        assertEquals(1L, listing.getProduct().getId());
        assertEquals("Test 1", listing.getProduct().getName());
        assertNotNull(listing.getVersion());
        assertEquals(2L, listing.getVersion().getId());
        assertEquals("1.0", listing.getVersion().getVersion());
    }

    @Test
    public void normalize_hasProductNameHasVersion_productNotFound() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .developer(Developer.builder()
                        .developerId(1L)
                        .name("New Developer 1")
                        .build())
                .product(Product.builder()
                        .name("Test 1")
                        .build())
                .version(ProductVersion.builder()
                        .version("1.0")
                        .build())
                .build();
        Mockito.when(productDao.getByDeveloperAndName(ArgumentMatchers.anyLong(), ArgumentMatchers.anyString()))
        .thenReturn(null);
        normalizer.normalize(listing);

        assertNotNull(listing.getDeveloper());
        assertEquals(1L, listing.getDeveloper().getDeveloperId());
        assertEquals("New Developer 1", listing.getDeveloper().getName());
        assertNotNull(listing.getProduct());
        assertNull(listing.getProduct().getId());
        assertEquals("Test 1", listing.getProduct().getName());
        assertNotNull(listing.getVersion());
        assertNull(listing.getVersion().getId());
        assertEquals("1.0", listing.getVersion().getVersion());
    }

    @Test
    public void normalize_hasProductNameHasVersion_versionNotFound() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .developer(Developer.builder()
                        .developerId(1L)
                        .name("New Developer 1")
                        .build())
                .product(Product.builder()
                        .name("Test 1")
                        .build())
                .version(ProductVersion.builder()
                        .version("1.0")
                        .build())
                .build();
        Mockito.when(productDao.getByDeveloperAndName(ArgumentMatchers.anyLong(), ArgumentMatchers.anyString()))
        .thenReturn(Product.builder()
                .id(1L)
                .name("Test 1")
                .build());
        Mockito.when(versionDao.getByProductAndVersion(ArgumentMatchers.anyLong(), ArgumentMatchers.anyString()))
        .thenReturn(null);
        normalizer.normalize(listing);

        assertNotNull(listing.getDeveloper());
        assertEquals(1L, listing.getDeveloper().getDeveloperId());
        assertEquals("New Developer 1", listing.getDeveloper().getName());
        assertNotNull(listing.getProduct());
        assertEquals(1L, listing.getProduct().getId());
        assertEquals("Test 1", listing.getProduct().getName());
        assertNotNull(listing.getVersion());
        assertNull(listing.getVersion().getId());
        assertEquals("1.0", listing.getVersion().getVersion());
    }
}
