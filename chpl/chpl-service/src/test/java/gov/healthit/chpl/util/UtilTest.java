package gov.healthit.chpl.util;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;

import gov.healthit.chpl.dto.CertificationCriterionDTO;
import junit.framework.TestCase;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { gov.healthit.chpl.CHPLTestConfig.class })
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class,
    DirtiesContextTestExecutionListener.class})
public class UtilTest extends TestCase {

    private CertificationCriterionDTO c1;
    private CertificationCriterionDTO c2;

    @Before
    public void setup() {
        c1 = new CertificationCriterionDTO();
        c2 = new CertificationCriterionDTO();
    }

    @Test
    public void sortCriteriaBetweenEditions() {
        c1.setNumber("170.302 (h)");
        c2.setNumber("170.314 (a)(1)");
        assertTrue("2011 should be earlier than 2014", Util.sortCriteria(c1,  c2) < 0);
        c1.setNumber("170.315 (b)(2)");
        assertTrue("2015 should be later than 2014", Util.sortCriteria(c1,  c2) > 0);
        c2.setNumber("170.304 (d)");
        assertTrue("2015 should be later than 2011", Util.sortCriteria(c1,  c2) > 0);
    }

    @Test
    public void sortCriteriaWithOneParagraph() {
        c1.setNumber("170.302 (h)");
        c2.setNumber("170.302 (i)");
        assertTrue("h should be earlier than i", Util.sortCriteria(c1,  c2) < 0);
    }

    @Test
    public void sortCriteriaWithTwoParagraphs() {
        c1.setNumber("170.314 (a)(3)");
        c2.setNumber("170.314 (a)(2)");
        assertTrue("3 should be after 2", Util.sortCriteria(c1,  c2) > 0);
    }

    @Test
    public void sortCriteriaWithThreeParagraphs() {
        c1.setNumber("170.314 (d)(3)(B)");
        c2.setNumber("170.314 (d)(3)(A)");
        assertTrue("B should be after A", Util.sortCriteria(c1,  c2) > 0);
    }

    @Test
    public void sortCriteriaWithDifferingParagraphComponentCount() {
        c1.setNumber("170.314 (d)(3)");
        c2.setNumber("170.314 (d)(3)(A)");
        assertTrue("fewer paragraphs should be before ones with more", Util.sortCriteria(c1,  c2) < 0);
    }

    @Test
    public void sortCriteriaWithMatchingNumbers() {
        c1.setNumber("170.314 (a)(3)");
        c1.setTitle("This is a title");
        c2.setNumber("170.314 (a)(3)");
        c2.setTitle("This is a title (Cures Update)");
        assertTrue("should sort by title if paragraphs match", Util.sortCriteria(c1,  c2) < 0);
    }
}
