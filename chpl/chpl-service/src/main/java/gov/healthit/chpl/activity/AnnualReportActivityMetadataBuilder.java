package gov.healthit.chpl.activity;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import gov.healthit.chpl.domain.activity.ActivityMetadata;
import gov.healthit.chpl.domain.activity.AnnualReportActivityMetadata;
import gov.healthit.chpl.dto.ActivityDTO;
import gov.healthit.chpl.surveillance.report.domain.AnnualReport;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component("annualReportActivityMetadataBuilder")
public class AnnualReportActivityMetadataBuilder extends ActivityMetadataBuilder {
    @Override
    protected void addConceptSpecificMetadata(ActivityDTO dto, ActivityMetadata metadata) {
        AnnualReportActivityMetadata annualReportActivityMetadata = (AnnualReportActivityMetadata) metadata;
        ObjectMapper jsonMapper = new ObjectMapper();

        AnnualReport report = null;
        if (dto.getNewData() != null) {
            try {
                report = jsonMapper.readValue(dto.getNewData(), AnnualReport.class);
            } catch (Exception e) {
                LOGGER.warn("Could not parse activity ID " + dto.getId() + " new data " + "as AnnualReport. "
                        + "JSON was: " + dto.getNewData());
            }
        } else if (dto.getOriginalData() != null) {
            try {
                report = jsonMapper.readValue(dto.getOriginalData(), AnnualReport.class);
            } catch (Exception e) {
                LOGGER.warn("Could not parse activity ID " + dto.getId() + " original data " + "as AnnualReport. "
                        + "JSON was: " + dto.getOriginalData());
            }
        }

        if (report != null) {
            parseReportMetadata(annualReportActivityMetadata, report);
        }
    }

    private void parseReportMetadata(AnnualReportActivityMetadata metadata, AnnualReport report) {
        if (report.getAcb() != null) {
            metadata.setAcb(report.getAcb());
        }
        metadata.setYear(report.getYear());
    }
}
