package gov.healthit.chpl.dao.surveillance.report;

import java.util.List;

import gov.healthit.chpl.dto.surveillance.report.QuarterlyReportSurveillanceMapDTO;
import gov.healthit.chpl.dto.surveillance.report.SurveillanceOutcomeDTO;
import gov.healthit.chpl.dto.surveillance.report.SurveillanceProcessTypeDTO;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;

public interface QuarterlyReportSurveillanceMapDAO {
    public QuarterlyReportSurveillanceMapDTO getByReportAndSurveillance(Long quarterlyReportId, Long surveillanceId);
    public List<QuarterlyReportSurveillanceMapDTO> getByReportsAndSurveillance(List<Long> quarterlyReportIds, Long surveillanceId);
    public List<QuarterlyReportSurveillanceMapDTO> getByReport(Long quarterlyReportId);
    public List<QuarterlyReportSurveillanceMapDTO> getBySurveillance(Long surveillanceId);
    public QuarterlyReportSurveillanceMapDTO getById(Long id) throws EntityRetrievalException;
    public List<SurveillanceProcessTypeDTO> getSurveillanceProcessTypes();
    public List<SurveillanceOutcomeDTO> getSurveillanceOutcomes();
    public QuarterlyReportSurveillanceMapDTO create(QuarterlyReportSurveillanceMapDTO toCreate) throws EntityCreationException;
    public QuarterlyReportSurveillanceMapDTO update(QuarterlyReportSurveillanceMapDTO toUpdate) throws EntityRetrievalException;
    public void delete(Long idToDelete) throws EntityRetrievalException;
}
