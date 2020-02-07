package old.gov.healthit.chpl.dao.impl;

import gov.healthit.chpl.auth.permission.GrantedPermission;
import gov.healthit.chpl.auth.user.JWTAuthenticatedUser;
import gov.healthit.chpl.dao.statistics.NonconformityTypeStatisticsDAO;
import gov.healthit.chpl.dao.statistics.SurveillanceStatisticsDAO;
import gov.healthit.chpl.dto.NonconformityTypeStatisticsDTO;

import java.util.List;

import junit.framework.TestCase;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.transaction.annotation.Transactional;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
    old.gov.healthit.chpl.CHPLTestConfig.class
})
@TestExecutionListeners({
        DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class,
        TransactionalTestExecutionListener.class, DbUnitTestExecutionListener.class
})
@DatabaseSetup("classpath:data/testData.xml")
public class NonconformityTypeStatisticsDAOTest extends TestCase {

    @Autowired
    private SurveillanceStatisticsDAO statisticsDAO;

    @Autowired
    private NonconformityTypeStatisticsDAO nonconformStatDAO;

    private static JWTAuthenticatedUser adminUser;

    @BeforeClass
    public static void setUpClass() throws Exception {
        adminUser = new JWTAuthenticatedUser();
        adminUser.setFullName("Administrator");
        adminUser.setId(-2L);
        adminUser.setFriendlyName("Administrator");
        adminUser.setSubjectName("admin");
        adminUser.getPermissions().add(new GrantedPermission("ROLE_ADMIN"));
    }

    @Test
    @Transactional
    public void buildNonconformityTypeStatistics() {
        SecurityContextHolder.getContext().setAuthentication(adminUser);

        List<NonconformityTypeStatisticsDTO> dtos = statisticsDAO.getAllNonconformitiesByCriterion();
        assertNotNull(dtos);
        assertEquals(1, dtos.size());
        for (NonconformityTypeStatisticsDTO dto : dtos) {
            nonconformStatDAO.create(dto);
        }
        List<NonconformityTypeStatisticsDTO> created = nonconformStatDAO.getAllNonconformityStatistics();
        assertNotNull(created);
        assertEquals(1, created.size());
    }
}
