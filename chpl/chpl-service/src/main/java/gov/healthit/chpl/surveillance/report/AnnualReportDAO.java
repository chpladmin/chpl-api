package gov.healthit.chpl.surveillance.report;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.surveillance.report.domain.AnnualReport;
import gov.healthit.chpl.surveillance.report.entity.AnnualReportEntity;
import gov.healthit.chpl.util.AuthUtil;

@Repository("annualReportDao")
public class AnnualReportDAO extends BaseDAOImpl {
    private static final String ANNUAL_REPORT_HQL = "SELECT ar "
            + " FROM AnnualReportEntity ar "
            + " JOIN FETCH ar.acb acb "
            + " LEFT JOIN FETCH acb.address "
            + " WHERE ar.deleted = false ";

    public List<AnnualReport> getAll() {
        Query query = entityManager.createQuery(ANNUAL_REPORT_HQL);
        List<AnnualReportEntity> entityResults = query.getResultList();
        List<AnnualReport> results = new ArrayList<AnnualReport>();
        if (entityResults != null && entityResults.size() > 0) {
            for (AnnualReportEntity entityResult : entityResults) {
                results.add(new AnnualReport(entityResult));
            }
        }
        return results;
    }

    /**
     * Get the report for a specific year and ACB.
     * There should only be zero or one result.
     */
    public AnnualReport getByAcbAndYear(Long acbId, Integer year) {
        String queryStr = ANNUAL_REPORT_HQL
                + " AND ar.year = :year "
                + " AND acb.id = :acbId";
        Query query = entityManager.createQuery(queryStr);
        query.setParameter("year", year);
        query.setParameter("acbId", acbId);
        List<AnnualReportEntity> entityResults = query.getResultList();
        AnnualReport result = null;
        if (entityResults != null && entityResults.size() > 0) {
            result = new AnnualReport(entityResults.get(0));
        }
        return result;
    }

    public List<AnnualReport> getByAcb(Long acbId) {
        String queryStr = ANNUAL_REPORT_HQL
                + " AND acb.id = :acbId";
        Query query = entityManager.createQuery(queryStr);
        query.setParameter("acbId", acbId);
        List<AnnualReportEntity> entityResults = query.getResultList();
        List<AnnualReport> results = new ArrayList<AnnualReport>();
        if (entityResults != null && entityResults.size() > 0) {
            for (AnnualReportEntity entityResult : entityResults) {
                results.add(new AnnualReport(entityResult));
            }
        }
        return results;
    }

    public AnnualReport getById(Long id) throws EntityRetrievalException {
        AnnualReportEntity entity = getEntityById(id);
        if (entity != null) {
            return new AnnualReport(entity);
        }
        return null;
    }

    public AnnualReport create(AnnualReport toCreate) throws EntityCreationException {
        if (toCreate.getAcb() == null) {
            throw new EntityCreationException("Missing ACB ID to create annual report.");
        }
        AnnualReportEntity toCreateEntity = AnnualReportEntity.builder()
                .certificationBodyId(toCreate.getAcb().getId())
                .year(toCreate.getYear())
                .findingsSummary(toCreate.getPriorityChangesFromFindingsSummary())
                .obstacleSummary(toCreate.getObstacleSummary())
                .creationDate(new Date())
                .lastModifiedUser(AuthUtil.getAuditId())
                .build();

        super.create(toCreateEntity);

        AnnualReport createdDto = null;
        try {
            AnnualReportEntity createdEntity = getEntityById(toCreateEntity.getId());
            createdDto = new AnnualReport(createdEntity);
        } catch (EntityRetrievalException ex) {
            createdDto = toCreate;
            createdDto.setId(toCreateEntity.getId());
        }
        return createdDto;
    }

    public AnnualReport update(AnnualReport toUpdate) throws EntityRetrievalException {
        AnnualReportEntity toUpdateEntity = getEntityById(toUpdate.getId());
        toUpdateEntity.setYear(toUpdate.getYear());
        toUpdateEntity.setFindingsSummary(toUpdate.getPriorityChangesFromFindingsSummary());
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
