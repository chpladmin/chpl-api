package gov.healthit.chpl.activity;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.domain.activity.ActivityMetadata;
import gov.healthit.chpl.domain.activity.AnnualReportActivityMetadata;
import gov.healthit.chpl.dto.ActivityDTO;
import gov.healthit.chpl.surveillance.report.dto.AnnualReportDTO;

@Component("annualReportActivityMetadataBuilder")
public class AnnualReportActivityMetadataBuilder extends ActivityMetadataBuilder {
    private static final Logger LOGGER = LogManager.getLogger(AnnualReportActivityMetadataBuilder.class);

    @Override
    protected void addConceptSpecificMetadata(final ActivityDTO dto, final ActivityMetadata metadata) {
        AnnualReportActivityMetadata annualReportActivityMetadata = (AnnualReportActivityMetadata) metadata;
        ObjectMapper jsonMapper = new ObjectMapper();

        AnnualReportDTO report = null;
        if (dto.getNewData() != null) {
            try {
                report = jsonMapper.readValue(dto.getNewData(), AnnualReportDTO.class);
            } catch (Exception e) {
                LOGGER.warn("Could not parse activity ID " + dto.getId() + " new data " + "as AnnualReportDTO. "
                        + "JSON was: " + dto.getNewData());
            }
        } else if (dto.getOriginalData() != null) {
            try {
                report = jsonMapper.readValue(dto.getOriginalData(), AnnualReportDTO.class);
            } catch (Exception e) {
                LOGGER.warn("Could not parse activity ID " + dto.getId() + " original data " + "as AnnualReportDTO. "
                        + "JSON was: " + dto.getOriginalData());
            }
        }

        if (report != null) {
            parseReportMetadata(annualReportActivityMetadata, report);
        }
    }

    private void parseReportMetadata(final AnnualReportActivityMetadata metadata, final AnnualReportDTO report) {
        if (report.getAcb() != null) {
            CertificationBody acb = new CertificationBody(report.getAcb());
            metadata.setAcb(acb);
        }
        metadata.setYear(report.getYear());
    }
}
