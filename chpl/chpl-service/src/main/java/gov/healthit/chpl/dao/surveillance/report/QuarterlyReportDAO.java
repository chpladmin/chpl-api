package gov.healthit.chpl.dao.surveillance.report;

import java.util.Date;
import java.util.List;

import gov.healthit.chpl.dto.surveillance.report.QuarterlyReportDTO;
import gov.healthit.chpl.dto.surveillance.report.QuarterlyReportExclusionDTO;
import gov.healthit.chpl.dto.surveillance.report.QuarterlyReportRelevantListingDTO;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;

public interface QuarterlyReportDAO {
    public QuarterlyReportDTO getByQuarterAndAcbAndYear(Long quarterId, Long acbId, Integer year);
    public List<QuarterlyReportDTO> getByAcbAndYear(Long acbId, Integer year);
    public List<QuarterlyReportDTO> getByAcb(Long acbId);
    public List<QuarterlyReportDTO> getAll();
    public QuarterlyReportDTO getById(Long id) throws EntityRetrievalException;
    public boolean isListingRelevant(Long listingId, Date startDate, Date endDate);
    public List<QuarterlyReportRelevantListingDTO> getRelevantListings(QuarterlyReportDTO quarterlyReport);
    public QuarterlyReportRelevantListingDTO getRelevantListing(Long listingId, Date startDate, Date endDate);
    public List<QuarterlyReportExclusionDTO> getExclusions(Long quarterlyReportId);
    public QuarterlyReportExclusionDTO getExclusion(Long quarterlyReportId, Long listingId);
    public QuarterlyReportDTO create(QuarterlyReportDTO toCreate) throws EntityCreationException;
    public QuarterlyReportExclusionDTO createExclusion(QuarterlyReportExclusionDTO toCreate)
            throws EntityCreationException;
    public QuarterlyReportDTO update(QuarterlyReportDTO toUpdate) throws EntityRetrievalException;
    public QuarterlyReportExclusionDTO updateExclusion(QuarterlyReportExclusionDTO toUpdate)
            throws EntityRetrievalException;
    public void delete(Long idToDelete) throws EntityRetrievalException;
    public void deleteExclusion(Long idToDelete) throws EntityRetrievalException;
}
