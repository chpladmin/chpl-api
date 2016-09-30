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
import gov.healthit.chpl.domain.AggregateCount;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.dto.DeveloperDTO;
import gov.healthit.chpl.dto.ProductDTO;

@Component("parseActivities")
public class ParseActivities{
	private static final String DEFAULT_PROPERTIES_FILE = "environment.properties";
	private static final Logger logger = LogManager.getLogger(ParseActivities.class);
	private Email email;
	public DeveloperDAO developerDAO;
	public CertifiedProductDAO certifiedProductDAO;
	public ProductDAO productDAO;
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
	private List<TableHeader> tableHeaders;
	public List<ActivitiesOutput> activitiesList;
	private CSV csv;
	private List<String> tableHeaderFieldNames;
	private List<String> commaSeparatedOutput;
	private Map<List<ActivitiesOutput>, List<String>> activitiesOutputFieldsMap;
	private Map<List<TableHeader>, List<String>> tableHeaderFieldsMap;
	private List<ActivitiesOutput> summaryActivitiesList;
	private Table summaryOutputTable;
	private TimePeriod summaryTimePeriod;
	public List<CertifiedProductDetailsDTO> certifiedProductDTOs_2014;
	public List<CertifiedProductDetailsDTO> certifiedProductDTOs_2015;
	public List<DeveloperDTO> developerDTOs;
	public List<CertifiedProductDetailsDTO> certifiedProductDTOs;
	public List<ProductDTO> productDTOs;
	private List<File> files;
	
	public ParseActivities(){
	}

	/**
	 * This application generates a weekly summary email with an attached CSV providing aggregate counts of key CHPL metrics
	 * @param args
	 * @throws Exception
	 */
	public static void main( String[] args ) throws Exception {
		ParseActivities parseActivities = new ParseActivities();
		parseActivities.setCommandLineArgs(args, parseActivities);
		InputStream in = App.class.getClassLoader().getResourceAsStream(DEFAULT_PROPERTIES_FILE);
		parseActivities.loadProperties(parseActivities, in);
		parseActivities.loadEmailProperties(parseActivities);
		parseActivities.ctx = LocalContextFactory.createLocalContext(parseActivities.props.getProperty("dbDriverClass"));
		parseActivities.ctx.addDataSource(parseActivities.props.getProperty("dataSourceName"),parseActivities.props.getProperty("dataSourceConnection"), 
				 parseActivities.props.getProperty("dataSourceUsername"), parseActivities.props.getProperty("dataSourcePassword"));
		 AbstractApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);
		 parseActivities.initializeSpringClasses(parseActivities, context);
		 parseActivities.setNumDaysInSummaryEmail(parseActivities, parseActivities.getNumDaysInSummaryEmail(parseActivities));
		 parseActivities.setSummaryTimePeriod(parseActivities.getSummaryTimePeriod(parseActivities));
		 parseActivities.developerDTOs = parseActivities.developerDAO.findAll();
		 parseActivities.certifiedProductDTOs = parseActivities.certifiedProductDAO.findAll();
		 parseActivities.productDTOs = parseActivities.productDAO.findAll();
		 parseActivities.setCertifiedProductDetailsDTOs(parseActivities);
		 parseActivities.activitiesList = parseActivities.getActivitiesByPeriodUsingStartAndEndDate(parseActivities);
		 parseActivities.setTableHeaders(parseActivities.getTableHeaders(parseActivities));
		 parseActivities.setTableHeaderFieldNames(parseActivities.getTableHeaderFieldNames(parseActivities));
		 //parseActivities.setTableHeaderFieldsMap(parseActivities.getTableHeaderFieldsMap(parseActivities));
		 //parseActivities.setActivitiesOutputFieldsMap(parseActivities.getActivitiesOutputFieldsMap(parseActivities));
		 parseActivities.setCommaSeparatedOutput(parseActivities.getCommaSeparatedOutput(parseActivities));
		 parseActivities.setCSV(parseActivities.getCSV(parseActivities, "summaryCounts.csv"));
		 parseActivities.getSummaryActivities(parseActivities);
		 parseActivities.summaryOutputTable = parseActivities.getFormattedTable(parseActivities, parseActivities.summaryActivitiesList, parseActivities.tableHeaders);
		 parseActivities.setFiles(parseActivities.getFiles(parseActivities));
		 parseActivities.setEmailProperties(parseActivities);
		 parseActivities.email.sendEmail(parseActivities.email.getEmailTo(), parseActivities.email.getEmailSubject(), parseActivities.email.getEmailMessage(), 
				 parseActivities.email.getProps(), parseActivities.email.getFiles());
		 context.close();
	}
	
	public void getSummaryActivities(ParseActivities parseActivities){
		Calendar calendarCounter = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		calendarCounter.setTime(endDate);
		
		parseActivities.activitiesList = new LinkedList<ActivitiesOutput>();
		
		while(summaryTimePeriod.getStartDate().before(calendarCounter.getTime())){
			 Date counterDate = summaryTimePeriod.getEndDate();

			 // Get aggregate count for developers
			 AggregateCount developerCount = new AggregateCount(developerDTOs);
			 Integer totalDevelopers = developerCount.getCountDuringPeriodUsingField(startDate, summaryTimePeriod.getEndDate(), "creationDate");
			 // Get aggregate count for products
			 AggregateCount productCount = new AggregateCount(productDTOs);
			 Integer totalProducts = productCount.getCountDuringPeriodUsingField(startDate, summaryTimePeriod.getEndDate(), "creationDate");
			 // Get aggregate count for certified products
			 AggregateCount certifiedProductCount = new AggregateCount(certifiedProductDTOs);
			 Integer totalCertifiedProducts = certifiedProductCount.getCountDuringPeriodUsingField(startDate, summaryTimePeriod.getEndDate(), "creationDate");
			 // Get aggregate count for CPs_2014
			 AggregateCount certifiedProductCount_2014 = new AggregateCount(certifiedProductDTOs_2014);
			 Integer totalCertifiedProducts_2014 = certifiedProductCount_2014.getCountDuringPeriodUsingField(startDate, summaryTimePeriod.getEndDate(), "creationDate");
			 // Get aggregate count for CPs_2015
			 AggregateCount certifiedProductCount_2015 = new AggregateCount(certifiedProductDTOs_2015);
			 Integer totalCertifiedProducts_2015 = certifiedProductCount_2015.getCountDuringPeriodUsingField(startDate, summaryTimePeriod.getEndDate(), "creationDate");
			 
			 // Format date string: dow mon dd yyyy
			 String dateString = counterDate.toString().substring(0, 10) + " " + counterDate.toString().substring(24, 28);
			// Populate ActivitiesOutput
			 ActivitiesOutput activitiesOutput = new ActivitiesOutput(dateString, totalDevelopers, totalProducts, totalCertifiedProducts, 
					 totalCertifiedProducts_2014, totalCertifiedProducts_2015);
			 
			 // Add an activitiesOutput record to activitiesList
			 activitiesList.add(activitiesOutput);
			 
			 // decrement calendar by 7 days
			 calendarCounter.add(Calendar.DATE, -parseActivities.numDaysInSummaryEmail);	 
		 } 
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
	 */
	public List<ActivitiesOutput> getActivitiesByPeriodUsingStartAndEndDate(ParseActivities parseActivities){
		 Calendar calendarCounter = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		 calendarCounter.setTime(endDate); 
		 
		List<ActivitiesOutput> activitiesList = new LinkedList<ActivitiesOutput>();
		 while(startDate.before(calendarCounter.getTime())){
			 Date counterDate = calendarCounter.getTime();
			 TimePeriod timePeriod = new TimePeriod(startDate, counterDate);

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
			 
			 // Format date string: dow mon dd yyyy
			 String dateString = counterDate.toString().substring(0, 10) + " " + counterDate.toString().substring(24, 28);
			// Populate ActivitiesOutput
			 ActivitiesOutput activitiesOutput = new ActivitiesOutput(dateString, totalDevelopers, totalProducts, totalCertifiedProducts, 
					 totalCertifiedProducts_2014, totalCertifiedProducts_2015);
			 
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
	public Table getFormattedTable(ParseActivities parseActivities, List<ActivitiesOutput> activitiesList, List<TableHeader> tableHeaders) throws Exception{
		 // Generate key-value map for table creation
		 Map<String, String> tableRows = new LinkedHashMap<String, String>();
		 char fieldDelimiter = ',';
		 for (ActivitiesOutput activity : activitiesList){
			 tableRows.put(activity.getDate(), activity.getTotalDevelopers().toString() + fieldDelimiter + activity.getTotalProducts().toString()
					 + fieldDelimiter + activity.getTotalCPs().toString() + fieldDelimiter + activity.getTotalCPs_2014().toString() + fieldDelimiter + 
					 activity.getTotalCPs_2015().toString());
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
	
	public void setCommandLineArgs(String[] args, ParseActivities parseActivities) throws Exception{
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
				parseActivities.startDate = isoFormat.parse(args[0]);
				parseActivities.endDate = isoFormat.parse(args[1]);
				parseActivities.numDaysInPeriod = 7;
			}
			catch(ParseException e){
				throw new ParseException("Please enter startDate and endDate command-line arguments in the format of yyyy-MM-dd", e.getErrorOffset());
			}
			break;
		case 3:
			try{
				parseActivities.startDate = isoFormat.parse(args[0]);
				parseActivities.endDate = isoFormat.parse(args[1]);
				parseActivities.numDaysInPeriod = 7;
			}
			catch(ParseException e){
				throw new ParseException("Please enter startDate and endDate command-line arguments in the format of yyyy-MM-dd", e.getErrorOffset());
			}
			try{
				Integer numDaysArg = Integer.parseInt(args[2]);
				parseActivities.numDaysInPeriod = numDaysArg;
			} catch(NumberFormatException e){
				System.out.println("Third command line argument could not be parsed to integer. " + e.getMessage());
			}
			break;
		default:
			throw new Exception("ParseActivities expects two or three command-line arguments: startDate, endDate and optionally numDaysInPeriod");
		}
	}
	
	public void initializeSpringClasses(ParseActivities parseActivities, AbstractApplicationContext context){
		 System.out.println(context.getClassLoader());

		 parseActivities.setDeveloperDAO((DeveloperDAO)context.getBean("developerDAO"));
		 parseActivities.setCertifiedProductDAO((CertifiedProductDAO)context.getBean("certifiedProductDAO"));
		 parseActivities.setProductDAO((ProductDAO)context.getBean("productDAO"));
		 parseActivities.setEmail((Email)context.getBean("email"));
	}
	
	public Properties loadEmailProperties(ParseActivities parseActivities){
		props.put("mail.smtp.host", props.getProperty("smtpHost"));
		props.put("mail.smtp.port", props.getProperty("smtpPort"));
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable", "true");
		return props;
	}
	
	public Properties loadProperties(ParseActivities parseActivities, InputStream in) throws IOException{
		if (in == null) {
			parseActivities.props = null;
			throw new FileNotFoundException("Environment Properties File not found in class path.");
		} else {
			parseActivities.props = new Properties();
			parseActivities.props.load(in);
			in.close();
		}
		return parseActivities.props;
	}
	
	public CSV getCSV(ParseActivities parseActivities, String fileName) 
			throws FileNotFoundException, IllegalArgumentException, IllegalAccessException{
			 
			 // Create CSV with output
			 File csvFile = new File(fileName);
			 //File csvFile = new File("summaryCounts.csv");
			 CSV csv = new CSV(parseActivities.commaSeparatedOutput.toString(), csvFile); 
			 return csv;
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

	public Email getEmail() {
		return email;
	}

	public void setEmail(Email email) {
		this.email = email;
	}

	public TimePeriod getSummaryTimePeriod(ParseActivities parseActivities) {
		Calendar calendarCounter = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		calendarCounter.setTime(endDate);
		Date summaryEndDate = calendarCounter.getTime();
		calendarCounter.add(Calendar.DATE, -numDaysInSummaryEmail);
		Date summaryStartDate = calendarCounter.getTime();
		// Reset calendarCounter to endDate
		calendarCounter.add(Calendar.DATE, numDaysInSummaryEmail);
		return new TimePeriod(summaryStartDate, summaryEndDate);
	}

	public void setSummaryTimePeriod(TimePeriod summaryTimePeriod) {
		this.summaryTimePeriod = summaryTimePeriod;
	}

	public Integer getNumDaysInSummaryEmail(ParseActivities parseActivities) {
		return Integer.parseInt(parseActivities.props.getProperty("summaryEmailPeriodInDays", "7"));
	}

	public void setNumDaysInSummaryEmail(ParseActivities parseActivities, Integer numDaysInSummaryEmail) {
		parseActivities.numDaysInSummaryEmail = numDaysInSummaryEmail;
	}
	
	public void setCertifiedProductDetailsDTOs(ParseActivities parseActivities){
		certifiedProductDTOs_2014 = new ArrayList<CertifiedProductDetailsDTO>();
		certifiedProductDTOs_2015 = new ArrayList<CertifiedProductDetailsDTO>();
		for(CertifiedProductDetailsDTO dto : parseActivities.certifiedProductDTOs){
			 if(dto.getYear().equals("2014")){
				 certifiedProductDTOs_2014.add(dto);
			 }
			 else if(dto.getYear().equals("2015")){
				 certifiedProductDTOs_2015.add(dto);
			 }
		 }
	}

	public List<File> getFiles(ParseActivities parseActivities) {
		List<File> files = new ArrayList<File>();
		 files.add(parseActivities.csv.getFile());
		return files;
	}

	public void setFiles(List<File> files) {
		this.files = files;
	}
	
	public void setEmailProperties(ParseActivities parseActivities){
		 parseActivities.email.setEmailTo(parseActivities.props.getProperty("summaryEmail").toString().split(";"));
		 parseActivities.email.setEmailSubject("CHPL - Weekly Summary Statistics Report");
		 parseActivities.email.setEmailMessage(parseActivities.summaryOutputTable.getTable());
		 parseActivities.email.setProps(parseActivities.props);
		 parseActivities.email.setFiles(parseActivities.files);
	}
	
	public void setTableHeaders(List<TableHeader> tableHeaders){
		this.tableHeaders = tableHeaders;
	}
	
	public List<TableHeader> getTableHeaders(ParseActivities parseActivities){
		parseActivities.dateHeader = new TableHeader("Date", 17, String.class);
		parseActivities.totalDevsHeader = new TableHeader("Total Developers", 17, String.class);
		parseActivities.totalProdsHeader = new TableHeader("Total Products", 15, String.class);
		parseActivities.totalCPsHeader = new TableHeader("Total CPs", 10, String.class);
		parseActivities.totalCPs2014Header = new TableHeader("Total 2014 CPs", 15, String.class);
		parseActivities.totalCPs2015Header = new TableHeader("Total 2015 CPs", 15, String.class);
		
		parseActivities.tableHeaders = new LinkedList<TableHeader>();
		parseActivities.tableHeaders.addAll(Arrays.asList(dateHeader, totalDevsHeader, totalProdsHeader, totalCPsHeader, totalCPs2014Header, totalCPs2015Header));
		return parseActivities.tableHeaders;
	}
	
	public List<String> getTableHeaderFieldNames(ParseActivities parseActivities){
		return Arrays.asList(dateHeader.getHeaderName(), totalDevsHeader.getHeaderName(), totalProdsHeader.getHeaderName(), 
				 totalCPsHeader.getHeaderName(), totalCPs2014Header.getHeaderName(), totalCPs2015Header.getHeaderName());
	}
	
	public void setTableHeaderFieldNames(List<String> tableHeaderFieldNames){
		this.tableHeaderFieldNames = tableHeaderFieldNames;
	}
	
	public List<String> getCommaSeparatedOutput(ParseActivities parseActivities){
		List<Field> fields = new ArrayList<Field>(ReflectiveHelper.getInheritedPrivateFields(ActivitiesOutput.class));
		
		 List<String> commaSeparatedTableHeaders = new LinkedList<String>(CSV.getCommaSeparatedList(parseActivities.tableHeaders, "headerName"));
		 List<String> commaSeparatedActivitiesOutput = new LinkedList<String>(CSV.getCommaSeparatedListWithFields(parseActivities.activitiesList, fields));
		 List<String> headerAndBody = new LinkedList<String>(commaSeparatedTableHeaders);
		 headerAndBody.addAll(commaSeparatedActivitiesOutput);
		 return headerAndBody;
	}
	
	public void setCommaSeparatedOutput(List<String> commaSeparatedOutput){
		this.commaSeparatedOutput = commaSeparatedOutput;
	}
	
	public Map<List<TableHeader>, List<String>> getTableHeaderFieldsMap(ParseActivities parseActivities){
		Map<List<TableHeader>, List<String>> tableHeaderFieldsMap = new LinkedHashMap<List<TableHeader>, List<String>>();
		 tableHeaderFieldsMap.put(tableHeaders, tableHeaderFieldNames);
		return tableHeaderFieldsMap;
	}
	
	public void setTableHeaderFieldsMap(Map<List<TableHeader>, List<String>> tableHeaderFieldsMap){
		this.tableHeaderFieldsMap = tableHeaderFieldsMap;
	}
	
	public Map<List<ActivitiesOutput>, List<String>> getActivitiesOutputFieldsMap(ParseActivities parseActivities){
		 Map<List<ActivitiesOutput>, List<String>> activitiesOutputFieldsMap = new LinkedHashMap<List<ActivitiesOutput>, List<String>>();
		 activitiesOutputFieldsMap.put(activitiesList, tableHeaderFieldNames);
		 return activitiesOutputFieldsMap;
	}
	
	public void setActivitiesOutputFieldsMap(Map<List<ActivitiesOutput>, List<String>> activitiesOutputFieldsMap){
		this.activitiesOutputFieldsMap = activitiesOutputFieldsMap;
	}

}
