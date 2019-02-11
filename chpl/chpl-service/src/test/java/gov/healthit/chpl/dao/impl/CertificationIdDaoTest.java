package gov.healthit.chpl.dao.impl;

import java.util.ArrayList;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
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

import gov.healthit.chpl.caching.UnitTestRules;
import gov.healthit.chpl.dao.CertificationIdDAO;
import gov.healthit.chpl.domain.SimpleCertificationId;
import gov.healthit.chpl.domain.SimpleCertificationIdWithProducts;
import gov.healthit.chpl.dto.CertificationIdAndCertifiedProductDTO;
import gov.healthit.chpl.exception.EntityCreationException;
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
public class CertificationIdDaoTest extends TestCase {

    @Autowired
    private CertificationIdDAO ehrDao;

    @Rule
    @Autowired
    public UnitTestRules cacheInvalidationRule;

    @Test
    @Transactional
    @Rollback
    public void testCreateEhrCertificationWithSingleProduct() throws EntityCreationException, EntityRetrievalException {
        List<Long> ids = new ArrayList<Long>();
        ids.add(1L);
        ehrDao.create(ids, "2014");

        List<SimpleCertificationId> results = ehrDao.getAllCertificationIdsWithProducts();
        assertNotNull(results);
        assertEquals(1, results.size());
        SimpleCertificationId result = results.get(0);
        assertTrue(result instanceof SimpleCertificationIdWithProducts);
        SimpleCertificationIdWithProducts resultCasted = (SimpleCertificationIdWithProducts) result;
        assertNotNull(resultCasted.getProducts());
        assertTrue(resultCasted.getProducts().contains("CHP-024050"));
    }

    @Test
    @Transactional
    @Rollback
    public void testCreateEhrCertificationWithMultipleProducts()
            throws EntityCreationException, EntityRetrievalException {
        List<Long> ids = new ArrayList<Long>();
        ids.add(1L);
        ids.add(2L);
        ehrDao.create(ids, "2014");

        List<SimpleCertificationId> results = ehrDao.getAllCertificationIdsWithProducts();
        assertNotNull(results);
        assertEquals(1, results.size());
        SimpleCertificationId result = results.get(0);
        assertTrue(result instanceof SimpleCertificationIdWithProducts);
        SimpleCertificationIdWithProducts resultCasted = (SimpleCertificationIdWithProducts) result;
        assertNotNull(resultCasted.getProducts());
        assertTrue(resultCasted.getProducts().contains("CHP-024050"));
        assertTrue(resultCasted.getProducts().contains("CHP-024051"));
    }
}
