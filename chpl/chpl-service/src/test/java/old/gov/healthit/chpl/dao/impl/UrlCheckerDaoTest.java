package old.gov.healthit.chpl.dao.impl;

import java.util.Date;
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
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.scheduler.job.urlStatus.UrlCheckerDao;
import gov.healthit.chpl.scheduler.job.urlStatus.UrlResult;
import gov.healthit.chpl.scheduler.job.urlStatus.UrlType;
import junit.framework.TestCase;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        old.gov.healthit.chpl.CHPLTestConfig.class
})
@TestExecutionListeners({
        DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class,
        TransactionalTestExecutionListener.class, DbUnitTestExecutionListener.class
})
@DatabaseSetup("classpath:data/testData.xml")
public class UrlCheckerDaoTest extends TestCase {
    @Autowired
    private UrlCheckerDao urlCheckerDao;

    @Rule
    @Autowired
    public UnitTestRules cacheInvalidationRule;

    @Test
    @Transactional
    public void getAllSystemUrls() {
        List<UrlResult> allSystemUrls = urlCheckerDao.getAllSystemUrls();
        assertNotNull(allSystemUrls);
        assertTrue(allSystemUrls.size() > 0);
        assertEquals(7, allSystemUrls.size());
    }

    @Test
    @Transactional
    public void getAllUrlResults_Empty() throws EntityRetrievalException {
        List<UrlResult> allUrlResults = urlCheckerDao.getAllUrlResults();
        assertNotNull(allUrlResults);
        assertEquals(0, allUrlResults.size());
    }

    @Test
    @Transactional()
    @Rollback(true)
    public void createUrlResult() throws EntityCreationException {
        UrlResult toCreate = new UrlResult();
        toCreate.setLastChecked(new Date());
        toCreate.setResponseCode(200);
        toCreate.setUrl("http://test.com");
        toCreate.setUrlType(UrlType.DEVELOPER);
        UrlResult created = urlCheckerDao.createUrlResult(toCreate);
        assertNotNull(created);
        assertNotNull(created.getId());

        List<UrlResult> allUrlResults = urlCheckerDao.getAllUrlResults();
        assertNotNull(allUrlResults);
        assertEquals(1, allUrlResults.size());
        UrlResult result = allUrlResults.get(0);
        assertNotNull(result.getId());
        assertEquals(created.getId().longValue(), result.getId().longValue());
        assertEquals(200, result.getResponseCode().intValue());
        assertEquals("http://test.com", result.getUrl());
        assertEquals(UrlType.DEVELOPER, result.getUrlType());
    }

    @Test
    @Transactional()
    @Rollback(true)
    public void updateUrlResult() throws EntityRetrievalException, EntityCreationException {
        UrlResult toCreate = new UrlResult();
        toCreate.setLastChecked(new Date());
        toCreate.setResponseCode(200);
        toCreate.setUrl("http://test.com");
        toCreate.setUrlType(UrlType.DEVELOPER);
        UrlResult created = urlCheckerDao.createUrlResult(toCreate);
        assertNotNull(created);
        assertNotNull(created.getId());

        Date updatedLastCheckedDate = new Date();
        int updatedResponseCode = 401;
        created.setResponseCode(updatedResponseCode);
        created.setLastChecked(updatedLastCheckedDate);
        urlCheckerDao.updateUrlResult(created);

        List<UrlResult> allUrlResults = urlCheckerDao.getAllUrlResults();
        assertNotNull(allUrlResults);
        assertEquals(1, allUrlResults.size());
        UrlResult result = allUrlResults.get(0);
        assertNotNull(result.getId());
        assertEquals(created.getId().longValue(), result.getId().longValue());
        assertEquals(updatedLastCheckedDate.getTime(), result.getLastChecked().getTime());
        assertEquals(updatedResponseCode, result.getResponseCode().intValue());
    }

    @Test
    @Transactional()
    @Rollback(true)
    public void deleteUrlResult() throws EntityCreationException, EntityRetrievalException {
        UrlResult toCreate = new UrlResult();
        toCreate.setLastChecked(new Date());
        toCreate.setResponseCode(200);
        toCreate.setUrl("http://test.com");
        toCreate.setUrlType(UrlType.DEVELOPER);
        UrlResult created = urlCheckerDao.createUrlResult(toCreate);
        assertNotNull(created);
        assertNotNull(created.getId());

        urlCheckerDao.deleteUrlResult(created.getId());
        List<UrlResult> allUrlResults = urlCheckerDao.getAllUrlResults();
        assertNotNull(allUrlResults);
        assertEquals(0, allUrlResults.size());
    }
}
