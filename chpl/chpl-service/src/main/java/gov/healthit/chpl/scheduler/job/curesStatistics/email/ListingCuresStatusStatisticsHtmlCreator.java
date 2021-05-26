package gov.healthit.chpl.scheduler.job.curesStatistics.email;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.IntStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.statistics.ListingCuresStatusStatisticsDAO;
import gov.healthit.chpl.domain.statistics.ListingCuresStatusStatistic;
import gov.healthit.chpl.util.HtmlEmailTemplate;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2(topic = "curesStatisticsEmailJobLogger")
public class ListingCuresStatusStatisticsHtmlCreator {
    private static final String HTML_DATE_FORMAT = "MM-dd-yyyy";
    private ListingCuresStatusStatisticsDAO listingCuresStatusStatisticsDao;
    private String emailBody;
    private String emailStyles;
    private DateTimeFormatter dateFormatter;

    @Autowired
    public ListingCuresStatusStatisticsHtmlCreator(ListingCuresStatusStatisticsDAO listingCuresStatusStatisticsDao,
            @Value("${curesStatisticsReport.listingCuresStatusStatistics.emailBody}") String emailBody,
            @Value("${email_styles}") String emailStyles) {
        this.listingCuresStatusStatisticsDao = listingCuresStatusStatisticsDao;
        this.emailBody = emailBody;
        this.emailStyles = emailStyles;
        this.dateFormatter = DateTimeFormatter.ofPattern(HTML_DATE_FORMAT);
    }

    public String createEmailBody() {
        List<ListingCuresStatusStatistic> statistics = null;
        LocalDate statisticDate = listingCuresStatusStatisticsDao.getDateOfMostRecentStatistics();
        if (statisticDate != null) {
            statistics = listingCuresStatusStatisticsDao.getStatisticsForDate(statisticDate);
            LOGGER.info("Generating HTML email text for " + statistics.size() + " statistics.");
        } else {
            LOGGER.error("No most statistics were found.");
        }
        return getEmailText(statisticDate, statistics);
    }

    private String getEmailText(LocalDate statisticsDate, List<ListingCuresStatusStatistic> statistics) {
        HtmlEmailTemplate email = new HtmlEmailTemplate();
        email.setStyles(emailStyles);
        email.setBody(getBody(statisticsDate, statistics));
        return email.build();
    }

    private String getBody(LocalDate statisticsDate, List<ListingCuresStatusStatistic> statistics) {
        return String.format(emailBody, formatStatisticsDate(statisticsDate), getTable(statistics));
    }

    private String formatStatisticsDate(LocalDate statisticsDate) {
        if (statisticsDate == null) {
            return "No Date Available";
        }
        return dateFormatter.format(statisticsDate);
    }

    private String getTable(List<ListingCuresStatusStatistic> statistics) {
        StringBuilder table = new StringBuilder();
        table.append("<table class='blueTable'>\n");
        table.append("    <thead>\n");
        table.append("        <tr>\n");
        table.append("            <th>\n");
        table.append("                Listings that are 2015 Cures Update\n");
        table.append("            </th>\n");
        table.append("            <th>\n");
        table.append("                Total # of 2015 Edition Listings\n");
        table.append("            </th>\n");
        table.append("        </tr>\n");
        table.append("    </thead>\n");
        table.append("    <tbody>\n");
        if (statistics != null && statistics.size() > 0) {
            IntStream.range(0, statistics.size())
                .forEach(index -> table.append(getTableRow((index % 2 == 0), statistics.get(index))));
        } else {
            table.append(getNoResultsTableRow());
        }
        table.append("    </tbody>\n");
        table.append("</table>\n");
        return table.toString();
    }

    private String getTableRow(boolean isEven, ListingCuresStatusStatistic statistic) {
        StringBuilder row = new StringBuilder();
        row.append("        <tr class='" + (isEven ? "even" : "odd") + "'>\n");
        row.append("            <td>\n");
        row.append("                " + statistic.getCuresListingCount() + "\n");
        row.append("            </td>\n");
        row.append("            <td>\n");
        row.append("                " + statistic.getTotalListingCount() + "\n");
        row.append("            </td>\n");
        row.append("        </tr>\n");
        return row.toString();
    }

    private String getNoResultsTableRow() {
        return "        <tr colspan='2'>No data available</tr>\n";
    }
}
