package gov.healthit.chpl.caching;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.rules.ExternalResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import net.sf.ehcache.CacheManager;

/**
 * Class that encapsulates caching actions every unit test may need.
 * @author kekey
 *
 */
@Service
public class UnitTestRules extends ExternalResource {
    private static final Logger LOGGER = LogManager.getLogger(UnitTestRules.class);

    @Autowired
    private EntityManagerFactory emf;
    @Autowired private CacheManager cacheManager;

    @Override
    protected void before() {
        LOGGER.info("Clearing all caches before running @Test");
        cacheManager.clearAll();
        LOGGER.info("Setting security context authentication to null before running @Test");
        SecurityContextHolder.getContext().setAuthentication(null);

        LOGGER.info("Setting sequence values to work with dbunit data before running @Test");
        EntityManager entityManager = emf.createEntityManager();
        EntityTransaction tx = entityManager.getTransaction();
        tx.begin();
        Query resetSeqQuery = entityManager.createNativeQuery(
                "SELECT pg_catalog.setval('certified_product_certified_product_id_seq', 20, true)");
        resetSeqQuery.getSingleResult();
        resetSeqQuery = entityManager.createNativeQuery(
                "SELECT pg_catalog.setval('certified_product_testing_lab_map_id_seq', 19, true)");
        resetSeqQuery.getSingleResult();
        resetSeqQuery = entityManager.createNativeQuery(
                "SELECT pg_catalog.setval('certification_result_certification_result_id_seq', 12, true)");
        resetSeqQuery.getSingleResult();
        resetSeqQuery = entityManager.createNativeQuery(
                "SELECT pg_catalog.setval('user_user_id_seq', 7, true)");
        resetSeqQuery.getSingleResult();
        tx.commit();
        entityManager.close();
    }
}
