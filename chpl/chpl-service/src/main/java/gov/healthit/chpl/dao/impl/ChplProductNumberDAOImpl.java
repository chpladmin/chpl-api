package gov.healthit.chpl.dao.impl;


import javax.persistence.Query;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Repository;

import gov.healthit.chpl.dao.ChplProductNumberDAO;

/**
 * @author TYoung
 *
 */
@Repository
public class ChplProductNumberDAOImpl extends BaseDAOImpl implements ChplProductNumberDAO {

    private Environment env;

    /**
     * Constructor for dependency injection.
     * @param env - Environment
     */
    @Autowired
    public ChplProductNumberDAOImpl(final Environment env) {
        this.env = env;
    }

    @Override
    public String getChplProductNumber(final Long certifiedProductId) {
        String schema = env.getRequiredProperty("persistenceUnitName");
        String sql = "SELECT * FROM " + schema + ".get_chpl_product_number(:id);";
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("id", certifiedProductId);
        String chplProductNumber = (String) query.getSingleResult();
        return chplProductNumber;
    }
}
