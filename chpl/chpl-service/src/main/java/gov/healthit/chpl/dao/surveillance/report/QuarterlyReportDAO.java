package gov.healthit.chpl.dao.surveillance.report;

import java.util.List;

import gov.healthit.chpl.dto.surveillance.report.QuarterlyReportDTO;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;

public interface QuarterlyReportDAO {
    public List<QuarterlyReportDTO> getByAcbAndYear(Long acbId, Integer year);
    public List<QuarterlyReportDTO> getByAcb(Long acbId);
    public QuarterlyReportDTO getById(Long id) throws EntityRetrievalException;
    public QuarterlyReportDTO create(QuarterlyReportDTO toCreate) throws EntityCreationException;
    public QuarterlyReportDTO update(QuarterlyReportDTO toUpdate) throws EntityRetrievalException;
    public void delete(Long idToDelete) throws EntityRetrievalException;
}
