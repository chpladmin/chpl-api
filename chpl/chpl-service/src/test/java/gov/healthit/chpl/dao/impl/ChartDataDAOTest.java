package gov.healthit.chpl.dao.impl;

import static org.junit.Assert.*;
import gov.healthit.chpl.dao.ChartDataDAO;
import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dto.AddressDTO;
import gov.healthit.chpl.dto.ChartDataDTO;
import gov.healthit.chpl.entity.ChartDataEntity;

import java.util.Date;
import java.util.List;

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

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { gov.healthit.chpl.CHPLTestConfig.class })
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class,
    DirtiesContextTestExecutionListener.class,
    TransactionalTestExecutionListener.class,
    DbUnitTestExecutionListener.class })
@DatabaseSetup("classpath:data/testData.xml")
public class ChartDataDAOTest {
	
	@Autowired
	private ChartDataDAO chartDataDao;
	
	@Test
	@Transactional
	public void getAllChartData() {
		List<ChartDataDTO> results = chartDataDao.findAll();
		assertNotNull(results);
		assertEquals(1, results.size());
	}
	
	@Test
	@Transactional
	public void getChartDataById() throws EntityRetrievalException {
		ChartDataDTO result = chartDataDao.getById(1L);
		assertNotNull(result);
	}
	
	@Test
	@Transactional
	public void createChartData() throws EntityRetrievalException, EntityCreationException {
		ChartDataEntity chartDataEntity = new ChartDataEntity();
		chartDataEntity.setDataDate(new Date());
		chartDataEntity.setId(2L);
		chartDataEntity.setJsonDataObject("[[09-05-2017, 1]]");
		chartDataEntity.setLastModifiedDate(new Date());
		chartDataEntity.setLastModifiedUser(-1L);
		chartDataEntity.setTypeOfStatId(1L);
		ChartDataDTO chartData = new ChartDataDTO(chartDataEntity);
		ChartDataDTO toCreate = chartDataDao.create(chartData);
		ChartDataDTO created = chartDataDao.getById(2L);
		assertNotNull(created);
		assertEquals(created.getJsonDataObject(), chartDataEntity.getJsonDataObject());
	}
}
