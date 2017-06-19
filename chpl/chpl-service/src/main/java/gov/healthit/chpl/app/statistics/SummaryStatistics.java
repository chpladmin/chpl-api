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
import java.util.List;
import java.util.Properties;
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
import gov.healthit.chpl.auth.SendMailUtil;
import gov.healthit.chpl.domain.DateRange;
import gov.healthit.chpl.domain.statistics.CertifiedBodyStatistics;
import gov.healthit.chpl.domain.statistics.Statistics;

@Component("summaryStatistics")
public class SummaryStatistics {
	private static final String DEFAULT_PROPERTIES_FILE = "environment.properties";
	private static final Logger logger = LogManager.getLogger(SummaryStatistics.class);
	private static Date startDate;
	private static Date endDate;
	private static Integer numDaysInPeriod;
	private Properties props;
	private AsynchronousStatisticsInitializor asynchronousStatisticsInitializor;
	
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
		 logger.info("Sending email to " + props.getProperty("summaryEmail").toString());
		 SendMailUtil mailUtil = new SendMailUtil();
		 mailUtil.sendEmail(props.getProperty("summaryEmail").toString().split(";"), props.getProperty("summaryEmailSubject").toString(), htmlMessage, files, props);
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
		 emailMessage.append("<ul><li>Total # of Developers with Active 2014 Listings - " + stats.getTotalDevelopersWith2014Listings() + "</li>");
		 int counter = 1;
		 for(CertifiedBodyStatistics cbStat : stats.getTotalDevelopersByCertifiedBodyWithListingsEachYear()){
			 if(cbStat.getYear() == 2014){
				 if(counter == 1){
					emailMessage.append("<ul><li>Certified by " + cbStat.getName() + " - " + 
								 getActiveDevelopersForAcb(2014, stats.getTotalDevelopersByCertifiedBodyWithListingsEachYear(), cbStat.getName()) + "</li>");
					 
				 } else{
					emailMessage.append("<li>Certified by " + cbStat.getName() + " - " + 
								 getActiveDevelopersForAcb(2014, stats.getTotalDevelopersByCertifiedBodyWithListingsEachYear(), cbStat.getName()) + "</li>");
				 }
				 counter++;
			 }
		 }
		 emailMessage.append("</ul></li>");
		 
		 emailMessage.append("<ul><li>Total # of Developers with Suspended by ONC-ACB/Suspended by ONC 2014 Listings</li>");
		 counter = 1;
		 List<String> acbsWithSuspendedListings = new ArrayList<String>(); // make sure not to add one ACB more than once
		 for(CertifiedBodyStatistics cbStat : stats.getTotalDevelopersByCertifiedBodyWithListingsInEachCertificationStatusAndYear()){
			 if(cbStat.getYear() == 2014 && cbStat.getCertificationStatusName().toLowerCase().contains("suspended")){
				 if(counter == 1){
					 if(!acbsWithSuspendedListings.contains(cbStat.getName())){
						 emailMessage.append("<ul><li>Certified by " + cbStat.getName() + " - " + 
								 getSuspendedDevelopersForAcb(2014, stats.getTotalDevelopersByCertifiedBodyWithListingsInEachCertificationStatusAndYear(), cbStat.getName()) + "</li>");
						 acbsWithSuspendedListings.add(cbStat.getName());
					 }
				 } else{
					 if(!acbsWithSuspendedListings.contains(cbStat.getName())){
						 emailMessage.append("<li>Certified by " + cbStat.getName() + " - " + 
								 getSuspendedDevelopersForAcb(2014, stats.getTotalDevelopersByCertifiedBodyWithListingsInEachCertificationStatusAndYear(), cbStat.getName()) + "</li>");
						 acbsWithSuspendedListings.add(cbStat.getName());
					 }
					 
				 }
				 counter++;
			 }
		 }
		 emailMessage.append("</ul></li>");
		 
		 emailMessage.append("<ul><li>Total # of Developers with 2014 Listings (Regardless of Status) </li>");
		 counter = 1;
		 for(CertifiedBodyStatistics cbStat : stats.getTotalDevelopersByCertifiedBodyWithListingsEachYear()){
			 if(cbStat.getYear() == 2014){
				 if(counter == 1){
					 emailMessage.append("<ul><li>Certified by " + cbStat.getName() + " - " + cbStat.getTotalDevelopersWithListings() + "</li>");
				 } else{
					 emailMessage.append("<li>Certified by " + cbStat.getName() + " - " + cbStat.getTotalDevelopersWithListings() + "</li>");
				 }
				 counter++;
			 }
		 }
		 emailMessage.append("</ul></li>");
		 
		 emailMessage.append("<li>Total # of Developers with Active 2015 Listings - " + stats.getTotalDevelopersWith2015Listings() + "</ul></li>");
		 counter = 1;
		 for(CertifiedBodyStatistics cbStat : stats.getTotalDevelopersByCertifiedBodyWithListingsInEachCertificationStatusAndYear()){
			 if(cbStat.getYear() == 2015){
				 if(counter == 1){
					emailMessage.append("<ul><li>Certified by " + cbStat.getName() + " - " + 
							getActiveDevelopersForAcb(2015, stats.getTotalDevelopersByCertifiedBodyWithListingsInEachCertificationStatusAndYear(), cbStat.getName()) + "</li>");
				 } else{
					emailMessage.append("<li>Certified by " + cbStat.getName() + " - " + 
							getActiveDevelopersForAcb(2015, stats.getTotalDevelopersByCertifiedBodyWithListingsInEachCertificationStatusAndYear(), cbStat.getName()) + "</li>");
				 }
				 counter++;
			 }
		 }
		 emailMessage.append("</ul></li>");
		 
		 emailMessage.append("<ul><li>Total # of Developers with Suspended by ONC-ACB/Suspended by ONC 2015 Listings</li>");
		 counter = 1;
		 acbsWithSuspendedListings.clear(); // make sure not to add one ACB more than once
		 for(CertifiedBodyStatistics cbStat : stats.getTotalDevelopersByCertifiedBodyWithListingsInEachCertificationStatusAndYear()){
			 if(cbStat.getYear() == 2015 && cbStat.getCertificationStatusName().toLowerCase().contains("suspended")){
				 if(counter == 1){
					 if(!acbsWithSuspendedListings.contains(cbStat.getName())){
						 emailMessage.append("<ul><li>Certified by " + cbStat.getName() + " - " + 
								 getSuspendedDevelopersForAcb(2015, stats.getTotalDevelopersByCertifiedBodyWithListingsInEachCertificationStatusAndYear(), cbStat.getName()) + "</li>");
						 acbsWithSuspendedListings.add(cbStat.getName());
					 }
				 } else{
					 if(!acbsWithSuspendedListings.contains(cbStat.getName())){
						 emailMessage.append("<li>Certified by " + cbStat.getName() + " - " + 
								 getSuspendedDevelopersForAcb(2015, stats.getTotalDevelopersByCertifiedBodyWithListingsInEachCertificationStatusAndYear(), cbStat.getName()) + "</li>");
						 acbsWithSuspendedListings.add(cbStat.getName());
					 }
					 
				 }
				 counter++;
			 }
		 }
		 emailMessage.append("</ul></li>");
		 
		 emailMessage.append("<h4>Total # of Certified Unique Products (Regardless of Status or Edition - Including 2011) -  " + stats.getTotalCertifiedProducts() + "</h4>");
		 emailMessage.append("<ul><li>Total # of unique Products with Active (Including Suspended) 2014 Listings - " + stats.getTotalCPsActive2014Listings() + "</li>");
		 emailMessage.append("<li>Total # of unique Products with Active (Including Suspended) 2015 Listings - " + stats.getTotalCPsActive2015Listings() + "</li>");
		 emailMessage.append("<li>Total # of unique  Products with Active Listings (Including Suspended) (Regardless of Edition) - " + stats.getTotalCPsActiveListings() + "</ul></li>");
		 emailMessage.append("<h4>Total # of Listings (Regardless of Status or Edition) -  " + stats.getTotalListings() + "</h4>");
		 emailMessage.append("<ul><li>Total # of Active (Including Suspended) 2014 Listings - " + stats.getTotalActive2014Listings());
		 counter = 1;
		 for(CertifiedBodyStatistics cbStat : stats.getTotalActiveListingsByCertifiedBody()){
			 if(cbStat.getYear() != 2014){
				 continue;
			 }
			 if(counter == 1){
				 emailMessage.append("<ul><li>Certified by " + cbStat.getName() + " - " + cbStat.getTotalListings() + "</li>");
			 } else{
				 emailMessage.append("<li>Certified by " + cbStat.getName() + " - " + cbStat.getTotalListings() + "</li>");
			 }
			 counter++;
		 }
		 emailMessage.append("</ul></li>");
		 emailMessage.append("<li>Total # of Active (Including Suspended) 2015 Listings - " + stats.getTotalActive2015Listings());
		 int statCounter2015 = 1;
		 for(CertifiedBodyStatistics cbStat : stats.getTotalActiveListingsByCertifiedBody()){
			 if(cbStat.getYear() != 2015){
				 continue;
			 }
			 if(statCounter2015 == 1){
				 emailMessage.append("<ul><li>Certified by " + cbStat.getName() + " - " + cbStat.getTotalListings() + "</li>");
			 } else {
				 emailMessage.append("<li>Certified by " + cbStat.getName() + " - " + cbStat.getTotalListings() + "</li>");
			 }
			 statCounter2015++;
		 }
		 emailMessage.append("</ul></li>");
		 emailMessage.append("<li>Total # of 2014 Listings (Regardless of Status) - " + stats.getTotal2014Listings() + "</li>");
		 emailMessage.append("<li>Total # of 2015 Listings (Regardless of Status) - " + stats.getTotal2015Listings() + "</li>");
		 emailMessage.append("<li>Total # of 2011 Listings (Regardless of Status) - " + stats.getTotal2011Listings() + "</li></ul></li>");
		 emailMessage.append("<h4>Total # of Surveillance Activities -  " + stats.getTotalSurveillanceActivities() + "</h4>");
		 emailMessage.append("<ul><li>Open Surveillance Activities - " + stats.getTotalOpenSurveillanceActivities() + "</li>");
		 emailMessage.append("<li>Closed Surveillance Activities - " + stats.getTotalClosedSurveillanceActivities() + "</ul></li>");
		 emailMessage.append("<h4>Total # of NCs -  " + stats.getTotalNonConformities() + "</h4>");
		 emailMessage.append("<ul><li>Open NCs - " + stats.getTotalOpenNonconformities() + "</li>");
		 emailMessage.append("<li>Closed NCs - " + stats.getTotalClosedNonconformities() + "</li></ul></li>");
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
			if(cbStat.getYear() == year && cbStat.getName().equalsIgnoreCase(acb) 
					&& (cbStat.getCertificationStatusName().toLowerCase().contains("active") 
							|| cbStat.getCertificationStatusName().toLowerCase().contains("suspended"))){
				count = count + cbStat.getTotalDevelopersWithListings();
			}
		}
		
		return count;
	}

}
