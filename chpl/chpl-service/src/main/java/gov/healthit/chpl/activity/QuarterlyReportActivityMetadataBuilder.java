package gov.healthit.chpl.activity;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import gov.healthit.chpl.domain.activity.ActivityConcept;
import gov.healthit.chpl.domain.activity.ActivityMetadata;
import gov.healthit.chpl.domain.activity.QuarterlyReportActivityMetadata;
import gov.healthit.chpl.dto.ActivityDTO;
import gov.healthit.chpl.surveillance.report.dto.QuarterlyReportDTO;
import gov.healthit.chpl.surveillance.report.dto.QuarterlyReportRelevantListingDTO;

@Component("quarterlyReportActivityMetadataBuilder")
public class QuarterlyReportActivityMetadataBuilder extends ActivityMetadataBuilder {
    private static final Logger LOGGER = LogManager.getLogger(QuarterlyReportActivityMetadataBuilder.class);

    @Override
    protected void addConceptSpecificMetadata(final ActivityDTO dto, final ActivityMetadata metadata) {
        QuarterlyReportActivityMetadata quarterlyReportActivityMetadata = (QuarterlyReportActivityMetadata) metadata;
        ObjectMapper jsonMapper = new ObjectMapper();

        //either a report object or relevant listing object is in the activity
        QuarterlyReportDTO report = null;
        QuarterlyReportRelevantListingDTO listing = null;
        if (dto.getNewData() != null) {
            if (dto.getConcept() == ActivityConcept.QUARTERLY_REPORT_LISTING) {
                try {
                    listing = jsonMapper.readValue(dto.getNewData(), QuarterlyReportRelevantListingDTO.class);
                } catch (Exception e) {
                    LOGGER.warn("Could not parse activity ID " + dto.getId()
                            + " new data " + "as QuarterlyReportRelevantListingDTO. "
                            + "JSON was: " + dto.getNewData());
                }
            } else if (dto.getConcept() == ActivityConcept.QUARTERLY_REPORT) {
                try {
                    report = jsonMapper.readValue(dto.getNewData(), QuarterlyReportDTO.class);
                } catch (Exception e) {
                    LOGGER.warn("Could not parse activity ID " + dto.getId() + " new data " + "as QuarterlyReportDTO. "
                            + "JSON was: " + dto.getNewData());
                }
            }
        } else if (dto.getOriginalData() != null) {
            if (dto.getConcept() == ActivityConcept.QUARTERLY_REPORT_LISTING) {
                try {
                    listing = jsonMapper.readValue(dto.getOriginalData(), QuarterlyReportRelevantListingDTO.class);
                } catch (Exception e) {
                    LOGGER.warn("Could not parse activity ID " + dto.getId()
                            + " original data " + "as QuarterlyReportRelevantListingDTO. "
                            + "JSON was: " + dto.getOriginalData());
                }
            } else if (dto.getConcept() == ActivityConcept.QUARTERLY_REPORT) {
                try {
                    report = jsonMapper.readValue(dto.getOriginalData(), QuarterlyReportDTO.class);
                } catch (Exception e) {
                    LOGGER.warn("Could not parse activity ID " + dto.getId() + " original data " + "as QuarterlyReportDTO. "
                            + "JSON was: " + dto.getOriginalData());
                }
            }
        }

        if (report != null) {
            parseReportMetadata(quarterlyReportActivityMetadata, report);
        } else if (listing != null) {
            parseRelevantListingMetadata(quarterlyReportActivityMetadata, listing);
        }
    }

    private void parseReportMetadata(final QuarterlyReportActivityMetadata metadata, final QuarterlyReportDTO report) {
        if (report.getAcb() != null) {
            metadata.setAcb(report.getAcb());
        }
        if (report.getQuarter() != null) {
            metadata.setQuarterName(report.getQuarter().getName());
        }
        metadata.setYear(report.getYear());
    }

    private void parseRelevantListingMetadata(final QuarterlyReportActivityMetadata metadata,
            final QuarterlyReportRelevantListingDTO listing) {
        QuarterlyReportDTO report = listing.getQuarterlyReport();
        if (report != null) {
            parseReportMetadata(metadata, report);
        }
    }
}
