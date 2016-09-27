package gov.healthit.chpl.app;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.dao.DeveloperDAO;
import gov.healthit.chpl.dao.ProductDAO;
import gov.healthit.chpl.domain.AggregateCount;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.dto.DeveloperDTO;
import gov.healthit.chpl.dto.ProductDTO;

@Component("parseActivities")
public class ParseActivities{
	private static final String DEFAULT_PROPERTIES_FILE = "environment.properties";
	private static final Logger logger = LogManager.getLogger(ParseActivities.class);
	private DeveloperDAO developerDAO;
	private CertifiedProductDAO certifiedProductDAO;
	private ProductDAO productDAO;
	private Email email;
	private static Date startDate;
	private static Date endDate;
	private static Integer numDaysInPeriod = 7;
	
	public ParseActivities(){
	}

	public static void main( String[] args ) throws Exception {
		ParseActivities parseActivities = new ParseActivities();
		parseActivities.setCommandLineArgs(args);
		Properties props = null;
		InputStream in = App.class.getClassLoader().getResourceAsStream(DEFAULT_PROPERTIES_FILE);
		
		// Load properties from Environment.properties
		if (in == null) {
			props = null;
			throw new FileNotFoundException("Environment Properties File not found in class path.");
		} else {
			props = new Properties();
			props.load(in);
			in.close();
		}
		
		props.put("mail.smtp.host", props.getProperty("smtpHost"));
		props.put("mail.smtp.port", props.getProperty("smtpPort"));
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable", "true");
		
		//set up data source context
		 LocalContext ctx = LocalContextFactory.createLocalContext(props.getProperty("dbDriverClass"));
		 ctx.addDataSource(props.getProperty("dataSourceName"),props.getProperty("dataSourceConnection"), 
				 props.getProperty("dataSourceUsername"), props.getProperty("dataSourcePassword"));
		 
		 //init spring classes
		 AbstractApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);
		 System.out.println(context.getClassLoader());

		 parseActivities.setDeveloperDAO((DeveloperDAO)context.getBean("developerDAO"));
		 parseActivities.setCertifiedProductDAO((CertifiedProductDAO)context.getBean("certifiedProductDAO"));
		 parseActivities.setProductDAO((ProductDAO)context.getBean("productDAO"));
		 parseActivities.setEmail((Email)context.getBean("email"));
		 
		 // get DTOs
		 List<DeveloperDTO> developerDTOs = parseActivities.developerDAO.findAll();
		 List<CertifiedProductDetailsDTO> certifiedProductDTOs = parseActivities.certifiedProductDAO.findAll();
		 List<ProductDTO> productDTOs = parseActivities.productDAO.findAll();
		 
		 // Populate CPs_2014 and CPs_2015
		 List<CertifiedProductDetailsDTO> certifiedProductDTOs_2014 = new ArrayList<CertifiedProductDetailsDTO>();
		 List<CertifiedProductDetailsDTO> certifiedProductDTOs_2015 = new ArrayList<CertifiedProductDetailsDTO>();
		 for(CertifiedProductDetailsDTO dto : certifiedProductDTOs){
			 if(dto.getYear() == "2014"){
				 certifiedProductDTOs_2014.add(dto);
			 }
			 else if(dto.getYear() == "2015"){
				 certifiedProductDTOs_2015.add(dto);
			 }
		 }
		 
		 // Get endDate for most recent week
		 Calendar calendarCounter = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		 calendarCounter.setTime(endDate);
		 

		 
		 // Get activities
		 List<ActivitiesOutput> activitiesList = new ArrayList<ActivitiesOutput>();
		 while(startDate.before(calendarCounter.getTime())){
			 Date counterDate = calendarCounter.getTime();
			 TimePeriod timePeriod = new TimePeriod(counterDate, numDaysInPeriod);

			 // Get aggregate count for developers
			 AggregateCount developerCount = new AggregateCount(developerDTOs);
			 Integer totalDevelopers = developerCount.getCountDuringPeriodUsingField(timePeriod.getStartDate(), timePeriod.getEndDate(), "creationDate");
			 // Get aggregate count for products
			 AggregateCount productCount = new AggregateCount(productDTOs);
			 Integer totalProducts = productCount.getCountDuringPeriodUsingField(timePeriod.getStartDate(), timePeriod.getEndDate(), "creationDate");
			 // Get aggregate count for certified products
			 AggregateCount certifiedProductCount = new AggregateCount(certifiedProductDTOs);
			 Integer totalCertifiedProducts = certifiedProductCount.getCountDuringPeriodUsingField(timePeriod.getStartDate(), timePeriod.getEndDate(), "creationDate");
			 // Get aggregate count for CPs_2014
			 AggregateCount certifiedProductCount_2014 = new AggregateCount(certifiedProductDTOs_2014);
			 Integer totalCertifiedProducts_2014 = certifiedProductCount_2014.getCountDuringPeriodUsingField(timePeriod.getStartDate(), timePeriod.getEndDate(), "creationDate");
			 // Get aggregate count for CPs_2015
			 AggregateCount certifiedProductCount_2015 = new AggregateCount(certifiedProductDTOs_2015);
			 Integer totalCertifiedProducts_2015 = certifiedProductCount_2015.getCountDuringPeriodUsingField(timePeriod.getStartDate(), timePeriod.getEndDate(), "creationDate");
			 
			 // Populate ActivitiesOutput
			 ActivitiesOutput activitiesOutput = new ActivitiesOutput();
			 activitiesOutput.setTotalDevelopers(totalDevelopers);
			 activitiesOutput.setTotalProducts(totalProducts);
			 activitiesOutput.setTotalCPs(totalCertifiedProducts);
			 activitiesOutput.setTotalCPs_2014(totalCertifiedProducts_2014);
			 activitiesOutput.setTotalCPs_2015(totalCertifiedProducts_2015);
			 
			 activitiesList.add(activitiesOutput);
			 
			 //table.generateTableDataRow(activitiesOutput, timePeriod);
			 
			 // decrement calendar by 7 days
			 calendarCounter.add(Calendar.DATE, -numDaysInPeriod);	 
		 } 
		 
		 // Generate comma separated data for table creation
		 List<String> commaSeparatedRowOutput = new ArrayList<String>();
		 for(ActivitiesOutput activity : activitiesList){
			 commaSeparatedRowOutput.add(activity.getTotalDevelopers() + "," + activity.getTotalProducts() + "," + activity.getTotalCPs() + "," + 
		 activity.getTotalCPs_2014() + "," + activity.getTotalCPs_2015());
		 }
		 
		 // set table headers
		 List<String> tableHeaders = new ArrayList<String>();
		 tableHeaders.add("Date");
		 tableHeaders.add("Total Developers");
		 tableHeaders.add("Total Products");
		 tableHeaders.add("Total CPs");
		 tableHeaders.add("Total 2014 CPs");
		 tableHeaders.add("Total 2015 CPs");
		 
		 Table table = new Table(commaSeparatedRowOutput, tableHeaders, '+', '-');

		 
		 //System.out.print(table.getTable().toString());
		 
		 // Send email
		 parseActivities.email.setEmailTo(props.getProperty("summaryEmail").toString().split(";"));
		 parseActivities.email.setEmailSubject("CHPL - Weekly Summary Statistics Report");
		 parseActivities.email.setEmailMessage(table.getTable().toString());
		 parseActivities.email.setProps(props);
		 parseActivities.email.sendSummaryEmail();
		 context.close();
	}
	
	private void setCommandLineArgs(String[] args) throws Exception{
		SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd");
		isoFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		Integer numArgs = args.length;
		switch(numArgs){
		case 0:
			throw new Exception("ParseActivities expects two or three command-line arguments: startDate, endDate and optionally numDaysInPeriod");
		case 1:
			throw new Exception("ParseActivities expects two or three command-line arguments: startDate, endDate and optionally numDaysInPeriod");
		case 2:
			try{
				ParseActivities.startDate = isoFormat.parse(args[0]);
				ParseActivities.endDate = isoFormat.parse(args[1]);
			}
			catch(ParseException e){
				throw new ParseException("Please enter startDate and endDate command-line arguments in the format of yyyy-MM-dd", e.getErrorOffset());
			}
			break;
		case 3:
			try{
				ParseActivities.startDate = isoFormat.parse(args[0]);
				ParseActivities.endDate = isoFormat.parse(args[1]);
			}
			catch(ParseException e){
				throw new ParseException("Please enter startDate and endDate command-line arguments in the format of yyyy-MM-dd", e.getErrorOffset());
			}
			try{
				Integer numDaysArg = Integer.parseInt(args[2]);
				ParseActivities.numDaysInPeriod = numDaysArg;
			} catch(NumberFormatException e){
				System.out.println("Third command line argument could not be parsed to integer. " + e.getMessage());
			}
			break;
		default:
			throw new Exception("ParseActivities expects two or three command-line arguments: startDate, endDate and optionally numDaysInPeriod");
		}
	}
	
	public DeveloperDAO getDeveloperDAO() {
		return developerDAO;
	}

	public void setDeveloperDAO(DeveloperDAO developerDAO) {
		this.developerDAO = developerDAO;
	}
	
	public CertifiedProductDAO getCertifiedProductDAO() {
		return certifiedProductDAO;
	}

	public void setCertifiedProductDAO(CertifiedProductDAO certifiedProductDAO) {
		this.certifiedProductDAO = certifiedProductDAO;
	}
	
	public ProductDAO getProductDAO() {
		return productDAO;
	}

	public void setProductDAO(ProductDAO productDAO) {
		this.productDAO = productDAO;
	}
	
	public Email getEmail(){
		return email;
	}
	
	public void setEmail(Email email){
		this.email = email;
	}

}
