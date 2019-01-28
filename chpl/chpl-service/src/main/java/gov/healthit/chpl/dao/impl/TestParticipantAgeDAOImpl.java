package gov.healthit.chpl.dao.impl;

import java.util.List;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;

import gov.healthit.chpl.dao.TestParticipantAgeDAO;
import gov.healthit.chpl.dto.TestParticipantAgeDTO;
import gov.healthit.chpl.entity.TestParticipantAgeEntity;

@Repository("testParticipantAgeDAO")
public class TestParticipantAgeDAOImpl extends BaseDAOImpl implements TestParticipantAgeDAO {

    @Override
    public TestParticipantAgeDTO getById(Long id) {
        TestParticipantAgeDTO dto = null;
        TestParticipantAgeEntity entity = getEntityById(id);

        if (entity != null) {
            dto = new TestParticipantAgeDTO(entity);
        }
        return dto;

    }

    private TestParticipantAgeEntity getEntityById(Long id) {

        TestParticipantAgeEntity entity = null;

        Query query = entityManager.createQuery(
                "from TestParticipantAgeEntity where (NOT deleted = true) AND (id = :entityid) ", TestParticipantAgeEntity.class);
        query.setParameter("entityid", id);
        List<TestParticipantAgeEntity> result = query.getResultList();

        if (result.size() > 0) {
            entity = result.get(0);
        }

        return entity;
    }
}
