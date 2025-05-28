package gov.healthit.chpl.scheduler.job.updatedlistingstatusreport;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import jakarta.persistence.Query;

@Repository
public class UpdatedListingStatusReportDao extends BaseDAOImpl {

    private ListingNotUpToDateReasonDao listingNotUpToDateReasonDao;

    @Autowired
    public UpdatedListingStatusReportDao(ListingNotUpToDateReasonDao listingNotUpToDateReasonDao) {
        this.listingNotUpToDateReasonDao = listingNotUpToDateReasonDao;
    }

    public void create(UpdatedListingStatusReport ulsr) {
        UpdatedListingStatusReportEntity entity = UpdatedListingStatusReportEntity.builder()
                .certifiedProductId(ulsr.getCertifiedProductId())
                .reportDay(LocalDate.now())
                .chplProductNumber(ulsr.getChplProductNumber())
                .product(ulsr.getProduct())
                .version(ulsr.getVersion())
                .developerId(ulsr.getDeveloperId())
                .developer(ulsr.getDeveloper())
                .certificationBodyId(ulsr.getCertificationBodyId())
                .certificationBody(ulsr.getCertificationBody())
                .certificationStatusId(ulsr.getCertificationStatusId())
                .certificationStatus(ulsr.getCertificationStatus())
                .certificationResultId(ulsr.getCertificationResultId())
                .codeSetId(ulsr.getCodeSet() != null ? ulsr.getCodeSet().getId() : null)
                .functionalityTestedId(ulsr.getFunctionalityTested() != null ? ulsr.getFunctionalityTested().getId() : null)
                .standardId(ulsr.getStandard() != null ? ulsr.getStandard().getId() : null)
                .listingNotUpToDateReasonId(
                        listingNotUpToDateReasonDao.getByName(ulsr.getListingNotUpToDateReason().getName()).getId())
                .build();

        create(entity);
    }

    public List<UpdatedListingStatusReport> getUpdatedListingStatusReportsByDate(LocalDate reportDate) {
        List<UpdatedListingStatusReport> standardUpdateReports = getUpdatedListingStandardReportEntitiesByDate(reportDate).stream()
                .map(ent -> ent.toDomain())
                .toList();
        List<UpdatedListingStatusReport> functionalityTestedUpdateReports = getUpdatedListingFunctionalityTestedReportEntitiesByDate(reportDate).stream()
                .map(ent -> ent.toDomain())
                .toList();
        List<UpdatedListingStatusReport> codeSetUpdateReports = getUpdatedListingCodeSetReportEntitiesByDate(reportDate).stream()
                .map(ent -> ent.toDomain())
                .toList();
        return Stream.concat(Stream.concat(standardUpdateReports.stream(), functionalityTestedUpdateReports.stream()),
                codeSetUpdateReports.stream())
                .collect(Collectors.toList());
    }

    public void deleteUpdatedListingStatusReportsByDay(LocalDate reportDay) {
        String hql = "UPDATE UpdatedListingStatusReportEntity "
                + "SET deleted = true "
                + "WHERE reportDay = :reportDay";
        Query query = entityManager.createQuery(hql);
        query.setParameter("reportDay", reportDay);
        query.executeUpdate();
    }

    public LocalDate getMaxReportDate() {
        return entityManager
                .createQuery("SELECT MAX(reportDay) "
                            + "FROM UpdatedListingStatusReportEntity ulsr "
                            + "WHERE (NOT ulsr.deleted = true) ", LocalDate.class)
                .getSingleResult();
    }

    private List<UpdatedListingStatusReportEntity> getUpdatedListingStandardReportEntitiesByDate(LocalDate reportDate) {
        return entityManager
                .createQuery("SELECT ulsr "
                            + "FROM UpdatedListingStatusReportEntity ulsr "
                            + "JOIN FETCH ulsr.certificationResult cr "
                            + "JOIN FETCH cr.certificationCriterion cc "
                            + "JOIN FETCH cc.certificationEdition edition "
                            + "JOIN FETCH cc.rule "
                            + "JOIN FETCH ulsr.standard std "
                            + "LEFT OUTER JOIN FETCH s.rule "
                            + "LEFT OUTER JOIN FETCH s.mappedCriteria criteriaMapping "
                            + "LEFT OUTER JOIN FETCH criteriaMapping.criterion stdCriterion "
                            + "LEFT OUTER JOIN FETCH stdCriterion.certificationEdition "
                            + "LEFT JOIN FETCH stdCriterion.rule "
                            + "WHERE (NOT ulsr.deleted = true) "
                            + "AND ulsr.reportDay = :reportDate", UpdatedListingStatusReportEntity.class)
                .setParameter("reportDate", reportDate)
                .getResultList();
    }

    private List<UpdatedListingStatusReportEntity> getUpdatedListingFunctionalityTestedReportEntitiesByDate(LocalDate reportDate) {
        return entityManager
                .createQuery("SELECT ulsr "
                            + "FROM UpdatedListingStatusReportEntity ulsr "
                            + "JOIN FETCH ulsr.certificationResult cr "
                            + "JOIN FETCH cr.certificationCriterion cc "
                            + "JOIN FETCH cc.certificationEdition edition "
                            + "JOIN FETCH cc.rule "
                            + "JOIN FETCH ulsr.functionalityTested ft "
                            + "LEFT OUTER JOIN FETCH ft.practiceType "
                            + "LEFT OUTER JOIN FETCH ft.mappedCriteria criteriaMapping "
                            + "LEFT OUTER JOIN FETCH criteriaMapping.criterion ftCriterion "
                            + "LEFT OUTER JOIN FETCH ftCriterion.certificationEdition "
                            + "LEFT JOIN FETCH ftCriterion.rule "
                            + "WHERE (NOT ulsr.deleted = true) "
                            + "AND ulsr.reportDay = :reportDate", UpdatedListingStatusReportEntity.class)
                .setParameter("reportDate", reportDate)
                .getResultList();
    }

    private List<UpdatedListingStatusReportEntity> getUpdatedListingCodeSetReportEntitiesByDate(LocalDate reportDate) {
        return entityManager
                .createQuery("SELECT ulsr "
                            + "FROM UpdatedListingStatusReportEntity ulsr "
                            + "JOIN FETCH ulsr.certificationResult cr "
                            + "JOIN FETCH cr.certificationCriterion cc "
                            + "JOIN FETCH cc.certificationEdition edition "
                            + "JOIN FETCH cc.rule "
                            + "JOIN FETCH ulsr.codeSet codeSet "
                            + "LEFT OUTER JOIN FETCH cs.mappedCriteria criteriaMapping "
                            + "LEFT OUTER JOIN FETCH criteriaMapping.criterion csCriterion "
                            + "LEFT OUTER JOIN FETCH csCriterion.certificationEdition "
                            + "LEFT JOIN FETCH csCriterion.rule "
                            + "WHERE (NOT ulsr.deleted = true) "
                            + "AND ulsr.reportDay = :reportDate", UpdatedListingStatusReportEntity.class)
                .setParameter("reportDate", reportDate)
                .getResultList();
    }

}
