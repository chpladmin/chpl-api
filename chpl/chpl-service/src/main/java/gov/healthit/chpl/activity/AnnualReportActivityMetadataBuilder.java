package gov.healthit.chpl.activity;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import gov.healthit.chpl.dao.surveillance.report.AnnualReportDAO;
import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.domain.activity.ActivityMetadata;
import gov.healthit.chpl.domain.activity.AnnualReportActivityMetadata;
import gov.healthit.chpl.dto.ActivityDTO;
import gov.healthit.chpl.dto.auth.UserDTO;
import gov.healthit.chpl.dto.surveillance.report.AnnualReportDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;

@Component("annualReportActivityMetadataBuilder")
public class AnnualReportActivityMetadataBuilder extends ActivityMetadataBuilder {
    private static final Logger LOGGER = LogManager.getLogger(AnnualReportActivityMetadataBuilder.class);
    private AnnualReportDAO annualReportDao;

    @Autowired
    public AnnualReportActivityMetadataBuilder(final AnnualReportDAO annualReportDao) {
        super();
        this.annualReportDao = annualReportDao;
    }

    @Override
    protected void addConceptSpecificMetadata(final ActivityDTO dto, final ActivityMetadata metadata) {
        AnnualReportActivityMetadata annualReportActivityMetadata = (AnnualReportActivityMetadata) metadata;
        ObjectMapper jsonMapper = new ObjectMapper();

        AnnualReportDTO report = null;
        Long annualReportId = null;
        if (dto.getNewData() != null) {
            boolean parsed = false;
            try {
                report = jsonMapper.readValue(dto.getNewData(), AnnualReportDTO.class);
                parsed = true;
            } catch (Exception e) {
                LOGGER.warn("Could not parse activity ID " + dto.getId() + " new data " + "as AnnualReportDTO. "
                        + "JSON was: " + dto.getNewData());
            }
            if (!parsed) {
                try {
                    //if data is a user object then the activity was an export
                    jsonMapper.readValue(dto.getNewData(), UserDTO.class);
                    annualReportId = dto.getActivityObjectId();
                    parsed = true;
                } catch (Exception e) {
                    LOGGER.warn("Could not parse activity ID " + dto.getId() + " new data " + "as UserDTO. "
                            + "JSON was: " + dto.getNewData());
                }
            }
        } else if (dto.getOriginalData() != null) {
            boolean parsed = false;
            try {
                jsonMapper.readValue(dto.getOriginalData(), AnnualReportDTO.class);
                parsed = true;
            } catch (Exception e) {
                LOGGER.warn("Could not parse activity ID " + dto.getId() + " original data " + "as AnnualReportDTO. "
                        + "JSON was: " + dto.getOriginalData());
            }
            if (!parsed) {
                try {
                    //if data is a user object then the activity was an export
                    jsonMapper.readValue(dto.getOriginalData(), UserDTO.class);
                    annualReportId = dto.getActivityObjectId();
                    parsed = true;
                } catch (Exception e) {
                    LOGGER.warn("Could not parse activity ID " + dto.getId() + " original data " + "as UserDTO. "
                            + "JSON was: " + dto.getNewData());
                }
            }
        }

        if (report != null) {
            parseReportMetadata(annualReportActivityMetadata, report);
        } else if (annualReportId != null) {
            fetchReportMetadata(annualReportActivityMetadata, annualReportId);
        }
    }

    private void parseReportMetadata(final AnnualReportActivityMetadata metadata, final AnnualReportDTO report) {
        if (report.getAcb() != null) {
            CertificationBody acb = new CertificationBody();
            acb.setId(report.getAcb().getId());
            acb.setName(report.getAcb().getName());
            metadata.setAcb(acb);
        }
        metadata.setYear(report.getYear());
    }

    private void fetchReportMetadata(final AnnualReportActivityMetadata metadata, final Long reportId) {
        AnnualReportDTO report = null;
        try {
            report = annualReportDao.getById(reportId);
        } catch (EntityRetrievalException ignore) { }

        if (report != null) {
            parseReportMetadata(metadata, report);
        }
    }
}
