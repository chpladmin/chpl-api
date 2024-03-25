package gov.healthit.chpl.activity;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import gov.healthit.chpl.domain.activity.ActivityConcept;
import gov.healthit.chpl.domain.activity.ActivityMetadata;
import gov.healthit.chpl.domain.activity.QuarterlyReportActivityMetadata;
import gov.healthit.chpl.dto.ActivityDTO;
import gov.healthit.chpl.surveillance.report.domain.QuarterlyReport;
import gov.healthit.chpl.surveillance.report.domain.RelevantListing;

@Component("quarterlyReportActivityMetadataBuilder")
public class QuarterlyReportActivityMetadataBuilder extends ActivityMetadataBuilder {
    private static final Logger LOGGER = LogManager.getLogger(QuarterlyReportActivityMetadataBuilder.class);

    @Override
    protected void addConceptSpecificMetadata(ActivityDTO activity, ActivityMetadata metadata) {
        QuarterlyReportActivityMetadata quarterlyReportActivityMetadata = (QuarterlyReportActivityMetadata) metadata;
        ObjectMapper jsonMapper = new ObjectMapper();

        //either a report object or relevant listing object is in the activity
        QuarterlyReport report = null;
        RelevantListing listing = null;
        if (activity.getNewData() != null) {
            if (activity.getConcept() == ActivityConcept.QUARTERLY_REPORT_LISTING) {
                try {
                    listing = jsonMapper.readValue(activity.getNewData(), RelevantListing.class);
                } catch (Exception e) {
                    LOGGER.warn("Could not parse activity ID " + activity.getId()
                            + " new data " + "as RelevantListing. "
                            + "JSON was: " + activity.getNewData());
                }
            } else if (activity.getConcept() == ActivityConcept.QUARTERLY_REPORT) {
                try {
                    report = jsonMapper.readValue(activity.getNewData(), QuarterlyReport.class);
                } catch (Exception e) {
                    LOGGER.warn("Could not parse activity ID " + activity.getId() + " new data " + "as QuarterlyReport. "
                            + "JSON was: " + activity.getNewData());
                }
            }
        } else if (activity.getOriginalData() != null) {
            if (activity.getConcept() == ActivityConcept.QUARTERLY_REPORT_LISTING) {
                try {
                    listing = jsonMapper.readValue(activity.getOriginalData(), RelevantListing.class);
                } catch (Exception e) {
                    LOGGER.warn("Could not parse activity ID " + activity.getId()
                            + " original data " + "as RelevantListing. "
                            + "JSON was: " + activity.getOriginalData());
                }
            } else if (activity.getConcept() == ActivityConcept.QUARTERLY_REPORT) {
                try {
                    report = jsonMapper.readValue(activity.getOriginalData(), QuarterlyReport.class);
                } catch (Exception e) {
                    LOGGER.warn("Could not parse activity ID " + activity.getId() + " original data " + "as QuarterlyReport. "
                            + "JSON was: " + activity.getOriginalData());
                }
            }
        }

        if (report != null) {
            parseReportMetadata(quarterlyReportActivityMetadata, report);
        } else if (listing != null) {
            parseRelevantListingMetadata(quarterlyReportActivityMetadata, listing);
        }
    }

    private void parseReportMetadata(QuarterlyReportActivityMetadata metadata, QuarterlyReport report) {
        if (report.getAcb() != null) {
            metadata.setAcb(report.getAcb());
        }
        if (report.getQuarter() != null) {
            metadata.setQuarterName(report.getQuarter());
        }
        metadata.setYear(report.getYear());
    }

    private void parseRelevantListingMetadata(QuarterlyReportActivityMetadata metadata, RelevantListing listing) {
        QuarterlyReport report = listing.getQuarterlyReport();
        if (report != null) {
            parseReportMetadata(metadata, report);
        }
    }
}
