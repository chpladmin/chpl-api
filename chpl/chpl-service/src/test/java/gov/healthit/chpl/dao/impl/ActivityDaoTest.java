package gov.healthit.chpl.dao.impl;

import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.domain.ActivityConcept;
import gov.healthit.chpl.dto.ActivityDTO;

import java.util.List;

import junit.framework.TestCase;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;



@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { gov.healthit.chpl.CHPLTestConfig.class })
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class,
    DirtiesContextTestExecutionListener.class,
    TransactionalTestExecutionListener.class,
    DbUnitTestExecutionListener.class })
@DatabaseSetup("classpath:data/testData.xml")
public class ActivityDaoTest extends TestCase {
	
	
	
	@Test
	public void testCreateActivity(){
		//create(ActivityDTO dto) 
	}
	
	@Test
	public void testUpdateActivity(){
		//update(ActivityDTO dto)
	}
	
	@Test
	public void testDelete(){
		// void delete(Long id) 
	}
	
	public void testGetById(){
		// ActivityDTO getById(Long id)
	}
	
	public void testFindAll(){
		//List<ActivityDTO> findAll();
	}
	
	public void findByObjectId(){
		//List<ActivityDTO> findByObjectId(Long objectId, ActivityConcept concept);
	}
	
	public void findByConcept(){
		//List<ActivityDTO> findByConcept(ActivityConcept concept);
	}
	
	public void findAllInLastNDays(){
		//List<ActivityDTO> findAllInLastNDays(Integer lastNDays);
	}
	
	public void findByObjectIdInLastNDays(){
		//List<ActivityDTO> findByObjectId(Long objectId, ActivityConcept concept, Integer lastNDays);
	}
	
	public void findByConceptInLastNDays(){
		//List<ActivityDTO> findByConcept(ActivityConcept concept, Integer lastNDays);
	}
	
}
