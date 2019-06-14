package gov.healthit.chpl.dao.surveillance.report;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.dto.surveillance.report.AnnualReportDTO;
import gov.healthit.chpl.entity.surveillance.report.AnnualReportEntity;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.util.AuthUtil;

@Repository("annualReportDao")
public class AnnualReportDAOImpl extends BaseDAOImpl implements AnnualReportDAO {

    @Override
    public List<AnnualReportDTO> getAll() {
        String queryStr = "SELECT ar "
                + " FROM AnnualReportEntity ar "
                + " JOIN FETCH ar.acb acb "
                + " WHERE ar.deleted = false";
        Query query = entityManager.createQuery(queryStr);
        List<AnnualReportEntity> entityResults = query.getResultList();
        List<AnnualReportDTO> results = new ArrayList<AnnualReportDTO>();
        if (entityResults != null && entityResults.size() > 0) {
            for (AnnualReportEntity entityResult : entityResults) {
                results.add(new AnnualReportDTO(entityResult));
            }
        }
        return results;
    }

    /**
     * Get the report for a specific year and ACB.
     * There should only be zero or one result.
     */
    @Override
    public AnnualReportDTO getByAcbAndYear(final Long acbId, final Integer year) {
        String queryStr = "SELECT ar "
                + " FROM AnnualReportEntity ar "
                + " JOIN FETCH ar.acb acb "
                + " WHERE ar.deleted = false "
                + " AND ar.year = :year "
                + " AND acb.id = :acbId";
        Query query = entityManager.createQuery(queryStr);
        query.setParameter("year", year);
        query.setParameter("acbId", acbId);
        List<AnnualReportEntity> entityResults = query.getResultList();
        AnnualReportDTO result = null;
        if (entityResults != null && entityResults.size() > 0) {
            result = new AnnualReportDTO(entityResults.get(0));
        }
        return result;
    }

    /**
     * Get all of the annual reports by ACB.
     * There could be 0 or more reports.
     */
    @Override
    public List<AnnualReportDTO> getByAcb(final Long acbId) {
        String queryStr = "SELECT ar "
                + " FROM AnnualReportEntity ar "
                + " JOIN FETCH ar.acb acb "
                + " WHERE ar.deleted = false "
                + " AND acb.id = :acbId";
        Query query = entityManager.createQuery(queryStr);
        query.setParameter("acbId", acbId);
        List<AnnualReportEntity> entityResults = query.getResultList();
        List<AnnualReportDTO> results = new ArrayList<AnnualReportDTO>();
        if (entityResults != null && entityResults.size() > 0) {
            for (AnnualReportEntity entityResult : entityResults) {
                results.add(new AnnualReportDTO(entityResult));
            }
        }
        return results;
    }

    /**
     * Get an annual report by database ID.
     */
    @Override
    public AnnualReportDTO getById(final Long id) throws EntityRetrievalException {
        AnnualReportEntity entity = getEntityById(id);
        if (entity != null) {
            return new AnnualReportDTO(entity);
        }
        return null;
    }

    @Override
    public AnnualReportDTO create(final AnnualReportDTO toCreate) throws EntityCreationException {
        AnnualReportEntity toCreateEntity = new AnnualReportEntity();
        if (toCreate.getAcb() == null) {
            throw new EntityCreationException("Missing ACB ID to create annual report.");
        }
        toCreateEntity.setCertificationBodyId(toCreate.getAcb().getId());
        toCreateEntity.setYear(toCreate.getYear());
        toCreateEntity.setFindingsSummary(toCreate.getFindingsSummary());
        toCreateEntity.setObstacleSummary(toCreate.getObstacleSummary());
        toCreateEntity.setCreationDate(new Date());
        toCreateEntity.setLastModifiedUser(AuthUtil.getAuditId());

        super.create(toCreateEntity);
        toCreate.setId(toCreateEntity.getId());
        return toCreate;
    }

    @Override
    public AnnualReportDTO update(final AnnualReportDTO toUpdate) throws EntityRetrievalException {
        AnnualReportEntity toUpdateEntity = getEntityById(toUpdate.getId());
        toUpdateEntity.setYear(toUpdate.getYear());
        toUpdateEntity.setFindingsSummary(toUpdate.getFindingsSummary());
        toUpdateEntity.setObstacleSummary(toUpdate.getObstacleSummary());
        toUpdateEntity.setLastModifiedUser(AuthUtil.getAuditId());

        super.update(toUpdateEntity);
        return getById(toUpdateEntity.getId());
    }

    @Override
    public void delete(final Long idToDelete) throws EntityRetrievalException {
        AnnualReportEntity toDeleteEntity = getEntityById(idToDelete);
        toDeleteEntity.setDeleted(true);
        toDeleteEntity.setLastModifiedUser(AuthUtil.getAuditId());
        super.update(toDeleteEntity);
    }

    private AnnualReportEntity getEntityById(final Long id) throws EntityRetrievalException {
        String queryStr = "SELECT ar "
                + " FROM AnnualReportEntity ar "
                + " JOIN FETCH ar.acb "
                + " WHERE ar.deleted = false "
                + " AND ar.id = :id";
        Query query = entityManager.createQuery(queryStr);
        query.setParameter("id", id);
        List<AnnualReportEntity> results = query.getResultList();
        if (results == null || results.size() == 0) {
            throw new EntityRetrievalException("No annual report with ID " + id + " exists.");
        }
        return results.get(0);
    }
}
