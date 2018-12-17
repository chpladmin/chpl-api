package gov.healthit.chpl.util;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;

import com.github.springtestdbunit.annotation.DatabaseSetup;

import gov.healthit.chpl.caching.UnitTestRules;
import junit.framework.TestCase;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { gov.healthit.chpl.CHPLTestConfig.class })
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class,
    DirtiesContextTestExecutionListener.class})
@DatabaseSetup("classpath:data/testData.xml")
public class ChplProductNumberUtilTest extends TestCase {

    @Autowired private ChplProductNumberUtil util;

    @Rule
    @Autowired
    public UnitTestRules cacheInvalidationRule;

    @Test
    public void testIsLegacyChplProductNumberLegacy() {
        String chplProductNumber = "CHP-000001";
        assertTrue(util.isLegacy(chplProductNumber));
    }

    @Test
    public void testIsNewChplProductNumberLegacy() {
        String chplProductNumber = "15.02.02.3007.A056.01.00.0.180214";
        assertFalse(util.isLegacy(chplProductNumber));
    }
}
