package gov.healthit.chpl.dao;

import java.util.List;

import javax.persistence.Query;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.domain.Address;
import gov.healthit.chpl.entity.AddressEntity;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;

@Repository("addressDao")
public class AddressDAO extends BaseDAOImpl {

    public Long create(Address address) {
        AddressEntity toInsert = new AddressEntity();
        toInsert.setStreetLineOne(address.getLine1());
        toInsert.setStreetLineTwo(address.getLine2());
        toInsert.setCity(address.getCity());
        toInsert.setState(address.getState());
        toInsert.setZipcode(address.getZipcode());
        toInsert.setCountry(StringUtils.isEmpty(address.getCountry()) ? Address.DEFAULT_COUNTRY : address.getCountry());
        create(toInsert);
        return toInsert.getId();
    }

    public void update(Address address) throws EntityRetrievalException {
        AddressEntity addressEntity = this.getEntityById(address.getAddressId());
        addressEntity.setStreetLineOne(address.getLine1());
        addressEntity.setStreetLineTwo(address.getLine2());
        addressEntity.setCity(address.getCity());
        addressEntity.setState(address.getState());
        addressEntity.setZipcode(address.getZipcode());
        addressEntity.setCountry(StringUtils.isEmpty(address.getCountry()) ? Address.DEFAULT_COUNTRY : address.getCountry());
        update(addressEntity);
    }

    public List<Address> findAll() {
        List<AddressEntity> result = this.findAllEntities();
        return result.stream()
                .map(addressEntity -> addressEntity.toDomain())
                .toList();
    }

    public Address getById(Long id) throws EntityRetrievalException {
        AddressEntity result = this.getEntityById(id);
        if (result != null) {
            return result.toDomain();
        }
        return null;
    }

    public Long saveAddress(Address address) throws EntityRetrievalException, EntityCreationException {
        Long savedAddressId = address.getAddressId();
        if (address.getAddressId() != null) {
            // update the address
            Address toUpdate = getById(address.getAddressId());
            if (toUpdate != null) {
                toUpdate.setLine1(address.getLine1());
                toUpdate.setLine2(address.getLine2());
                toUpdate.setCity(address.getCity());
                toUpdate.setState(address.getState());
                toUpdate.setZipcode(address.getZipcode());
                toUpdate.setCountry(StringUtils.isEmpty(address.getCountry()) ? Address.DEFAULT_COUNTRY : address.getCountry());
                update(toUpdate);
            }
        } else {
            savedAddressId = create(address);
        }
        return savedAddressId;
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
