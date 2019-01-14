package gov.healthit.chpl.dao.impl;

import java.util.Collection;
import java.util.Date;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.transaction.annotation.Transactional;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;

import gov.healthit.chpl.auth.domain.Authority;
import gov.healthit.chpl.auth.permission.GrantedPermission;
import gov.healthit.chpl.auth.permission.UserPermissionRetrievalException;
import gov.healthit.chpl.auth.user.JWTAuthenticatedUser;
import gov.healthit.chpl.caching.UnitTestRules;
import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.dao.CertifiedProductSearchResultDAO;
import gov.healthit.chpl.dao.SurveillanceDAO;
import gov.healthit.chpl.dao.search.CertifiedProductSearchDAO;
import gov.healthit.chpl.domain.CertifiedProduct;
import gov.healthit.chpl.domain.Surveillance;
import gov.healthit.chpl.domain.SurveillanceRequirement;
import gov.healthit.chpl.domain.SurveillanceRequirementType;
import gov.healthit.chpl.domain.SurveillanceResultType;
import gov.healthit.chpl.domain.SurveillanceType;
import gov.healthit.chpl.domain.search.CertifiedProductBasicSearchResult;
import gov.healthit.chpl.domain.search.NonconformitySearchOptions;
import gov.healthit.chpl.domain.search.SearchRequest;
import gov.healthit.chpl.domain.search.SearchSetOperator;
import gov.healthit.chpl.domain.search.SurveillanceSearchFilter;
import gov.healthit.chpl.dto.CertifiedProductDTO;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;
import junit.framework.TestCase;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        gov.healthit.chpl.CHPLTestConfig.class
})
@TestExecutionListeners({
        DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class,
        TransactionalTestExecutionListener.class, DbUnitTestExecutionListener.class
})
@DatabaseSetup("classpath:data/testData.xml")
public class CertifiedProductSearchResultDaoTest extends TestCase {
    @Autowired
    private CertifiedProductSearchResultDAO searchResultDao;
    @Autowired
    private SurveillanceDAO survDao;
    @Autowired
    private CertifiedProductDAO cpDao;

    @Autowired
    private CertifiedProductSearchDAO searchDao;

    @Rule
    @Autowired
    public UnitTestRules cacheInvalidationRule;

    private static JWTAuthenticatedUser adminUser;

    @BeforeClass
    public static void setUpClass() throws Exception {
        adminUser = new JWTAuthenticatedUser();
        adminUser.setFullName("Administrator");
        adminUser.setId(-2L);
        adminUser.setFriendlyName("Administrator");
        adminUser.setSubjectName("admin");
        adminUser.getPermissions().add(new GrantedPermission("ROLE_ADMIN"));
    }

    @Test
    @Transactional
    public void testCountSearchResults() {

        SearchRequest searchRequest = new SearchRequest();
        searchRequest.setDeveloper("Test");
        int countProducts = searchDao.getTotalResultCount(searchRequest);
        assertEquals(12, countProducts);

        searchRequest.setVersion("1.0.0");
        int countProductsVersionSpecific = searchDao.getTotalResultCount(searchRequest);
        assertEquals(3, countProductsVersionSpecific);
    }

    @Test
    @Transactional
    public void testPageSizeIsCorrectFirstPage() {

        SearchRequest searchRequest = new SearchRequest();
        // there are 9 results here.
        searchRequest.setDeveloper("Test Developer 1");
        searchRequest.setPageNumber(0);
        searchRequest.setPageSize(5);
        Collection<CertifiedProductBasicSearchResult> products = searchDao.search(searchRequest);
        assertEquals(5, products.size());

        for (CertifiedProductBasicSearchResult dto : products) {
            assertTrue(dto.getDeveloper().startsWith("Test Developer 1"));
        }
    }

    @Test
    @Transactional
    public void testPageSizeIsCorrectNotFirstPage() {

        SearchRequest searchRequest = new SearchRequest();
        // there are 9 results here.
        searchRequest.setDeveloper("Test Developer 1");
        searchRequest.setPageNumber(1);
        searchRequest.setPageSize(3);
        Collection<CertifiedProductBasicSearchResult> products = searchDao.search(searchRequest);
        assertEquals(3, products.size());

        for (CertifiedProductBasicSearchResult dto : products) {
            assertTrue(dto.getDeveloper().startsWith("Test Developer 1"));
        }
    }

    @Test
    @Transactional
    public void testSearchDeveloper() {

        SearchRequest searchRequest = new SearchRequest();
        searchRequest.setDeveloper("Test Developer 1");
        Collection<CertifiedProductBasicSearchResult> products = searchDao.search(searchRequest);
        assertEquals(10, products.size());

        for (CertifiedProductBasicSearchResult dto : products) {
            assertTrue(dto.getDeveloper().startsWith("Test Developer 1"));
        }
    }

    @Test
    @Transactional
    public void testSearchDeveloperAndOrder() {
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.setDeveloper("Test Developer 1");
        searchRequest.setOrderBy(SearchRequest.ORDER_BY_DEVELOPER);
        Collection<CertifiedProductBasicSearchResult> products = searchDao.search(searchRequest);
        assertEquals(10, products.size());
    }

    @Test
    @Transactional
    public void testSearchProduct() {

        SearchRequest searchRequest = new SearchRequest();

        searchRequest.setProduct("Test Product 1");
        Collection<CertifiedProductBasicSearchResult> products = searchDao.search(searchRequest);
        assertEquals(6, products.size());

        for (CertifiedProductBasicSearchResult dto : products) {
            assertTrue(dto.getProduct().startsWith("Test Product 1"));
        }

    }

    @Test
    @Transactional
    public void testSearchVersion() {

        SearchRequest searchRequest = new SearchRequest();
        searchRequest.setVersion("1.0.1");
        Collection<CertifiedProductBasicSearchResult> products = searchDao.search(searchRequest);
        assertEquals(2, products.size());

        for (CertifiedProductBasicSearchResult dto : products) {
            assertTrue(dto.getVersion().startsWith("1.0.1"));
        }

    }

    @Test
    @Transactional
    public void testSearchCertificationEdition() {

        SearchRequest searchRequest = new SearchRequest();
        searchRequest.getCertificationEditions().add("2014");
        Collection<CertifiedProductBasicSearchResult> products = searchDao.search(searchRequest);
        assertEquals(3, products.size());

        for (CertifiedProductBasicSearchResult dto : products) {
            assertTrue(dto.getEdition().startsWith("2014"));
        }
    }

    @Test
    @Transactional
    public void testSearchCertificationBody() {

        SearchRequest searchRequest = new SearchRequest();
        searchRequest.getCertificationBodies().add("InfoGard");
        Collection<CertifiedProductBasicSearchResult> products = searchDao.search(searchRequest);
        assertEquals(7, products.size());

        for (CertifiedProductBasicSearchResult dto : products) {
            assertTrue(dto.getAcb().startsWith("InfoGard"));
        }

    }

    @Test
    @Transactional
    public void testSearchPracticeType() {

        SearchRequest searchRequest = new SearchRequest();
        searchRequest.setPracticeType("Ambulatory");
        Collection<CertifiedProductBasicSearchResult> products = searchDao.search(searchRequest);
        assertEquals(7, products.size());

        for (CertifiedProductBasicSearchResult dto : products) {
            assertTrue(dto.getPracticeType().startsWith("Ambulatory"));
        }

    }

    @Test
    @Transactional
    public void testSearchVisibleOnCHPL() {

        SearchRequest searchRequest = new SearchRequest();
        Collection<CertifiedProductBasicSearchResult> products = searchDao.search(searchRequest);
        assertEquals(18, products.size());
    }

    @Test
    @Transactional
    @Rollback(true)
    public void testSearchActiveSurveillanceWithoutNonconformities()
            throws EntityRetrievalException, UserPermissionRetrievalException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        Surveillance surv = new Surveillance();

        CertifiedProductDTO cpDto = cpDao.getById(1L);
        CertifiedProduct cp = new CertifiedProduct();
        cp.setId(cpDto.getId());
        cp.setChplProductNumber(cp.getChplProductNumber());
        cp.setEdition(cp.getEdition());
        surv.setCertifiedProduct(cp);
        surv.setStartDate(new Date());
        surv.setRandomizedSitesUsed(10);
        SurveillanceType type = survDao.findSurveillanceType("Randomized");
        surv.setType(type);

        SurveillanceRequirement req = new SurveillanceRequirement();
        req.setRequirement("170.314 (a)(1)");
        SurveillanceRequirementType reqType = survDao.findSurveillanceRequirementType("Certified Capability");
        req.setType(reqType);
        SurveillanceResultType resType = survDao.findSurveillanceResultType("No Non-Conformity");
        req.setResult(resType);

        surv.getRequirements().add(req);
        surv.setAuthority(Authority.ROLE_ADMIN);

        Long insertedId = survDao.insertSurveillance(surv);
        assertNotNull(insertedId);
        SecurityContextHolder.getContext().setAuthentication(null);

        SearchRequest searchRequest = new SearchRequest();
        SurveillanceSearchFilter survFilter = new SurveillanceSearchFilter();
        survFilter.setHasHadSurveillance(Boolean.TRUE);
        searchRequest.setSurveillance(survFilter);
        Collection<CertifiedProductBasicSearchResult> products = searchDao.search(searchRequest);
        assertEquals(2, products.size());

        searchRequest = new SearchRequest();
        survFilter = new SurveillanceSearchFilter();
        survFilter.setHasHadSurveillance(Boolean.FALSE);
        searchRequest.setSurveillance(survFilter);
        products = searchDao.search(searchRequest);
        assertEquals(9, products.size());

        searchRequest = new SearchRequest();
        survFilter = new SurveillanceSearchFilter();
        survFilter.getNonconformityOptions().add(NonconformitySearchOptions.CLOSED_NONCONFORMITY);
        searchRequest.setSurveillance(survFilter);
        products = searchDao.search(searchRequest);
        assertEquals(1, products.size());

        searchRequest = new SearchRequest();
        survFilter = new SurveillanceSearchFilter();
        survFilter.getNonconformityOptions().add(NonconformitySearchOptions.OPEN_NONCONFORMITY);
        searchRequest.setSurveillance(survFilter);
        products = searchDao.search(searchRequest);
        assertEquals(1, products.size());

        searchRequest = new SearchRequest();
        survFilter = new SurveillanceSearchFilter();
        survFilter.getNonconformityOptions().add(NonconformitySearchOptions.OPEN_NONCONFORMITY);
        survFilter.getNonconformityOptions().add(NonconformitySearchOptions.CLOSED_NONCONFORMITY);
        survFilter.setNonconformityOptionsOperator(SearchSetOperator.AND);
        searchRequest.setSurveillance(survFilter);
        products = searchDao.search(searchRequest);
        assertEquals(1, products.size());
    }

    @Test
    @Transactional
    public void testSearch() {

        SearchRequest searchRequest = new SearchRequest();

        searchRequest.setSearchTerm("Test");
        searchRequest.setDeveloper("Test Developer 1");
        searchRequest.setProduct("Test");
        searchRequest.setVersion("2.0");
        searchRequest.getCertificationEditions().add("2014");
        searchRequest.getCertificationBodies().add("InfoGard");
        searchRequest.setPracticeType("Ambulatory");
        searchRequest.setOrderBy("product");
        searchRequest.setSortDescending(true);
        searchRequest.setPageNumber(0);

        Collection<CertifiedProductBasicSearchResult> products = searchDao.search(searchRequest);
        assertEquals(1, products.size());

    }

    @Test
    @Transactional
    public void testFetchSingleItem() {

        try {
            CertifiedProductDetailsDTO product = searchResultDao.getById(1L);

            assertEquals(-1, product.getCertificationBodyId().intValue());
            assertEquals("InfoGard", product.getCertificationBodyName());
            assertEquals("CHP-024050", product.getChplProductNumber());
            assertEquals(2, product.getCertificationEditionId().intValue());
            assertEquals("Test Developer 1", product.getDeveloper().getName());
            assertEquals(6, product.getCountCertifications().intValue());
            assertEquals(1, product.getCountCqms().intValue());

        } catch (EntityRetrievalException e) {
            fail("EntityRetrievalException");
        }

    }
}
