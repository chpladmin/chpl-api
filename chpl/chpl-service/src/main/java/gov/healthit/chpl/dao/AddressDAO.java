package gov.healthit.chpl.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.dto.AddressDTO;
import gov.healthit.chpl.entity.AddressEntity;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.util.AuthUtil;

@Repository("addressDao")
public class AddressDAO extends BaseDAOImpl {

    public AddressEntity create(AddressDTO dto) throws EntityCreationException, EntityRetrievalException {
        AddressEntity toInsert = new AddressEntity();
        toInsert.setStreetLineOne(dto.getStreetLineOne());
        toInsert.setStreetLineTwo(dto.getStreetLineTwo());
        toInsert.setCity(dto.getCity());
        toInsert.setState(dto.getState());
        toInsert.setZipcode(dto.getZipcode());
        toInsert.setCountry(dto.getCountry());
        if (dto.getDeleted() != null) {
            toInsert.setDeleted(dto.getDeleted());
        } else {
            toInsert.setDeleted(false);
        }
        if (dto.getLastModifiedUser() != null) {
            toInsert.setLastModifiedUser(dto.getLastModifiedUser());
        } else {
            toInsert.setLastModifiedUser(AuthUtil.getAuditId());
        }
        if (dto.getLastModifiedDate() != null) {
            toInsert.setLastModifiedDate(dto.getLastModifiedDate());
        } else {
            toInsert.setLastModifiedDate(new Date());
        }
        if (dto.getCreationDate() != null) {
            toInsert.setCreationDate(dto.getCreationDate());
        } else {
            toInsert.setCreationDate(new Date());
        }
        create(toInsert);
        return toInsert;
    }

    public AddressEntity update(AddressDTO addressDto) throws EntityRetrievalException {
        AddressEntity address = this.getEntityById(addressDto.getId());
        address.setStreetLineTwo(addressDto.getStreetLineTwo());
        if (addressDto.getStreetLineOne() != null) {
            address.setStreetLineOne(addressDto.getStreetLineOne());
        }
        if (addressDto.getCity() != null) {
            address.setCity(addressDto.getCity());
        }
        if (addressDto.getState() != null) {
            address.setState(addressDto.getState());
        }
        if (addressDto.getZipcode() != null) {
            address.setZipcode(addressDto.getZipcode());
        }
        if (addressDto.getCountry() != null) {
            address.setCountry(addressDto.getCountry());
        }
        if (addressDto.getDeleted() != null) {
            address.setDeleted(addressDto.getDeleted());
        }
        if (addressDto.getLastModifiedUser() != null) {
            address.setLastModifiedUser(addressDto.getLastModifiedUser());
        } else {
            address.setLastModifiedUser(AuthUtil.getAuditId());
        }
        if (addressDto.getLastModifiedDate() != null) {
            address.setLastModifiedDate(addressDto.getLastModifiedDate());
        } else {
            address.setLastModifiedDate(new Date());
        }
        update(address);
        return address;
    }

    public List<AddressDTO> findAll() {
        List<AddressEntity> result = this.findAllEntities();
        List<AddressDTO> dtos = new ArrayList<AddressDTO>(result.size());
        for (AddressEntity entity : result) {
            dtos.add(new AddressDTO(entity));
        }
        return dtos;
    }

    public AddressDTO getById(Long id) throws EntityRetrievalException {
        AddressDTO dto = null;
        AddressEntity ae = this.getEntityById(id);
        if (ae != null) {
            dto = new AddressDTO(ae);
        }
        return dto;
    }

    public AddressEntity saveAddress(AddressDTO addressDto) throws EntityRetrievalException, EntityCreationException {
        AddressEntity address = null;
        if (addressDto.getId() != null) {
            // update the address
            AddressDTO toUpdate = getById(addressDto.getId());
            if (toUpdate != null) {
                toUpdate.setStreetLineOne(addressDto.getStreetLineOne());
                toUpdate.setStreetLineTwo(addressDto.getStreetLineTwo());
                toUpdate.setCity(addressDto.getCity());
                toUpdate.setState(addressDto.getState());
                toUpdate.setZipcode(addressDto.getZipcode());
                toUpdate.setCountry(addressDto.getCountry());
                address = update(toUpdate);
            }
        } else {
            address = create(addressDto);
        }
        return address;
    }

    private List<AddressEntity> findAllEntities() {
        Query query = entityManager.createQuery("SELECT a from AddressEntity a where (NOT a.deleted = true)");
        return query.getResultList();
    }

    public AddressEntity getEntityById(Long id) throws EntityRetrievalException {
        AddressEntity entity = null;
        Query query = entityManager.createQuery(
                "from AddressEntity a where (NOT deleted = true) AND (address_id = :entityid) ", AddressEntity.class);
        query.setParameter("entityid", id);
        List<AddressEntity> result = query.getResultList();

        if (result.size() > 1) {
            throw new EntityRetrievalException("Data error. Duplicate address id in database.");
        } else if (result.size() == 1) {
            entity = result.get(0);
        }
        return entity;
    }
}
