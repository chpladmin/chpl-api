package gov.healthit.chpl.activity;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import gov.healthit.chpl.dao.surveillance.report.QuarterlyReportDAO;
import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.domain.activity.ActivityMetadata;
import gov.healthit.chpl.domain.activity.QuarterlyReportActivityMetadata;
import gov.healthit.chpl.dto.ActivityDTO;
import gov.healthit.chpl.dto.auth.UserDTO;
import gov.healthit.chpl.dto.surveillance.report.QuarterlyReportDTO;
import gov.healthit.chpl.dto.surveillance.report.QuarterlyReportRelevantListingDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;

@Component("quarterlyReportActivityMetadataBuilder")
public class QuarterlyReportActivityMetadataBuilder extends ActivityMetadataBuilder {
    private static final Logger LOGGER = LogManager.getLogger(QuarterlyReportActivityMetadataBuilder.class);
    private QuarterlyReportDAO quarterlyReportDao;

    @Autowired
    public QuarterlyReportActivityMetadataBuilder(final QuarterlyReportDAO quarterlyReportDao) {
        super();
        this.quarterlyReportDao = quarterlyReportDao;
    }

    @Override
    protected void addConceptSpecificMetadata(final ActivityDTO dto, final ActivityMetadata metadata) {
        QuarterlyReportActivityMetadata quarterlyReportActivityMetadata = (QuarterlyReportActivityMetadata) metadata;
        ObjectMapper jsonMapper = new ObjectMapper();

        //either a report object or relevant listing object is in the activity
        QuarterlyReportDTO report = null;
        QuarterlyReportRelevantListingDTO listing = null;
        Long quarterlyReportId = null;
        if (dto.getNewData() != null) {
            boolean parsed = false;
            try {
                report = jsonMapper.readValue(dto.getNewData(), QuarterlyReportDTO.class);
                parsed = true;
            } catch (Exception e) {
                LOGGER.warn("Could not parse activity ID " + dto.getId() + " new data " + "as QuarterlyReportDTO. "
                        + "JSON was: " + dto.getNewData());
            }
            if (!parsed) {
                try {
                    listing = jsonMapper.readValue(dto.getNewData(), QuarterlyReportRelevantListingDTO.class);
                    parsed = true;
                } catch (Exception e) {
                    LOGGER.warn("Could not parse activity ID " + dto.getId()
                            + " new data " + "as QuarterlyReportRelevantListingDTO. "
                            + "JSON was: " + dto.getNewData());
                }
            }
            if (!parsed) {
                try {
                    //data is a user object when the listing is exported
                    //we don't need to get anything from this
                    jsonMapper.readValue(dto.getNewData(), UserDTO.class);
                    quarterlyReportId = dto.getActivityObjectId();
                    parsed = true;
                } catch (Exception e) {
                    LOGGER.warn("Could not parse activity ID " + dto.getId()
                            + " new data " + "as UserDTO. "
                            + "JSON was: " + dto.getNewData());
                }
            }
        } else if (dto.getOriginalData() != null) {
            boolean parsed = false;
            try {
                report = jsonMapper.readValue(dto.getOriginalData(), QuarterlyReportDTO.class);
                parsed = true;
            } catch (Exception e) {
                LOGGER.warn("Could not parse activity ID " + dto.getId() + " original data " + "as QuarterlyReportDTO. "
                        + "JSON was: " + dto.getOriginalData());
            }
            if (!parsed) {
                try {
                    listing = jsonMapper.readValue(dto.getOriginalData(), QuarterlyReportRelevantListingDTO.class);
                    parsed = true;
                } catch (Exception e) {
                    LOGGER.warn("Could not parse activity ID " + dto.getId()
                            + " original data " + "as QuarterlyReportRelevantListingDTO. "
                            + "JSON was: " + dto.getOriginalData());
                }
            }
            if (!parsed) {
                try {
                    //data is a user object when the listing is exported
                    //we don't need to get anything from this
                    jsonMapper.readValue(dto.getNewData(), UserDTO.class);
                    quarterlyReportId = dto.getActivityObjectId();
                    parsed = true;
                } catch (Exception e) {
                    LOGGER.warn("Could not parse activity ID " + dto.getId()
                            + " original data " + "as UserDTO. "
                            + "JSON was: " + dto.getOriginalData());
                }
            }
        }

        if (report != null) {
            parseReportMetadata(quarterlyReportActivityMetadata, report);
        } else if (listing != null) {
            parseRelevantListingMetadata(quarterlyReportActivityMetadata, listing);
        } else if (quarterlyReportId != null) {
            fetchReportMetadata(quarterlyReportActivityMetadata, quarterlyReportId);
        }
    }

    private void parseReportMetadata(final QuarterlyReportActivityMetadata metadata, final QuarterlyReportDTO report) {
        if (report.getAcb() != null) {
            CertificationBody acb = new CertificationBody();
            acb.setId(report.getAcb().getId());
            acb.setName(report.getAcb().getName());
            metadata.setAcb(acb);
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

    private void fetchReportMetadata(final QuarterlyReportActivityMetadata metadata, final Long reportId) {
        QuarterlyReportDTO report = null;
        try {
            report = quarterlyReportDao.getById(reportId);
        } catch (EntityRetrievalException ignore) { }

        if (report != null) {
            parseReportMetadata(metadata, report);
        }
    }
}
