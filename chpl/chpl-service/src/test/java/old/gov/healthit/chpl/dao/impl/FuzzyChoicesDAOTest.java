package old.gov.healthit.chpl.dao.impl;

import java.util.ArrayList;
import java.util.List;

import gov.healthit.chpl.auth.permission.GrantedPermission;
import gov.healthit.chpl.auth.user.JWTAuthenticatedUser;
import gov.healthit.chpl.dao.FuzzyChoicesDAO;
import gov.healthit.chpl.entity.FuzzyType;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.dto.FuzzyChoicesDTO;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.BeforeClass;
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

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
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
public class FuzzyChoicesDAOTest {

    @Autowired
    FuzzyChoicesDAO fuzzyDao;

    private static JWTAuthenticatedUser authUser;

    @BeforeClass
    public static void setUpClass() throws Exception {
        authUser = new JWTAuthenticatedUser();
        authUser.setFullName("Admin");
        authUser.setId(-2L);
        authUser.getPermissions().add(new GrantedPermission("ROLE_ADMIN"));
    }

    @Test
    @Transactional(readOnly = true)
    public void testFuzzyDao() throws JsonParseException, JsonMappingException, IOException, EntityRetrievalException,
            EntityCreationException {
        SecurityContextHolder.getContext().setAuthentication(authUser);
        FuzzyChoicesDTO fuzzyReturned1 = fuzzyDao.getByType(FuzzyType.UCD_PROCESS);
        assertTrue(fuzzyReturned1.getChoices().contains("Multiple Standards"));
        FuzzyChoicesDTO fuzzyReturned2 = fuzzyDao.getByType(FuzzyType.QMS_STANDARD);
        assertTrue(fuzzyReturned2.getChoices().contains("ISO 13485:2003"));
        FuzzyChoicesDTO fuzzyReturned3 = fuzzyDao.getByType(FuzzyType.ACCESSIBILITY_STANDARD);
        assertTrue(fuzzyReturned3.getChoices().contains("WCAG 2.0 Level AA"));
    }

    @Test
    @Transactional
    @Rollback(true)
    public void updateFuzzyChoicesList() throws EntityRetrievalException, EntityCreationException, JsonParseException,
            JsonMappingException, IOException {
        // arrange
        SecurityContextHolder.getContext().setAuthentication(authUser);
        Long lastModifiedUserId = -4L;
        FuzzyChoicesDTO fuzzy = fuzzyDao.getByType(FuzzyType.UCD_PROCESS);
        FuzzyChoicesDTO fuzzyToUpdate = fuzzyDao.getByType(FuzzyType.UCD_PROCESS);
        List<String> choices = new ArrayList<String>();
        choices.add("a string");
        fuzzyToUpdate.setChoices(choices);
        authUser.setId(lastModifiedUserId);

        // act
        FuzzyChoicesDTO updatedFuzzy = fuzzyDao.update(fuzzyToUpdate);

        // assert
        assertNotNull(updatedFuzzy);
        assertNotNull(updatedFuzzy.getId());
        assertEquals(updatedFuzzy.getChoices(), choices);
        assertEquals(lastModifiedUserId.longValue(), updatedFuzzy.getLastModifiedUser().longValue());
    }
}
