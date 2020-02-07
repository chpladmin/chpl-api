package old.gov.healthit.chpl.dao.impl;

import static org.junit.Assert.assertEquals;

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
import gov.healthit.chpl.dao.CQMResultDetailsDAO;
import gov.healthit.chpl.dto.CQMResultDetailsDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        old.gov.healthit.chpl.CHPLTestConfig.class
})
@TestExecutionListeners({
        DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class,
        TransactionalTestExecutionListener.class, DbUnitTestExecutionListener.class
})
@DatabaseSetup("classpath:data/testData.xml")
public class CQMResultDetailsDaoTest {

    @Autowired
    CQMResultDetailsDAO cqmResultDetailsDAO;

    @Rule
    @Autowired
    public UnitTestRules cacheInvalidationRule;

    @Test
    @Transactional
    public void testGetCQMResultDetailsByCertifiedProductId() throws EntityRetrievalException {

        List<CQMResultDetailsDTO> dtos = cqmResultDetailsDAO.getCQMResultDetailsByCertifiedProductId(1L);

        assertEquals(4, dtos.size());
        assertEquals("0001", dtos.get(0).getNqfNumber());
        assertEquals(dtos.get(0).getSuccess(), false);
        assertEquals(dtos.get(0).getVersion(), null);

    }

}
