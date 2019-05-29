package gov.healthit.chpl.manager;

import java.util.List;

import org.apache.poi.ss.usermodel.Workbook;

import gov.healthit.chpl.dto.surveillance.report.QuarterlyReportDTO;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.InvalidArgumentsException;

public interface SurveillanceReportManager {
    public QuarterlyReportDTO createQuarterlyReport(QuarterlyReportDTO toCreate)
            throws EntityCreationException, InvalidArgumentsException;
    public QuarterlyReportDTO updateQuarterlyReport(QuarterlyReportDTO toUpdate) throws EntityRetrievalException;
    public void deleteQuarterlyReport(Long id) throws EntityRetrievalException;
    public List<QuarterlyReportDTO> getQuarterlyReports();
    public QuarterlyReportDTO getQuarterlyReport(Long id) throws EntityRetrievalException;
    public Workbook exportQuarterlyReport(Long id);
}
