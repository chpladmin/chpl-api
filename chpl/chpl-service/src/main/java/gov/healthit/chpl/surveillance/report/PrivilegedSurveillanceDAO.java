package gov.healthit.chpl.surveillance.report;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.Query;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.surveillance.report.domain.PrivilegedSurveillance;
import gov.healthit.chpl.surveillance.report.domain.SurveillanceOutcome;
import gov.healthit.chpl.surveillance.report.domain.SurveillanceProcessType;
import gov.healthit.chpl.surveillance.report.entity.QuarterlyReportSurveillanceMapEntity;
import gov.healthit.chpl.surveillance.report.entity.SurveillanceOutcomeEntity;
import gov.healthit.chpl.surveillance.report.entity.SurveillanceProcessTypeEntity;
import gov.healthit.chpl.util.ChplProductNumberUtil;
import lombok.extern.log4j.Log4j2;

@Repository("quarterlyReportSurveillanceMapDao")
@Log4j2
public class PrivilegedSurveillanceDAO extends BaseDAOImpl {
    private ChplProductNumberUtil chplProductNumberUtil;

    @Autowired
    public PrivilegedSurveillanceDAO(ChplProductNumberUtil chplProductNumberUtil) {
        this.chplProductNumberUtil = chplProductNumberUtil;
    }

    private static final String MAP_HQL = "SELECT map "
            + " FROM QuarterlyReportSurveillanceMapEntity map "
            + " JOIN FETCH map.quarterlyReport qr "
            + " JOIN FETCH qr.quarter "
            + " JOIN FETCH qr.acb acb "
            + " LEFT JOIN FETCH acb.address "
            + " JOIN FETCH map.surveillance surv "
            + " LEFT JOIN FETCH surv.surveillanceType "
            + " WHERE map.deleted = false "
            + " AND qr.deleted = false "
            + " AND surv.deleted = false ";

    /**
     * Gets the mapping between a specific quarterly report and
     * a specific surveillance. Should only be one mapping.
     */
    public PrivilegedSurveillance getByReportAndSurveillance(Long quarterlyReportId, Long surveillanceId) {
        List<Long> quarterlyReportIds = new ArrayList<Long>();
        quarterlyReportIds.add(quarterlyReportId);
        List<PrivilegedSurveillance> result = getByReportsAndSurveillance(quarterlyReportIds, surveillanceId);
        if (result != null && result.size() == 1) {
            return result.get(0);
        } else if (result != null && result.size() > 1) {
            LOGGER.warn("Found " + result.size() + " mappings for quarterly report id "
                    + quarterlyReportId + " and surveillance " + surveillanceId);
        }
        return null;
    }

    /**
     * Gets the surveillance mapping data across multiple quarterly reports
     * (possibly useful when compiling annual report)
     */
    public List<PrivilegedSurveillance> getByReportsAndSurveillance(List<Long> quarterlyReportIds,
            Long surveillanceId) {
        String queryStr = MAP_HQL
                + " AND map.quarterlyReportId IN (:quarterlyReportIds) "
                + " AND map.surveillanceId = :surveillanceId ";
        Query query = entityManager.createQuery(queryStr);
        query.setParameter("quarterlyReportIds", quarterlyReportIds);
        query.setParameter("surveillanceId", surveillanceId);
        List<QuarterlyReportSurveillanceMapEntity> entities = query.getResultList();
        return entities.stream()
                .map(entity -> entity.toDomain())
                .collect(Collectors.toList());
    }

    public List<PrivilegedSurveillance> getByReport(Long quarterlyReportId) {
        String queryStr = MAP_HQL
                + " AND map.quarterlyReportId = :quarterlyReportId ";
        Query query = entityManager.createQuery(queryStr);
        query.setParameter("quarterlyReportId", quarterlyReportId);

        List<QuarterlyReportSurveillanceMapEntity> entities = query.getResultList();
        return entities.stream()
                .map(entity -> entity.toDomain())
                .collect(Collectors.toList());
    }

    public List<PrivilegedSurveillance> getByReports(List<Long> quarterlyReportIds) {
        String queryStr = MAP_HQL
                + " AND map.quarterlyReportId IN (:quarterlyReportIds) ";
        Query query = entityManager.createQuery(queryStr);
        query.setParameter("quarterlyReportIds", quarterlyReportIds);

        List<QuarterlyReportSurveillanceMapEntity> entities = query.getResultList();
        return entities.stream()
                .map(entity -> entity.toDomain())
                .collect(Collectors.toList());
    }

    public List<PrivilegedSurveillance> getBySurveillance(Long surveillanceId) {
        String queryStr = MAP_HQL
                + " AND map.surveillanceId = :surveillanceId";
        Query query = entityManager.createQuery(queryStr);
        query.setParameter("surveillanceId", surveillanceId);

        List<QuarterlyReportSurveillanceMapEntity> entities = query.getResultList();
        return entities.stream()
                .map(entity -> entity.toDomain())
                .collect(Collectors.toList());
    }

    public PrivilegedSurveillance getById(Long id) throws EntityRetrievalException {
        QuarterlyReportSurveillanceMapEntity entity = getEntityById(id);
        if (entity == null) {
            return null;
        }
        String chplProductNumber = chplProductNumberUtil
                .generate(entity.getSurveillance().getCertifiedProductId());
        entity.getSurveillance().setChplProductNumber(chplProductNumber);
        return entity.toDomain();
    }

    public List<SurveillanceOutcome> getSurveillanceOutcomes() {
        List<SurveillanceOutcomeEntity> entities =
                entityManager.createQuery("SELECT soe FROM SurveillanceOutcomeEntity soe WHERE deleted = false").getResultList();
        return entities.stream()
                .map(entity -> entity.toDomain())
                .collect(Collectors.toList());
    }

    public List<SurveillanceProcessType> getSurveillanceProcessTypes() {
        List<SurveillanceProcessTypeEntity> entities =
                entityManager.createQuery("SELECT spte FROM SurveillanceProcessTypeEntity spte WHERE deleted = false").getResultList();
        return entities.stream()
                .map(entity -> entity.toDomain())
                .collect(Collectors.toList());
    }

    public Long create(Long quarterlyReportId, PrivilegedSurveillance toCreate)
            throws EntityCreationException {
        QuarterlyReportSurveillanceMapEntity entity = new QuarterlyReportSurveillanceMapEntity();
        entity.setQuarterlyReportId(quarterlyReportId);
        entity.setSurveillanceId(toCreate.getId());
        if (toCreate.getSurveillanceOutcome() != null) {
            entity.setSurveillanceOutcomeId(toCreate.getSurveillanceOutcome().getId());
        }
        entity.setSurveillanceOutcomeOther(toCreate.getSurveillanceOutcomeOther());
        if (toCreate.getSurveillanceProcessType() != null) {
            entity.setSurveillanceProcessTypeId(toCreate.getSurveillanceProcessType().getId());
        }
        entity.setSurveillanceProcessTypeOther(toCreate.getSurveillanceProcessTypeOther());
        entity.setK1Reviewed(toCreate.getK1Reviewed());
        entity.setGroundsForInitiating(toCreate.getGroundsForInitiating());
        entity.setNonconformityCauses(toCreate.getNonconformityCauses());
        entity.setNonconformityNature(toCreate.getNonconformityNature());
        entity.setStepsToSurveil(toCreate.getStepsToSurveil());
        entity.setStepsToEngage(toCreate.getStepsToEngage());
        entity.setAdditionalCostsEvaluation(toCreate.getAdditionalCostsEvaluation());
        entity.setLimitationsEvaluation(toCreate.getLimitationsEvaluation());
        entity.setNondisclosureEvaluation(toCreate.getNondisclosureEvaluation());
        entity.setDirectionDeveloperResolution(toCreate.getDirectionDeveloperResolution());
        entity.setCompletedCapVerification(toCreate.getCompletedCapVerification());
        entity.setDeleted(false);
        create(entity);
        return entity.getId();
    }

    public void update(Long quarterlyReportId, Long surveillanceId, PrivilegedSurveillance toUpdate)
            throws EntityRetrievalException {
        String queryStr = MAP_HQL
                + " AND map.quarterlyReportId = :quarterlyReportId "
                + " AND map.surveillanceId = :surveillanceId ";
        Query query = entityManager.createQuery(queryStr);
        query.setParameter("quarterlyReportId", quarterlyReportId);
        query.setParameter("surveillanceId", surveillanceId);
        List<QuarterlyReportSurveillanceMapEntity> entities = query.getResultList();
        if (CollectionUtils.isEmpty(entities)) {
            LOGGER.error("No surveillance entries exist for quarterly report: " + quarterlyReportId + " and surveillance: " + surveillanceId);
            return;
        }

        QuarterlyReportSurveillanceMapEntity entity = entities.get(0);
        if (toUpdate.getSurveillanceOutcome() != null) {
            entity.setSurveillanceOutcomeId(toUpdate.getSurveillanceOutcome().getId());
        } else {
            entity.setSurveillanceOutcomeId(null);
        }
        entity.setSurveillanceOutcomeOther(toUpdate.getSurveillanceOutcomeOther());

        if (toUpdate.getSurveillanceProcessType() != null) {
            entity.setSurveillanceProcessTypeId(toUpdate.getSurveillanceProcessType().getId());
        } else {
            entity.setSurveillanceProcessTypeId(null);
        }
        entity.setSurveillanceProcessTypeOther(toUpdate.getSurveillanceProcessTypeOther());

        entity.setK1Reviewed(toUpdate.getK1Reviewed());
        entity.setGroundsForInitiating(toUpdate.getGroundsForInitiating());
        entity.setNonconformityCauses(toUpdate.getNonconformityCauses());
        entity.setNonconformityNature(toUpdate.getNonconformityNature());
        entity.setStepsToSurveil(toUpdate.getStepsToSurveil());
        entity.setStepsToEngage(toUpdate.getStepsToEngage());
        entity.setAdditionalCostsEvaluation(toUpdate.getAdditionalCostsEvaluation());
        entity.setLimitationsEvaluation(toUpdate.getLimitationsEvaluation());
        entity.setNondisclosureEvaluation(toUpdate.getNondisclosureEvaluation());
        entity.setDirectionDeveloperResolution(toUpdate.getDirectionDeveloperResolution());
        entity.setCompletedCapVerification(toUpdate.getCompletedCapVerification());
        update(entity);
    }

    public void delete(Long idToDelete) throws EntityRetrievalException {
        QuarterlyReportSurveillanceMapEntity entity = getEntityById(idToDelete);
        entity.setDeleted(true);
        update(entity);
    }

    private QuarterlyReportSurveillanceMapEntity getEntityById(Long id) throws EntityRetrievalException {
        String queryStr = MAP_HQL
                + " AND map.id = :id";
        Query query = entityManager.createQuery(queryStr);
        query.setParameter("id", id);

        List<QuarterlyReportSurveillanceMapEntity> entities = query.getResultList();
        if (entities != null && entities.size() > 0) {
            return entities.get(0);
        } else if (entities == null || entities.size() == 0) {
            throw new EntityRetrievalException("No quarterly report surveillance mapping exists with ID " + id + ".");
        } else if (entities.size() > 1) {
            throw new EntityRetrievalException(
                    "Multiple quarterly report surveillance mapping entities were found with ID " + id);
        }
        return null;
    }
}
