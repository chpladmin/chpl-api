package gov.healthit.chpl.dto;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
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
import gov.healthit.chpl.entity.developer.DeveloperStatusType;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        gov.healthit.chpl.CHPLTestConfig.class
})
@TestExecutionListeners({
        DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class,
        TransactionalTestExecutionListener.class, DbUnitTestExecutionListener.class
})
@DatabaseSetup("classpath:data/testData.xml")
public class DecertifiedDeveloperDTOTest {

    private static JWTAuthenticatedUser authUser;

    @Rule
    @Autowired
    public UnitTestRules cacheInvalidationRule;

    @BeforeClass
    public static void setUpClass() throws Exception {
        authUser = new JWTAuthenticatedUser();
        authUser.setFullName("Admin");
        authUser.setId(-2L);
        authUser.getPermissions().add(new GrantedPermission("ROLE_ADMIN"));
    }

    /**
     * Given a decertifiedDeveloperDTO object is constructed with the default
     * constructor Given a decertifiedDeveloperDTO object is constructed with
     * the overridden constructor When the decertifiedDeveloperDTO object is
     * constructed with null constructor parameters When the
     * decertifiedDeveloperDTO object is initialized with the default
     * constructor and null values are used in the object's methods Then no
     * exceptions or errors are thrown
     */
    @Test
    @Transactional
    public void decertifiedDeveloperHandlesNulls() {
        Date decertDate = new Date();
        Long developerId1 = -1L;
        List<Long> acbList1 = new ArrayList<Long>();
        acbList1.add(-1L);
        acbList1.add(null);
        String developerStatus1 = String.valueOf(DeveloperStatusType.UnderCertificationBanByOnc);
        Long numMeaningfulUse1 = 66L;
        DecertifiedDeveloperDTO dto1 = new DecertifiedDeveloperDTO(developerId1, acbList1, developerStatus1, decertDate,
                numMeaningfulUse1);
        assertTrue(dto1 != null);

        Long developerId2 = -1L;
        List<Long> acbList2 = new ArrayList<Long>();
        acbList2.add(-1L);
        acbList2.add(null);
        String developerStatus2 = String.valueOf(DeveloperStatusType.UnderCertificationBanByOnc);
        Long numMeaningfulUse2 = null;
        DecertifiedDeveloperDTO dto2 = new DecertifiedDeveloperDTO(developerId2, acbList2, developerStatus2, decertDate,
                numMeaningfulUse2);
        assertTrue(dto2 != null);

        DecertifiedDeveloperDTO dto3 = new DecertifiedDeveloperDTO();
        Long developerId3 = -1L;
        List<Long> acbList3 = new ArrayList<Long>();
        acbList3.add(-1L);
        acbList3.add(null);
        String developerStatus3 = String.valueOf(DeveloperStatusType.UnderCertificationBanByOnc);
        Long numMeaningfulUse3 = null;
        dto3.addAcb(-5L);
        dto3.setAcbList(acbList3);
        dto3.setDeveloperId(developerId3);
        dto3.setDeveloperStatus(developerStatus3);
        dto3.incrementNumMeaningfulUse(numMeaningfulUse3);
        assertTrue(dto3 != null);

        Long developerId4 = -1L;
        List<Long> acbList4 = new ArrayList<Long>();
        acbList4.add(null);
        String developerStatus4 = String.valueOf(DeveloperStatusType.UnderCertificationBanByOnc);
        Long numMeaningfulUse4 = null;
        DecertifiedDeveloperDTO dto4 = new DecertifiedDeveloperDTO(developerId4, null, developerStatus4, decertDate,
                numMeaningfulUse4);
        assertTrue(dto4 != null);
        DecertifiedDeveloperDTO dto5 = new DecertifiedDeveloperDTO(developerId4, acbList4, developerStatus4, decertDate,
                numMeaningfulUse4);
        assertTrue(dto5 != null);
    }

}
