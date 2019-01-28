package gov.healthit.chpl.dao.impl;

import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
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
import gov.healthit.chpl.dao.DeveloperStatusDAO;
import gov.healthit.chpl.dto.DeveloperStatusDTO;
import gov.healthit.chpl.entity.developer.DeveloperStatusType;
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
public class DeveloperStatusDaoTest extends TestCase {

    @Autowired
    private DeveloperStatusDAO developerStatusDao;

    @Rule
    @Autowired
    public UnitTestRules cacheInvalidationRule;

    @Test
    @Transactional
    public void getAllDeveloperStatuses() {
        List<DeveloperStatusDTO> results = developerStatusDao.findAll();
        assertNotNull(results);
        assertEquals(3, results.size());
    }

    @Test
    @Transactional
    public void getDeveloperStatusById() {
        DeveloperStatusDTO result = developerStatusDao.getById(1L);
        assertNotNull(result);
        assertEquals(1, result.getId().longValue());
    }

    @Test
    @Transactional
    public void getDeveloperStatusByName() {
        DeveloperStatusDTO result = developerStatusDao.getByName(DeveloperStatusType.Active.toString());
        assertNotNull(result);
        assertEquals(DeveloperStatusType.Active.toString(), result.getStatusName());
    }
}
