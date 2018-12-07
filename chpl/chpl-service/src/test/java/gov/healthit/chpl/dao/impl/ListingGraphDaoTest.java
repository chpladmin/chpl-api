package gov.healthit.chpl.dao.impl;

import java.util.ArrayList;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.annotation.Rollback;
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
import gov.healthit.chpl.dao.ListingGraphDAO;
import gov.healthit.chpl.dto.CertifiedProductDTO;
import gov.healthit.chpl.dto.ListingToListingMapDTO;
import gov.healthit.chpl.exception.EntityCreationException;
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
public class ListingGraphDaoTest extends TestCase {

    @Autowired
    private ListingGraphDAO listingGraphDao;
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

    @Test
    @Transactional
    @Rollback(true)
    public void testGetLargestIcsValue() {
        List<Long> listingIds = new ArrayList<Long>();
        listingIds.add(5L);
        listingIds.add(6L);

        Integer largestIcs = listingGraphDao.getLargestIcs(listingIds);
        assertNotNull(largestIcs);
        assertEquals(2, largestIcs.longValue());
    }

    @Test
    @Transactional
    @Rollback(true)
    public void testGetLargestIcsValueWithNullIcsCodes() {
        List<Long> listingIds = new ArrayList<Long>();
        listingIds.add(5L);
        listingIds.add(17L);

        Integer largestIcs = listingGraphDao.getLargestIcs(listingIds);
        assertNotNull(largestIcs);
        assertEquals(1, largestIcs.longValue());
    }

    @Test
    @Transactional
    @Rollback(true)
    public void testGetParentListings() {
        List<CertifiedProductDTO> parents = listingGraphDao.getParents(5L);
        assertNotNull(parents);
        assertEquals(2, parents.size());
        for (CertifiedProductDTO parent : parents) {
            switch (parent.getId().intValue()) {
            case 6:
            case 9:
                break;
            default:
                fail("Expected 6 and 9 but found " + parent.getId().intValue());
            }
        }
    }

    @Test
    @Transactional
    @Rollback(true)
    public void testGetChildListings() {
        List<CertifiedProductDTO> children = listingGraphDao.getChildren(9L);
        assertNotNull(children);
        assertEquals(1, children.size());
        for (CertifiedProductDTO child : children) {
            switch (child.getId().intValue()) {
            case 5:
                break;
            default:
                fail("Expected 5 but found " + child.getId().intValue());
            }
        }
    }

    @Test
    @Transactional
    @Rollback(true)
    public void testGetListingMapByParentAndChildIds() {
        ListingToListingMapDTO dto = listingGraphDao.getListingMap(5L, 6L);
        assertNotNull(dto);
        assertEquals(5, dto.getChildId().longValue());
        assertEquals(6, dto.getParentId().longValue());
    }

    @Test
    @Transactional
    @Rollback(true)
    public void testCreateListingMap() throws EntityCreationException {
        SecurityContextHolder.getContext().setAuthentication(authUser);

        ListingToListingMapDTO dto = new ListingToListingMapDTO();
        dto.setChildId(1L);
        dto.setParentId(2L);

        listingGraphDao.createListingMap(dto);

        SecurityContextHolder.getContext().setAuthentication(null);
    }
}
