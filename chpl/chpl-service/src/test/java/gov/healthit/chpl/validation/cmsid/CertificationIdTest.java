package gov.healthit.chpl.validation.cmsid;

import java.util.ArrayList;
import java.util.List;

import org.ff4j.FF4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.transaction.annotation.Transactional;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;

import gov.healthit.chpl.FeatureList;
import gov.healthit.chpl.certificationId.Validator;
import gov.healthit.chpl.certificationId.ValidatorFactory;
import gov.healthit.chpl.dto.CQMMetDTO;
import gov.healthit.chpl.manager.CertificationIdManager;
import junit.framework.TestCase;

@ActiveProfiles({
    "Ff4jMock"
})
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
    gov.healthit.chpl.CHPLTestConfig.class, gov.healthit.chpl.Ff4jTestConfiguration.class
})
@TestExecutionListeners({
        DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class,
        TransactionalTestExecutionListener.class, DbUnitTestExecutionListener.class
})
@DatabaseSetup("classpath:data/testData.xml")
public class CertificationIdTest extends TestCase {
    @Autowired
    private CertificationIdManager certificationIdManager;

    @Autowired
    private ValidatorFactory validatorFactory;

    @Autowired
    private FF4j ff4j;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        Mockito.doReturn(true).when(ff4j).check(FeatureList.EFFECTIVE_RULE_DATE);
    }

    @Test
    @Transactional
    public void certificationId2014InfoMessagesTest() {

        List<Long> productIdList = new ArrayList<Long>();
        productIdList.add(294L);

        Validator validator = validatorFactory.getValidator("2014");

        // Lookup Criteria for Validating
        List<String> criteriaDtos = certificationIdManager.getCriteriaNumbersMetByCertifiedProductIds(productIdList);

        // Lookup CQMs for Validating
        List<CQMMetDTO> cqmDtos = certificationIdManager.getCqmsMetByCertifiedProductIds(productIdList);

        validator.validate(criteriaDtos, cqmDtos, new ArrayList<Integer>(2014));
        assertNotNull(validator.getMissingXOr());
        assertNotNull(validator.getMissingCombo());
        assertNotNull(validator.getMissingOr());
        assertNotNull(validator.getMissingAnd());
    }

    @Test
    @Transactional
    public void certificationId2015InfoMessagesTest() {

        List<Long> productIdList = new ArrayList<Long>();
        productIdList.add(9261L);

        Validator validator = validatorFactory.getValidator("2015");

        // Lookup Criteria for Validating
        List<String> criteriaDtos = certificationIdManager.getCriteriaNumbersMetByCertifiedProductIds(productIdList);

        // Lookup CQMs for Validating
        List<CQMMetDTO> cqmDtos = certificationIdManager.getCqmsMetByCertifiedProductIds(productIdList);

        validator.validate(criteriaDtos, cqmDtos, new ArrayList<Integer>(2015));
        assertTrue(validator.getMissingXOr().isEmpty());
        assertTrue(validator.getMissingCombo().isEmpty());
        assertNotNull(validator.getMissingOr());
        assertNotNull(validator.getMissingAnd());
    }

    @Test
    @Transactional
    public void certificationId20142015InfoMessagesTest() {

        List<Long> productIdList = new ArrayList<Long>();
        productIdList.add(294L);
        productIdList.add(9261L);

        Validator validator = validatorFactory.getValidator("2014/2015");

        // Lookup Criteria for Validating
        List<String> criteriaDtos = certificationIdManager.getCriteriaNumbersMetByCertifiedProductIds(productIdList);

        // Lookup CQMs for Validating
        List<CQMMetDTO> cqmDtos = certificationIdManager.getCqmsMetByCertifiedProductIds(productIdList);

        validator.validate(criteriaDtos, cqmDtos, new ArrayList<Integer>(2014));
        assertTrue(validator.getMissingXOr().isEmpty());
        assertNotNull(validator.getMissingCombo());
        assertNotNull(validator.getMissingOr());
        assertTrue(validator.getMissingAnd().isEmpty());
    }
}
