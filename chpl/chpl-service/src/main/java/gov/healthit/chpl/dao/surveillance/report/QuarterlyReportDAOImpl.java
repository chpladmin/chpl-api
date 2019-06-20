package gov.healthit.chpl.dao.surveillance.report;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.dto.CertifiedProductDTO;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.dto.surveillance.report.QuarterlyReportDTO;
import gov.healthit.chpl.dto.surveillance.report.QuarterlyReportExclusionDTO;
import gov.healthit.chpl.dto.surveillance.report.QuarterlyReportRelevantListingDTO;
import gov.healthit.chpl.entity.listing.CertifiedProductDetailsEntity;
import gov.healthit.chpl.entity.surveillance.report.QuarterlyReportEntity;
import gov.healthit.chpl.entity.surveillance.report.QuarterlyReportExcludedListingMapEntity;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.util.AuthUtil;
import gov.healthit.chpl.util.ChplProductNumberUtil;

@Repository("quarterlyReportDao")
public class QuarterlyReportDAOImpl extends BaseDAOImpl implements QuarterlyReportDAO {

    @Override
    public QuarterlyReportDTO getByQuarterAndAcbAndYear(final Long quarterId, final Long acbId, final Integer year) {
        String queryStr = "SELECT qr "
                + " FROM QuarterlyReportEntity qr "
                + " JOIN FETCH qr.quarter "
                + " JOIN FETCH qr.acb acb "
                + " WHERE qr.deleted = false "
                + " AND qr.year = :year "
                + " AND acb.id = :acbId "
                + " AND qr.id = :quarterId";
        Query query = entityManager.createQuery(queryStr);
        query.setParameter("quarterId", quarterId);
        query.setParameter("year", year);
        query.setParameter("acbId", acbId);
        List<QuarterlyReportEntity> entityResults = query.getResultList();
        QuarterlyReportDTO result = null;
        if (entityResults != null && entityResults.size() > 0) {
            result = new QuarterlyReportDTO(entityResults.get(0));
        }
        return result;
    }

    /**
     * Get the quarterly reports for a specific year and ACB.
     * There could be 0-4 quarterly reports.
     */
    @Override
    public List<QuarterlyReportDTO> getByAcbAndYear(final Long acbId, final Integer year) {
        String queryStr = "SELECT qr "
                + " FROM QuarterlyReportEntity qr "
                + " JOIN FETCH qr.quarter "
                + " JOIN FETCH qr.acb acb "
                + " WHERE qr.deleted = false "
                + " AND qr.year = :year "
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
                + " JOIN FETCH qr.acb acb "
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
     * Get all of the quarterly report.
     */
    @Override
    public List<QuarterlyReportDTO> getAll() {
        String queryStr = "SELECT qr "
                + " FROM QuarterlyReportEntity qr "
                + " JOIN FETCH qr.quarter "
                + " JOIN FETCH qr.acb acb "
                + " WHERE qr.deleted = false ";
        Query query = entityManager.createQuery(queryStr);
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

    /**
     * Returns listings with at least one surveillance that was in an open state during a time interval.
     * @param acb return listings owned by this acb
     * @param startDate the beginning of the time interval to check for open surveillance
     * @param endDate the end of the time interval to check for open surveillance
     * @return
     */
    @Override
    @Transactional(readOnly = true)
    public List<QuarterlyReportRelevantListingDTO> getRelevantListings(final Long acbId,
            final Date startDate, final Date endDate) {
        String queryStr = "SELECT DISTINCT cp "
                + "FROM CertifiedProductDetailsEntity cp, SurveillanceEntity surv "
                + "WHERE cp.certificationBodyId = :acbId "
                + "AND surv.certifiedProductId = cp.id "
                + "AND cp.deleted = false "
                + "AND surv.deleted = false "
                + "AND surv.startDate <= :endDate "
                + "AND (surv.endDate IS NULL OR surv.endDate >= :startDate)";
        Query query = entityManager.createQuery(queryStr);
        query.setParameter("acbId", acbId);
        query.setParameter("startDate", startDate);
        query.setParameter("endDate", endDate);
        List<CertifiedProductDetailsEntity> entities = query.getResultList();

        List<QuarterlyReportRelevantListingDTO> products = new ArrayList<QuarterlyReportRelevantListingDTO>();
        for (CertifiedProductDetailsEntity entity : entities) {
            QuarterlyReportRelevantListingDTO product = new QuarterlyReportRelevantListingDTO(entity);
            products.add(product);
        }
        return products;
    }

    /**
     * Get listings excluded from a quarterly report.
     */
    @Override
    public List<QuarterlyReportExclusionDTO> getExclusions(final Long quarterlyReportId) {
        String queryStr = "SELECT exclusions "
                + " FROM QuarterlyReportExcludedListingMapEntity exclusions "
                + " WHERE exclusions.deleted = false "
                + " AND exclusions.quarterlyReportId = :id";
        Query query = entityManager.createQuery(queryStr);
        query.setParameter("id", quarterlyReportId);
        List<QuarterlyReportExcludedListingMapEntity> results = query.getResultList();
        List<QuarterlyReportExclusionDTO> exclusions = new ArrayList<QuarterlyReportExclusionDTO>();
        for (QuarterlyReportExcludedListingMapEntity result : results) {
            QuarterlyReportExclusionDTO exclusion = new QuarterlyReportExclusionDTO(result);
            exclusions.add(exclusion);
        }
        return exclusions;
    }

    /**
     * Get listings excluded from a quarterly report.
     */
    @Override
    public QuarterlyReportExclusionDTO getExclusion(final Long quarterlyReportId, final Long listingId) {
        String queryStr = "SELECT exclusions "
                + " FROM QuarterlyReportExcludedListingMapEntity exclusion "
                + " WHERE exclusion.deleted = false "
                + " AND exclusion.listingId = :listingId"
                + " AND exclusions.quarterlyReportId = :quarterlyReportId";
        Query query = entityManager.createQuery(queryStr);
        query.setParameter("listingId", listingId);
        query.setParameter("quarterlyReportId", quarterlyReportId);
        List<QuarterlyReportExcludedListingMapEntity> results = query.getResultList();
        QuarterlyReportExclusionDTO exclusion = null;
        if (results != null && results.size() > 0) {
            exclusion = new QuarterlyReportExclusionDTO(results.get(0));
        }
        return exclusion;
    }

    @Override
    public QuarterlyReportDTO create(final QuarterlyReportDTO toCreate) throws EntityCreationException {
        QuarterlyReportEntity toCreateEntity = new QuarterlyReportEntity();
        if (toCreate.getAcb() == null || toCreate.getAcb().getId() == null) {
            throw new EntityCreationException("An ACB must be provided in order to create a quarterly report.");
        }
        if (toCreate.getYear() == null) {
            throw new EntityCreationException("A year must be provided in order to create a quarterly report.");
        }
        if (toCreate.getQuarter() == null) {
            throw new EntityCreationException("A quarter must be provided in order to create a quarterly report.");
        }
        toCreateEntity.setCertificationBodyId(toCreate.getAcb().getId());
        toCreateEntity.setYear(toCreate.getYear());
        toCreateEntity.setPrioritizedElementSummary(toCreate.getPrioritizedElementSummary());
        toCreateEntity.setQuarterId(toCreate.getQuarter().getId());
        toCreateEntity.setActivitiesOutcomesSummary(toCreate.getActivitiesOutcomesSummary());
        toCreateEntity.setReactiveSummary(toCreate.getReactiveSummary());
        toCreateEntity.setTransparencyDisclosureSummary(toCreate.getTransparencyDisclosureSummary());
        toCreateEntity.setCreationDate(new Date());
        toCreateEntity.setLastModifiedUser(AuthUtil.getAuditId());

        super.create(toCreateEntity);
        toCreate.setId(toCreateEntity.getId());
        return toCreate;
    }

    @Override
    public QuarterlyReportExclusionDTO createExclusion(final QuarterlyReportExclusionDTO toCreate)
            throws EntityCreationException {
        QuarterlyReportExcludedListingMapEntity toCreateEntity = new QuarterlyReportExcludedListingMapEntity();
        if (toCreate.getQuarterlyReportId() == null) {
            throw new EntityCreationException("A quarterly report ID must be provided in order to create an exclusion.");
        }
        if (toCreate.getListingId() == null) {
            throw new EntityCreationException("A listing ID must be provided in order to create an exclusion.");
        }
        toCreateEntity.setListingId(toCreate.getListingId());
        toCreateEntity.setQuarterlyReportId(toCreate.getQuarterlyReportId());
        if (!StringUtils.isEmpty(toCreate.getReason())) {
            toCreateEntity.setReason(toCreate.getReason().trim());
        }
        toCreateEntity.setCreationDate(new Date());
        toCreateEntity.setLastModifiedUser(AuthUtil.getAuditId());

        super.create(toCreateEntity);
        toCreate.setId(toCreateEntity.getId());
        return toCreate;
    }

    @Override
    public QuarterlyReportDTO update(final QuarterlyReportDTO toUpdate) throws EntityRetrievalException {
        QuarterlyReportEntity toUpdateEntity = getEntityById(toUpdate.getId());
        toUpdateEntity.setActivitiesOutcomesSummary(toUpdate.getActivitiesOutcomesSummary());
        toUpdateEntity.setPrioritizedElementSummary(toUpdate.getPrioritizedElementSummary());
        toUpdateEntity.setReactiveSummary(toUpdate.getReactiveSummary());
        toUpdateEntity.setTransparencyDisclosureSummary(toUpdate.getTransparencyDisclosureSummary());
        toUpdateEntity.setLastModifiedUser(AuthUtil.getAuditId());

        super.update(toUpdateEntity);
        return getById(toUpdateEntity.getId());
    }

    /**
     * The only field that can be updated is the reason.
     */
    @Override
    public QuarterlyReportExclusionDTO updateExclusion(final QuarterlyReportExclusionDTO toUpdate)
            throws EntityRetrievalException {
        QuarterlyReportExcludedListingMapEntity toUpdateEntity = getExcludedEntityById(toUpdate.getId());
        toUpdateEntity.setReason(toUpdate.getReason());
        toUpdateEntity.setLastModifiedUser(AuthUtil.getAuditId());
        super.update(toUpdateEntity);
        return new QuarterlyReportExclusionDTO(toUpdateEntity);
    }

    @Override
    public void delete(final Long idToDelete) throws EntityRetrievalException {
        QuarterlyReportEntity toDeleteEntity = getEntityById(idToDelete);
        toDeleteEntity.setDeleted(true);
        toDeleteEntity.setLastModifiedUser(AuthUtil.getAuditId());
        super.update(toDeleteEntity);
    }

    @Override
    public void deleteExclusion(final Long idToDelete) throws EntityRetrievalException {
        QuarterlyReportExcludedListingMapEntity toUpdateEntity = getExcludedEntityById(idToDelete);
        toUpdateEntity.setDeleted(true);
        toUpdateEntity.setLastModifiedUser(AuthUtil.getAuditId());
        super.update(toUpdateEntity);
    }

    private QuarterlyReportEntity getEntityById(final Long id) throws EntityRetrievalException {
        String queryStr = "SELECT qr "
                + " FROM QuarterlyReportEntity qr "
                + " JOIN FETCH qr.quarter "
                + " JOIN FETCH qr.acb acb "
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

    private QuarterlyReportExcludedListingMapEntity getExcludedEntityById(final Long id) throws EntityRetrievalException {
        String queryStr = "SELECT qr "
                + " FROM QuarterlyReportExcludedListingMapEntity qr "
                + " WHERE qr.deleted = false "
                + " AND qr.id = :id";
        Query query = entityManager.createQuery(queryStr);
        query.setParameter("id", id);
        List<QuarterlyReportExcludedListingMapEntity> results = query.getResultList();
        if (results == null || results.size() == 0) {
            throw new EntityRetrievalException("No quarterly report exclusion with ID " + id + " exists.");
        }
        return results.get(0);
    }
}
