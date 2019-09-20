package gov.healthit.chpl.changerequest.dao;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;

import gov.healthit.chpl.changerequest.entity.DeveloperCertificationBodyMapEntity;
import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.dto.DeveloperDTO;

@Repository
public class DeveloperCertificationBodyMapDAOImpl extends BaseDAOImpl implements DeveloperCertificationBodyMapDAO {

    @Override
    public List<CertificationBody> getCertificationBodiesForDeveloper(final Long developerId) {
        String hql = "FROM DeveloperCertificationBodyMapEntity main "
                + "JOIN FETCH main.developer dev "
                + "JOIN FETCH main.certificationBody cb "
                + "WHERE dev.id = :developerId";

        return entityManager
                .createQuery(hql, DeveloperCertificationBodyMapEntity.class)
                .setParameter("developerId", developerId)
                .getResultList().stream()
                .map(item -> new CertificationBody(item.getCertificationBody()))
                .collect(Collectors.<CertificationBody> toList());
    }

    @Override
    public List<Developer> getDevelopersForCertificationBody(final Long certificationBodyId) {
        String hql = "FROM DeveloperCertificationBodyMapEntity "
                + "JOIN FETCH main.developer dev "
                + "JOIN FETCH main.certificationBody cb "
                + "WHERE cb.id = :certificationBodyId";

        return entityManager
                .createQuery(hql, DeveloperCertificationBodyMapEntity.class)
                .setParameter("certificationBodyId", certificationBodyId)
                .getResultList().stream()
                .map(item -> new Developer(new DeveloperDTO(item.getDeveloper())))
                .collect(Collectors.<Developer> toList());
    }

}
