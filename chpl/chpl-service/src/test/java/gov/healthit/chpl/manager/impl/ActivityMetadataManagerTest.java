package gov.healthit.chpl.manager.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonParseException;
import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;

import gov.healthit.chpl.auth.permission.GrantedPermission;
import gov.healthit.chpl.auth.user.JWTAuthenticatedUser;
import gov.healthit.chpl.auth.user.UserRetrievalException;
import gov.healthit.chpl.caching.UnitTestRules;
import gov.healthit.chpl.domain.UserActivity;
import gov.healthit.chpl.domain.activity.ActivityConcept;
import gov.healthit.chpl.domain.activity.ActivityDetails;
import gov.healthit.chpl.domain.activity.ActivityMetadata;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.dto.DeveloperDTO;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.manager.ActivityManager;
import gov.healthit.chpl.manager.ActivityMetadataManager;
import gov.healthit.chpl.util.JSONUtils;
import junit.framework.TestCase;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        gov.healthit.chpl.CHPLTestConfig.class
})
@TestExecutionListeners({
        DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class,
        TransactionalTestExecutionListener.class, DbUnitTestExecutionListener.class
})
@DatabaseSetup("classpath:data/testData.xml")
public class ActivityMetadataManagerTest extends TestCase {
    @Autowired
    private ActivityMetadataManager metadataManager;

    private static JWTAuthenticatedUser adminUser, acbUser, atlUser;

    @Rule
    @Autowired
    public UnitTestRules cacheInvalidationRule;

    @BeforeClass
    public static void setUpClass() throws Exception {
        adminUser = new JWTAuthenticatedUser();
        adminUser.setFullName("Administrator");
        adminUser.setId(-2L);
        adminUser.setFriendlyName("Administrator");
        adminUser.setSubjectName("admin");
        adminUser.getPermissions().add(new GrantedPermission("ROLE_ADMIN"));

        acbUser = new JWTAuthenticatedUser();
        acbUser.setFullName("Test");
        acbUser.setId(3L);
        acbUser.setFriendlyName("User3");
        acbUser.setSubjectName("testUser3");
        acbUser.getPermissions().add(new GrantedPermission("ROLE_ACB"));

        atlUser = new JWTAuthenticatedUser();
        atlUser.setFullName("ATL");
        atlUser.setId(4L);
        atlUser.setFriendlyName("User");
        atlUser.setSubjectName("atlUser");
        atlUser.getPermissions().add(new GrantedPermission("ROLE_ATL"));
    }

    @Test
    @Transactional
    public void testGetActivityMetadataForAllListingsLoggedIn() throws JsonParseException, IOException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        Calendar start = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        start.set(2015, 9, 1, 0, 0);
        Calendar end = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        end.set(2015, 9, 20, 0, 0);

        List<ActivityMetadata> metadatas = metadataManager.getListingActivityMetadata(start.getTime(), end.getTime());
        assertEquals(3, metadatas.size());

        for (ActivityMetadata metadata : metadatas) {
            assertNotNull(metadata.getId());
            assertNotNull(metadata.getDate());
            assertNotNull(metadata.getObjectId());
            assertEquals(ActivityConcept.CERTIFIED_PRODUCT, metadata.getConcept());
        }
    }

    @Test
    @Transactional
    public void testGetActivityMetadataForListingLoggedIn() throws JsonParseException, IOException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        Long objectId = 1L;
        Calendar start = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        start.set(2015, 9, 1, 0, 0);
        Calendar end = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        end.set(2015, 9, 20, 0, 0);

        List<ActivityMetadata> metadatas = metadataManager.getListingActivityMetadata(objectId, start.getTime(), end.getTime());
        assertEquals(3, metadatas.size());

        for (ActivityMetadata metadata : metadatas) {
            assertNotNull(metadata.getId());
            assertNotNull(metadata.getDate());
            assertEquals(ActivityConcept.CERTIFIED_PRODUCT, metadata.getConcept());
            assertEquals(objectId, metadata.getObjectId());
        }
    }
}
