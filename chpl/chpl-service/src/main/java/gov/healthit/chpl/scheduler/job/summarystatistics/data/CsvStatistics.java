package gov.healthit.chpl.scheduler.job.summarystatistics.data;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CsvStatistics implements Serializable {
    private static final long serialVersionUID = -5219649276730810669L;

    private Date endDate;
    private Long totalDevelopers;
    private Long totalDevelopersWith2014Listings;
    private Long totalDevelopersWith2015Listings;

    private Long totalUniqueProducts;

    private Long totalUniqueProductsActive2014Listings;
    private Long totalUniqueProductsActive2015Listings;
    private Long totalUniqueProductsActiveListings;

    private Long totalListings;
    private Long total2011Listings;
    private Long total2014Listings;
    private Long total2015Listings;

    private Long totalSurveillanceActivities;
    private Long totalOpenSurveillanceActivities;
    private Long totalClosedSurveillanceActivities;
    private Long totalNonConformities;
    private Long totalOpenNonconformities;
    private Long totalClosedNonconformities;

    private Long totalCPs2014Listings;

    private Long totalCPsSuspended2014Listings;
    private Long totalCPs2015Listings;

    private Long totalCPsSuspended2015Listings;
    private Long totalActive2014Listings;
}
