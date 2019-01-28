package gov.healthit.chpl.dto;

import static org.junit.Assert.assertTrue;
import gov.healthit.chpl.entity.FuzzyType;
import gov.healthit.chpl.entity.FuzzyChoicesEntity;

import java.io.IOException;

import org.junit.Test;
import org.junit.runner.RunWith;
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

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        gov.healthit.chpl.CHPLTestConfig.class
})
@TestExecutionListeners({
        DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class,
        TransactionalTestExecutionListener.class, DbUnitTestExecutionListener.class
})
public class FuzzyChoiceDTOTest {

    @Test
    @Transactional
    public void testFuzzy() throws JsonParseException, JsonMappingException, IOException {
        FuzzyChoicesEntity entity = new FuzzyChoicesEntity();
        entity.setId(1L);
        entity.setFuzzyType(FuzzyType.UCD_PROCESS);
        entity.setChoices(
                "[\"Multiple Standards\",\"ISO 9241-210:2010 4.2\",\"ISO/IEC 25062:2006\",\"Homegrown\",\"NISTIR 7742\",\"(NISTIR 7741) NIST Guide to the Processes Approach for Improving the Usability of Electronic Health Records\",\"IEC 62366\",\"Internal Process Used\",\"IEC 62366-1\",\"ISO 13407\",\"ISO 16982\",\"ISO/IEC 62367\"]");
        FuzzyChoicesDTO fuzzy = new FuzzyChoicesDTO(entity);
        assertTrue(fuzzy.getChoices().contains("Multiple Standards"));
    }

}
