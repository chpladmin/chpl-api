package gov.healthit.chpl.attestation.dao;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;

import gov.healthit.chpl.attestation.domain.AttestationPeriod;
import gov.healthit.chpl.attestation.domain.AttestationPeriodDeveloperException;
import gov.healthit.chpl.attestation.domain.AttestationSubmission;
import gov.healthit.chpl.attestation.entity.AttestationPeriodDeveloperExceptionEntity;
import gov.healthit.chpl.attestation.entity.AttestationPeriodEntity;
import gov.healthit.chpl.attestation.entity.AttestationSubmissionEntity;
import gov.healthit.chpl.attestation.entity.AttestationSubmissionResponseEntity;
import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.entity.developer.DeveloperEntity;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.util.AuthUtil;
import lombok.extern.log4j.Log4j2;

@Repository
@Log4j2
public class AttestationDAO extends BaseDAOImpl{

    public List<AttestationPeriod> getAllPeriods() {
        return getAllPeriodEntities().stream()
                .map(ent -> new AttestationPeriod(ent))
                .collect(Collectors.toList());
    }

    public List<AttestationSubmission> getDeveloperAttestationSubmissionsByDeveloper(Long developerId) {
        return getAttestationSubmissionEntitiesByDeveloper(developerId).stream()
                .map(ent -> ent.toDomain())
                .collect(Collectors.toList());
    }

    public List<AttestationSubmissionResponseEntity> getAttestationSubmissionResponseEntities(Long attestationSubmissionId) {
        String hql = "SELECT DISTINCT asre "
                + "FROM AttestationSubmissionResponseEntity asre "
                + "JOIN FETCH asre.response resp "
                + "JOIN FETCH asre.formItem fi "
                + "WHERE (NOT asre.deleted = true) "
                + "AND (NOT resp.deleted = true) "
                + "AND (NOT fi.deleted = true) "
                + "AND (asre.attestationSubmissionId = :attestationSubmissionId) ";

        List<AttestationSubmissionResponseEntity> result = entityManager
                .createQuery(hql, AttestationSubmissionResponseEntity.class)
                .setParameter("attestationSubmissionId", attestationSubmissionId)
                .getResultList();

        return result;
    }

    public List<AttestationPeriodDeveloperException> getAttestationPeriodDeveloperExceptions(Long developerId, Long attestationPeriodId) {
        return getAttestationPeriodDeveloperExceptionEntities(developerId, attestationPeriodId).stream()
                .map(entity -> new AttestationPeriodDeveloperException(entity))
                .collect(Collectors.toList());
    }

    public void deleteAttestationPeriodDeveloperExceptions(Long developerId, Long attestationPeriodId) {
        getAttestationPeriodDeveloperExceptionEntities(developerId, attestationPeriodId).stream()
                .forEach(entity -> {
                    entity.setDeleted(true);
                    entity.setLastModifiedUser(AuthUtil.getAuditId());
                    update(entity);
                });
    }

    public AttestationPeriodDeveloperException createAttestationPeriodDeveloperException(AttestationPeriodDeveloperException adpe) throws EntityRetrievalException {
        AttestationPeriodDeveloperExceptionEntity entity = AttestationPeriodDeveloperExceptionEntity.builder()
                .developer(DeveloperEntity.builder()
                        .id(adpe.getDeveloper().getId())
                        .build())
                .period(AttestationPeriodEntity.builder()
                        .id(adpe.getPeriod().getId())
                        .build())
                .exceptionEnd(adpe.getExceptionEnd())
                .creationDate(new Date())
                .lastModifiedDate(new Date())
                .lastModifiedUser(AuthUtil.getAuditId())
                .deleted(false)
                .build();

        create(entity);

        return new AttestationPeriodDeveloperException(getAttestationPeriodDeveloperExceptionEntity(entity.getId()));
    }

    public AttestationPeriodDeveloperException getCurrentAttestationPeriodDeveloperException(Long developerId) {
        AttestationPeriodDeveloperExceptionEntity entity = getCurrentAttestationPeriodDeveloperExceptionEntity(developerId);
        if (entity != null) {
            return new AttestationPeriodDeveloperException(entity);
        } else {
            return null;
        }
    }

    public List<AttestationPeriodEntity> getAllPeriodEntities() {
        List<AttestationPeriodEntity> result = entityManager.createQuery(
                "FROM AttestationPeriodEntity ape "
                + "LEFT JOIN FETCH ape.form "
                + "WHERE (NOT ape.deleted = true)",
                AttestationPeriodEntity.class).getResultList();
        return result;
    }

    private List<AttestationSubmissionEntity> getAttestationSubmissionEntitiesByDeveloper(Long developerId) {
        String hql = "SELECT DISTINCT ase "
                + "FROM AttestationSubmissionEntity ase "
                + "JOIN FETCH ase.attestationPeriod per "
                + "LEFT JOIN FETCH per.form "
                + "WHERE (NOT ase.deleted = true) "
                + "AND (NOT per.deleted = true) "
                + "AND (ase.developerId = :developerId) ";

        List<AttestationSubmissionEntity> result = entityManager
                .createQuery(hql, AttestationSubmissionEntity.class)
                .setParameter("developerId", developerId)
                .getResultList();

        return result;
    }

    private List<AttestationPeriodDeveloperExceptionEntity> getAttestationPeriodDeveloperExceptionEntities(Long developerId, Long attestationPeriodId) {
        String hql = "SELECT apde "
                + "FROM AttestationPeriodDeveloperExceptionEntity apde "
                + "JOIN FETCH apde.developer d "
                + "JOIN FETCH apde.period p "
                + "WHERE d.id = :developerId "
                + "AND p.id = :attestationPeriodId "
                + "AND apde.deleted = false ";

        List<AttestationPeriodDeveloperExceptionEntity> result = entityManager
                .createQuery(hql, AttestationPeriodDeveloperExceptionEntity.class)
                .setParameter("developerId", developerId)
                .setParameter("attestationPeriodId", attestationPeriodId)
                .getResultList();

        return result;
    }

    private AttestationPeriodDeveloperExceptionEntity getAttestationPeriodDeveloperExceptionEntity(Long id) throws EntityRetrievalException {
        String hql = "SELECT apde "
                + "FROM AttestationPeriodDeveloperExceptionEntity apde "
                + "JOIN FETCH apde.developer d "
                + "JOIN FETCH apde.period p "
                + "WHERE apde.id = :id "
                + "AND apde.deleted = false ";

        List<AttestationPeriodDeveloperExceptionEntity> result = entityManager
                .createQuery(hql, AttestationPeriodDeveloperExceptionEntity.class)
                .setParameter("id", id)
                .getResultList();

        if (result == null || result.size() == 0) {
            throw new EntityRetrievalException(
                    "Data error. Attestation Period Developer Exception not found in database.");
        } else if (result.size() > 1) {
            throw new EntityRetrievalException(
                    "Data error. Duplicate Attestation Period Developer Exception in database.");
        }

        if (result.size() == 0) {
            return null;
        }
        return result.get(0);
    }

    private AttestationPeriodDeveloperExceptionEntity getCurrentAttestationPeriodDeveloperExceptionEntity(Long developerId) {
        String hql = "SELECT apde "
                + "FROM AttestationPeriodDeveloperExceptionEntity apde "
                + "JOIN FETCH apde.developer d "
                + "JOIN FETCH apde.period p "
                + "WHERE d.id = :developerId "
                + "AND apde.deleted = false ";

        List<AttestationPeriodDeveloperExceptionEntity> result = entityManager
                .createQuery(hql, AttestationPeriodDeveloperExceptionEntity.class)
                .setParameter("developerId", developerId)
                .getResultList();

        if (result.size() == 0) {
            return null;
        }
        return result.get(0);
    }
}
