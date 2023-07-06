package gov.healthit.chpl.dao;


import javax.persistence.Query;

import org.springframework.stereotype.Repository;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;

@Repository
public class ChplProductNumberDAO extends BaseDAOImpl {

    public String getChplProductNumber(final Long certifiedProductId) {
        String sql = "SELECT * FROM " + SCHEMA_NAME + ".get_chpl_product_number(:id);";
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("id", certifiedProductId);
        String chplProductNumber = (String) query.getSingleResult();
        return chplProductNumber;
    }
}
