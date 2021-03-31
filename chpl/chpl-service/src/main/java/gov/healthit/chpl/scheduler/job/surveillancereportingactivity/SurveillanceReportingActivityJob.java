package gov.healthit.chpl.scheduler.job.surveillancereportingactivity;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.surveillance.Surveillance;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.manager.CertifiedProductDetailsManager;
import lombok.extern.log4j.Log4j2;

@Log4j2()
public class SurveillanceReportingActivityJob implements Job {
    @Autowired
    private CertifiedProductDAO certifiedProductDAO;

    @Autowired
    private CertifiedProductDetailsManager certifiedProductDetailsManager;

    @Value("${executorThreadCountForQuartzJobs}")
    private Integer threadCount;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);

        LOGGER.info("********* Starting the Surveillance Reporting Activity job. *********");
        try {

            List<CertifiedProductSearchDetails> listings = getListingsBasedOnHavingSurveillance(
                    getListingsWithSurveillance().stream()
                            .map(cp -> cp.getId())
                            .collect(Collectors.toList()));

            List<Surveillance> surveillances = filterSurveillancesBasedOnDates(listings, getStartDate(context), getEndDate(context));
        } catch (Exception e) {

        }
        LOGGER.info("********* Completed the Surveillance Reporting Activity job. *********");

    }



    private List<CertifiedProductDetailsDTO> getListingsWithSurveillance() throws EntityRetrievalException {
        LOGGER.info("Finding all listings with surveillance.");
        List<CertifiedProductDetailsDTO> listings = certifiedProductDAO.findWithSurveillance();
        LOGGER.info("Found " + listings.size() + " listings with surveillance.");
        return listings;
    }

    private List<CertifiedProductSearchDetails> getListingsBasedOnHavingSurveillance(List<Long> listingIds) {
      //Using a `parallelStream` where we supply a ThreadPool to control the number of threads being used
        ForkJoinPool customThreadPool = new ForkJoinPool(threadCount);

        try {
            return customThreadPool.submit(() ->
            listingIds.parallelStream()
            .map(id -> getCertifiedProductSearchDetails(id))
            .peek(id -> LOGGER.info("Listing: " + id + " - Retreived details"))
            .collect(Collectors.toList())).get();
        } catch (Exception e) {
            LOGGER.error("Could not retrieve details for listings.", e);
            throw new RuntimeException("Could not retrieve details for listings.", e);
        }
    }

    private CertifiedProductSearchDetails getCertifiedProductSearchDetails(Long listingId) {
        try {
            CertifiedProductSearchDetails detail = certifiedProductDetailsManager.getCertifiedProductDetails(listingId);
            return detail;
        } catch (Exception e) {
            LOGGER.error("Could not retrieve the details for listing: " + listingId);
            throw new RuntimeException(e);
        }
    }

    private List<Surveillance> filterSurveillancesBasedOnDates(List<CertifiedProductSearchDetails> listings, LocalDate startDate, LocalDate endDate) {
        return listings.stream()
                .flatMap(listing -> listing.getSurveillance().stream())
                .filter(surv -> isSurveillanceOpenBetweenDates(surv, startDate, endDate))
                .collect(Collectors.toList());
    }

    private Boolean isSurveillanceOpenBetweenDates(Surveillance surveillance, LocalDate startDate, LocalDate endDate) {
        LocalDate surveillanceStartDate = surveillance.getStartDate() != null ? convertToLocalDate(surveillance.getStartDate()) : LocalDate.MIN;
        LocalDate surveillanceEndDate = surveillance.getEndDate() != null ? convertToLocalDate(surveillance.getEndDate()) : LocalDate.MAX;

        //Logic taken from https://stackoverflow.com/questions/325933/determine-whether-two-date-ranges-overlap
        return startDate.isBefore(surveillanceEndDate) && surveillanceStartDate.isBefore(endDate);
    }

    private LocalDate convertToLocalDate(Date dateToConvert) {
        return dateToConvert.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
    }

    private LocalDate getStartDate(JobExecutionContext context) {
        return LocalDate.parse(context.getMergedJobDataMap().getString("startDate"));
    }

    private LocalDate getEndDate(JobExecutionContext context) {
        return LocalDate.parse(context.getMergedJobDataMap().getString("endDate"));
    }
}
