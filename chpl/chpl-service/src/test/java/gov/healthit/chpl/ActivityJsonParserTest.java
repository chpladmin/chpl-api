package gov.healthit.chpl;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;

import gov.healthit.chpl.CHPLTestConfig;
import gov.healthit.chpl.auth.json.User;
import gov.healthit.chpl.caching.UnitTestRules;
import gov.healthit.chpl.dao.ActivityDAO;
import gov.healthit.chpl.dao.search.CertifiedProductSearchDAO;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.activity.ActivityConcept;
import gov.healthit.chpl.domain.activity.ActivityMetadata;
import gov.healthit.chpl.domain.search.CertifiedProductFlatSearchResult;
import gov.healthit.chpl.dto.ActivityDTO;
import gov.healthit.chpl.scheduler.job.chartdata.CriterionProductStatisticsCalculator;
import junit.framework.TestCase;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        gov.healthit.chpl.CHPLTestConfig.class
})
@TestExecutionListeners({
    DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class,
    TransactionalTestExecutionListener.class
})
public class ActivityJsonParserTest extends TestCase {
    private ObjectMapper jsonMapper = new ObjectMapper();
    @Autowired ActivityDAO activityDao;

    @Test
    @Transactional
    public void parseListingActivity() {
        Calendar startCal = Calendar.getInstance();
        startCal.set(2017, 12, 1, 0, 0, 0);
        Calendar endCal = Calendar.getInstance();
        endCal.set(2018, 5, 27, 23, 0, 0);
        List<ActivityDTO> activityDtos =
                activityDao.findByConcept(ActivityConcept.CERTIFIED_PRODUCT, startCal.getTime(), endCal.getTime());

        System.out.println("Found " + activityDtos.size() + " from " + startCal.getTime() + " until " + endCal.getTime());

        for (ActivityDTO dto : activityDtos) {
            ActivityMetadata metadata = new ActivityMetadata();
            //parse all normal data that every listing activity should have
            metadata.setId(dto.getId());
            metadata.setDate(dto.getActivityDate());
            metadata.setObjectId(dto.getActivityObjectId());
            metadata.setConcept(dto.getConcept());
            metadata.setResponsibleUser(dto.getUser() == null ? null : new User(dto.getUser()));
            //try parsing the certified products as such
            CertifiedProductSearchDetails origListing = null;
            if (dto.getOriginalData() != null) {
                System.out.println("Activity ID " + dto.getId() + " origData not null. Parsing as CertifiedProductSearchDetails.");
                try {
                    origListing =
                        jsonMapper.readValue(dto.getOriginalData(), CertifiedProductSearchDetails.class);
                    System.out.println("Certification Status:" + origListing.getCertificationStatus());
                } catch(Exception ex) {
                    System.out.println("Could not parse activity ID " + dto.getId() + " original data. JSON was: ");
                    System.out.println(dto.getOriginalData());
                    ex.printStackTrace();
                    fail();
                }
            }

            CertifiedProductSearchDetails newListing = null;
            if (dto.getNewData() != null) {
                System.out.println("Activity ID " + dto.getId() + " newData not null. Parsing as CertifiedProductSearchDetails.");
                try {
                    newListing =
                        jsonMapper.readValue(dto.getNewData(), CertifiedProductSearchDetails.class);
                    System.out.println("Certification Status:" + newListing.getCertificationStatus());
                } catch(Exception ex) {
                    System.out.println("Could not parse activity ID " + dto.getId() + " new data. JSON was: ");
                    System.out.println(dto.getNewData());
                    ex.printStackTrace();
                    fail();
                }
            }

            if(newListing != null) {
                if(newListing.getChplProductNumber() == null) {
                    fail("No CHPL Product number");
                }
                System.out.println(newListing.getChplProductNumber());
                if (newListing.getCertifyingBody() != null && newListing.getCertifyingBody().get("name") != null) {
                    System.out.println(newListing.getCertifyingBody().get("name").toString());
                } else {
                    fail("no ACB name");
                }
                if(newListing.getCertificationDate() == null) {
                    if(dto.getId().longValue() != 28762) {
                        fail("No certification date");
                    }
                }
                System.out.println(newListing.getCertificationDate());
                if (newListing.getDeveloper() != null) {
                    System.out.println(newListing.getDeveloper().getName());
                } else {
                    fail("No developer name");
                }
                if (newListing.getCertificationEdition() != null && newListing.getCertificationEdition().get("name") != null) {
                    System.out.println(newListing.getCertificationEdition().get("name").toString());
                } else {
                    fail("No certification edition");
                }
                if (newListing.getProduct() != null) {
                    System.out.println(newListing.getProduct().getName());
                } else {
                    fail("No product name");
                }
            }
        }
    }
}
