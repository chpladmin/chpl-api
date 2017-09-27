package gov.healthit.chpl.dao.impl;

import static org.junit.Assert.*;
import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dao.statistics.ChartDataDAO;
import gov.healthit.chpl.domain.ChartDataStatType;
import gov.healthit.chpl.dto.AddressDTO;
import gov.healthit.chpl.dto.ChartDataDTO;
import gov.healthit.chpl.dto.ChartDataStatTypeDTO;
import gov.healthit.chpl.entity.ChartDataEntity;
import gov.healthit.chpl.entity.ChartDataStatTypeEntity;

import java.text.ParseException;
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
	
	private String FAKE_CHART_DATA = "[[2017-09-01, 1],[2017-09-02, 2],[2017-09-03, 3],[2017-09-04, 4],[2017-09-05, 5]]";
	
	@Autowired
	private ChartDataDAO chartDataDao;
	
	@Test
	@Transactional
	public void getAllChartData() throws ParseException {
		List<ChartDataDTO> results = chartDataDao.findAllData();
		assertNotNull(results);
		assertNotNull(results.get(0).getDate());
		assertNotNull(results.get(0).getData());
		assertEquals(results.get(0).getData(), FAKE_CHART_DATA);
		assertEquals(1, results.size());
	}
	
	@Test
	@Transactional
	public void getChartDataById() throws EntityRetrievalException, ParseException {
		ChartDataDTO result = chartDataDao.getById(1L);
		assertNotNull(result);
	}
	
	@Test
	@Transactional
	public void createChartData() throws EntityRetrievalException, EntityCreationException, ParseException {
		ChartDataEntity chartDataEntity = new ChartDataEntity();
		chartDataEntity.setDataDate(new Date());
		chartDataEntity.setJsonDataObject("[[09-05-2017, 1]]");
		ChartDataStatTypeEntity statType = new ChartDataStatTypeEntity();
		statType.setId(1L);
		chartDataEntity.setTypeOfStatId(statType);
		ChartDataDTO chartData = new ChartDataDTO(chartDataEntity);
		ChartDataDTO toCreate = chartDataDao.create(chartData);
		assertNotNull(toCreate);
	}
	
	@Test
	@Transactional
	public void updateChartData() throws EntityRetrievalException, EntityCreationException, ParseException {
		ChartDataDTO chartData = chartDataDao.getById(1L);
		ChartDataStatTypeEntity statTypeEntity = new ChartDataStatTypeEntity();
		statTypeEntity.setId(2L);
		ChartDataStatTypeDTO statTypeDto = new ChartDataStatTypeDTO(statTypeEntity);
		chartData.setStatisticType(statTypeDto);
		chartData.setData("[[09-05-2017, 1]]");
		ChartDataDTO updated = chartDataDao.update(chartData);
		assertEquals(statTypeDto,updated.getStatisticType());
		assertEquals("[[09-05-2017, 1]]",updated.getData());
	}
	
	@Test
	@Transactional
	public void getAllDataTypes() throws EntityRetrievalException, EntityCreationException {
		List<ChartDataStatTypeDTO> results = chartDataDao.findAllTypes();
		assertNotNull(results);
	}
}
