package gov.healthit.chpl.changerequest.dao;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;

import gov.healthit.chpl.changerequest.entity.DeveloperCertificationBodyMapEntity;
import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.domain.Developer;

@Repository
public class DeveloperCertificationBodyMapDAO extends BaseDAOImpl {

    public List<CertificationBody> getCertificationBodiesForDeveloper(Long developerId) {
        String hql = "SELECT main "
                + "FROM DeveloperCertificationBodyMapEntity main "
                + "JOIN FETCH main.developer dev "
                + "JOIN FETCH main.certificationBody cb "
                + "LEFT JOIN FETCH cb.address "
                + "WHERE dev.id = :developerId";
        return entityManager
                .createQuery(hql, DeveloperCertificationBodyMapEntity.class)
                .setParameter("developerId", developerId)
                .getResultList().stream()
                .map(item -> new CertificationBody(item.getCertificationBody()))
                .collect(Collectors.<CertificationBody>toList());
    }

    public List<Developer> getDevelopersForCertificationBody(Long certificationBodyId) {
        String hql = "FROM DeveloperCertificationBodyMapEntity main"
                + "JOIN FETCH main.developer dev "
                + "JOIN FETCH main.certificationBody cb "
                + "LEFT JOIN FETCH dev.address "
                + "LEFT JOIN FETCH dev.contact "
                + "LEFT JOIN FETCH dev.statusEvents statusEvents "
                + "LEFT JOIN FETCH statusEvents.developerStatus "
                + "LEFT JOIN FETCH dev.publicAttestations devAtt "
                + "LEFT JOIN FETCH devAtt.period per "
                + "WHERE cb.id = :certificationBodyId";

        return entityManager
                .createQuery(hql, DeveloperCertificationBodyMapEntity.class)
                .setParameter("certificationBodyId", certificationBodyId)
                .getResultList().stream()
                .map(item -> item.getDeveloper().toDomain())
                .collect(Collectors.<Developer>toList());
    }
}
