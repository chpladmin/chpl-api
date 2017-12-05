package gov.healthit.chpl.upload;


import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
import gov.healthit.chpl.dao.ActivityDAO;
import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.domain.concept.ActivityConcept;
import gov.healthit.chpl.dto.ActivityDTO;
import gov.healthit.chpl.upload.certifiedProduct.CertifiedProductHandler2014Version1;
import junit.framework.TestCase;



@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { gov.healthit.chpl.CHPLTestConfig.class })
public class CertifiedProductHandlerTest extends TestCase {
	
	private String[] criteriaNames = {
            "170.314 (a)(1)", "170.314 (a)(2)", "170.314 (a)(3)", "170.314 (a)(4)", "170.314 (a)(5)",
            "170.314 (a)(6)", "170.314 (a)(7)", "170.314 (a)(8)", "170.314 (a)(9)", "170.314 (a)(10)",
            "170.314 (a)(11)", "170.314 (a)(12)", "170.314 (a)(13)", "170.314 (a)(14)", "170.314 (a)(15)",
            "170.314 (a)(16)", "170.314 (a)(17)", "170.314 (a)(18)", "170.314 (a)(19)", "170.314 (a)(20)",
            "170.314 (b)(1)", "170.314 (b)(2)", "170.314 (b)(3)", "170.314 (b)(4)", "170.314 (b)(5)(A)",
            "170.314 (b)(5)(B)", "170.314 (b)(6)", "170.314 (b)(7)", "170.314 (b)(8)", "170.314 (b)(9)",
            "170.314 (c)(1)", "170.314 (c)(2)", "170.314 (c)(3)", "170.314 (d)(1)", "170.314 (d)(2)",
            "170.314 (d)(3)", "170.314 (d)(4)", "170.314 (d)(5)", "170.314 (d)(6)", "170.314 (d)(7)",
            "170.314 (d)(8)", "170.314 (d)(9)", "170.314 (e)(1)", "170.314 (e)(2)", "170.314 (e)(3)",
            "170.314 (f)(1)", "170.314 (f)(2)", "170.314 (f)(3)", "170.314 (f)(4)", "170.314 (f)(5)",
            "170.314 (f)(6)", "170.314 (f)(7)", "170.314 (g)(1)", "170.314 (g)(2)", "170.314 (g)(3)",
            "170.314 (g)(4)", "170.314 (h)(1)",  "170.314 (h)(2)", "170.314 (h)(3)"
    };
	
	@Autowired
	@Qualifier("certifiedProductHandler2014Version1")
	private CertifiedProductHandler2014Version1 productHandler;
	
	
	@Test
	public void testCertificationList(){
		String[] constructedList = productHandler.getCriteriaNames();
		System.out.println(constructedList.toString());
		assertTrue(Arrays.asList(constructedList).containsAll(Arrays.asList(criteriaNames)));
		
	}
	
	
}
