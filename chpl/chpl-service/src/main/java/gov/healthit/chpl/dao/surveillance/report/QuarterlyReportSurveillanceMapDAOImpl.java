package gov.healthit.chpl.dao.surveillance.report;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Repository;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.dto.surveillance.report.QuarterlyReportSurveillanceMapDTO;
import gov.healthit.chpl.dto.surveillance.report.SurveillanceOutcomeDTO;
import gov.healthit.chpl.dto.surveillance.report.SurveillanceProcessTypeDTO;
import gov.healthit.chpl.entity.surveillance.report.QuarterlyReportSurveillanceMapEntity;
import gov.healthit.chpl.entity.surveillance.report.SurveillanceOutcomeEntity;
import gov.healthit.chpl.entity.surveillance.report.SurveillanceProcessTypeEntity;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.util.AuthUtil;

@Repository("quarterlyReportSurveillanceMapDao")
public class QuarterlyReportSurveillanceMapDAOImpl extends BaseDAOImpl implements QuarterlyReportSurveillanceMapDAO {
    private static final Logger LOGGER = LogManager.getLogger(QuarterlyReportSurveillanceMapDAOImpl.class);

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
    @Override
    public QuarterlyReportSurveillanceMapDTO getByReportAndSurveillance(final Long quarterlyReportId, final Long surveillanceId) {
        List<Long> quarterlyReportIds = new ArrayList<Long>();
        quarterlyReportIds.add(quarterlyReportId);
        List<QuarterlyReportSurveillanceMapDTO> result = getByReportsAndSurveillance(quarterlyReportIds, surveillanceId);
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
    @Override
    public List<QuarterlyReportSurveillanceMapDTO> getByReportsAndSurveillance(final List<Long> quarterlyReportIds,
            final Long surveillanceId) {
        String queryStr = MAP_HQL
                + " AND map.quarterlyReportId IN (:quarterlyReportIds) "
                + " AND map.surveillanceId = :surveillanceId ";
        Query query = entityManager.createQuery(queryStr);
        query.setParameter("quarterlyReportIds", quarterlyReportIds);
        query.setParameter("surveillanceId", surveillanceId);

        List<QuarterlyReportSurveillanceMapDTO> result = new ArrayList<QuarterlyReportSurveillanceMapDTO>();
        List<QuarterlyReportSurveillanceMapEntity> entities = query.getResultList();
        if (entities != null && entities.size() > 0) {
            for (QuarterlyReportSurveillanceMapEntity entity : entities) {
                result.add(new QuarterlyReportSurveillanceMapDTO(entity));
            }
        }
        return result;
    }

    /**
     * Get all surveillance mappings for a particular report.
     */
    @Override
    public List<QuarterlyReportSurveillanceMapDTO> getByReport(final Long quarterlyReportId) {
        String queryStr = MAP_HQL
                + " AND map.quarterlyReportId = :quarterlyReportId ";
        Query query = entityManager.createQuery(queryStr);
        query.setParameter("quarterlyReportId", quarterlyReportId);

        List<QuarterlyReportSurveillanceMapDTO> result = new ArrayList<QuarterlyReportSurveillanceMapDTO>();
        List<QuarterlyReportSurveillanceMapEntity> entities = query.getResultList();
        if (entities != null && entities.size() > 0) {
            for (QuarterlyReportSurveillanceMapEntity entity : entities) {
                result.add(new QuarterlyReportSurveillanceMapDTO(entity));
            }
        }
        return result;
    }

    /**
     * Get all quarterly report mappings for a particular surveillance.
     */
    @Override
    public List<QuarterlyReportSurveillanceMapDTO> getBySurveillance(final Long surveillanceId) {
        String queryStr = MAP_HQL
                + " AND map.surveillanceId = :surveillanceId";
        Query query = entityManager.createQuery(queryStr);
        query.setParameter("surveillanceId", surveillanceId);

        List<QuarterlyReportSurveillanceMapDTO> result = new ArrayList<QuarterlyReportSurveillanceMapDTO>();
        List<QuarterlyReportSurveillanceMapEntity> entities = query.getResultList();
        if (entities != null && entities.size() > 0) {
            for (QuarterlyReportSurveillanceMapEntity entity : entities) {
                result.add(new QuarterlyReportSurveillanceMapDTO(entity));
            }
        }
        return result;
    }

    @Override
    public QuarterlyReportSurveillanceMapDTO getById(final Long id) throws EntityRetrievalException {
        QuarterlyReportSurveillanceMapEntity entity = getEntityById(id);
        if (entity == null) {
            return null;
        }
        return new QuarterlyReportSurveillanceMapDTO(entity);
    }

    @Override
    public List<SurveillanceOutcomeDTO> getSurveillanceOutcomes() {
        List<SurveillanceOutcomeEntity> entities =
                entityManager.createQuery("SELECT soe FROM SurveillanceOutcomeEntity soe WHERE deleted = false").getResultList();
        List<SurveillanceOutcomeDTO> results = new ArrayList<SurveillanceOutcomeDTO>();
        if(entities != null && entities.size() > 0) {
            for(SurveillanceOutcomeEntity entity: entities) {
                results.add(new SurveillanceOutcomeDTO(entity));
            }
        }
        return results;
    }

    @Override
    public List<SurveillanceProcessTypeDTO> getSurveillanceProcessTypes() {
        List<SurveillanceProcessTypeEntity> entities =
                entityManager.createQuery("SELECT spte FROM SurveillanceProcessTypeEntity spte WHERE deleted = false").getResultList();
        List<SurveillanceProcessTypeDTO> results = new ArrayList<SurveillanceProcessTypeDTO>();
        if(entities != null && entities.size() > 0) {
            for(SurveillanceProcessTypeEntity entity: entities) {
                results.add(new SurveillanceProcessTypeDTO(entity));
            }
        }
        return results;
    }

    @Override
    public QuarterlyReportSurveillanceMapDTO create(final QuarterlyReportSurveillanceMapDTO toCreate)
            throws EntityCreationException {
        QuarterlyReportSurveillanceMapEntity entity = new QuarterlyReportSurveillanceMapEntity();
        entity.setQuarterlyReportId(toCreate.getQuarterlyReport().getId());
        entity.setSurveillanceId(toCreate.getId());
        entity.setSurveillanceOutcomeId(toCreate.getSurveillanceOutcome().getId());
        entity.setSurveillanceProcessTypeId(toCreate.getSurveillanceProcessType().getId());
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
        entity.setLastModifiedDate(new Date());
        entity.setCreationDate(new Date());
        entity.setDeleted(false);
        entity.setLastModifiedUser(AuthUtil.getAuditId());
        create(entity);
        return new QuarterlyReportSurveillanceMapDTO(entity);
    }

    @Override
    public QuarterlyReportSurveillanceMapDTO update(final QuarterlyReportSurveillanceMapDTO toUpdate)
            throws EntityRetrievalException {
        QuarterlyReportSurveillanceMapEntity entity = getEntityById(toUpdate.getMappingId());
        entity.setQuarterlyReportId(toUpdate.getQuarterlyReport().getId());
        entity.setSurveillanceId(toUpdate.getId());
        entity.setSurveillanceOutcomeId(toUpdate.getSurveillanceOutcome().getId());
        entity.setSurveillanceProcessTypeId(toUpdate.getSurveillanceProcessType().getId());
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
        entity.setLastModifiedDate(new Date());
        entity.setDeleted(false);
        entity.setLastModifiedUser(AuthUtil.getAuditId());
        update(entity);
        return new QuarterlyReportSurveillanceMapDTO(entity);
    }

    @Override
    public void delete(final Long idToDelete) throws EntityRetrievalException {
        QuarterlyReportSurveillanceMapEntity entity = getEntityById(idToDelete);
        entity.setLastModifiedDate(new Date());
        entity.setDeleted(true);
        entity.setLastModifiedUser(AuthUtil.getAuditId());
        update(entity);
    }

    private QuarterlyReportSurveillanceMapEntity getEntityById(final Long id) throws EntityRetrievalException {
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
