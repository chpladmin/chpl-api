package gov.healthit.chpl.dao;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.domain.Address;
import gov.healthit.chpl.entity.AddressEntity;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import jakarta.persistence.Query;

@Repository("addressDao")
public class AddressDAO extends BaseDAOImpl {

    public Long create(Address address) {
        AddressEntity toInsert = new AddressEntity();
        toInsert.setStreetLineOne(StringUtils.normalizeSpace(address.getLine1()));
        toInsert.setStreetLineTwo(StringUtils.normalizeSpace(address.getLine2()));
        toInsert.setCity(StringUtils.normalizeSpace(address.getCity()));
        toInsert.setState(StringUtils.normalizeSpace(address.getState()));
        toInsert.setZipcode(StringUtils.normalizeSpace(address.getZipcode()));
        toInsert.setCountry(StringUtils.isEmpty(address.getCountry())
                ? Address.DEFAULT_COUNTRY : StringUtils.normalizeSpace(address.getCountry()));
        create(toInsert);
        return toInsert.getId();
    }

    public void update(Address address) throws EntityRetrievalException {
        AddressEntity addressEntity = this.getEntityById(address.getAddressId());
        addressEntity.setStreetLineOne(StringUtils.normalizeSpace(address.getLine1()));
        addressEntity.setStreetLineTwo(StringUtils.normalizeSpace(address.getLine2()));
        addressEntity.setCity(StringUtils.normalizeSpace(address.getCity()));
        addressEntity.setState(StringUtils.normalizeSpace(address.getState()));
        addressEntity.setZipcode(StringUtils.normalizeSpace(address.getZipcode()));
        addressEntity.setCountry(StringUtils.isEmpty(address.getCountry())
                ? Address.DEFAULT_COUNTRY : StringUtils.normalizeSpace(address.getCountry()));
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
                toUpdate.setLine1(StringUtils.normalizeSpace(address.getLine1()));
                toUpdate.setLine2(StringUtils.normalizeSpace(address.getLine2()));
                toUpdate.setCity(StringUtils.normalizeSpace(address.getCity()));
                toUpdate.setState(StringUtils.normalizeSpace(address.getState()));
                toUpdate.setZipcode(StringUtils.normalizeSpace(address.getZipcode()));
                toUpdate.setCountry(StringUtils.isEmpty(address.getCountry())
                        ? Address.DEFAULT_COUNTRY : StringUtils.normalizeSpace(address.getCountry()));
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
                "from AddressEntity a where (NOT deleted = true) AND (id = :entityid) ", AddressEntity.class);
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
