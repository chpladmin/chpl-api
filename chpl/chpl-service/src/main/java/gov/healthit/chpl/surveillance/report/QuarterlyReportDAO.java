package gov.healthit.chpl.surveillance.report;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.entity.listing.CertifiedProductDetailsEntity;
import gov.healthit.chpl.entity.listing.ListingWithPrivilegedSurveillanceEntity;
import gov.healthit.chpl.entity.surveillance.SurveillanceEntity;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.surveillance.report.dto.QuarterlyReportDTO;
import gov.healthit.chpl.surveillance.report.dto.QuarterlyReportExclusionDTO;
import gov.healthit.chpl.surveillance.report.dto.QuarterlyReportRelevantListingDTO;
import gov.healthit.chpl.surveillance.report.entity.QuarterlyReportEntity;
import gov.healthit.chpl.surveillance.report.entity.QuarterlyReportExcludedListingMapEntity;
import gov.healthit.chpl.util.AuthUtil;

@Repository("quarterlyReportDao")
public class QuarterlyReportDAO extends BaseDAOImpl {
    private static final String QUARTERLY_REPORT_HQL = "SELECT qr "
            + " FROM QuarterlyReportEntity qr "
            + " JOIN FETCH qr.quarter quarter "
            + " JOIN FETCH qr.acb acb "
            + " LEFT JOIN FETCH acb.address "
            + " WHERE qr.deleted = false ";

    public QuarterlyReportDTO getByQuarterAndAcbAndYear(Long quarterId, Long acbId, Integer year) {
        String queryStr = QUARTERLY_REPORT_HQL
                + " AND qr.year = :year "
                + " AND acb.id = :acbId "
                + " AND quarter.id = :quarterId";
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

    public List<QuarterlyReportDTO> getByAcbAndYear(Long acbId, Integer year) {
        String queryStr = QUARTERLY_REPORT_HQL
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

    public List<QuarterlyReportDTO> getByAcb(Long acbId) {
        String queryStr = QUARTERLY_REPORT_HQL
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

    public List<QuarterlyReportDTO> getAll() {
        Query query = entityManager.createQuery(QUARTERLY_REPORT_HQL);
        List<QuarterlyReportEntity> entityResults = query.getResultList();
        List<QuarterlyReportDTO> results = new ArrayList<QuarterlyReportDTO>();
        if (entityResults != null && entityResults.size() > 0) {
            for (QuarterlyReportEntity entityResult : entityResults) {
                results.add(new QuarterlyReportDTO(entityResult));
            }
        }
        return results;
    }

    public QuarterlyReportDTO getById(Long id) throws EntityRetrievalException {
        QuarterlyReportEntity entity = getEntityById(id);
        if (entity != null) {
            return new QuarterlyReportDTO(entity);
        }
        return null;
    }

    public boolean isListingRelevant(Long acbId, Long listingId) {
        String queryStr = "SELECT DISTINCT cp "
                + "FROM CertifiedProductEntity cp "
                + "WHERE cp.id = :listingId "
                + "AND cp.certificationBodyId = :acbId ";
        Query query = entityManager.createQuery(queryStr);
        query.setParameter("listingId", listingId);
        query.setParameter("acbId", acbId);
        List<CertifiedProductDetailsEntity> entities = query.getResultList();
        return entities != null && entities.size() > 0;
    }

    /**
     * Returns true if the surveillance specified is related to a listing on the
     * ACB that's relevant to the quarterly report
     * and was open during the reporting period; false otherwise.
     */
    public boolean isSurveillanceRelevant(QuarterlyReportDTO quarterlyReport, Long survId) {
        String queryStr = "SELECT surv "
                + "FROM SurveillanceEntity surv "
                + "JOIN FETCH surv.certifiedProduct listing "
                + "WHERE surv.id = :survId "
                + "AND listing.certificationBodyId = :acbId "
                + "AND surv.startDate <= :endDate "
                + "AND (surv.endDate IS NULL OR surv.endDate >= :startDate)";
        Query query = entityManager.createQuery(queryStr);
        query.setParameter("survId", survId);
        query.setParameter("acbId", quarterlyReport.getAcb().getId());
        query.setParameter("startDate", quarterlyReport.getStartDate());
        query.setParameter("endDate", quarterlyReport.getEndDate());
        List<SurveillanceEntity> entities = query.getResultList();
        return entities != null && entities.size() > 0;
    }

    /**
     * Returns listings with at least one surveillance that was in an open state during a time interval.
     */
    @Transactional(readOnly = true)
    public List<QuarterlyReportRelevantListingDTO> getListingsWithRelevantSurveillance(QuarterlyReportDTO quarterlyReport) {
        String queryStr = "SELECT DISTINCT listing "
                + "FROM ListingWithPrivilegedSurveillanceEntity listing "
                + "JOIN FETCH listing.surveillances surv "
                + "LEFT JOIN FETCH surv.surveillanceType "
                + "WHERE listing.certificationBodyId = :acbId "
                + "AND listing.deleted = false "
                + "AND surv.startDate <= :endDate "
                + "AND (surv.endDate IS NULL OR surv.endDate >= :startDate)";
        Query query = entityManager.createQuery(queryStr);
        query.setParameter("acbId", quarterlyReport.getAcb().getId());
        query.setParameter("startDate", quarterlyReport.getStartDate());
        query.setParameter("endDate", quarterlyReport.getEndDate());

        List<ListingWithPrivilegedSurveillanceEntity> listingsWithSurvEntities = query.getResultList();
        List<QuarterlyReportRelevantListingDTO> result = new ArrayList<QuarterlyReportRelevantListingDTO>();
        for (ListingWithPrivilegedSurveillanceEntity listingsWithSurvEntity : listingsWithSurvEntities) {
            result.add(new QuarterlyReportRelevantListingDTO(listingsWithSurvEntity));
        }
        return result;
    }

    /**
     * Returns a relevant listing object if the listing is relevant during the dates specified.
     * Otherwise returns null.
     */
    public QuarterlyReportRelevantListingDTO getRelevantListing(Long listingId,
            QuarterlyReportDTO quarterlyReport) {
        String queryStr = "SELECT DISTINCT listing "
                + "FROM ListingWithPrivilegedSurveillanceEntity listing "
                + "LEFT JOIN FETCH listing.surveillances surv "
                + "LEFT JOIN FETCH surv.surveillanceType "
                + "WHERE listing.id = :listingId "
                + "AND listing.deleted = false "
                + "AND listing.certificationBodyId = :acbId ";
        Query query = entityManager.createQuery(queryStr);
        query.setParameter("listingId", listingId);
        query.setParameter("acbId", quarterlyReport.getAcb().getId());
        List<ListingWithPrivilegedSurveillanceEntity> entities = query.getResultList();

        QuarterlyReportRelevantListingDTO relevantListing = null;
        if (entities != null && entities.size() > 0) {
            relevantListing = new QuarterlyReportRelevantListingDTO(entities.get(0));
        }
        return relevantListing;
    }

    /**
     * Returns listings owned by the ACB
     */
    @Transactional(readOnly = true)
    public List<QuarterlyReportRelevantListingDTO> getRelevantListings(QuarterlyReportDTO quarterlyReport) {
        String queryStr = "SELECT DISTINCT listing "
                + "FROM ListingWithPrivilegedSurveillanceEntity listing "
                + "LEFT JOIN FETCH listing.surveillances surv "
                + "LEFT JOIN FETCH surv.surveillanceType "
                + "WHERE listing.certificationBodyId = :acbId "
                + "AND listing.deleted = false ";
        Query query = entityManager.createQuery(queryStr);
        query.setParameter("acbId", quarterlyReport.getAcb().getId());

        List<ListingWithPrivilegedSurveillanceEntity> entities = query.getResultList();
        List<QuarterlyReportRelevantListingDTO> result = new ArrayList<QuarterlyReportRelevantListingDTO>();
        for (ListingWithPrivilegedSurveillanceEntity entity : entities) {
            result.add(new QuarterlyReportRelevantListingDTO(entity));
        }
        return result;
    }

    public List<QuarterlyReportExclusionDTO> getExclusions(Long quarterlyReportId) {
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

    public QuarterlyReportExclusionDTO getExclusion(Long quarterlyReportId, Long listingId) {
        String queryStr = "SELECT exclusion "
                + " FROM QuarterlyReportExcludedListingMapEntity exclusion "
                + " WHERE exclusion.deleted = false "
                + " AND exclusion.listingId = :listingId"
                + " AND exclusion.quarterlyReportId = :quarterlyReportId";
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

    public QuarterlyReportDTO create(QuarterlyReportDTO toCreate) throws EntityCreationException {
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

    public QuarterlyReportExclusionDTO createExclusion(QuarterlyReportExclusionDTO toCreate)
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

    public QuarterlyReportDTO update(QuarterlyReportDTO toUpdate) throws EntityRetrievalException {
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
    public QuarterlyReportExclusionDTO updateExclusion(QuarterlyReportExclusionDTO toUpdate)
            throws EntityRetrievalException {
        QuarterlyReportExcludedListingMapEntity toUpdateEntity = getExcludedEntityById(toUpdate.getId());
        toUpdateEntity.setReason(toUpdate.getReason());
        toUpdateEntity.setLastModifiedUser(AuthUtil.getAuditId());
        super.update(toUpdateEntity);
        return new QuarterlyReportExclusionDTO(toUpdateEntity);
    }

    public void delete(Long idToDelete) throws EntityRetrievalException {
        QuarterlyReportEntity toDeleteEntity = getEntityById(idToDelete);
        toDeleteEntity.setDeleted(true);
        toDeleteEntity.setLastModifiedUser(AuthUtil.getAuditId());
        super.update(toDeleteEntity);
    }

    public void deleteExclusion(Long idToDelete) throws EntityRetrievalException {
        QuarterlyReportExcludedListingMapEntity toUpdateEntity = getExcludedEntityById(idToDelete);
        toUpdateEntity.setDeleted(true);
        toUpdateEntity.setLastModifiedUser(AuthUtil.getAuditId());
        super.update(toUpdateEntity);
    }

    private QuarterlyReportEntity getEntityById(Long id) throws EntityRetrievalException {
        String queryStr = QUARTERLY_REPORT_HQL
                + " AND qr.id = :id";
        Query query = entityManager.createQuery(queryStr);
        query.setParameter("id", id);
        List<QuarterlyReportEntity> results = query.getResultList();
        if (results == null || results.size() == 0) {
            throw new EntityRetrievalException("No quarterly report with ID " + id + " exists.");
        }
        return results.get(0);
    }

    private QuarterlyReportExcludedListingMapEntity getExcludedEntityById(Long id) throws EntityRetrievalException {
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
