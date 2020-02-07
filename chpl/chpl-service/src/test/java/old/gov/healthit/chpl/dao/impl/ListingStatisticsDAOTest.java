package old.gov.healthit.chpl.dao.impl;

import java.util.Date;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.transaction.annotation.Transactional;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;

import gov.healthit.chpl.auth.permission.GrantedPermission;
import gov.healthit.chpl.auth.user.JWTAuthenticatedUser;
import gov.healthit.chpl.caching.UnitTestRules;
import gov.healthit.chpl.dao.statistics.ListingStatisticsDAO;
import gov.healthit.chpl.domain.DateRange;
import gov.healthit.chpl.domain.statistics.CertifiedBodyStatistics;
import junit.framework.TestCase;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        old.gov.healthit.chpl.CHPLTestConfig.class
})
@TestExecutionListeners({
        DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class,
        TransactionalTestExecutionListener.class, DbUnitTestExecutionListener.class
})
@DatabaseSetup("classpath:data/testData.xml")
public class ListingStatisticsDAOTest extends TestCase {

    @Autowired
    private ListingStatisticsDAO listingStatisticsDAO;

    @Rule
    @Autowired
    public UnitTestRules cacheInvalidationRule;

    private static JWTAuthenticatedUser authUser;

    @BeforeClass
    public static void setUpClass() throws Exception {
        authUser = new JWTAuthenticatedUser();
        authUser.setFullName("Admin");
        authUser.setId(-2L);
        authUser.getPermissions().add(new GrantedPermission("ROLE_ADMIN"));
    }

    @Test
    @Transactional
    public void getTotalCPListingsEachYearByCertifiedBodyAndCertificationStatus() {
        DateRange dr = new DateRange(new Date(0), new Date());
        List<CertifiedBodyStatistics> results = listingStatisticsDAO
                .getTotalCPListingsEachYearByCertifiedBodyAndCertificationStatus(dr);
        assertNotNull(results);
    }

    @Test
    @Transactional
    public void getTotalCPListingsEachYearByCertifiedBody() {
        DateRange dr = new DateRange(new Date(0), new Date());
        List<CertifiedBodyStatistics> results = listingStatisticsDAO.getTotalCPListingsEachYearByCertifiedBody(dr);
        assertNotNull(results);
    }

}
