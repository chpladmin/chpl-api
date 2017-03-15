package gov.healthit.chpl.app;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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
import gov.healthit.chpl.dao.SurveillanceDAO;
import gov.healthit.chpl.domain.AggregateCount;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.dto.DeveloperDTO;
import gov.healthit.chpl.dto.ProductDTO;
import gov.healthit.chpl.entity.SurveillanceEntity;
import gov.healthit.chpl.entity.SurveillanceNonconformityEntity;

@Component("parseActivities")
public class ParseActivities{
	private static final String DEFAULT_PROPERTIES_FILE = "environment.properties";
	private static final Logger logger = LogManager.getLogger(ParseActivities.class);
	private Email email;
	public DeveloperDAO developerDAO;
	public CertifiedProductDAO certifiedProductDAO;
	public ProductDAO productDAO;
	public SurveillanceDAO surveillanceDAO;
	private Date startDate;
	private Date endDate;
	private Integer numDaysInPeriod;
	private Integer numDaysInSummaryEmail;
	private Properties props;
	private LocalContext ctx;
	private TableHeader dateHeader;
	private TableHeader totalDevsHeader;
	private TableHeader totalProdsHeader;
	private TableHeader totalCPsHeader;
	private TableHeader totalCPs2014Header;
	private TableHeader totalCPs2015Header;
	private TableHeader totalNumSurvActivitiesHeader;
	private TableHeader numOpenSurvActivitiesHeader;
	private TableHeader numClosedSurvActivitiesHeader;
	private TableHeader totalNumNonConformitiesHeader;
	private TableHeader numOpenNonConformitiesHeader;
	private TableHeader numClosedNonConformitiesHeader;
	private List<TableHeader> tableHeaders;
	public List<ActivitiesOutput> activitiesList;
	private CSV csv;
	private String commaSeparatedOutput;
	private List<ActivitiesOutput> summaryActivitiesList;
	private Table summaryOutputTable;
	private TimePeriod summaryTimePeriod;
	public List<CertifiedProductDetailsDTO> certifiedProductDTOs_2014;
	public List<CertifiedProductDetailsDTO> certifiedProductDTOs_2015;
	public List<DeveloperDTO> developerDTOs;
	public List<CertifiedProductDetailsDTO> certifiedProductDTOs;
	public List<CertifiedProductDetailsDTO> certifiedProductDTOsWithSurv;
	public List<SurveillanceEntity> surveillanceEntities;
	public List<SurveillanceEntity> surveillanceOpenEntities;
	public List<SurveillanceEntity> surveillanceClosedEntities;
	public List<SurveillanceNonconformityEntity> surveillanceNonConformityEntities;
	public List<SurveillanceNonconformityEntity> surveillanceOpenNonConformityEntities;
	public List<SurveillanceNonconformityEntity> surveillanceClosedNonConformityEntities;
	public List<ProductDTO> productDTOs;
	private List<File> files;
	private Integer totalNumSurvActivities;
	private Integer numOpenSurvActivities;
	private Integer numClosedSurvActivities;
	private Integer totalNumNonConformities;
	private Integer numOpenNonConformities;
	private Integer numClosedNonConformities;
	private Integer totalDevelopers;
	private Integer totalProducts;
	private Integer totalCPs;
	private Integer totalCPs2014;
	private Integer totalCPs2015;
	
	public ParseActivities(){
	}

	/**
	 * This application generates a weekly summary email with an attached CSV providing aggregate counts of key CHPL metrics
	 * @param args
	 * @throws Exception
	 */
	public static void main( String[] args ) throws Exception {
		ParseActivities parseActivities = new ParseActivities();
		
		parseActivities.setCommandLineArgs(args);
		InputStream in = ParseActivities.class.getClassLoader().getResourceAsStream(DEFAULT_PROPERTIES_FILE);
		parseActivities.loadProperties(in);
		parseActivities.loadEmailProperties();
		parseActivities.ctx = LocalContextFactory.createLocalContext(parseActivities.props.getProperty("dbDriverClass"));
		parseActivities.ctx.addDataSource(parseActivities.props.getProperty("dataSourceName"),
				parseActivities.props.getProperty("dataSourceConnection"), 
				 parseActivities.props.getProperty("dataSourceUsername"), parseActivities.props.getProperty("dataSourcePassword"));
		 AbstractApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);
		 parseActivities.initializeSpringClasses(context);
		 parseActivities.setNumDaysInSummaryEmail(parseActivities.getNumDaysInSummaryEmail());
		 parseActivities.setSummaryTimePeriod(parseActivities.getSummaryTimePeriod());
		 parseActivities.developerDTOs = parseActivities.developerDAO.findAllIncludingDeleted();
		 parseActivities.certifiedProductDTOs = parseActivities.certifiedProductDAO.findAll();
		 parseActivities.surveillanceEntities = parseActivities.surveillanceDAO.getAllSurveillance();
		 parseActivities.surveillanceNonConformityEntities = parseActivities.surveillanceDAO.getAllSurveillanceNonConformities();
		 parseActivities.productDTOs = parseActivities.productDAO.findAllIncludingDeleted();
		 parseActivities.updateSurveillanceEntities();
		 parseActivities.setCertifiedProductDetailsDTOs();
		 parseActivities.updateCounts();
		 parseActivities.setActivitiesList(parseActivities.getActivitiesByPeriodUsingStartAndEndDate());
		 parseActivities.setTableHeaders(parseActivities.getTableHeaders());
		 parseActivities.setCommaSeparatedOutput(parseActivities.getCommaSeparatedOutput());
		 parseActivities.setCSV(parseActivities.getCSV(parseActivities.props.getProperty("downloadFolderPath").toString() + File.separator + "summaryCounts.csv"));
		 parseActivities.setSummaryActivities(parseActivities.getSummaryActivities());
		 parseActivities.setSummaryOutputTable(parseActivities.getFormattedTable(parseActivities.summaryActivitiesList, 
				 parseActivities.tableHeaders));
		 parseActivities.setFiles(parseActivities.getFiles());
		 parseActivities.setEmailProperties();
		 parseActivities.email.sendEmail(parseActivities.email.getEmailTo(), parseActivities.email.getEmailSubject(), 
						 parseActivities.email.getEmailMessage(), 
						  parseActivities.email.getProps(), parseActivities.email.getFiles());
		 logger.info("Completed ParseActivities execution.");
		 context.close();
	}
	
	/**
	 * Gets a list of ActivitiesOutput for the ParseActivities object.
	 * Requires setting the following first: endDate, summaryTimePeriod, developerDTOs, productDTOs, certifiedProductDTOs, 
	 * certifiedProductDTOs_2014, certifiedProductDTOs_2015, activitiesList, and numDaysInEmail
	 * @param parseActivities
	 * @return
	 * @throws ParseException 
	 */
	public List<ActivitiesOutput> getSummaryActivities() throws ParseException{
		Calendar calendarCounter = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		calendarCounter.setTime(endDate);
		
		activitiesList = new LinkedList<ActivitiesOutput>();
		
		while(summaryTimePeriod.getStartDate().before(calendarCounter.getTime())){
			 Date counterDate = summaryTimePeriod.getEndDate();

			 // Get aggregate count for developers
			 AggregateCount developerCount = new AggregateCount(developerDTOs);
			 Integer totalDevelopers = developerCount.getCountDuringPeriod(startDate, 
					 summaryTimePeriod.getEndDate(), "creationDate", "lastModifiedDate", "deleted");
			 // Get aggregate count for products
			 AggregateCount productCount = new AggregateCount(productDTOs);
			 Integer totalProducts = productCount.getCountDuringPeriod(startDate, 
					 summaryTimePeriod.getEndDate(), "creationDate", "lastModifiedDate", "deleted");
			 // Get aggregate count for certified products
			 AggregateCount certifiedProductCount = new AggregateCount(certifiedProductDTOs);
			 Integer totalCertifiedProducts = certifiedProductCount.getCountDuringPeriodUsingField(startDate, 
					 summaryTimePeriod.getEndDate(), "creationDate");
			 // Get aggregate count for CPs_2014
			 AggregateCount certifiedProductCount_2014 = new AggregateCount(certifiedProductDTOs_2014);
			 Integer totalCertifiedProducts_2014 = certifiedProductCount_2014.getCountDuringPeriodUsingField(startDate, 
					 summaryTimePeriod.getEndDate(), "creationDate");
			 // Get aggregate count for CPs_2015
			 AggregateCount certifiedProductCount_2015 = new AggregateCount(certifiedProductDTOs_2015);
			 Integer totalCertifiedProducts_2015 = certifiedProductCount_2015.getCountDuringPeriodUsingField(startDate, 
					 summaryTimePeriod.getEndDate(), "creationDate");
			 
			 AggregateCount numSurvActivitiesCount = new AggregateCount(surveillanceEntities);
			 Integer totalNumSurvActivities = numSurvActivitiesCount.getCountDuringPeriodUsingField(startDate, 
					 summaryTimePeriod.getEndDate(), "creationDate");
			 
			 AggregateCount numOpenSurvActivitiesCount = new AggregateCount(certifiedProductDTOsWithSurv);
			 Integer totalNumOpenSurvActivities = numOpenSurvActivitiesCount.getCountDuringPeriodUsingField(startDate, 
					 summaryTimePeriod.getEndDate(), "creationDate");
			 
			 AggregateCount numClosedSurvActivitiesCount = new AggregateCount(certifiedProductDTOsWithSurv);
			 Integer totalNumClosedSurvActivities = numClosedSurvActivitiesCount.getCountDuringPeriodUsingField(startDate, 
					 summaryTimePeriod.getEndDate(), "creationDate");
			 
			 AggregateCount numNonConformitiesCount = new AggregateCount(certifiedProductDTOsWithSurv);
			 Integer totalNumNonConformities = numNonConformitiesCount.getCountDuringPeriodUsingField(startDate, 
					 summaryTimePeriod.getEndDate(), "creationDate");
			 
			 AggregateCount numOpenNonConformitiesCount = new AggregateCount(certifiedProductDTOsWithSurv);
			 Integer totalNumOpenNonConformities = numOpenNonConformitiesCount.getCountDuringPeriodUsingField(startDate, 
					 summaryTimePeriod.getEndDate(), "creationDate");
			 
			 AggregateCount numClosedNonConformitiesCount = new AggregateCount(certifiedProductDTOsWithSurv);
			 Integer totalNumClosedNonConformities = numClosedNonConformitiesCount.getCountDuringPeriodUsingField(startDate, 
					 summaryTimePeriod.getEndDate(), "creationDate");
			 
			 SimpleDateFormat dateFormat = new SimpleDateFormat("E MMM dd yyyy");
			 dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
			 String dateString = dateFormat.format(counterDate);
			 
			 ActivitiesOutput activitiesOutput = new ActivitiesOutput(dateString, totalDevelopers, totalProducts, totalCertifiedProducts, 
					 totalCertifiedProducts_2014, totalCertifiedProducts_2015, totalNumSurvActivities, totalNumOpenSurvActivities, totalNumClosedSurvActivities, 
					 totalNumNonConformities, totalNumOpenNonConformities, totalNumClosedNonConformities);
			 
			 // Add an activitiesOutput record to activitiesList
			 activitiesList.add(activitiesOutput);
			 
			 // decrement calendar by 7 days
			 calendarCounter.add(Calendar.DATE, -numDaysInSummaryEmail);	 
		 } 
		return activitiesList;
	}
	
	/**
	 * Gets a list of ActivitiesOutput using DTOs with all non-deleted data. 
	 * Uses command-line startDate, endDate, and numDaysInPeriod arguments to return only data within the start & end date
	 * Each ActivitiesOutput object is associated with a date
	 * Results are serialized (stored in order) in a linked list
	 * @param developerDTOs
	 * @param productDTOs
	 * @param certifiedProductDTOs
	 * @param certifiedProductDTOs_2014
	 * @param certifiedProductDTOs_2015
	 * @return
	 * @throws ParseException 
	 */
	public List<ActivitiesOutput> getActivitiesByPeriodUsingStartAndEndDate() throws ParseException{
		 Calendar calendarCounter = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		 calendarCounter.setTime(endDate); 
		 
		List<ActivitiesOutput> activitiesList = new LinkedList<ActivitiesOutput>();
		 while(startDate.before(calendarCounter.getTime())){
			 Date counterDate = calendarCounter.getTime();
			 TimePeriod timePeriod = new TimePeriod(startDate, counterDate);

			 // Get aggregate count for developers
			 AggregateCount developerCount = new AggregateCount(developerDTOs);
			 Integer totalDevelopers = developerCount.getCountDuringPeriod(timePeriod.getStartDate(), timePeriod.getEndDate(), "creationDate", "lastModifiedDate", "deleted");
			 // Get aggregate count for products
			 AggregateCount productCount = new AggregateCount(productDTOs);
			 Integer totalProducts = productCount.getCountDuringPeriod(timePeriod.getStartDate(), timePeriod.getEndDate(), "creationDate", "lastModifiedDate", "deleted");
			 // Get aggregate count for certified products
			 AggregateCount certifiedProductCount = new AggregateCount(certifiedProductDTOs);
			 Integer totalCertifiedProducts = certifiedProductCount.getCountDuringPeriodUsingField(timePeriod.getStartDate(), timePeriod.getEndDate(), "creationDate");
			 // Get aggregate count for CPs_2014
			 AggregateCount certifiedProductCount_2014 = new AggregateCount(certifiedProductDTOs_2014);
			 Integer totalCertifiedProducts_2014 = certifiedProductCount_2014.getCountDuringPeriodUsingField(timePeriod.getStartDate(), timePeriod.getEndDate(), "creationDate");
			 // Get aggregate count for CPs_2015
			 AggregateCount certifiedProductCount_2015 = new AggregateCount(certifiedProductDTOs_2015);
			 Integer totalCertifiedProducts_2015 = certifiedProductCount_2015.getCountDuringPeriodUsingField(timePeriod.getStartDate(), timePeriod.getEndDate(), "creationDate");
			 
			 AggregateCount survActs = new AggregateCount(this.surveillanceEntities);
			 Integer totalSurvActs = survActs.getCountDuringPeriodUsingField(timePeriod.getStartDate(), timePeriod.getEndDate(), "creationDate");
			 
			 AggregateCount survOpenActs = new AggregateCount(this.surveillanceOpenEntities);
			 Integer totalSurvOpenActs = survOpenActs.getCountDuringPeriodUsingField(timePeriod.getStartDate(), timePeriod.getEndDate(), "creationDate");
			 
			 AggregateCount survClosedActs = new AggregateCount(this.surveillanceClosedEntities);
			 Integer totalSurvClosedActs = survClosedActs.getCountDuringPeriodUsingField(timePeriod.getStartDate(), timePeriod.getEndDate(), "creationDate");
			 
			 AggregateCount ncs = new AggregateCount(this.surveillanceNonConformityEntities);
			 Integer totalNcs = ncs.getCountDuringPeriodUsingField(timePeriod.getStartDate(), timePeriod.getEndDate(), "creationDate");
			 
			 AggregateCount openNcs = new AggregateCount(this.surveillanceOpenNonConformityEntities);
			 Integer totalOpenNcs = openNcs.getCountDuringPeriodUsingField(timePeriod.getStartDate(), timePeriod.getEndDate(), "creationDate");
			 
			 AggregateCount closedNcs = new AggregateCount(this.surveillanceClosedNonConformityEntities);
			 Integer totalClosedNcs = closedNcs.getCountDuringPeriodUsingField(timePeriod.getStartDate(), timePeriod.getEndDate(), "creationDate");
			 
			 SimpleDateFormat dateFormat = new SimpleDateFormat("E MMM dd yyyy");
			 dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
			 String dateString = dateFormat.format(counterDate);

			 ActivitiesOutput activitiesOutput = new ActivitiesOutput(dateString, totalDevelopers, totalProducts, totalCertifiedProducts, 
					 totalCertifiedProducts_2014, totalCertifiedProducts_2015, totalSurvActs, totalSurvOpenActs, totalSurvClosedActs, 
					 totalNcs, totalOpenNcs, totalClosedNcs);
			 
			 // Add an activitiesOutput record to activitiesList
			 activitiesList.add(activitiesOutput);
			 
			 // decrement calendar by 7 days
			 calendarCounter.add(Calendar.DATE, -numDaysInPeriod);	 
		 } 
		 return activitiesList;
	}
	
	/**
	 * Gets a formatted Table object using a list of ActivitiesOutput and list of table headers
	 * @param activitiesList
	 * @param tableHeaders
	 * @return
	 * @throws Exception
	 */
	public Table getFormattedTable(List<ActivitiesOutput> activitiesList, List<TableHeader> tableHeaders) throws Exception{
		 // Generate key-value map for table creation
		 Map<String, String> tableRows = new LinkedHashMap<String, String>();
		 char fieldDelimiter = ',';
		 for (ActivitiesOutput activity : activitiesList){
			 tableRows.put(activity.getDate(), activity.getTotalDevelopers().toString() + fieldDelimiter + activity.getTotalProducts().toString()
					 + fieldDelimiter + activity.getTotalCPs().toString() + fieldDelimiter + activity.getTotalCPs_2014().toString() + fieldDelimiter + 
					 activity.getTotalCPs_2015().toString() + fieldDelimiter + activity.getTotalNumSurvActivities().toString() + fieldDelimiter + 
					 activity.getNumOpenSurvActivities() + fieldDelimiter + activity.getNumClosedSurvActivities() + fieldDelimiter + 
					 activity.getTotalNumNonConformities() + fieldDelimiter + activity.getNumOpenNonConformities() + fieldDelimiter + activity.getNumClosedNonConformities());
		 }
		 
		 // Prepare table formatting constructor arguments
		 String htmlPreText = "<pre><br>";
		 String htmlPostText = "</br></pre>";
		 char columnSeparator = '|';
		 
		 // Initialize table justification
		 Boolean isLeftJustified = true;
		 Justification justification = new Justification(isLeftJustified, !isLeftJustified);
		 
		// Initialize table formatting
		 TableFormatting tableFormatting = new TableFormatting(htmlPreText, htmlPostText, columnSeparator, fieldDelimiter, justification);
		 
		// Initialize table outline
		 char startChar = '+';
		 char middleChars = '-';
		 TableOutline tableOutline = new TableOutline(startChar, middleChars, tableHeaders, tableFormatting);
		 
		 // Initialize table header line
		 TableHeaderLine tableHeaderLine = new TableHeaderLine(tableHeaders, tableFormatting);
		 
		 // Initialize table row
		 TableRow tableRow = new TableRow(tableRows, tableFormatting, tableHeaders);
		 
		 // Table(TableHeaderLine tableHeaderLine, TableOutline tableOutline, TableFormatting tableFormatting, TableRow tableRows)
		 Table table = new Table(tableHeaderLine, tableOutline, tableRow);
		 return table;
	}
	
	/**
	 * Sets the startDate, endDate, and numDaysInPeriod using the command-line arguments
	 * @param args
	 * @param parseActivities
	 * @throws Exception
	 */
	public void setCommandLineArgs(String[] args) throws Exception{
		SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd");
		isoFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		Integer numArgs = args.length;
		switch(numArgs){
		case 0:
		case 1:
			throw new Exception("ParseActivities expects two or three command-line arguments: startDate, endDate and optionally numDaysInPeriod");
		case 2:
			try{
				startDate = isoFormat.parse(args[0]);
				endDate = isoFormat.parse(args[1]);
				numDaysInPeriod = 7;
			}
			catch(ParseException e){
				throw new ParseException("Please enter startDate and endDate command-line arguments in the format of yyyy-MM-dd", e.getErrorOffset());
			}
			break;
		case 3:
			try{
				startDate = isoFormat.parse(args[0]);
				endDate = isoFormat.parse(args[1]);
				numDaysInPeriod = 7;
			}
			catch(ParseException e){
				throw new ParseException("Please enter startDate and endDate command-line arguments in the format of yyyy-MM-dd", e.getErrorOffset());
			}
			try{
				Integer numDaysArg = Integer.parseInt(args[2]);
				numDaysInPeriod = numDaysArg;
			} catch(NumberFormatException e){
				System.out.println("Third command line argument could not be parsed to integer. " + e.getMessage());
			}
			break;
		default:
			throw new Exception("ParseActivities expects two or three command-line arguments: startDate, endDate and optionally numDaysInPeriod");
		}
	}
	
	/**
	 * Get relevant beans
	 * @param parseActivities
	 * @param context
	 */
	public void initializeSpringClasses(AbstractApplicationContext context){
		 System.out.println(context.getClassLoader());

		 setDeveloperDAO((DeveloperDAO)context.getBean("developerDAO"));
		 setCertifiedProductDAO((CertifiedProductDAO)context.getBean("certifiedProductDAO"));
		 setProductDAO((ProductDAO)context.getBean("productDAO"));
		 setSurveillanceDAO((SurveillanceDAO)context.getBean("surveillanceDAO"));
		 setEmail((Email)context.getBean("email"));
	}
	
	/**
	 * Set the ParseActivities.Properties (props) to prepare for sending an email
	 * @param parseActivities
	 * @return
	 */
	public Properties loadEmailProperties(){
		props.put("mail.smtp.host", props.getProperty("smtpHost"));
		props.put("mail.smtp.port", props.getProperty("smtpPort"));
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable", "true");
		return props;
	}
	
	/**
	 * Set the ParseActivities.Properties (props) using an InputStream to get all properties from the InputStream
	 * @param parseActivities
	 * @param in
	 * @return
	 * @throws IOException
	 */
	public Properties loadProperties(InputStream in) throws IOException{
		if (in == null) {
			props = null;
			throw new FileNotFoundException("Environment Properties File not found in class path.");
		} else {
			props = new Properties();
			props.load(in);
			in.close();
		}
		return props;
	}
	
	/**
	 * Gets a CSV using a list of comma separated output strings and a fileName
	 * @param parseActivities
	 * @param fileName
	 * @return
	 * @throws FileNotFoundException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	public CSV getCSV(String fileName) 
			throws FileNotFoundException, IllegalArgumentException, IllegalAccessException{
			 File csvFile = new File(fileName);
			 CSV csv = new CSV(commaSeparatedOutput.toString(), csvFile); 
			 return csv;
	}

	/**
	 * Gets the summary email TimePeriod (with start & end date) using a config property set in ParseActivities.numDaysInSummaryEmail.
	 * @param parseActivities
	 * @return
	 */
	public TimePeriod getSummaryTimePeriod() {
		Calendar calendarCounter = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		calendarCounter.setTime(endDate);
		Date summaryEndDate = calendarCounter.getTime();
		calendarCounter.add(Calendar.DATE, -numDaysInSummaryEmail);
		Date summaryStartDate = calendarCounter.getTime();
		// Reset calendarCounter to endDate
		calendarCounter.add(Calendar.DATE, numDaysInSummaryEmail);
		return new TimePeriod(summaryStartDate, summaryEndDate);
	}

	/**
	 * Uses the ParseActivities Properties (props) object to get the summaryEmailPeriodInDays. Defaults to 7
	 * @param parseActivities
	 * @return
	 */
	public Integer getNumDaysInSummaryEmail() {
		return Integer.parseInt(props.getProperty("summaryEmailPeriodInDays", "7"));
	}
	
	/**
	 * Sets the ParseActivities object's certifiedProductDTOs_2014 and certifiedProductDTOs_2015 using the certifiedProductDTOs and each dto's "year"
	 * @param parseActivities
	 */
	public void setCertifiedProductDetailsDTOs(){
		certifiedProductDTOs_2014 = new ArrayList<CertifiedProductDetailsDTO>();
		certifiedProductDTOs_2015 = new ArrayList<CertifiedProductDetailsDTO>();
		for(CertifiedProductDetailsDTO dto : certifiedProductDTOs){
			 if(dto.getYear().equals("2014")){
				 certifiedProductDTOs_2014.add(dto);
			 }
			 else if(dto.getYear().equals("2015")){
				 certifiedProductDTOs_2015.add(dto);
			 }
		 }
	}

	/**
	 * Gets a list of files with the parseActivities.csv
	 * @param parseActivities
	 * @return
	 */
	public List<File> getFiles() {
		List<File> files = new ArrayList<File>();
		 files.add(csv.getFile());
		return files;
	}

	/**
	 * Sets the email properties that are specific to the ParseActivities application
	 * @param parseActivities
	 */
	public void setEmailProperties(){
		 email.setEmailTo(props.getProperty("summaryEmail").toString().split(";"));
		 logger.info("Sending email to " + props.getProperty("summaryEmail").toString());
		 email.setEmailSubject("CHPL - Weekly Summary Statistics Report");
		 Calendar calendarCounter = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		 StringBuilder emailMessage = new StringBuilder();
				 emailMessage.append("<ul><li>Date: " + calendarCounter.getTime() + "</li>");
				 emailMessage.append("<li>Total Developers: " + this.totalDevelopers + "</li>");
				 emailMessage.append("<li>Total Products: " + this.totalProducts + "</li>");
				 emailMessage.append("<li>Total CPs: " + this.totalCPs + "</li>");
				 emailMessage.append("<li>Total 2014 CPs: " + this.totalCPs2014 + "</li>");
				 emailMessage.append("<li>Total 2015 CPs: " + this.totalCPs2015 + "</li>");
				 emailMessage.append("<li>Total Surveillance Activities: " + this.totalNumSurvActivities + "</li>");
				 emailMessage.append("<li>Total Open Surveillance Activities: " + this.numOpenSurvActivities + "</li>");
				 emailMessage.append("<li>Total Closed Surveillance Activities: " + this.numClosedSurvActivities + "</li>");
				 emailMessage.append("<li>Total Non-conformities: " + this.totalNumNonConformities + "</li>");
				 emailMessage.append("<li>Total Open Non-conformities: " + this.numOpenNonConformities + "</li>");
				 emailMessage.append("<li>Total Closed Non-conformities: " + this.numClosedNonConformities + "</li></ul>");
		 email.setEmailMessage(emailMessage.toString());
		 logger.info(emailMessage.toString());
		 email.setProps(props);
		 email.setFiles(files);
	}
	
	/**
	 * Defines ParseActivities table headers and puts them into a list<TableHeader> object that is returned
	 * @param parseActivities
	 * @return
	 */
	public List<TableHeader> getTableHeaders(){
		dateHeader = new TableHeader("Date", 17, String.class);
		totalDevsHeader = new TableHeader("Total Developers", 17, String.class);
		totalProdsHeader = new TableHeader("Total Products", 15, String.class);
		totalCPsHeader = new TableHeader("Total CPs", 10, String.class);
		totalCPs2014Header = new TableHeader("Total 2014 CPs", 15, String.class);
		totalCPs2015Header = new TableHeader("Total 2015 CPs", 15, String.class);
		totalNumSurvActivitiesHeader = new TableHeader("Total Surv Acts", 16, String.class);
		numOpenSurvActivitiesHeader = new TableHeader("Open Surv Acts", 15, String.class);
		numClosedSurvActivitiesHeader = new TableHeader("Closed Surv Acts", 17, String.class);
		totalNumNonConformitiesHeader = new TableHeader("Total NCs", 10, String.class);
		numOpenNonConformitiesHeader = new TableHeader("Open NCs", 9, String.class);
		numClosedNonConformitiesHeader = new TableHeader("Closed NCs", 11, String.class);
		
		tableHeaders = new LinkedList<TableHeader>();
		tableHeaders.addAll(Arrays.asList(dateHeader, totalDevsHeader, totalProdsHeader, totalCPsHeader, totalCPs2014Header, 
				totalCPs2015Header, totalNumSurvActivitiesHeader, numOpenSurvActivitiesHeader, numClosedSurvActivitiesHeader,
				totalNumNonConformitiesHeader, numOpenNonConformitiesHeader, numClosedNonConformitiesHeader));
		return tableHeaders;
	}
	
	/**
	 * Gets a string with comma separated output using the ParseActivities List<TableHeader> + the headerName and the activitiesList. 
	 * Combines the table header with the activitiesList and returns the result as a string.
	 * @param parseActivities
	 * @return
	 */
	public String getCommaSeparatedOutput(){
		 List<Field> fields = new ArrayList<Field>(ReflectiveHelper.getInheritedPrivateFields(ActivitiesOutput.class));
		 List<String> commaSeparatedTableHeaders = new LinkedList<String>(CSV.getCommaSeparatedList(tableHeaders, "headerName"));
		 List<String> commaSeparatedActivitiesOutput = new LinkedList<String>(CSV.getCommaSeparatedListWithFields(activitiesList, fields));
		 List<String> headerAndBody = new LinkedList<String>(commaSeparatedTableHeaders);
		 // Add newline to first record of activities output to separate header from rows
		 StringBuilder firstIndexString = new StringBuilder();
		 firstIndexString.append("\n" + commaSeparatedActivitiesOutput.get(0));
		 commaSeparatedActivitiesOutput.remove(0);
		 commaSeparatedActivitiesOutput.add(0, firstIndexString.toString());
		 headerAndBody.addAll(commaSeparatedActivitiesOutput);
		 return headerAndBody.toString().replace("[", "").replace("]", "");
	}
	 
	private void updateCounts(){
		this.totalNumSurvActivities = 0;
		this.numOpenSurvActivities = 0;
		this.numClosedSurvActivities = 0;
		this.totalNumNonConformities = 0;
		this.numOpenNonConformities = 0;
		this.numClosedNonConformities = 0;
		this.totalDevelopers = 0;
		this.totalProducts = 0;
		this.totalCPs = 0;
		this.totalCPs2014 = 0;
		this.totalCPs2015 = 0;
		if(this.certifiedProductDTOsWithSurv == null){
			this.certifiedProductDTOsWithSurv = certifiedProductDAO.findAll();
		}
		for(CertifiedProductDetailsDTO dto : certifiedProductDTOsWithSurv){
			// Get aggregate count for total # of surveillance activities
			if(dto.getCountSurveillance() > 0){
				this.totalNumSurvActivities += dto.getCountSurveillance();
			}
			// Get aggregate count for   # of open surveillance activities
			if(dto.getCountOpenSurveillance() > 0){
				this.numOpenSurvActivities += dto.getCountOpenSurveillance();
			}
			// Get aggregate count for   # of closed surveillance activities
			if(dto.getCountClosedSurveillance() > 0){
				this.numClosedSurvActivities += dto.getCountClosedSurveillance();
			}
			// Get aggregate count for  total # of non-conformities
			if((dto.getCountOpenNonconformities() + dto.getCountClosedNonconformities()) > 0){
				this.totalNumNonConformities += dto.getCountOpenNonconformities() + dto.getCountClosedNonconformities();
			}
			// Get aggregate count for   # of open NCs
			if(dto.getCountOpenNonconformities() > 0){
				this.numOpenNonConformities += dto.getCountOpenNonconformities();
			}
			// Get aggregate count for   # of closed NCs
			if(dto.getCountClosedNonconformities() > 0){
				this.numClosedNonConformities += dto.getCountClosedNonconformities();
			}
		}
		
		 for(DeveloperDTO dto : developerDTOs){
			 if(dto.getCreationDate().before(summaryTimePeriod.getEndDate())){
				 totalDevelopers++;
			 }
		 }
		 
		 for(ProductDTO dto: productDTOs){
			 if(dto.getCreationDate().before(summaryTimePeriod.getEndDate())){
				 totalProducts++;
			 }
		 }
		 
		 for(CertifiedProductDetailsDTO dto : certifiedProductDTOs){
			 if(dto.getCreationDate().before(summaryTimePeriod.getEndDate())){
				 this.totalCPs++;
			 }
		 }
		 
		 for(CertifiedProductDetailsDTO dto: certifiedProductDTOs_2014){
			 if(dto.getCreationDate().before(summaryTimePeriod.getEndDate())){
				 this.totalCPs2014++;
			 }
		 }
		 
		 for(CertifiedProductDetailsDTO dto: certifiedProductDTOs_2015){
			 if(dto.getCreationDate().before(summaryTimePeriod.getEndDate())){
				 this.totalCPs2015++;
			 }
		 }
	}
	
	private void updateSurveillanceEntities(){
		surveillanceOpenEntities = new ArrayList<SurveillanceEntity>();
		surveillanceClosedEntities = new ArrayList<SurveillanceEntity>();
		for(SurveillanceEntity entity : surveillanceEntities){
			if(entity.getSurveillanceTypeId() == 1){
				surveillanceOpenEntities.add(entity);
			}
			else if(entity.getSurveillanceTypeId() == 2){
				surveillanceClosedEntities.add(entity);
			}
		}
		
		surveillanceOpenNonConformityEntities = new ArrayList<SurveillanceNonconformityEntity>();
		surveillanceClosedNonConformityEntities = new ArrayList<SurveillanceNonconformityEntity>();
		
		for(SurveillanceNonconformityEntity entity : surveillanceNonConformityEntities){
			if(entity.getNonconformityStatusId() == 1){
				surveillanceOpenNonConformityEntities.add(entity);
			}
			else if(entity.getNonconformityStatusId() == 2){
				surveillanceClosedNonConformityEntities.add(entity);
			}
		}
	}
	
	public void setCommaSeparatedOutput(String commaSeparatedOutput){
		this.commaSeparatedOutput = commaSeparatedOutput;
	}
	
	public void setSummaryOutputTable(Table table){
		this.summaryOutputTable = table;
	}
	
	public void setActivitiesList(List<ActivitiesOutput> activitiesList){
		this.activitiesList = activitiesList;
	}
	
	public void setSummaryActivities(List<ActivitiesOutput> summaryActivitiesList){
		this.summaryActivitiesList = summaryActivitiesList;
	}
	
	public void setTableHeaders(List<TableHeader> tableHeaders){
		this.tableHeaders = tableHeaders;
	}
	
	public void setFiles(List<File> files) {
		this.files = files;
	}
	
	public void setNumDaysInSummaryEmail(Integer numDaysInSummaryEmail) {
		this.numDaysInSummaryEmail = numDaysInSummaryEmail;
	}
	
	public void setSummaryTimePeriod(TimePeriod summaryTimePeriod) {
		this.summaryTimePeriod = summaryTimePeriod;
	}
	
	public void setCSV(CSV csv){
		this.csv = csv;
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
	
	public void setSurveillanceDAO(SurveillanceDAO surveillanceDAO){
		this.surveillanceDAO = surveillanceDAO;
	}

	public Email getEmail() {
		return email;
	}

	public void setEmail(Email email) {
		this.email = email;
	}

}
