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
	private String commaSeparatedOutput;
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
		 parseActivities.developerDTOs = parseActivities.developerDAO.findAll();
		 parseActivities.certifiedProductDTOs = parseActivities.certifiedProductDAO.findAll();
		 parseActivities.productDTOs = parseActivities.productDAO.findAll();
		 parseActivities.setCertifiedProductDetailsDTOs();
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
			 
			 SimpleDateFormat dateFormat = new SimpleDateFormat("E MMM dd yyyy");
			 dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
			 String dateString = dateFormat.format(counterDate);
			 
			 ActivitiesOutput activitiesOutput = new ActivitiesOutput(dateString, totalDevelopers, totalProducts, totalCertifiedProducts, 
					 totalCertifiedProducts_2014, totalCertifiedProducts_2015);
			 
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
			 
			 SimpleDateFormat dateFormat = new SimpleDateFormat("E MMM dd yyyy");
			 dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
			 String dateString = dateFormat.format(counterDate);

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
	public Table getFormattedTable(List<ActivitiesOutput> activitiesList, List<TableHeader> tableHeaders) throws Exception{
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
			throw new Exception("ParseActivities expects two or three command-line arguments: startDate, endDate and optionally numDaysInPeriod");
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
		 email.setEmailSubject("CHPL - Weekly Summary Statistics Report");
		 email.setEmailMessage(summaryOutputTable.getTable());
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
		
		tableHeaders = new LinkedList<TableHeader>();
		tableHeaders.addAll(Arrays.asList(dateHeader, totalDevsHeader, totalProdsHeader, totalCPsHeader, totalCPs2014Header, totalCPs2015Header));
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

	public Email getEmail() {
		return email;
	}

	public void setEmail(Email email) {
		this.email = email;
	}

}
