package gov.healthit.chpl.app;

import gov.healthit.chpl.dao.CertificationCriterionDAO;
import gov.healthit.chpl.dao.CertificationEditionDAO;
import gov.healthit.chpl.dao.CertificationStatusEventDAO;
import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.dao.search.CertifiedProductSearchDAO;
import gov.healthit.chpl.dao.statistics.ChartDataDAO;
import gov.healthit.chpl.dao.statistics.ListingStatisticsDAO;
import gov.healthit.chpl.domain.CertifiedProduct;
import gov.healthit.chpl.domain.DateRange;
import gov.healthit.chpl.domain.search.CertifiedProductFlatSearchResult;
import gov.healthit.chpl.dto.CertificationStatusEventDTO;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.dto.ChartDataDTO;
import gov.healthit.chpl.manager.CertifiedProductDetailsManager;
import gov.healthit.chpl.manager.CertifiedProductManager;
import gov.healthit.chpl.manager.CertifiedProductSearchManager;
import gov.healthit.chpl.manager.ChartDataManager;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.stereotype.Component;

@Component("generateDailyChartData")
public class GenerateDailyChartData {
	private static final String DEFAULT_PROPERTIES_FILE = "environment.properties";
	private static final Logger logger = LogManager.getLogger(GenerateDailyChartData.class);
	private ListingStatisticsDAO listingStatisticsDao;
	private ChartDataDAO chartDataDao;
	private CertificationStatusEventDAO certificationStatusEventDao;
	private CertificationEditionDAO certificationEditionDao;
	
	public GenerateDailyChartData(){}

	public static void main(String[] args) throws Exception {
		//read in properties - we need these to set up the data source context
		Properties props = null;
		InputStream in = DownloadableResourceCreatorApp.class.getClassLoader().getResourceAsStream(DEFAULT_PROPERTIES_FILE);

		if (in == null) {
			props = null;
			throw new FileNotFoundException("Environment Properties File not found in class path.");
		} else {
			props = new Properties();
			props.load(in);
			in.close();
		}

		//set up data source context
		LocalContext ctx = LocalContextFactory.createLocalContext(props.getProperty("dbDriverClass"));
		ctx.addDataSource(props.getProperty("dataSourceName"),props.getProperty("dataSourceConnection"), 
				props.getProperty("dataSourceUsername"), props.getProperty("dataSourcePassword"));

		//init spring classes
		AbstractApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);
		GenerateDailyChartData app = new GenerateDailyChartData();
		app.setListingStatisticsDao((ListingStatisticsDAO)context.getBean("listingStatisticsDAO"));
		app.setChartDataDao((ChartDataDAO)context.getBean("chartDataDAO"));
		app.setCertificationStatusEventDao((CertificationStatusEventDAO)context.getBean("certificationStatusEventDAO"));
		app.setCertificationEditionDao((CertificationEditionDAO)context.getBean("certificationEditionDAO"));
		List<List<String>> doubleList = new ArrayList<List<String>>();
		LocalDate begin = LocalDate.of(2016, Month.APRIL, 1);
		LocalDate end = LocalDate.now();
		LocalDate current = begin;
		while(current.isBefore(end) || current.isEqual(end)){
			Date date1 = java.sql.Date.valueOf(begin);
			Date date2 = java.sql.Date.valueOf(current);
			DateRange dateRange = new DateRange(date1,date2);
			Long uniqueProductsOverTime = app.getListingStatisticsDao().getTotalCertifiedProducts(dateRange);
			Long uniqueProductsActive = app.getListingStatisticsDao().getTotalCPsActiveListings(dateRange);
			Long uniqueProductsActive2014 = app.getListingStatisticsDao().getTotalActive2014Listings(dateRange);
			Long uniqueProductsActive2015 = app.getListingStatisticsDao().getTotalActive2015Listings(dateRange);
			List<String> array = new ArrayList<String>();
			array.add(current.toString());
			array.add(uniqueProductsOverTime.toString());
			array.add(uniqueProductsActive.toString());
			array.add(uniqueProductsActive2014.toString());
			array.add(uniqueProductsActive2015.toString());
			current = current.plusDays(1);
			doubleList.add(array);
		}
		System.out.println(doubleList);
		Date date1 = java.sql.Date.valueOf(begin);
		List<Long> certifiedIdsWithChangedHistory = app.getListingStatisticsDao().getProductsWithChangedCertificationStatusPostApril2016(date1);
		System.out.println(certifiedIdsWithChangedHistory.size());
		for(Long id : certifiedIdsWithChangedHistory){
			List<CertificationStatusEventDTO> events = app.getCertificationStatusEventDao().findByCertifiedProductId(id);
			for(int i=0;i<events.size()-1;i++){
				if(events.get(i).getEventDate().after(date1)){
					Date beginDate = events.get(i).getCreationDate();
					Date endDate = events.get(i+1).getCreationDate();
					// if going from status 1 to something else decrement all dates in between begin and end
					if(events.get(i).getStatus().getId().equals(1L)){
						for(List<String> list : doubleList){
							if(java.sql.Date.valueOf(list.get(0)).after(beginDate)
									&& java.sql.Date.valueOf(list.get(0)).before(endDate)){
								String number = list.get(3);
								int num = Integer.getInteger(number);
								String newNum = String.valueOf(num-1);
								list.set(3, newNum);
								if(app.getCertificationEditionDao().getById(id).getYear().equals("2014")){
									String number2 = list.get(4);
									int num2 = Integer.getInteger(number2);
									String newNum2 = String.valueOf(num2-1);
									list.set(4, newNum2);
								}else{
									String number2 = list.get(5);
									int num2 = Integer.getInteger(number2);
									String newNum2 = String.valueOf(num2-1);
									list.set(5, newNum2);
								}
							}
						}
						// if going from some other status to 1 increment all dates in between begin and end
					}else if(events.get(i+1).getStatus().getId().equals(1L)){
						for(List<String> list : doubleList){
							if(java.sql.Date.valueOf(list.get(0)).after(beginDate)
									&& java.sql.Date.valueOf(list.get(0)).before(endDate)){
								String number = list.get(3);
								int num = Integer.getInteger(number);
								String newNum = String.valueOf(num+1);
								list.set(3, newNum);
								if(app.getCertificationEditionDao().getById(id).getYear().equals("2014")){
									String number2 = list.get(4);
									int num2 = Integer.getInteger(number2);
									String newNum2 = String.valueOf(num2-1);
									list.set(4, newNum2);
								}else{
									String number2 = list.get(5);
									int num2 = Integer.getInteger(number2);
									String newNum2 = String.valueOf(num2-1);
									list.set(5, newNum2);
								}
							}
						}
					}
				}
			}
		}
		System.out.println(doubleList);
		ChartDataDTO chartData = new ChartDataDTO();
		chartData.setDataDate(new Date());
		chartData.setJsonDataObject(doubleList.toString());
		chartData.setLastModifiedUser(1L);
		chartData.setTypeOfStatId(1L);
		app.getChartDataDao().create(chartData);
		
	}
	
	public CertificationEditionDAO getCertificationEditionDao() {
		return certificationEditionDao;
	}

	public void setCertificationEditionDao(
			CertificationEditionDAO certificationEditionDao) {
		this.certificationEditionDao = certificationEditionDao;
	}

	public CertificationStatusEventDAO getCertificationStatusEventDao() {
		return certificationStatusEventDao;
	}

	public void setCertificationStatusEventDao(
			CertificationStatusEventDAO certificationStatusEventDao) {
		this.certificationStatusEventDao = certificationStatusEventDao;
	}

	public ListingStatisticsDAO getListingStatisticsDao() {
		return listingStatisticsDao;
	}

	public void setListingStatisticsDao(ListingStatisticsDAO listingStatisticsDao) {
		this.listingStatisticsDao = listingStatisticsDao;
	}

	public ChartDataDAO getChartDataDao() {
		return chartDataDao;
	}

	public void setChartDataDao(ChartDataDAO chartDataDao) {
		this.chartDataDao = chartDataDao;
	}
	
}
