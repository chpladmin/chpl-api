package gov.healthit.chpl.dao.changerequest;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.domain.changerequest.ChangeRequestCertificationBodyMap;
import gov.healthit.chpl.domain.changerequest.ChangeRequestConverter;
import gov.healthit.chpl.entity.CertificationBodyEntity;
import gov.healthit.chpl.entity.changerequest.ChangeRequestCertificationBodyMapEntity;
import gov.healthit.chpl.entity.changerequest.ChangeRequestEntity;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.util.AuthUtil;

@Repository("changeRequestCertificationBodyMapDAO")
public class ChangeRequestCertificationBodyMapDAOImpl extends BaseDAOImpl
        implements ChangeRequestCertificationBodyMapDAO {

    @Override
    public ChangeRequestCertificationBodyMap create(final ChangeRequestCertificationBodyMap map)
            throws EntityRetrievalException {
        ChangeRequestCertificationBodyMapEntity entity = getNewEntity(map);
        create(entity);
        return ChangeRequestConverter.convert(getEntity(entity.getId()));
    }

    @Override
    public List<ChangeRequestCertificationBodyMap> getByChangeRequestId(final Long changeRequestId) {
        String hql = "FROM ChangeRequestCertificationBodyMapEntity crAcbMap "
                + "WHERE (NOT crAcbMap.deleted = true) "
                + "AND (crAcbMap.changeRequest.id = :changeRequestId) ";

        return entityManager
                .createQuery(hql, ChangeRequestCertificationBodyMapEntity.class)
                .setParameter("changeRequestId", changeRequestId)
                .getResultList().stream()
                .map(ChangeRequestConverter::convert)
                .collect(Collectors.<ChangeRequestCertificationBodyMap> toList());
    }

    private ChangeRequestCertificationBodyMapEntity getNewEntity(final ChangeRequestCertificationBodyMap map) {
        ChangeRequestCertificationBodyMapEntity entity = new ChangeRequestCertificationBodyMapEntity();
        entity.setCertificationBody(
                getSession().load(CertificationBodyEntity.class, map.getCertificationBody().getId()));
        entity.setChangeRequest(getSession().load(ChangeRequestEntity.class, map.getChangeRequest().getId()));
        entity.setDeleted(false);
        entity.setLastModifiedUser(AuthUtil.getAuditId());
        entity.setCreationDate(new Date());
        entity.setLastModifiedDate(new Date());
        return entity;
    }

    private ChangeRequestCertificationBodyMapEntity getEntity(final Long changeRequestCertificationBodyMapId)
            throws EntityRetrievalException {
        Query query = entityManager.createQuery(
                "from ChangeRequestCertificationBodyMapEntity where (NOT deleted = true) "
                        + "AND (id = :changeRequestCertificationBodyMapId) ",
                ChangeRequestCertificationBodyMapEntity.class);
        query.setParameter("changeRequestCertificationBodyMapId", changeRequestCertificationBodyMapId);
        List<ChangeRequestCertificationBodyMapEntity> result = query.getResultList();

        if (result == null || result.size() == 0) {
            throw new EntityRetrievalException(
                    "Data error. Change request certification body map not found in database.");
        } else if (result.size() > 1) {
            throw new EntityRetrievalException(
                    "Data error. Duplicate change request certification body map in database.");
        }

        if (result.size() == 0) {
            return null;
        }
        return result.get(0);
    }
}
