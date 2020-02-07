package old.gov.healthit.chpl;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;

import gov.healthit.chpl.caching.UnitTestRules;
import junit.framework.TestCase;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        old.gov.healthit.chpl.CHPLTestConfig.class
})
@TestExecutionListeners({
        DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class,
        TransactionalTestExecutionListener.class
})
public class ErrorPropertiesTest extends TestCase {
    @Autowired
    MessageSource messageSource;

    @Rule
    @Autowired
    public UnitTestRules cacheInvalidationRule;

    @Test
    public void testFormatErrorMessage() {
        String result = String.format(messageSource.getMessage(
                new DefaultMessageSourceResolvable("pendingSurveillance.addSurveillancePermissionDenied"),
                LocaleContextHolder.getLocale()), "CHP-12345");
        assertEquals("User does not have permission to add surveillance to 'CHP-12345'.", result);
    }
}
