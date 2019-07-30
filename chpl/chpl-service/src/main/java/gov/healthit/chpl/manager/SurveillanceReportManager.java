package gov.healthit.chpl.manager;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.core.JsonProcessingException;

import gov.healthit.chpl.domain.KeyValueModel;
import gov.healthit.chpl.dto.job.JobDTO;
import gov.healthit.chpl.dto.surveillance.report.AnnualReportDTO;
import gov.healthit.chpl.dto.surveillance.report.QuarterlyReportDTO;
import gov.healthit.chpl.dto.surveillance.report.QuarterlyReportExclusionDTO;
import gov.healthit.chpl.dto.surveillance.report.QuarterlyReportRelevantListingDTO;
import gov.healthit.chpl.dto.surveillance.report.PrivilegedSurveillanceDTO;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.InvalidArgumentsException;
import gov.healthit.chpl.exception.UserRetrievalException;

public interface SurveillanceReportManager {
    Set<KeyValueModel> getSurveillanceOutcomes();
    Set<KeyValueModel> getSurveillanceProcessTypes();
    AnnualReportDTO createAnnualReport(AnnualReportDTO toCreate)
        throws EntityCreationException, InvalidArgumentsException, JsonProcessingException, EntityRetrievalException;
    AnnualReportDTO updateAnnualReport(AnnualReportDTO toUpdate)
            throws EntityRetrievalException, JsonProcessingException, EntityCreationException;
    void deleteAnnualReport(Long id)
            throws EntityRetrievalException, EntityCreationException, JsonProcessingException;
    List<AnnualReportDTO> getAnnualReports();
    AnnualReportDTO getAnnualReport(Long id) throws EntityRetrievalException;
    JobDTO exportAnnualReportAsBackgroundJob(Long id)
            throws EntityRetrievalException, EntityCreationException, UserRetrievalException, IOException;

    QuarterlyReportDTO createQuarterlyReport(QuarterlyReportDTO toCreate)
            throws EntityCreationException, InvalidArgumentsException, JsonProcessingException, EntityRetrievalException;
    QuarterlyReportExclusionDTO createQuarterlyReportExclusion(QuarterlyReportDTO report,
            Long listingId, String reason) throws EntityCreationException, InvalidArgumentsException,
            JsonProcessingException, EntityRetrievalException;
    QuarterlyReportDTO updateQuarterlyReport(QuarterlyReportDTO toUpdate)
            throws EntityRetrievalException, JsonProcessingException, EntityCreationException;
    QuarterlyReportExclusionDTO updateQuarterlyReportExclusion(QuarterlyReportDTO report,
            Long listingId, String reason) throws EntityRetrievalException, JsonProcessingException, EntityCreationException;
    PrivilegedSurveillanceDTO createOrUpdateQuarterlyReportSurveillanceMap(PrivilegedSurveillanceDTO toUpdate)
            throws EntityCreationException, EntityRetrievalException, JsonProcessingException;
    void deleteQuarterlyReport(Long id) throws EntityRetrievalException, EntityCreationException, JsonProcessingException;
    void deleteQuarterlyReportExclusion(Long reportId, Long listingId)
            throws JsonProcessingException, EntityRetrievalException, EntityCreationException;
    List<QuarterlyReportDTO> getQuarterlyReports();
    List<QuarterlyReportDTO> getQuarterlyReports(Long acbId, Integer year);
    QuarterlyReportExclusionDTO getExclusion(QuarterlyReportDTO report, Long listingId);
    QuarterlyReportRelevantListingDTO getRelevantListing(QuarterlyReportDTO report, Long listingId);
    List<QuarterlyReportRelevantListingDTO> getRelevantListings(QuarterlyReportDTO report);
    List<QuarterlyReportRelevantListingDTO> getListingsWithRelevantSurveillance(QuarterlyReportDTO report);
    QuarterlyReportDTO getQuarterlyReport(Long id) throws EntityRetrievalException;
    JobDTO exportQuarterlyReportAsBackgroundJob(Long id)
            throws EntityRetrievalException, EntityCreationException, UserRetrievalException, IOException;
}
