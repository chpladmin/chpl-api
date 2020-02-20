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
import gov.healthit.chpl.dao.AddressDAO;
import gov.healthit.chpl.dto.AddressDTO;
import gov.healthit.chpl.entity.AddressEntity;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
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
public class AddressDaoTest extends TestCase {

    @Autowired
    private AddressDAO addressDao;

    @Rule
    @Autowired
    public UnitTestRules cacheInvalidationRule;

    @Test
    @Transactional
    public void getAllAddresses() {
        List<AddressDTO> results = addressDao.findAll();
        assertNotNull(results);
        assertEquals(2, results.size());
    }

    @Test
    @Transactional
    public void getAddressById() throws EntityRetrievalException {
        AddressDTO result = addressDao.getById(-1L);
        assertNotNull(result);
        assertTrue(result.getId() == -1L);
    }

    @Test
    public void getAddressByDeveloperId() {
        Long developerId = -1L;
        AddressDTO result = null;
        try {
            result = addressDao.getById(developerId);
        } catch (EntityRetrievalException ex) {
            fail("Could not find address with the id");
        }
        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals(-1, result.getId().longValue());
    }

    @Test
    public void updateAddress() throws EntityRetrievalException {
        AddressDTO toUpdate = addressDao.getById(-1L);
        toUpdate.setCity("Annapolis");
        addressDao.update(toUpdate);
        toUpdate = addressDao.getById(-1L);
        assertNotNull(toUpdate);
        assertEquals("Annapolis", toUpdate.getCity());
    }

    @Test
    @Transactional
    @Rollback
    public void createAddress() {
        AddressDTO newAddress = new AddressDTO();
        newAddress.setStreetLineOne("800 Frederick Road");
        newAddress.setCity("Catonsville");
        newAddress.setState("MD");
        newAddress.setZipcode("21228");
        newAddress.setCountry("USA");
        newAddress.setLastModifiedUser(-2L);
        newAddress.setCreationDate(new Date());
        newAddress.setLastModifiedDate(new Date());
        newAddress.setDeleted(false);

        AddressEntity result = null;
        try {
            result = addressDao.create(newAddress);
        } catch (EntityRetrievalException ex) {
            fail("retrieval exception");
        } catch (EntityCreationException crex) {
            fail("creation exception");
        }

        assertNotNull(result);
        assertNotNull(result.getId());

        // try to look up the created thing
        try {
            AddressDTO inserted = addressDao.getById(result.getId());
            assertNotNull(inserted);
        } catch (EntityRetrievalException ex) {
            fail("could not find address with id " + result.getId());
        }
    }
}
