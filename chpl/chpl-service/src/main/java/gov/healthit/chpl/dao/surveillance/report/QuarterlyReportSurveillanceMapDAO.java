package gov.healthit.chpl.dao.surveillance.report;

import java.util.List;

import gov.healthit.chpl.dto.surveillance.report.PrivilegedSurveillanceDTO;
import gov.healthit.chpl.dto.surveillance.report.SurveillanceOutcomeDTO;
import gov.healthit.chpl.dto.surveillance.report.SurveillanceProcessTypeDTO;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;

public interface QuarterlyReportSurveillanceMapDAO {
    public PrivilegedSurveillanceDTO getByReportAndSurveillance(Long quarterlyReportId, Long surveillanceId);
    public List<PrivilegedSurveillanceDTO> getByReportsAndSurveillance(List<Long> quarterlyReportIds, Long surveillanceId);
    public List<PrivilegedSurveillanceDTO> getByReport(Long quarterlyReportId);
    public List<PrivilegedSurveillanceDTO> getBySurveillance(Long surveillanceId);
    public PrivilegedSurveillanceDTO getById(Long id) throws EntityRetrievalException;
    public List<SurveillanceProcessTypeDTO> getSurveillanceProcessTypes();
    public List<SurveillanceOutcomeDTO> getSurveillanceOutcomes();
    public PrivilegedSurveillanceDTO create(PrivilegedSurveillanceDTO toCreate) throws EntityCreationException;
    public PrivilegedSurveillanceDTO update(PrivilegedSurveillanceDTO toUpdate) throws EntityRetrievalException;
    public void delete(Long idToDelete) throws EntityRetrievalException;
}
