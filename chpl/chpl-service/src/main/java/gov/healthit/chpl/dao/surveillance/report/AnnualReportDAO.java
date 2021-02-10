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
public class AnnualReportDAO extends BaseDAOImpl {
    private static final String ANNUAL_REPORT_HQL = "SELECT ar "
            + " FROM AnnualReportEntity ar "
            + " JOIN FETCH ar.acb acb "
            + " LEFT JOIN FETCH acb.address "
            + " WHERE ar.deleted = false ";

    public List<AnnualReportDTO> getAll() {
        Query query = entityManager.createQuery(ANNUAL_REPORT_HQL);
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
    public AnnualReportDTO getByAcbAndYear(Long acbId, Integer year) {
        String queryStr = ANNUAL_REPORT_HQL
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

    public List<AnnualReportDTO> getByAcb(Long acbId) {
        String queryStr = ANNUAL_REPORT_HQL
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

    public AnnualReportDTO getById(Long id) throws EntityRetrievalException {
        AnnualReportEntity entity = getEntityById(id);
        if (entity != null) {
            return new AnnualReportDTO(entity);
        }
        return null;
    }

    public AnnualReportDTO create(AnnualReportDTO toCreate) throws EntityCreationException {
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

        AnnualReportDTO createdDto = null;
        try {
            AnnualReportEntity createdEntity = getEntityById(toCreateEntity.getId());
            createdDto = new AnnualReportDTO(createdEntity);
        } catch (EntityRetrievalException ex) {
            createdDto = toCreate;
            createdDto.setId(toCreateEntity.getId());
        }
        return createdDto;
    }

    public AnnualReportDTO update(AnnualReportDTO toUpdate) throws EntityRetrievalException {
        AnnualReportEntity toUpdateEntity = getEntityById(toUpdate.getId());
        toUpdateEntity.setYear(toUpdate.getYear());
        toUpdateEntity.setFindingsSummary(toUpdate.getFindingsSummary());
        toUpdateEntity.setObstacleSummary(toUpdate.getObstacleSummary());
        toUpdateEntity.setLastModifiedUser(AuthUtil.getAuditId());

        super.update(toUpdateEntity);
        return getById(toUpdateEntity.getId());
    }

    public void delete(Long idToDelete) throws EntityRetrievalException {
        AnnualReportEntity toDeleteEntity = getEntityById(idToDelete);
        toDeleteEntity.setDeleted(true);
        toDeleteEntity.setLastModifiedUser(AuthUtil.getAuditId());
        super.update(toDeleteEntity);
    }

    private AnnualReportEntity getEntityById(Long id) throws EntityRetrievalException {
        String queryStr = ANNUAL_REPORT_HQL
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
