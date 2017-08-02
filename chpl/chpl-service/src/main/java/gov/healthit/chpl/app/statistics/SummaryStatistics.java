package gov.healthit.chpl.app.statistics;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.Future;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.app.AppConfig;
import gov.healthit.chpl.app.LocalContext;
import gov.healthit.chpl.app.LocalContextFactory;
import gov.healthit.chpl.app.NotificationEmailerReportApp;
import gov.healthit.chpl.auth.SendMailUtil;
import gov.healthit.chpl.auth.permission.GrantedPermission;
import gov.healthit.chpl.dao.NotificationDAO;
import gov.healthit.chpl.domain.DateRange;
import gov.healthit.chpl.domain.concept.NotificationTypeConcept;
import gov.healthit.chpl.domain.statistics.CertifiedBodyStatistics;
import gov.healthit.chpl.domain.statistics.Statistics;
import gov.healthit.chpl.dto.notification.RecipientWithSubscriptionsDTO;

@Component("summaryStatistics")
public class SummaryStatistics {
	private static final String DEFAULT_PROPERTIES_FILE = "environment.properties";
	private static final Logger logger = LogManager.getLogger(SummaryStatistics.class);
	private static Date startDate;
	private static Date endDate;
	private static Integer numDaysInPeriod;
	private Properties props;
	private AsynchronousStatisticsInitializor asynchronousStatisticsInitializor;
	private NotificationDAO notificationDao;
	
	public SummaryStatistics(){}

	/**
	 * This application generates a weekly summary email with an attached CSV providing CHPL statistics
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		SummaryStatistics summaryStats = new SummaryStatistics();
		summaryStats.parseCommandLineArgs(args); // sets startDate, endDate, numDaysInPeriod
		InputStream in = SummaryStatistics.class.getClassLoader().getResourceAsStream(DEFAULT_PROPERTIES_FILE);
		Properties props = summaryStats.loadProperties(in);
		LocalContext ctx = LocalContextFactory.createLocalContext(summaryStats.props.getProperty("dbDriverClass"));
		ctx.addDataSource(summaryStats.props.getProperty("dataSourceName"), summaryStats.props.getProperty("dataSourceConnection"), 
				 summaryStats.props.getProperty("dataSourceUsername"), summaryStats.props.getProperty("dataSourcePassword"));
		 AbstractApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);
		 summaryStats.initializeSpringClasses(context);
		 Future<Statistics> futureEmailBodyStats = summaryStats.asynchronousStatisticsInitializor.getStatistics(new DateRange(startDate, endDate), true);
		 Statistics emailBodyStats = futureEmailBodyStats.get();
		 List<Statistics> csvStats = new ArrayList<Statistics>();
		 Calendar calendarCounter = Calendar.getInstance(TimeZone.getTimeZone(ZoneOffset.UTC));
		 calendarCounter.setTime(startDate); 
		 calendarCounter.add(Calendar.DATE, numDaysInPeriod);
		 while(endDate.compareTo(calendarCounter.getTime()) >= 0){
			 logger.info("Getting csvRecord for start date " + startDate.toString() + " end date " + calendarCounter.getTime().toString());
			 DateRange csvRange = new DateRange(startDate, new Date(calendarCounter.getTimeInMillis()));
			 Statistics historyStat = new Statistics();
			 historyStat.setDateRange(csvRange);
			 Future<Statistics> futureEmailCsvStats = summaryStats.asynchronousStatisticsInitializor.getStatistics(csvRange, false);
			 historyStat = futureEmailCsvStats.get();
			 csvStats.add(historyStat);
			 logger.info("Finished getting csvRecord for start date " + startDate.toString() + " end date " + calendarCounter.getTime().toString());
			 calendarCounter.add(Calendar.DATE, numDaysInPeriod);
		 }
		 logger.info("Finished getting statistics");
		 StatsCsvFileWriter.writeCsvFile(props.getProperty("downloadFolderPath") + File.separator + props.getProperty("summaryEmailName", "summaryStatistics.csv"), csvStats);
		 List<File> files = new ArrayList<File>();
		 File csvFile = new File(props.getProperty("downloadFolderPath") + File.separator + props.getProperty("summaryEmailName", "summaryStatistics.csv"));
		 files.add(csvFile);
		 String htmlMessage = summaryStats.createHtmlMessage(emailBodyStats, files);
		 
		 //send the email
		 Set<GrantedPermission> permissions = new HashSet<GrantedPermission>();
		 permissions.add(new GrantedPermission("ROLE_ADMIN"));
		 List<RecipientWithSubscriptionsDTO> recipients = summaryStats.getNotificationDao().getAllNotificationMappingsForType(permissions, NotificationTypeConcept.WEEKLY_STATISTICS, null);
		 if(recipients != null && recipients.size() > 0) {
			 String[] emailAddrs = new String[recipients.size()];
			 for(int i = 0; i < recipients.size(); i++) {
				 RecipientWithSubscriptionsDTO recip = recipients.get(i);
				 emailAddrs[i] = recip.getEmail();
				 logger.info("Sending email to " + recip.getEmail());
			 }
			 SendMailUtil mailUtil = new SendMailUtil();
			 mailUtil.sendEmail(null, emailAddrs, props.getProperty("summaryEmailSubject").toString(), htmlMessage, files, props);
		 }
		 
		 logger.info("Completed SummaryStatistics execution.");
		 context.close();
	}
	
	/**
	 * Updates the startDate, endDate, and numDaysInPeriod using the command-line arguments
	 * @param args
	 * @param parseActivities
	 * @throws Exception
	 */
	private void parseCommandLineArgs(String[] args) throws Exception{
		SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd");
		isoFormat.setTimeZone(TimeZone.getTimeZone(ZoneOffset.UTC));
		Integer numArgs = args.length;
		switch(numArgs){
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
				numDaysInPeriod = Integer.parseInt(args[2]);
			}
			catch(ParseException e){
				throw new ParseException("Please enter startDate and endDate command-line arguments in the format of yyyy-MM-dd", e.getErrorOffset());
			} 
			catch(NumberFormatException e){
				logger.info("Third command line argument could not be parsed to integer. " + e.getMessage());
				numDaysInPeriod = 7;
			}
			break;
		default:
			throw new Exception("ParseActivities expects two or three command-line arguments: startDate, endDate and optionally numDaysInPeriod");
		}
	}
	
	/**
	 * Get relevant beans
	 * @param context
	 */
	private void initializeSpringClasses(AbstractApplicationContext context){
		 logger.info(context.getClassLoader());
		 setAsynchronousStatisticsInitializor((AsynchronousStatisticsInitializor)context.getBean("asynchronousStatisticsInitializor"));
		 setNotificationDao((NotificationDAO)context.getBean("notificationDAO"));

	}
	
	/**
	 * Set the ParseActivities.Properties (props) using an InputStream to get all properties from the InputStream
	 * @param parseActivities
	 * @param in
	 * @return
	 * @throws IOException
	 */
	private Properties loadProperties(InputStream in) throws IOException{
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

	private String createHtmlMessage(Statistics stats, List<File> files){
		 Calendar calendarCounter = Calendar.getInstance(TimeZone.getTimeZone(ZoneOffset.UTC));
		 StringBuilder emailMessage = new StringBuilder();
		 emailMessage.append("Date: " + calendarCounter.getTime());
		 emailMessage.append("<h4>Total # of Unique Developers (Regardless of Edition) -  " + stats.getTotalDevelopers() + "</h4>");
		 emailMessage.append("<ul><li>Total # of Developers with Active 2014 Listings - " + stats.getTotalDevelopersWithActive2014Listings() + "</li>");
		 emailMessage.append("<ul>");
		 for(CertifiedBodyStatistics cbStat : stats.getTotalDevelopersByCertifiedBodyWithListingsEachYear()){
			 if(cbStat.getYear() == 2014 && 
					 getActiveDevelopersForAcb(2014, stats.getTotalDevelopersByCertifiedBodyWithListingsInEachCertificationStatusAndYear(), cbStat.getName()) > 0){
				 
				 emailMessage.append("<li>Certified by " + cbStat.getName() + " - " + 
								 getActiveDevelopersForAcb(2014, stats.getTotalDevelopersByCertifiedBodyWithListingsInEachCertificationStatusAndYear(), cbStat.getName()) + "</li>");
			 }
		 }
		 emailMessage.append("</ul>");
		 emailMessage.append("<li>Total # of Developers with Suspended by ONC-ACB/Suspended by ONC 2014 Listings</li>");
		 List<String> uniqueAcbList = new ArrayList<String>(); // make sure not to add one ACB more than once
		 Boolean hasSuspended = false;
		 emailMessage.append("<ul>");
		 for(CertifiedBodyStatistics cbStat : stats.getTotalDevelopersByCertifiedBodyWithListingsInEachCertificationStatusAndYear()){
			 if(cbStat.getYear() == 2014 && cbStat.getCertificationStatusName().toLowerCase().contains("suspended")){
				 if(!uniqueAcbList.contains(cbStat.getName())){
					 emailMessage.append("<li>Certified by " + cbStat.getName() + " - " + 
							 getSuspendedDevelopersForAcb(2014, stats.getTotalDevelopersByCertifiedBodyWithListingsInEachCertificationStatusAndYear(), cbStat.getName()) + "</li>");
					 uniqueAcbList.add(cbStat.getName());
					 hasSuspended = true;
				 }
			 }
		 }
		 emailMessage.append("</ul>");
		 if(!hasSuspended){
			 emailMessage.append("<ul><li>No certified bodies have suspended listings</li></ul>");
		 }
		 
		 emailMessage.append("<li>Total # of Developers with 2014 Listings (Regardless of Status) - " + stats.getTotalDevelopersWith2014Listings() + "</li>");
		 emailMessage.append("<ul>");
		 for(CertifiedBodyStatistics cbStat : stats.getTotalDevelopersByCertifiedBodyWithListingsEachYear()){
			 if(cbStat.getYear() == 2014 && cbStat.getTotalDevelopersWithListings() > 0){
				 emailMessage.append("<li>Certified by " + cbStat.getName() + " - " + cbStat.getTotalDevelopersWithListings() + "</li>");
			 }
		 }
		 emailMessage.append("</ul>");
		 
		 emailMessage.append("<li>Total # of Developers with Active 2015 Listings - " + stats.getTotalDevelopersWithActive2015Listings() + "</li>");
		 emailMessage.append("<ul>");
		 for(CertifiedBodyStatistics cbStat : stats.getTotalDevelopersByCertifiedBodyWithListingsEachYear()){
			 if(cbStat.getYear() == 2015 && 
				 getActiveDevelopersForAcb(2015, stats.getTotalDevelopersByCertifiedBodyWithListingsInEachCertificationStatusAndYear(), cbStat.getName()) > 0){
				 emailMessage.append("<li>Certified by " + cbStat.getName() + " - " + 
					 getActiveDevelopersForAcb(2015, stats.getTotalDevelopersByCertifiedBodyWithListingsInEachCertificationStatusAndYear(), cbStat.getName()) + "</li>");
			 }
		 }
		 emailMessage.append("</ul>");
		 
		 emailMessage.append("<li>Total # of Developers with Suspended by ONC-ACB/Suspended by ONC 2015 Listings</li>");
		 uniqueAcbList.clear(); // make sure not to add one ACB more than once
		 hasSuspended = false;
		 emailMessage.append("<ul>");
		 for(CertifiedBodyStatistics cbStat : stats.getTotalDevelopersByCertifiedBodyWithListingsInEachCertificationStatusAndYear()){
			 if(cbStat.getYear() == 2015 && cbStat.getCertificationStatusName().toLowerCase().contains("suspended")){
				 if(!uniqueAcbList.contains(cbStat.getName())){
					 emailMessage.append("<li>Certified by " + cbStat.getName() + " - " + 
						 getSuspendedDevelopersForAcb(2015, stats.getTotalDevelopersByCertifiedBodyWithListingsInEachCertificationStatusAndYear(), cbStat.getName()) + "</li>");
					 uniqueAcbList.add(cbStat.getName());
					 hasSuspended = true;
				 }
			 }
		 }
		 emailMessage.append("</ul>");
		 if(!hasSuspended){
			 emailMessage.append("<ul><li>No certified bodies have suspended listings</li></ul>");
		 }
		 
		 emailMessage.append("<li>Total # of Developers with 2015 Listings (Regardless of Status) - " + stats.getTotalDevelopersWith2015Listings() + "</li>");
		 emailMessage.append("<ul>");
		 for(CertifiedBodyStatistics cbStat : stats.getTotalDevelopersByCertifiedBodyWithListingsEachYear()){
			 if(cbStat.getYear() == 2015 && cbStat.getTotalDevelopersWithListings() > 0){
				 emailMessage.append("<li>Certified by " + cbStat.getName() + " - " + cbStat.getTotalDevelopersWithListings() + "</li>");
			 }
		 }
		 emailMessage.append("</ul></ul>");
		 
		 emailMessage.append("<h4>Total # of Certified Unique Products (Regardless of Status or Edition - Including 2011) -  " + stats.getTotalCertifiedProducts() + "</h4>");
		 emailMessage.append("<ul>");
		 emailMessage.append("<li>Total # of Unique Products with 2014 Listings (Regardless of Status) -  " + stats.getTotalCPs2014Listings() + "</li>");
		 emailMessage.append("<ul>");
		 for(CertifiedBodyStatistics cbStat : stats.getTotalCPListingsEachYearByCertifiedBody()){
			 if(cbStat.getYear() == 2014 && cbStat.getTotalListings() > 0){
				 emailMessage.append("<li>Certified by " + cbStat.getName() + " - " + cbStat.getTotalListings() + "</li>");
			 }
		 }
		 emailMessage.append("</ul>");
		 
		 uniqueAcbList.clear();
		 emailMessage.append("<li>Total # of Unique Products with Active 2014 Listings - " + stats.getTotalCPsActive2014Listings() + "</li>");
		 emailMessage.append("<ul>");
		 for(CertifiedBodyStatistics cbStat : stats.getTotalCPListingsEachYearByCertifiedBodyAndCertificationStatus()){
			 if(!uniqueAcbList.contains(cbStat.getName()) && cbStat.getYear() == 2014 && cbStat.getTotalListings() > 0 && 
					 (cbStat.getCertificationStatusName().equalsIgnoreCase("active"))){ 
				 emailMessage.append("<li>Certified by " + cbStat.getName() + " - " + 
					 getActiveCPsForAcb(2014, stats.getTotalCPListingsEachYearByCertifiedBodyAndCertificationStatus(), cbStat.getName()) + "</li>");
				 uniqueAcbList.add(cbStat.getName());
			 }
		 }
		 emailMessage.append("</ul>");
		 
		 hasSuspended = false;
		 emailMessage.append("<li>Total # of Unique Products with Suspended by ONC-ACB/Suspended by ONC 2014 Listings -  " + stats.getTotalCPsSuspended2014Listings() + "</li>");
		 emailMessage.append("<ul>");
		 for(CertifiedBodyStatistics cbStat : stats.getTotalCPListingsEachYearByCertifiedBodyAndCertificationStatus()){
			 if(!uniqueAcbList.contains(cbStat.getName()) && cbStat.getYear() == 2014 && cbStat.getTotalListings() > 0 && 
					 cbStat.getCertificationStatusName().contains("suspended")){
				 emailMessage.append("<li>Certified by " + cbStat.getName() + " - " + 
					 getSuspendedCPsForAcb(2014, stats.getTotalCPListingsEachYearByCertifiedBodyAndCertificationStatus(), cbStat.getName()) + "</li>");
				 uniqueAcbList.add(cbStat.getName());
				 hasSuspended = true;
			 }
		 }
		 emailMessage.append("</ul>");
		 if(!hasSuspended){
			 emailMessage.append("<ul><li>No certified bodies have suspended listings</li></ul>");
		 }

		 emailMessage.append("<li>Total # of Unique Products with 2015 Listings (Regardless of Status) -  " + stats.getTotalCPs2015Listings() + "</li>");
		 emailMessage.append("<ul>");
		 for(CertifiedBodyStatistics cbStat : stats.getTotalCPListingsEachYearByCertifiedBody()){
			 if(cbStat.getYear() == 2015 && cbStat.getTotalListings() > 0){
				 emailMessage.append("<li>Certified by " + cbStat.getName() + " - " + cbStat.getTotalListings() + "</li>");
			 }
		 }
		 emailMessage.append("</ul>");
		 
		 uniqueAcbList.clear();
		 emailMessage.append("<li>Total # of Unique Products with Active 2015 Listings -  " + stats.getTotalCPsActive2015Listings() + "</li>");
		 emailMessage.append("<ul>");
		 for(CertifiedBodyStatistics cbStat : stats.getTotalCPListingsEachYearByCertifiedBodyAndCertificationStatus()){
			 if(!uniqueAcbList.contains(cbStat.getName()) && cbStat.getYear() == 2015 && cbStat.getTotalListings() > 0 && (
					 cbStat.getCertificationStatusName().equalsIgnoreCase("active"))){
				 emailMessage.append("<li>Certified by " + cbStat.getName() + " - " + 
					 getActiveCPsForAcb(2015, stats.getTotalCPListingsEachYearByCertifiedBodyAndCertificationStatus(), cbStat.getName()) + "</li>");
				 uniqueAcbList.add(cbStat.getName());
			 }
		 }
		 emailMessage.append("</ul>");
		 
		 uniqueAcbList.clear();
		 hasSuspended = false;
		 emailMessage.append("<li>Total # of Unique Products with Suspended by ONC-ACB/Suspended by ONC 2015 Listings -  " + stats.getTotalCPsSuspended2015Listings() + "</li>");
		 emailMessage.append("<ul>");
		 for(CertifiedBodyStatistics cbStat : stats.getTotalCPListingsEachYearByCertifiedBodyAndCertificationStatus()){
			 if(!uniqueAcbList.contains(cbStat.getName()) && cbStat.getYear() == 2015 && cbStat.getTotalListings() > 0 && 
					 cbStat.getCertificationStatusName().contains("suspended")){
				 emailMessage.append("<li>Certified by " + cbStat.getName() + " - " + 
					 getSuspendedCPsForAcb(2015, stats.getTotalCPListingsEachYearByCertifiedBodyAndCertificationStatus(), cbStat.getName()) + "</li>");
				 uniqueAcbList.add(cbStat.getName());
				 hasSuspended = true;
			 }
		 }
		 emailMessage.append("</ul>");
		 if(!hasSuspended){
			 emailMessage.append("<ul><li>No certified bodies have suspended listings</li></ul>");
		 }
		 
		 uniqueAcbList.clear();
		 emailMessage.append("<li>Total # of Unique Products with Active Listings (Regardless of Edition) - " + stats.getTotalCPsActiveListings() + "</ul></li>");
		 emailMessage.append("</ul>");
		 emailMessage.append("<h4>Total # of Listings (Regardless of Status or Edition) -  " + stats.getTotalListings() + "</h4>");
		 emailMessage.append("<ul><li>Total # of Active 2014 Listings - " + stats.getTotalActive2014Listings() + "</li>");
		 
		 emailMessage.append("<ul>");
		 for(CertifiedBodyStatistics cbStat : stats.getTotalActiveListingsByCertifiedBody()){
			 if(cbStat.getYear() == 2014 && cbStat.getTotalListings() > 0){
				 emailMessage.append("<li>Certified by " + cbStat.getName() + " - " + cbStat.getTotalListings() + "</li>");
			 }
		 }
		 emailMessage.append("</ul>");
		 
		 emailMessage.append("<li>Total # of Active 2015 Listings - " + stats.getTotalActive2015Listings() + "</li>");
		 emailMessage.append("<ul>");
		 for(CertifiedBodyStatistics cbStat : stats.getTotalActiveListingsByCertifiedBody()){
			 if(cbStat.getYear() == 2015 && cbStat.getTotalListings() > 0){
				 emailMessage.append("<li>Certified by " + cbStat.getName() + " - " + cbStat.getTotalListings() + "</li>");
			 }
		 }
		 emailMessage.append("</ul>");
		 
		 emailMessage.append("<li>Total # of 2014 Listings (Regardless of Status) - " + stats.getTotal2014Listings() + "</li>");
		 emailMessage.append("<li>Total # of 2015 Listings (Regardless of Status) - " + stats.getTotal2015Listings() + "</li>");
		 emailMessage.append("<li>Total # of 2011 Listings (Regardless of Status) - " + stats.getTotal2011Listings() + "</li></ul>");
		 emailMessage.append("<h4>Total # of Surveillance Activities -  " + stats.getTotalSurveillanceActivities() + "</h4>");
		 emailMessage.append("<ul><li>Open Surveillance Activities - " + stats.getTotalOpenSurveillanceActivities() + "</li>");
		 emailMessage.append("<li>Closed Surveillance Activities - " + stats.getTotalClosedSurveillanceActivities() + "</li></ul>");
		 emailMessage.append("<h4>Total # of NCs -  " + stats.getTotalNonConformities() + "</h4>");
		 emailMessage.append("<ul><li>Open NCs - " + stats.getTotalOpenNonconformities() + "</li>");
		 emailMessage.append("<li>Closed NCs - " + stats.getTotalClosedNonconformities() + "</li></ul>");
		 logger.info(emailMessage.toString());
		 return emailMessage.toString();
	}
	
	private void setAsynchronousStatisticsInitializor(AsynchronousStatisticsInitializor asynchronousStatisticsInitializor){
		this.asynchronousStatisticsInitializor = asynchronousStatisticsInitializor;
	}
	
	private Long getSuspendedDevelopersForAcb(Integer year, List<CertifiedBodyStatistics> cbStats, String acb){
		Long count = 0L;
		for(CertifiedBodyStatistics cbStat : cbStats){
			if(cbStat.getYear() == year && cbStat.getName().equalsIgnoreCase(acb) && cbStat.getCertificationStatusName().toLowerCase().contains("suspended")){
				count = count + cbStat.getTotalDevelopersWithListings();
			}
		}
		return count;
	}
	
	private Long getActiveDevelopersForAcb(Integer year, List<CertifiedBodyStatistics> cbStats, String acb){
		Long count = 0L;
		for(CertifiedBodyStatistics cbStat : cbStats){
			if(cbStat.getYear().equals(year) && cbStat.getName().equalsIgnoreCase(acb) && 
					(cbStat.getCertificationStatusName().toLowerCase().equalsIgnoreCase("active"))){
				count = count + cbStat.getTotalDevelopersWithListings();
			}
		}
		return count;
	}
	
	private Long getActiveCPsForAcb(Integer year, List<CertifiedBodyStatistics> cbStats, String acb){
		Long count = 0L;
		for(CertifiedBodyStatistics cbStat : cbStats){
			if(cbStat.getYear().equals(year) && cbStat.getName().equalsIgnoreCase(acb) && 
					(cbStat.getCertificationStatusName().toLowerCase().equalsIgnoreCase("active"))){
				count = count + cbStat.getTotalListings();
			}
		}
		return count;
	}
	
	private Long getSuspendedCPsForAcb(Integer year, List<CertifiedBodyStatistics> cbStats, String acb){
		Long count = 0L;
		for(CertifiedBodyStatistics cbStat : cbStats){
			if(cbStat.getYear().equals(year) && cbStat.getName().equalsIgnoreCase(acb) && cbStat.getCertificationStatusName().toLowerCase().contains("suspended")){
				count = count + cbStat.getTotalListings();
			}
		}
		return count;
	}

	public NotificationDAO getNotificationDao() {
		return notificationDao;
	}

	public void setNotificationDao(NotificationDAO notificationDao) {
		this.notificationDao = notificationDao;
	}

}
