package gov.healthit.chpl.dao.surveillance.report;

import java.util.List;

import gov.healthit.chpl.dto.surveillance.report.AnnualReportDTO;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;

public interface AnnualReportDAO {
    public List<AnnualReportDTO> getAll();
    public AnnualReportDTO getByAcbAndYear(Long acbId, Integer year);
    public List<AnnualReportDTO> getByAcb(Long acbId);
    public AnnualReportDTO getById(Long id) throws EntityRetrievalException;
    public AnnualReportDTO create(AnnualReportDTO toCreate) throws EntityCreationException;
    public AnnualReportDTO update(AnnualReportDTO toUpdate) throws EntityRetrievalException;
    public void delete(Long idToDelete) throws EntityRetrievalException;
}
