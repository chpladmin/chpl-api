package gov.healthit.chpl.report.criteriauptodate;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import jakarta.persistence.Query;

@Repository
public class UpdatedCriterionStatusReportDao extends BaseDAOImpl {
    private CriterionNotUpToDateReasonDao criterionNotUpToDateReasonDao;

    @Autowired
    public UpdatedCriterionStatusReportDao(CriterionNotUpToDateReasonDao criterionNotUpToDateReasonDao) {
        this.criterionNotUpToDateReasonDao = criterionNotUpToDateReasonDao;
    }

    public void create(UpdatedCriterionStatusReport ucsr) {
        UpdatedCriterionStatusReportEntity entity = UpdatedCriterionStatusReportEntity.builder()
                .certifiedProductId(ucsr.getCertifiedProductId())
                .reportDay(LocalDate.now())
                .chplProductNumber(ucsr.getChplProductNumber())
                .product(ucsr.getProduct())
                .version(ucsr.getVersion())
                .developerId(ucsr.getDeveloperId())
                .developer(ucsr.getDeveloper())
                .certificationBodyId(ucsr.getCertificationBodyId())
                .certificationBody(ucsr.getCertificationBody())
                .certificationStatusId(ucsr.getCertificationStatusId())
                .certificationStatus(ucsr.getCertificationStatus())
                .certificationResultId(ucsr.getCertificationResultId())
                .codeSetId(ucsr.getCodeSet() != null ? ucsr.getCodeSet().getId() : null)
                .functionalityTestedId(ucsr.getFunctionalityTested() != null ? ucsr.getFunctionalityTested().getId() : null)
                .standardId(ucsr.getStandard() != null ? ucsr.getStandard().getId() : null)
                .standardGroupName(ucsr.getStandardGroupName())
                .criterionNotUpToDateReasonId(
                        criterionNotUpToDateReasonDao.getByName(ucsr.getCriterionNotUpToDateReason().getName()).getId())
                .build();

        create(entity);
    }

    public List<UpdatedCriterionStatusReport> getUpdatedCriterionStatusReportsByDay(LocalDate reportDate) {
        List<UpdatedCriterionStatusReport> standardUpdateReports = getUpdatedCriterionStandardReportEntitiesByDate(reportDate).stream()
                .map(ent -> ent.toDomain())
                .toList();
        List<UpdatedCriterionStatusReport> standardGroupUpdateReports = getUpdatedCriterionStandardGroupReportEntitiesByDate(reportDate).stream()
                .map(ent -> ent.toDomain())
                .toList();
        List<UpdatedCriterionStatusReport> functionalityTestedUpdateReports = getUpdatedCriterionFunctionalityTestedReportEntitiesByDate(reportDate).stream()
                .map(ent -> ent.toDomain())
                .toList();
        List<UpdatedCriterionStatusReport> codeSetUpdateReports = getUpdatedCriterionCodeSetReportEntitiesByDate(reportDate).stream()
                .map(ent -> ent.toDomain())
                .toList();

        List<List<UpdatedCriterionStatusReport>> allReportLists = List.of(standardUpdateReports,
                standardGroupUpdateReports,
                functionalityTestedUpdateReports,
                codeSetUpdateReports);

        return allReportLists.stream()
                    .flatMap(List::stream)
                    .collect(Collectors.toList());
    }

    public boolean doUpdatedCriterionStatusReportsExistOnDay(LocalDate reportDay) {
        String hql = "SELECT count(ucsr) "
                + "FROM UpdatedCriterionStatusReportEntity ucsr "
                + "WHERE reportDay = :reportDay "
                + "AND deleted = false ";
        Query query = entityManager.createQuery(hql);
        query.setParameter("reportDay", reportDay);
        Long countOfReports = (Long) query.getSingleResult();
        return countOfReports != null && countOfReports.longValue() > 0;
    }

    public void deleteUpdatedCriterionStatusReportsByDay(LocalDate reportDay) {
        String hql = "UPDATE UpdatedCriterionStatusReportEntity "
                + "SET deleted = true "
                + "WHERE reportDay = :reportDay";
        Query query = entityManager.createQuery(hql);
        query.setParameter("reportDay", reportDay);
        query.executeUpdate();
    }

    private List<UpdatedCriterionStatusReportEntity> getUpdatedCriterionStandardReportEntitiesByDate(LocalDate reportDate) {
        return entityManager
                .createQuery("SELECT ucsr "
                            + "FROM UpdatedCriterionStatusReportEntity ucsr "
                            + "JOIN FETCH ucsr.certificationResult cr "
                            + "JOIN FETCH cr.certificationCriterion cc "
                            + "JOIN FETCH cc.certificationEdition edition "
                            + "JOIN FETCH cc.rule "
                            + "JOIN FETCH ucsr.standard std "
                            + "LEFT OUTER JOIN FETCH std.rule "
                            + "LEFT OUTER JOIN FETCH std.mappedCriteria criteriaMapping "
                            + "LEFT OUTER JOIN FETCH criteriaMapping.criterion stdCriterion "
                            + "LEFT OUTER JOIN FETCH stdCriterion.certificationEdition "
                            + "LEFT JOIN FETCH stdCriterion.rule "
                            + "WHERE (NOT ucsr.deleted = true) "
                            + "AND ucsr.reportDay = :reportDate", UpdatedCriterionStatusReportEntity.class)
                .setParameter("reportDate", reportDate)
                .getResultList();
    }

    private List<UpdatedCriterionStatusReportEntity> getUpdatedCriterionStandardGroupReportEntitiesByDate(LocalDate reportDate) {
        return entityManager
                .createQuery("SELECT ucsr "
                            + "FROM UpdatedCriterionStatusReportEntity ucsr "
                            + "JOIN FETCH ucsr.certificationResult cr "
                            + "JOIN FETCH cr.certificationCriterion cc "
                            + "JOIN FETCH cc.certificationEdition edition "
                            + "JOIN FETCH cc.rule "
                            + "WHERE ucsr.standardGroupName IS NOT NULL "
                            + "AND (NOT ucsr.deleted = true) "
                            + "AND ucsr.reportDay = :reportDate", UpdatedCriterionStatusReportEntity.class)
                .setParameter("reportDate", reportDate)
                .getResultList();
    }

    private List<UpdatedCriterionStatusReportEntity> getUpdatedCriterionFunctionalityTestedReportEntitiesByDate(LocalDate reportDate) {
        return entityManager
                .createQuery("SELECT ucsr "
                            + "FROM UpdatedCriterionStatusReportEntity ucsr "
                            + "JOIN FETCH ucsr.certificationResult cr "
                            + "JOIN FETCH cr.certificationCriterion cc "
                            + "JOIN FETCH cc.certificationEdition edition "
                            + "JOIN FETCH cc.rule "
                            + "JOIN FETCH ucsr.functionalityTested ft "
                            + "LEFT OUTER JOIN FETCH ft.practiceType "
                            + "LEFT OUTER JOIN FETCH ft.mappedCriteria criteriaMapping "
                            + "LEFT OUTER JOIN FETCH criteriaMapping.criterion ftCriterion "
                            + "LEFT OUTER JOIN FETCH ftCriterion.certificationEdition "
                            + "LEFT JOIN FETCH ftCriterion.rule "
                            + "WHERE (NOT ucsr.deleted = true) "
                            + "AND ucsr.reportDay = :reportDate", UpdatedCriterionStatusReportEntity.class)
                .setParameter("reportDate", reportDate)
                .getResultList();
    }

    private List<UpdatedCriterionStatusReportEntity> getUpdatedCriterionCodeSetReportEntitiesByDate(LocalDate reportDate) {
        return entityManager
                .createQuery("SELECT ucsr "
                            + "FROM UpdatedCriterionStatusReportEntity ucsr "
                            + "JOIN FETCH ucsr.certificationResult cr "
                            + "JOIN FETCH cr.certificationCriterion cc "
                            + "JOIN FETCH cc.certificationEdition edition "
                            + "JOIN FETCH cc.rule "
                            + "JOIN FETCH ucsr.codeSet codeSet "
                            + "LEFT OUTER JOIN FETCH codeSet.mappedCriteria criteriaMapping "
                            + "LEFT OUTER JOIN FETCH criteriaMapping.criterion csCriterion "
                            + "LEFT OUTER JOIN FETCH csCriterion.certificationEdition "
                            + "LEFT JOIN FETCH csCriterion.rule "
                            + "WHERE (NOT ucsr.deleted = true) "
                            + "AND ucsr.reportDay = :reportDate", UpdatedCriterionStatusReportEntity.class)
                .setParameter("reportDate", reportDate)
                .getResultList();
    }
}
