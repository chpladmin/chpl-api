package gov.healthit.chpl.dao.scheduler;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.auth.user.User;
import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.dto.scheduler.BrokenSurveillanceRulesDTO;
import gov.healthit.chpl.entity.scheduler.BrokenSurveillanceRulesEntity;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;

/**
 * The implementation for InheritanceErrorsReportDAO.
 * @author alarned
 *
 */
@Repository("brokenSurveillanceRulesDAO")
public class BrokenSurveillanceRulesDAOImpl extends BaseDAOImpl implements BrokenSurveillanceRulesDAO {

    @Override
    public List<BrokenSurveillanceRulesDTO> findAll() {
        List<BrokenSurveillanceRulesEntity> result = this.findAllEntities();
        List<BrokenSurveillanceRulesDTO> dtos = new ArrayList<BrokenSurveillanceRulesDTO>(result.size());
        for (BrokenSurveillanceRulesEntity entity : result) {
            dtos.add(new BrokenSurveillanceRulesDTO(entity));
        }
        return dtos;
    }

    @Override
    @Transactional
    public void deleteAll() {
        List<BrokenSurveillanceRulesEntity> entities = this.findAllEntities();

        for (BrokenSurveillanceRulesEntity entity : entities) {
            if (!entity.getDeleted()) {
                entity.setDeleted(true);
                entityManager.merge(entity);
                entityManager.flush();
            }
        }
    }

    @Override
    @Transactional
    public void create(final List<BrokenSurveillanceRulesDTO> dtos)
            throws EntityCreationException, EntityRetrievalException {
        for (BrokenSurveillanceRulesDTO dto : dtos) {
            BrokenSurveillanceRulesEntity entity = new BrokenSurveillanceRulesEntity();
            entity.setChplProductNumber(dto.getChplProductNumber());
            entity.setDeveloper(dto.getDeveloper());
            entity.setProduct(dto.getProduct());
            entity.setVersion(dto.getVersion());
            entity.setAcb(dto.getAcb());
            entity.setUrl(dto.getUrl());
            entity.setCertificationStatus(dto.getCertificationStatus());
            entity.setDateOfLastStatusChange(dto.getDateOfLastStatusChange());
            entity.setSurveillanceId(dto.getSurveillanceId());
            entity.setDateSurveillanceBegan(dto.getDateSurveillanceBegan());
            entity.setDateSurveillanceEnded(dto.getDateSurveillanceEnded());
            entity.setSurveillanceType(dto.getSurveillanceType());
            entity.setLengthySuspensionRule(dto.getLengthySuspensionRule());
            entity.setCapNotApprovedRule(dto.getCapNotApprovedRule());
            entity.setCapNotStartedRule(dto.getCapNotStartedRule());
            entity.setCapNotCompletedRule(dto.getCapNotCompletedRule());
            entity.setCapNotClosedRule(dto.getCapNotClosedRule());
            entity.setClosedCapWithOpenNonconformityRule(dto.getClosedCapWithOpenNonconformityRule());
            entity.setNonconformity(dto.getNonconformity());
            entity.setNonconformityStatus(dto.getNonconformityStatus());
            entity.setNonconformityCriteria(dto.getNonconformityCriteria());
            entity.setDateOfDeterminationOfNonconformity(dto.getDateOfDeterminationOfNonconformity());
            entity.setCorrectiveActionPlanApprovedDate(dto.getCorrectiveActionPlanApprovedDate());
            entity.setDateCorrectiveActionBegan(dto.getDateCorrectiveActionBegan());
            entity.setDateCorrectiveActionMustBeCompleted(dto.getDateCorrectiveActionMustBeCompleted());
            entity.setDateCorrectiveActionWasCompleted(dto.getDateCorrectiveActionWasCompleted());
            entity.setNumberOfDaysFromDeterminationToCapApproval(dto.getNumberOfDaysFromDeterminationToCapApproval());
            entity.setNumberOfDaysFromDeterminationToPresent(dto.getNumberOfDaysFromDeterminationToPresent());
            entity.setNumberOfDaysFromCapApprovalToCapBegan(dto.getNumberOfDaysFromCapApprovalToCapBegan());
            entity.setNumberOfDaysFromCapApprovalToPresent(dto.getNumberOfDaysFromCapApprovalToPresent());
            entity.setNumberOfDaysFromCapBeganToCapCompleted(dto.getNumberOfDaysFromCapBeganToCapCompleted());
            entity.setNumberOfDaysFromCapBeganToPresent(dto.getNumberOfDaysFromCapBeganToPresent());
            entity.setDifferenceFromCapCompletedAndCapMustBeCompleted(
                    dto.getDifferenceFromCapCompletedAndCapMustBeCompleted());
            entity.setDeleted(false);
            entity.setLastModifiedUser(getUserId(User.SYSTEM_USER_ID));

            entityManager.persist(entity);
        }
        entityManager.flush();
    }

    private List<BrokenSurveillanceRulesEntity> findAllEntities() {
        Query query = entityManager.createQuery("from BrokenSurveillanceRulesEntity bsre "
                + "where (bsre.deleted = false)",
                BrokenSurveillanceRulesEntity.class);
        return query.getResultList();
    }
}
