package gov.healthit.chpl.dao.changerequest;

import java.util.Date;

import org.springframework.stereotype.Repository;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.domain.changerequest.ChangeRequestCertificationBodyMap;
import gov.healthit.chpl.domain.changerequest.ChangeRequestConverter;
import gov.healthit.chpl.entity.CertificationBodyEntity;
import gov.healthit.chpl.entity.changerequest.ChangeRequestCertificationBodyMapEntity;
import gov.healthit.chpl.entity.changerequest.ChangeRequestEntity;
import gov.healthit.chpl.util.AuthUtil;

@Repository("changeRequestCertificationBodyMapDAO")
public class ChangeRequestCertificationBodyMapDAOImpl extends BaseDAOImpl
        implements ChangeRequestCertificationBodyMapDAO {

    @Override
    public ChangeRequestCertificationBodyMap create(final ChangeRequestCertificationBodyMap map) {
        ChangeRequestCertificationBodyMapEntity entity = getNewEntity(map);
        create(entity);
        return ChangeRequestConverter.convert(entity);
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
}
