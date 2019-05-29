package gov.healthit.chpl.manager.impl;

import java.util.List;

import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.dao.surveillance.report.QuarterlyReportDAO;
import gov.healthit.chpl.dto.surveillance.report.QuarterlyReportDTO;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.manager.SurveillanceReportManager;

public class SurveillanceReportManagerImpl implements SurveillanceReportManager {
    private QuarterlyReportDAO quarterlyDao;

    @Autowired
    public SurveillanceReportManagerImpl(final QuarterlyReportDAO quarterlyDao) {
        this.quarterlyDao = quarterlyDao;
    }

    @Transactional
    public QuarterlyReportDTO createQuarterlyReport(final QuarterlyReportDTO toCreate)
    throws EntityCreationException {
        QuarterlyReportDTO created = quarterlyDao.create(toCreate);
        return created;
    }

    @Transactional
    public QuarterlyReportDTO updateQuarterlyReport(final QuarterlyReportDTO toUpdate) 
    throws EntityRetrievalException {
        QuarterlyReportDTO updated = quarterlyDao.update(toUpdate);
        return updated;
    }

    public void deleteQuarterlyReport(final Long id) {
        //TODO
    }

    public List<QuarterlyReportDTO> getQuarterlyReports() {
        //TODO
        return null;
    }

    public QuarterlyReportDTO getQuarterlyReport(Long id) throws EntityRetrievalException {
        return quarterlyDao.getById(id);
    }

    public Workbook exportQuarterlyReport(final Long id) {
        //TODO
        return null;
    }
}
