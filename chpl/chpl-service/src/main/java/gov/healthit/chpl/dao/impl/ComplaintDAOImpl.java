package gov.healthit.chpl.dao.impl;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Query;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.ComplaintDAO;
import gov.healthit.chpl.dto.ComplaintStatusTypeDTO;
import gov.healthit.chpl.dto.ComplaintTypeDTO;
import gov.healthit.chpl.entity.ComplaintStatusTypeEntity;
import gov.healthit.chpl.entity.ComplaintTypeEntity;

@Component
public class ComplaintDAOImpl extends BaseDAOImpl implements ComplaintDAO {

    @Override
    public List<ComplaintTypeDTO> getComplaintTypes() {
        List<ComplaintTypeEntity> entities = getComplaintTypeEntities();
        List<ComplaintTypeDTO> dtos = new ArrayList<ComplaintTypeDTO>();
        for (ComplaintTypeEntity entity : entities) {
            dtos.add(new ComplaintTypeDTO(entity));
        }
        return dtos;
    }

    @Override
    public List<ComplaintStatusTypeDTO> getComplaintStatusTypes() {
        List<ComplaintStatusTypeEntity> entities = getComplaintStatusTypeEntities();
        List<ComplaintStatusTypeDTO> dtos = new ArrayList<ComplaintStatusTypeDTO>();
        for (ComplaintStatusTypeEntity entity : entities) {
            dtos.add(new ComplaintStatusTypeDTO(entity));
        }
        return dtos;
    }

    private List<ComplaintTypeEntity> getComplaintTypeEntities() {
        Query query = entityManager.createQuery("from ComplaintTypeEntity where (NOT deleted = true) ",
                ComplaintTypeEntity.class);
        List<ComplaintTypeEntity> result = query.getResultList();
        return result;
    }

    private List<ComplaintStatusTypeEntity> getComplaintStatusTypeEntities() {
        Query query = entityManager.createQuery("from ComplaintStatusTypeEntity where (NOT deleted = true) ",
                ComplaintStatusTypeEntity.class);
        List<ComplaintStatusTypeEntity> result = query.getResultList();
        return result;
    }
}
