package gov.healthit.chpl.dao.surveillance.report;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.dto.surveillance.report.QuarterlyReportDTO;
import gov.healthit.chpl.entity.surveillance.report.QuarterlyReportEntity;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.util.AuthUtil;

@Repository("quarterlyReportDao")
public class QuarterlyReportDAOImpl extends BaseDAOImpl implements QuarterlyReportDAO {

    /**
     * Get the quarterly reports for a specific year and ACB.
     * There could be 0-4 quarterly reports.
     */
    @Override
    public List<QuarterlyReportDTO> getByAcbAndYear(final Long acbId, final Integer year) {
        String queryStr = "SELECT qr "
                + " FROM QuarterlyReportEntity qr "
                + " JOIN FETCH qr.quarter "
                + " JOIN FETCH qr.annualReport ar"
                + " JOIN FETCH ar.acb acb "
                + " WHERE qr.deleted = false "
                + " AND ar.year = :year "
                + " AND acb.id = :acbId";
        Query query = entityManager.createQuery(queryStr);
        query.setParameter("year", year);
        query.setParameter("acbId", acbId);
        List<QuarterlyReportEntity> entityResults = query.getResultList();
        List<QuarterlyReportDTO> results = new ArrayList<QuarterlyReportDTO>();
        if (entityResults != null && entityResults.size() > 0) {
            for (QuarterlyReportEntity entityResult : entityResults) {
                results.add(new QuarterlyReportDTO(entityResult));
            }
        }
        return results;
    }

    /**
     * Get all of the quarterly reports by ACB.
     * There could be 0 or more reports.
     */
    @Override
    public List<QuarterlyReportDTO> getByAcb(final Long acbId) {
        String queryStr = "SELECT qr "
                + " FROM QuarterlyReportEntity qr "
                + " JOIN FETCH qr.quarter "
                + " JOIN FETCH qr.annualReport ar"
                + " JOIN FETCH ar.acb acb "
                + " WHERE qr.deleted = false "
                + " AND acb.id = :acbId";
        Query query = entityManager.createQuery(queryStr);
        query.setParameter("acbId", acbId);
        List<QuarterlyReportEntity> entityResults = query.getResultList();
        List<QuarterlyReportDTO> results = new ArrayList<QuarterlyReportDTO>();
        if (entityResults != null && entityResults.size() > 0) {
            for (QuarterlyReportEntity entityResult : entityResults) {
                results.add(new QuarterlyReportDTO(entityResult));
            }
        }
        return results;
    }

    /**
     * Get a quarterly report by database ID.
     */
    @Override
    public QuarterlyReportDTO getById(final Long id) throws EntityRetrievalException {
        QuarterlyReportEntity entity = getEntityById(id);
        if (entity != null) {
            return new QuarterlyReportDTO(entity);
        }
        return null;
    }

    @Override
    public QuarterlyReportDTO create(final QuarterlyReportDTO toCreate) throws EntityCreationException {
        QuarterlyReportEntity toCreateEntity = new QuarterlyReportEntity();
        if (toCreate.getAnnualReport() == null) {
            throw new EntityCreationException("An annual report must be provided in order to create a quarterly report.");
        }
        if (toCreate.getQuarter() == null) {
            throw new EntityCreationException("A quarter must be provided in order to create a quarterly report.");
        }
        toCreateEntity.setAnnualReportId(toCreate.getAnnualReport().getId());
        toCreateEntity.setPrioritizedElementSummary(toCreate.getPrioritizedElementSummary());
        toCreateEntity.setQuarterId(toCreate.getQuarter().getId());
        toCreateEntity.setReactiveSummary(toCreate.getReactiveSummary());
        toCreateEntity.setTransparencyDisclosureSummary(toCreate.getTransparencyDisclosureSummary());
        toCreateEntity.setCreationDate(new Date());
        toCreateEntity.setLastModifiedUser(AuthUtil.getAuditId());

        super.create(toCreateEntity);
        toCreate.setId(toCreateEntity.getId());
        return toCreate;
    }

    @Override
    public QuarterlyReportDTO update(final QuarterlyReportDTO toUpdate) throws EntityRetrievalException {
        QuarterlyReportEntity toUpdateEntity = getEntityById(toUpdate.getId());
        toUpdateEntity.setPrioritizedElementSummary(toUpdate.getPrioritizedElementSummary());
        toUpdateEntity.setReactiveSummary(toUpdate.getReactiveSummary());
        toUpdateEntity.setTransparencyDisclosureSummary(toUpdate.getTransparencyDisclosureSummary());
        toUpdateEntity.setLastModifiedUser(AuthUtil.getAuditId());

        super.update(toUpdateEntity);
        return getById(toUpdateEntity.getId());
    }

    @Override
    public void delete(final Long idToDelete) throws EntityRetrievalException {
        QuarterlyReportEntity toDeleteEntity = getEntityById(idToDelete);
        toDeleteEntity.setDeleted(true);
        toDeleteEntity.setLastModifiedUser(AuthUtil.getAuditId());
        super.update(toDeleteEntity);
    }

    private QuarterlyReportEntity getEntityById(final Long id) throws EntityRetrievalException {
        String queryStr = "SELECT qr "
                + " FROM QuarterlyReportEntity qr "
                + " JOIN FETCH qr.quarter "
                + " JOIN FETCH qr.annualReport ar"
                + " JOIN FETCH ar.acb acb "
                + " WHERE qr.deleted = false "
                + " AND qr.id = :id";
        Query query = entityManager.createQuery(queryStr);
        query.setParameter("id", id);
        List<QuarterlyReportEntity> results = query.getResultList();
        if (results == null || results.size() == 0) {
            throw new EntityRetrievalException("No quarterly report with ID " + id + " exists.");
        }
        return results.get(0);
    }
}
