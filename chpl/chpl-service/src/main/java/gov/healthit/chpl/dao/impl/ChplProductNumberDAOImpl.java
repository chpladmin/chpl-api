package gov.healthit.chpl.dao.impl;


import javax.persistence.Query;

import org.springframework.stereotype.Repository;

import gov.healthit.chpl.dao.ChplProductNumberDAO;

/**
 * @author TYoung
 *
 */
@Repository
public class ChplProductNumberDAOImpl extends BaseDAOImpl implements ChplProductNumberDAO {

    @Override
    public String getChplProductNumber(final Long certifiedProductId) {
        String sql = "SELECT * FROM " + SCHEMA_NAME + ".get_chpl_product_number(:id);";
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("id", certifiedProductId);
        String chplProductNumber = (String) query.getSingleResult();
        return chplProductNumber;
    }
}
