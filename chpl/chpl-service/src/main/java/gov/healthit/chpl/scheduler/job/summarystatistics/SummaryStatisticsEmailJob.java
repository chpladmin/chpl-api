package gov.healthit.chpl.scheduler.job.summarystatistics;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import gov.healthit.chpl.auth.SendMailUtil;
import gov.healthit.chpl.dao.statistics.SummaryStatisticsDAO;
import gov.healthit.chpl.domain.statistics.CertifiedBodyAltTestStatistics;
import gov.healthit.chpl.domain.statistics.CertifiedBodyStatistics;
import gov.healthit.chpl.domain.statistics.Statistics;
import gov.healthit.chpl.entity.SummaryStatisticsEntity;
import gov.healthit.chpl.scheduler.JobConfig;
import gov.healthit.chpl.scheduler.job.QuartzJob;

/**
 * The SummaryStatisticsEmailJob implements a Quartz job and is schedulable by ADMINs.  When the job is triggered,
 * it will send the recipient an email with summary statistics of the CHPL data.
 * @author TYoung
 *
 */
public class SummaryStatisticsEmailJob extends QuartzJob {
    private static final Logger LOGGER = LogManager.getLogger(SummaryStatisticsEmailJob.class);
    private static final String DEFAULT_PROPERTIES_FILE = "environment.properties";
    private SummaryStatisticsDAO summaryStatisticsDAO;
    private Properties props;
    private AbstractApplicationContext context;

    /**
     * Constructor that initializes the SummaryStatisticsEmailJob object.
     * @throws Exception if thrown
     */
    public SummaryStatisticsEmailJob() throws Exception{
        super();
        setLocalContext();
        context = new AnnotationConfigApplicationContext(JobConfig.class);
        initiateSpringBeans(context);
        loadProperties();
    }

    @Override
    public void execute(final JobExecutionContext jobContext) throws JobExecutionException {
        try {
            SummaryStatisticsEntity summaryStatistics = summaryStatisticsDAO.getMostRecent();
            Statistics stats = getStatistics(summaryStatistics);
            String message = createHtmlMessage(stats, summaryStatistics.getEndDate());
            LOGGER.info(message);
            sendEmail(message, jobContext.getMergedJobDataMap().getString("email"));
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error(e);
        } finally {
            context.close();
        }
    }

    @Override
    protected void initiateSpringBeans(final AbstractApplicationContext context) throws IOException {
        setSummaryStatisticsDAO((SummaryStatisticsDAO) context.getBean("summaryStatisticsDAO"));

    }

    private void sendEmail(final String message, final String address) throws AddressException, MessagingException {
        SendMailUtil mailUtil = new SendMailUtil();
        String subject = props.getProperty("summaryEmailSubject").toString();
        mailUtil.sendEmail(address, subject, message, getSummaryStatisticsFile(), props);
    }

    
    private List<File> getSummaryStatisticsFile() {
        List<File> files = new ArrayList<File>();
        File file = new File( 
                        props.getProperty("downloadFolderPath") + File.separator
                        + props.getProperty("summaryEmailName", "summaryStatistics.csv"));
        files.add(file);
        return files;
    }

    private Statistics getStatistics(final SummaryStatisticsEntity summaryStatistics)
            throws JsonParseException, JsonMappingException, IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(summaryStatistics.getSummaryStatistics(), Statistics.class);
    }

    private String createHtmlMessage(final Statistics stats, final Date endDate) {
        StringBuilder emailMessage = new StringBuilder();

        emailMessage.append(createMessageHeader(endDate));
        emailMessage.append(createUniqueDeveloperSection(stats));
        emailMessage.append(createUniqueProductSection(stats));
        emailMessage.append(createListingSection(stats));

        emailMessage.append(
                "<h4>Total # of Surveillance Activities -  " + stats.getTotalSurveillanceActivities() + "</h4>");
        emailMessage.append(
                "<ul><li>Open Surveillance Activities - " + stats.getTotalOpenSurveillanceActivities() + "</li>");
        emailMessage.append(
                "<li>Closed Surveillance Activities - " + stats.getTotalClosedSurveillanceActivities() + "</li></ul>");
        emailMessage.append("<h4>Total # of NCs -  " + stats.getTotalNonConformities() + "</h4>");
        emailMessage.append("<ul><li>Open NCs - " + stats.getTotalOpenNonconformities() + "</li>");
        emailMessage.append("<li>Closed NCs - " + stats.getTotalClosedNonconformities() + "</li></ul>");
        return emailMessage.toString();
    }

    private String createMessageHeader(final Date endDate) {
        Calendar currDateCal = Calendar.getInstance(TimeZone.getTimeZone(ZoneOffset.UTC));
        Calendar endDateCal = Calendar.getInstance(TimeZone.getTimeZone(ZoneOffset.UTC));
        endDateCal.setTime(endDate);
        StringBuilder ret = new StringBuilder();
        ret.append("Email body has current statistics as of " + currDateCal.getTime());
        ret.append("<br/>");
        ret.append("Email attachment has weekly statistics ending " + endDateCal.getTime());
        return ret.toString();
    }

    private String createUniqueDeveloperSection(final Statistics stats) {
        final int edition2014 = 2014;
        final int edition2015 = 2015;
        List<String> uniqueAcbList = new ArrayList<String>();
        Boolean hasSuspended = false;
        StringBuilder ret = new StringBuilder();

        ret.append(
                "<h4>Total # of Unique Developers (Regardless of Edition) -  " + stats.getTotalDevelopers() + "</h4>");
        ret.append("<ul><li>Total # of Developers with Active 2014 Listings - "
                + stats.getTotalDevelopersWithActive2014Listings() + "</li>");
        ret.append("<ul>");
        for (CertifiedBodyStatistics cbStat : stats.getTotalDevelopersByCertifiedBodyWithListingsEachYear()) {
            if (cbStat.getYear() == edition2014 && getActiveDevelopersForAcb(edition2014,
                    stats.getTotalDevsByCertifiedBodyWithListingsInEachCertificationStatusAndYear(),
                    cbStat.getName()) > 0) {

                ret.append("<li>Certified by " + cbStat.getName() + " - "
                        + getActiveDevelopersForAcb(edition2014,
                                stats.getTotalDevsByCertifiedBodyWithListingsInEachCertificationStatusAndYear(),
                                cbStat.getName())
                        + "</li>");
            }
        }
        ret.append("</ul>");
        ret.append("<li>Total # of Developers with Suspended by ONC-ACB/Suspended by ONC 2014 Listings</li>");
        ret.append("<ul>");
        for (CertifiedBodyStatistics cbStat : stats
                .getTotalDevsByCertifiedBodyWithListingsInEachCertificationStatusAndYear()) {
            if (cbStat.getYear() == edition2014
                    && cbStat.getCertificationStatusName().toLowerCase().contains("suspended")) {
                if (!uniqueAcbList.contains(cbStat.getName())) {
                    ret.append("<li>Certified by " + cbStat.getName() + " - "
                            + getSuspendedDevelopersForAcb(edition2014,
                                    stats.getTotalDevsByCertifiedBodyWithListingsInEachCertificationStatusAndYear(),
                                    cbStat.getName())
                            + "</li>");
                    uniqueAcbList.add(cbStat.getName());
                    hasSuspended = true;
                }
            }
        }
        ret.append("</ul>");
        if (!hasSuspended) {
            ret.append("<ul><li>No certified bodies have suspended listings</li></ul>");
        }

        ret.append("<li>Total # of Developers with 2014 Listings (Regardless of Status) - "
                + stats.getTotalDevelopersWith2014Listings() + "</li>");
        ret.append("<ul>");
        for (CertifiedBodyStatistics cbStat : stats.getTotalDevelopersByCertifiedBodyWithListingsEachYear()) {
            if (cbStat.getYear() == edition2014 && cbStat.getTotalDevelopersWithListings() > 0) {
                ret.append("<li>Certified by " + cbStat.getName() + " - "
                        + cbStat.getTotalDevelopersWithListings() + "</li>");
            }
        }
        ret.append("</ul>");

        ret.append("<li>Total # of Developers with Active 2015 Listings - "
                + stats.getTotalDevelopersWithActive2015Listings() + "</li>");
        ret.append("<ul>");
        for (CertifiedBodyStatistics cbStat : stats.getTotalDevelopersByCertifiedBodyWithListingsEachYear()) {
            if (cbStat.getYear() == edition2015 && getActiveDevelopersForAcb(edition2015,
                    stats.getTotalDevsByCertifiedBodyWithListingsInEachCertificationStatusAndYear(),
                    cbStat.getName()) > 0) {
                ret.append("<li>Certified by " + cbStat.getName() + " - "
                        + getActiveDevelopersForAcb(edition2015,
                                stats.getTotalDevsByCertifiedBodyWithListingsInEachCertificationStatusAndYear(),
                                cbStat.getName())
                        + "</li>");
            }
        }
        ret.append("</ul>");

        ret.append("<li>Total # of Developers with Suspended by ONC-ACB/Suspended by ONC 2015 Listings</li>");
        uniqueAcbList.clear(); // make sure not to add one ACB more than once
        hasSuspended = false;
        ret.append("<ul>");
        for (CertifiedBodyStatistics cbStat : stats
                .getTotalDevsByCertifiedBodyWithListingsInEachCertificationStatusAndYear()) {
            if (cbStat.getYear() == edition2015
                    && cbStat.getCertificationStatusName().toLowerCase().contains("suspended")) {
                if (!uniqueAcbList.contains(cbStat.getName())) {
                    ret.append("<li>Certified by " + cbStat.getName() + " - "
                            + getSuspendedDevelopersForAcb(edition2015,
                                    stats.getTotalDevsByCertifiedBodyWithListingsInEachCertificationStatusAndYear(),
                                    cbStat.getName())
                            + "</li>");
                    uniqueAcbList.add(cbStat.getName());
                    hasSuspended = true;
                }
            }
        }
        ret.append("</ul>");
        if (!hasSuspended) {
            ret.append("<ul><li>No certified bodies have suspended listings</li></ul>");
        }

        ret.append("<li>Total # of Developers with 2015 Listings (Regardless of Status) - "
                + stats.getTotalDevelopersWith2015Listings() + "</li>");
        ret.append("<ul>");
        for (CertifiedBodyStatistics cbStat : stats.getTotalDevelopersByCertifiedBodyWithListingsEachYear()) {
            if (cbStat.getYear() == edition2015 && cbStat.getTotalDevelopersWithListings() > 0) {
                ret.append("<li>Certified by " + cbStat.getName() + " - "
                        + cbStat.getTotalDevelopersWithListings() + "</li>");
            }
        }
        ret.append("</ul></ul>");
        return ret.toString();
    }

    private String createUniqueProductSection(final Statistics stats) {
        final int edition2014 = 2014;
        final int edition2015 = 2015;
        List<String> uniqueAcbList = new ArrayList<String>();
        Boolean hasSuspended = false;
        StringBuilder ret = new StringBuilder();

        ret
        .append("<h4>Total # of Certified Unique Products "
                + "(Regardless of Status or Edition - Including 2011) - "
                + stats.getTotalCertifiedProducts() + "</h4>");
        ret.append("<ul>");
        ret.append("<li>Total # of Unique Products with 2014 Listings (Regardless of Status) -  "
                + stats.getTotalCPs2014Listings() + "</li>");
        ret.append("<ul>");
        for (CertifiedBodyStatistics cbStat : stats.getTotalCPListingsEachYearByCertifiedBody()) {
            if (cbStat.getYear() == edition2014 && cbStat.getTotalListings() > 0) {
                ret
                .append("<li>Certified by " + cbStat.getName() + " - " + cbStat.getTotalListings() + "</li>");
            }
        }
        ret.append("</ul>");

        ret.append("<li>Total # of Unique Products with Active 2014 Listings - "
                + stats.getTotalCPsActive2014Listings() + "</li>");
        ret.append("<ul>");
        for (CertifiedBodyStatistics cbStat : stats.getTotalCPListingsEachYearByCertifiedBodyAndCertificationStatus()) {
            if (!uniqueAcbList.contains(cbStat.getName())
                    && cbStat.getYear() == edition2014 && cbStat.getTotalListings() > 0
                    && (cbStat.getCertificationStatusName().equalsIgnoreCase("active"))) {
                ret.append("<li>Certified by " + cbStat.getName() + " - "
                        + getActiveCPsForAcb(edition2014,
                                stats.getTotalCPListingsEachYearByCertifiedBodyAndCertificationStatus(),
                                cbStat.getName())
                        + "</li>");
                uniqueAcbList.add(cbStat.getName());
            }
        }
        ret.append("</ul>");

        uniqueAcbList.clear();
        hasSuspended = false;
        ret
        .append("<li>Total # of Unique Products with Suspended by ONC-ACB/Suspended by ONC 2014 Listings -  "
                + stats.getTotalCPsSuspended2014Listings() + "</li>");
        ret.append("<ul>");
        for (CertifiedBodyStatistics cbStat : stats.getTotalCPListingsEachYearByCertifiedBodyAndCertificationStatus()) {
            if (!uniqueAcbList.contains(cbStat.getName()) && cbStat.getYear().intValue() == edition2014
                    && cbStat.getTotalListings() > 0
                    && cbStat.getCertificationStatusName().toLowerCase().contains("suspended")) {
                ret.append("<li>Certified by " + cbStat.getName() + " - "
                        + getSuspendedCPsForAcb(edition2014,
                                stats.getTotalCPListingsEachYearByCertifiedBodyAndCertificationStatus(),
                                cbStat.getName())
                        + "</li>");
                uniqueAcbList.add(cbStat.getName());
                hasSuspended = true;
            }
        }
        ret.append("</ul>");
        if (!hasSuspended) {
            ret.append("<ul><li>No certified bodies have suspended listings</li></ul>");
        }

        ret.append("<li>Total # of Unique Products with 2015 Listings (Regardless of Status) -  "
                + stats.getTotalCPs2015Listings() + "</li>");
        ret.append("<ul>");
        for (CertifiedBodyStatistics cbStat : stats.getTotalCPListingsEachYearByCertifiedBody()) {
            if (cbStat.getYear() == edition2015 && cbStat.getTotalListings() > 0) {
                ret
                .append("<li>Certified by " + cbStat.getName() + " - " + cbStat.getTotalListings() + "</li>");
            }
        }
        ret.append("</ul>");

        uniqueAcbList.clear();

        ret.append("<li>Total # of Unique Products with Active 2015 Listings -  "
                + stats.getTotalCPsActive2015Listings() + "</li>");
        ret.append("<ul>");
        for (CertifiedBodyStatistics cbStat : stats.getTotalCPListingsEachYearByCertifiedBodyAndCertificationStatus()) {
            if (!uniqueAcbList.contains(cbStat.getName())
                    && cbStat.getYear() == edition2015 && cbStat.getTotalListings() > 0
                    && (cbStat.getCertificationStatusName().equalsIgnoreCase("active"))) {
                ret.append("<li>Certified by " + cbStat.getName() + " - "
                        + getActiveCPsForAcb(edition2015,
                                stats.getTotalCPListingsEachYearByCertifiedBodyAndCertificationStatus(),
                                cbStat.getName())
                        + "</li>");
                uniqueAcbList.add(cbStat.getName());
            }
        }
        ret.append("</ul>");

        uniqueAcbList.clear();
        ret.append("<li>Total # of Unique Products with Suspended by ONC-ACB/Suspended by ONC 2015 Listings -  "
                + stats.getTotalCPsSuspended2015Listings() + "</li>");
        ret.append("<ul>");
        for (CertifiedBodyStatistics cbStat : stats.getTotalCPListingsEachYearByCertifiedBodyAndCertificationStatus()) {
            if (!uniqueAcbList.contains(cbStat.getName())
                    && cbStat.getYear() == edition2015 && cbStat.getTotalListings() > 0
                    && cbStat.getCertificationStatusName().toLowerCase().contains("suspended")) {
                ret.append("<li>Certified by " + cbStat.getName() + " - "
                        + getSuspendedCPsForAcb(edition2015,
                                stats.getTotalCPListingsEachYearByCertifiedBodyAndCertificationStatus(),
                                cbStat.getName())
                        + "</li>");
                uniqueAcbList.add(cbStat.getName());
                hasSuspended = true;
            }
        }
        ret.append("</ul>");
        if (!hasSuspended) {
            ret.append("<ul><li>No certified bodies have suspended listings</li></ul>");
        }


        ret.append("<li>Total # of Unique Products with Active Listings (Regardless of Edition) - "
                + stats.getTotalCPsActiveListings() + "</ul></li>");
        ret.append("</ul>");
        return ret.toString();
    }

    private String createListingSection(final Statistics stats) {
        final int edition2014 = 2014;
        final int edition2015 = 2015;
        StringBuilder ret = new StringBuilder();

        ret.append(
                "<h4>Total # of Listings (Regardless of Status or Edition) -  " + stats.getTotalListings() + "</h4>");
        ret.append("<ul><li>Total # of Active (Including Suspended by ONC/ONC-ACB 2014 Listings) - "
                + stats.getTotalActive2014Listings() + "</li>");

        ret.append("<ul>");
        for (CertifiedBodyStatistics cbStat : stats.getTotalActiveListingsByCertifiedBody()) {
            if (cbStat.getYear() == edition2014 && cbStat.getTotalListings() > 0) {
                ret
                .append("<li>Certified by " + cbStat.getName() + " - " + cbStat.getTotalListings() + "</li>");
            }
        }
        ret.append("</ul>");

        ret.append("<li>Total # of Active (Including Suspended by ONC/ONC-ACB 2015 Listings) - "
                + stats.getTotalActive2015Listings() + "</li>");
        ret.append("<ul>");
        for (CertifiedBodyStatistics cbStat : stats.getTotalActiveListingsByCertifiedBody()) {
            if (cbStat.getYear() == edition2015 && cbStat.getTotalListings() > 0) {
                ret
                .append("<li>Certified by " + cbStat.getName() + " - " + cbStat.getTotalListings() + "</li>");
            }
        }
        ret.append("</ul>");

        Boolean hasOtherTest = false;
        ret.append("<li>Total # of 2015 Listings with Alternative Test Methods -  "
                + stats.getTotalListingsWithAlternativeTestMethods() + "</li>");
        ret.append("<ul>");
        for (CertifiedBodyAltTestStatistics cbStat
                : stats.getTotalListingsWithCertifiedBodyAndAlternativeTestMethods()) {
            if (cbStat.getTotalListings() > 0) {
                ret.append("<li>Certified by " + cbStat.getName() + " - "
                        + cbStat.getTotalListings()
                        + "</li>");
                hasOtherTest = true;
            }
        }
        if (!hasOtherTest) {
            ret.append("<li>No listings have Alternative Test Methods</li>");
        }
        ret.append("</ul>");

        ret.append(
                "<li>Total # of 2014 Listings (Regardless of Status) - " + stats.getTotal2014Listings() + "</li>");
        ret.append(
                "<li>Total # of 2015 Listings (Regardless of Status) - " + stats.getTotal2015Listings() + "</li>");
        ret.append(
                "<li>Total # of 2011 Listings (Regardless of Status) - " + stats.getTotal2011Listings() + "</li></ul>");
        return ret.toString();

    }

    private Long getSuspendedDevelopersForAcb(
            final Integer year, final List<CertifiedBodyStatistics> cbStats, final String acb) {
        Long count = 0L;
        for (CertifiedBodyStatistics cbStat : cbStats) {
            if (cbStat.getYear().equals(year) && cbStat.getName().equalsIgnoreCase(acb)
                    && cbStat.getCertificationStatusName().toLowerCase().contains("suspended")) {
                count = count + cbStat.getTotalDevelopersWithListings();
            }
        }
        return count;
    }

    private Long getActiveDevelopersForAcb(
            final Integer year, final List<CertifiedBodyStatistics> cbStats, final String acb) {
        Long count = 0L;
        for (CertifiedBodyStatistics cbStat : cbStats) {
            if (cbStat.getYear().equals(year) && cbStat.getName().equalsIgnoreCase(acb)
                    && (cbStat.getCertificationStatusName().toLowerCase().equalsIgnoreCase("active"))) {
                count = count + cbStat.getTotalDevelopersWithListings();
            }
        }
        return count;
    }

    private Long getActiveCPsForAcb(final Integer year, final List<CertifiedBodyStatistics> cbStats, final String acb) {
        Long count = 0L;
        for (CertifiedBodyStatistics cbStat : cbStats) {
            if (cbStat.getYear().equals(year) && cbStat.getName().equalsIgnoreCase(acb)
                    && (cbStat.getCertificationStatusName().toLowerCase().equalsIgnoreCase("active"))) {
                count = count + cbStat.getTotalListings();
            }
        }
        return count;
    }

    private Long getSuspendedCPsForAcb(
            final Integer year, final List<CertifiedBodyStatistics> cbStats, final String acb) {
        Long count = 0L;
        for (CertifiedBodyStatistics cbStat : cbStats) {
            if (cbStat.getYear().equals(year) && cbStat.getName().equalsIgnoreCase(acb)
                    && cbStat.getCertificationStatusName().toLowerCase().contains("suspended")) {
                count = count + cbStat.getTotalListings();
            }
        }
        return count;
    }

    private Properties loadProperties() throws IOException {
        InputStream in =
                SummaryStatisticsCreatorJob.class.getClassLoader().getResourceAsStream(DEFAULT_PROPERTIES_FILE);
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

    public void setSummaryStatisticsDAO(final SummaryStatisticsDAO summaryStatisticsDAO) {
        this.summaryStatisticsDAO = summaryStatisticsDAO;
    }
}
