package gov.healthit.chpl.manager;

import java.io.IOException;
import java.util.List;

import org.apache.poi.ss.usermodel.Workbook;

import gov.healthit.chpl.dto.job.JobDTO;
import gov.healthit.chpl.dto.surveillance.report.AnnualReportDTO;
import gov.healthit.chpl.dto.surveillance.report.QuarterlyReportDTO;
import gov.healthit.chpl.dto.surveillance.report.QuarterlyReportExclusionDTO;
import gov.healthit.chpl.dto.surveillance.report.QuarterlyReportRelevantListingDTO;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.InvalidArgumentsException;
import gov.healthit.chpl.exception.UserRetrievalException;

public interface SurveillanceReportManager {
    public AnnualReportDTO createAnnualReport(AnnualReportDTO toCreate)
        throws EntityCreationException, InvalidArgumentsException;
    public AnnualReportDTO updateAnnualReport(AnnualReportDTO toUpdate) throws EntityRetrievalException;
    public void deleteAnnualReport(Long id) throws EntityRetrievalException;
    public List<AnnualReportDTO> getAnnualReports();
    public AnnualReportDTO getAnnualReport(Long id) throws EntityRetrievalException;
    public Workbook exportAnnualReport(Long id) throws EntityRetrievalException, IOException;
    public JobDTO exportAnnualReportAsBackgroundJob(Long id)
            throws EntityRetrievalException, EntityCreationException, UserRetrievalException, IOException;

    public QuarterlyReportDTO createQuarterlyReport(QuarterlyReportDTO toCreate)
            throws EntityCreationException, InvalidArgumentsException;
    public QuarterlyReportExclusionDTO createQuarterlyReportExclusion(QuarterlyReportDTO report,
            Long listingId, String reason) throws EntityCreationException, InvalidArgumentsException;
    public QuarterlyReportDTO updateQuarterlyReport(QuarterlyReportDTO toUpdate) throws EntityRetrievalException;
    public QuarterlyReportExclusionDTO updateQuarterlyReportExclusion(QuarterlyReportDTO report,
            Long listingId, String reason) throws EntityRetrievalException;
    public void deleteQuarterlyReport(Long id) throws EntityRetrievalException;
    public void deleteQuarterlyReportExclusion(Long reportId, Long listingId);
    public List<QuarterlyReportDTO> getQuarterlyReports();
    public List<QuarterlyReportDTO> getQuarterlyReports(Long acbId, Integer year);
    public QuarterlyReportExclusionDTO getExclusion(QuarterlyReportDTO report, Long listingId);
    public QuarterlyReportRelevantListingDTO getRelevantListing(QuarterlyReportDTO report, Long listingId);
    public List<QuarterlyReportRelevantListingDTO> getRelevantListings(QuarterlyReportDTO report);
    public QuarterlyReportDTO getQuarterlyReport(Long id) throws EntityRetrievalException;
    public Workbook exportQuarterlyReport(Long id) throws EntityRetrievalException, IOException;
    public JobDTO exportQuarterlyReportAsBackgroundJob(Long id)
            throws EntityRetrievalException, EntityCreationException, UserRetrievalException, IOException;
}
