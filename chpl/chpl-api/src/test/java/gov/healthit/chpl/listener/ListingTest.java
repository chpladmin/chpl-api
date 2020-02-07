package gov.healthit.chpl.listener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

import org.ff4j.FF4j;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;

import gov.healthit.chpl.FeatureList;
import gov.healthit.chpl.auth.permission.GrantedPermission;
import gov.healthit.chpl.auth.user.JWTAuthenticatedUser;
import gov.healthit.chpl.caching.UnitTestRules;
import gov.healthit.chpl.dao.QuestionableActivityDAO;
import gov.healthit.chpl.domain.CQMResultDetails;
import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertificationResultAdditionalSoftware;
import gov.healthit.chpl.domain.CertificationResultTestData;
import gov.healthit.chpl.domain.CertificationResultTestProcedure;
import gov.healthit.chpl.domain.CertificationResultTestStandard;
import gov.healthit.chpl.domain.CertificationStatus;
import gov.healthit.chpl.domain.CertificationStatusEvent;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.ListingUpdateRequest;
import gov.healthit.chpl.domain.UcdProcess;
import gov.healthit.chpl.domain.concept.QuestionableActivityTriggerConcept;
import gov.healthit.chpl.dto.questionableActivity.QuestionableActivityListingDTO;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.InvalidArgumentsException;
import gov.healthit.chpl.exception.MissingReasonException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.manager.CertifiedProductDetailsManager;
import gov.healthit.chpl.web.controller.CertifiedProductController;
import junit.framework.TestCase;

/**
 * Test Listing updates for questionable activity.
 * @author alarned
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { old.gov.healthit.chpl.CHPLTestConfig.class })
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class,
    DirtiesContextTestExecutionListener.class,
    TransactionalTestExecutionListener.class,
    DbUnitTestExecutionListener.class })
@DatabaseSetup("classpath:data/testData.xml")
public class ListingTest extends TestCase {

    @Autowired private QuestionableActivityDAO qaDao;
    @Autowired private CertifiedProductController cpController;
    @Autowired private CertifiedProductDetailsManager cpdManager;
    @Autowired private FF4j ff4j;

    private static JWTAuthenticatedUser adminUser;
    private static final long ADMIN_ID = -2L;
    private static final String REASON = "test reason";

    @Rule
    @Autowired
    public UnitTestRules cacheInvalidationRule;

    /**
     * Set up class for tests.
     *
     * @throws Exception if user can't be created
     */
    @BeforeClass
    public static void setUpClass() throws Exception {
        adminUser = new JWTAuthenticatedUser();
        adminUser.setFullName("Administrator");
        adminUser.setId(ADMIN_ID);
        adminUser.setFriendlyName("Administrator");
        adminUser.setSubjectName("admin");
        adminUser.getPermissions().add(new GrantedPermission("ROLE_ADMIN"));
        adminUser.getPermissions().add(new GrantedPermission("ROLE_ACB"));
    }

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        Mockito.doReturn(true).when(ff4j).check(FeatureList.EFFECTIVE_RULE_DATE);
    }

    @Test
    @Transactional
    @Rollback
    public void testUpdate2011ListingIncludesReason() throws
    EntityCreationException, EntityRetrievalException,
    ValidationException, InvalidArgumentsException, JsonProcessingException,
    MissingReasonException, IOException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);

        final long cpId = 3L;
        final long expectedId = 3L;
        Date beforeActivity = new Date();
        CertifiedProductSearchDetails listing = cpdManager.getCertifiedProductDetails(cpId);
        listing.setAcbCertificationId("NEWACBCERTIFICATIONID");
        ListingUpdateRequest updateRequest = new ListingUpdateRequest();
        updateRequest.setListing(listing);
        updateRequest.setReason(REASON);
        cpController.updateCertifiedProduct(updateRequest);
        Date afterActivity = new Date();

        List<QuestionableActivityListingDTO> activities =
                qaDao.findListingActivityBetweenDates(beforeActivity, afterActivity);
        assertNotNull(activities);
        assertEquals(1, activities.size());
        QuestionableActivityListingDTO activity = activities.get(0);
        assertEquals(expectedId, activity.getListingId().longValue());
        assertNull(activity.getBefore());
        assertNull(activity.getAfter());
        assertNotNull(activity.getReason());
        assertEquals(REASON, activity.getReason());
        assertEquals(QuestionableActivityTriggerConcept.EDITION_2011_EDITED.getName(), activity.getTrigger().getName());

        SecurityContextHolder.getContext().setAuthentication(null);
    }

    @Test
    @Transactional
    @Rollback
    public void testUpdateCertificationStatusIncludesReason() throws
    EntityCreationException, EntityRetrievalException, InvalidArgumentsException, JsonProcessingException,
    MissingReasonException, IOException, ValidationException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);

        Date beforeActivity = new Date();
        CertifiedProductSearchDetails listing = cpdManager.getCertifiedProductDetails(1L);
        for (CertificationResult result : listing.getCertificationResults()) {
            result.setSed(Boolean.FALSE);
            result.setGap(Boolean.FALSE);
        }
        CertificationStatusEvent currentStatus = listing.getCurrentStatus();
        int currStatusIndex = 0;
        List<CertificationStatusEvent> events = listing.getCertificationEvents();
        for (int i = 0; i < events.size(); i++) {
            CertificationStatusEvent currEvent = events.get(i);
            if (currEvent.getId().longValue() == currentStatus.getId().longValue()) {
                currStatusIndex = i;
            }
        }
        CertificationStatus status = new CertificationStatus();
        status.setId(2L);
        status.setName("Retired");
        currentStatus.setStatus(status);
        events.set(currStatusIndex, currentStatus);
        listing.setCertificationEvents(events);

        ListingUpdateRequest updateRequest = new ListingUpdateRequest();
        updateRequest.setListing(listing);
        updateRequest.setReason(REASON);
        cpController.updateCertifiedProduct(updateRequest);
        Date afterActivity = new Date();

        List<QuestionableActivityListingDTO> activities =
                qaDao.findListingActivityBetweenDates(beforeActivity, afterActivity);
        assertNotNull(activities);
        assertEquals(2, activities.size());
        QuestionableActivityListingDTO testedActivity = findActivityOfType(activities,
                QuestionableActivityTriggerConcept.CERTIFICATION_STATUS_EDITED_CURRENT);
        assertNotNull(testedActivity);
        assertEquals(1, testedActivity.getListingId().longValue());
        assertEquals("Active", testedActivity.getBefore());
        assertEquals("Retired", testedActivity.getAfter());
        assertNotNull(testedActivity.getReason());
        assertEquals(REASON, testedActivity.getReason());
        assertEquals(QuestionableActivityTriggerConcept.CERTIFICATION_STATUS_EDITED_CURRENT.getName(),
                testedActivity.getTrigger().getName());

        testedActivity = findActivityOfType(activities,
                QuestionableActivityTriggerConcept.EDITION_2014_EDITED);
        assertNotNull(testedActivity);
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    @Test
    @Transactional
    @Rollback
    public void testUpdateCertificationStatus() throws
    EntityCreationException, EntityRetrievalException, InvalidArgumentsException, JsonProcessingException,
    MissingReasonException, IOException, ValidationException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);

        Date beforeActivity = new Date();
        CertifiedProductSearchDetails listing = cpdManager.getCertifiedProductDetails(1L);
        for (CertificationResult result : listing.getCertificationResults()) {
            result.setSed(Boolean.FALSE);
            result.setGap(Boolean.FALSE);
        }
        CertificationStatusEvent currentStatus = listing.getCurrentStatus();
        int currStatusIndex = 0;
        List<CertificationStatusEvent> events = listing.getCertificationEvents();
        for (int i = 0; i < events.size(); i++) {
            CertificationStatusEvent currEvent = events.get(i);
            if (currEvent.getId().longValue() == currentStatus.getId().longValue()) {
                currStatusIndex = i;
            }
        }
        CertificationStatus status = new CertificationStatus();
        status.setId(2L);
        status.setName("Retired");
        currentStatus.setStatus(status);
        events.set(currStatusIndex, currentStatus);
        listing.setCertificationEvents(events);

        ListingUpdateRequest updateRequest = new ListingUpdateRequest();
        updateRequest.setListing(listing);
        updateRequest.setReason(REASON);
        cpController.updateCertifiedProduct(updateRequest);
        Date afterActivity = new Date();

        List<QuestionableActivityListingDTO> activities =
                qaDao.findListingActivityBetweenDates(beforeActivity, afterActivity);
        assertNotNull(activities);
        assertEquals(2, activities.size());
        QuestionableActivityListingDTO testedActivity = findActivityOfType(activities,
                QuestionableActivityTriggerConcept.CERTIFICATION_STATUS_EDITED_CURRENT);
        assertNotNull(testedActivity);
        assertEquals(1, testedActivity.getListingId().longValue());
        assertEquals("Active", testedActivity.getBefore());
        assertEquals("Retired", testedActivity.getAfter());
        assertEquals(REASON, testedActivity.getReason());
        assertEquals(QuestionableActivityTriggerConcept.CERTIFICATION_STATUS_EDITED_CURRENT.getName(),
                testedActivity.getTrigger().getName());

        testedActivity = findActivityOfType(activities,
                QuestionableActivityTriggerConcept.EDITION_2014_EDITED);
        assertNotNull(testedActivity);
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    @Test
    @Transactional
    @Rollback
    public void testUpdateCertificationStatusDate() throws
    EntityCreationException, EntityRetrievalException, InvalidArgumentsException, JsonProcessingException,
    MissingReasonException, IOException, ValidationException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);

        Date beforeActivity = new Date();
        Date eventDate = new Date("2/14/2018");
        CertifiedProductSearchDetails listing = cpdManager.getCertifiedProductDetails(1L);
        for (CertificationResult result : listing.getCertificationResults()) {
            result.setSed(Boolean.FALSE);
            result.setGap(Boolean.FALSE);
        }
        CertificationStatusEvent currentStatus = listing.getCurrentStatus();
        int currStatusIndex = 0;
        List<CertificationStatusEvent> events = listing.getCertificationEvents();
        for (int i = 0; i < events.size(); i++) {
            CertificationStatusEvent currEvent = events.get(i);
            if (currEvent.getId().longValue() == currentStatus.getId().longValue()) {
                currStatusIndex = i;
            }
        }
        currentStatus.setEventDate(eventDate.getTime());
        events.set(currStatusIndex, currentStatus);
        listing.setCertificationEvents(events);

        ListingUpdateRequest updateRequest = new ListingUpdateRequest();
        updateRequest.setListing(listing);
        updateRequest.setReason(REASON);
        cpController.updateCertifiedProduct(updateRequest);
        Date afterActivity = new Date();

        List<QuestionableActivityListingDTO> activities =
                qaDao.findListingActivityBetweenDates(beforeActivity, afterActivity);
        assertNotNull(activities);
        assertEquals(2, activities.size());
        QuestionableActivityListingDTO testedActivity = findActivityOfType(activities,
                QuestionableActivityTriggerConcept.CERTIFICATION_STATUS_DATE_EDITED_CURRENT);
        assertNotNull(testedActivity);
        assertEquals(1, testedActivity.getListingId().longValue());
        String timezoneDisplayStd = TimeZone.getDefault().getDisplayName(false, TimeZone.SHORT);
        String timezoneDisplayDst = TimeZone.getDefault().getDisplayName(true, TimeZone.SHORT);
        assertEquals("Tue Oct 20 13:14:00 " + timezoneDisplayDst + " 2015", testedActivity.getBefore());
        assertEquals("Wed Feb 14 00:00:00 " + timezoneDisplayStd + " 2018", testedActivity.getAfter());
        assertEquals(REASON, testedActivity.getReason());
        assertEquals(QuestionableActivityTriggerConcept.CERTIFICATION_STATUS_DATE_EDITED_CURRENT.getName(),
                testedActivity.getTrigger().getName());

        testedActivity = findActivityOfType(activities,
                QuestionableActivityTriggerConcept.EDITION_2014_EDITED);
        assertNotNull(testedActivity);
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    @Test
    @Transactional
    @Rollback
    public void testUpdateCertificationStatusHistoryDate() throws
    EntityCreationException, EntityRetrievalException, InvalidArgumentsException, JsonProcessingException,
    MissingReasonException, IOException, ValidationException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);

        final long dateDifference = 1000L;
        Date beforeActivity = new Date();
        CertifiedProductSearchDetails listing = cpdManager.getCertifiedProductDetails(1L);
        for(CertificationResult result : listing.getCertificationResults()) {
            result.setSed(Boolean.FALSE);
            result.setGap(Boolean.FALSE);
        }

        List<CertificationStatusEvent> events = listing.getCertificationEvents();
        int statusEventIndex = 0;
        for (int i = 0; i < events.size(); i++) {
            CertificationStatusEvent currEvent = events.get(i);
            if (currEvent.getStatus().getName().equals("Withdrawn by Developer")) {
                statusEventIndex = i;
            }
        }
        CertificationStatusEvent statusEvent = events.get(statusEventIndex);
        Long beforeEventDate = statusEvent.getEventDate();
        Long afterEventDate = beforeEventDate - dateDifference;
        statusEvent.setEventDate(afterEventDate);
        events.set(statusEventIndex, statusEvent);
        listing.setCertificationEvents(events);

        ListingUpdateRequest updateRequest = new ListingUpdateRequest();
        updateRequest.setListing(listing);
        updateRequest.setReason(REASON);
        cpController.updateCertifiedProduct(updateRequest);
        Date afterActivity = new Date();

        List<QuestionableActivityListingDTO> activities =
                qaDao.findListingActivityBetweenDates(beforeActivity, afterActivity);
        assertNotNull(activities);
        assertEquals(2, activities.size());
        QuestionableActivityListingDTO activity = findActivityOfType(activities,
                QuestionableActivityTriggerConcept.CERTIFICATION_STATUS_EDITED_HISTORY);
        assertNotNull(activity);
        assertEquals(1, activity.getListingId().longValue());
        String timezoneDisplay = TimeZone.getDefault().getDisplayName(true, TimeZone.SHORT);
        assertEquals("[Withdrawn by Developer (Sun Sep 20 13:14:00 " + timezoneDisplay + " 2015)]", activity.getBefore());
        assertEquals("[Withdrawn by Developer (Sun Sep 20 13:13:59 " + timezoneDisplay + " 2015)]", activity.getAfter());
        assertEquals(REASON, activity.getReason());
        assertEquals(QuestionableActivityTriggerConcept.CERTIFICATION_STATUS_EDITED_HISTORY.getName(),
                activity.getTrigger().getName());

        activity = findActivityOfType(activities,
                QuestionableActivityTriggerConcept.EDITION_2014_EDITED);
        assertNotNull(activity);
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    @Test
    @Transactional
    @Rollback
    public void testUpdateCertificationStatusHistoryStatus() throws
    EntityCreationException, EntityRetrievalException, InvalidArgumentsException, JsonProcessingException,
    MissingReasonException, IOException, ValidationException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);

        final long statusId = 4L;
        Date beforeActivity = new Date();
        CertifiedProductSearchDetails listing = cpdManager.getCertifiedProductDetails(1L);
        for (CertificationResult result : listing.getCertificationResults()) {
            result.setSed(Boolean.FALSE);
            result.setGap(Boolean.FALSE);
        }
        List<CertificationStatusEvent> events = listing.getCertificationEvents();
        int statusEventIndex = 0;
        for (int i = 0; i < events.size(); i++) {
            CertificationStatusEvent currEvent = events.get(i);
            if (currEvent.getStatus().getName().equals("Withdrawn by Developer")) {
                statusEventIndex = i;
            }
        }
        CertificationStatusEvent statusEvent = events.get(statusEventIndex);
        CertificationStatus status = new CertificationStatus();
        status.setId(statusId);
        status.setName("Withdrawn by ONC-ACB");
        statusEvent.setStatus(status);
        events.set(statusEventIndex, statusEvent);
        listing.setCertificationEvents(events);

        ListingUpdateRequest updateRequest = new ListingUpdateRequest();
        updateRequest.setListing(listing);
        updateRequest.setReason(REASON);
        cpController.updateCertifiedProduct(updateRequest);
        Date afterActivity = new Date();

        List<QuestionableActivityListingDTO> activities =
                qaDao.findListingActivityBetweenDates(beforeActivity, afterActivity);
        assertNotNull(activities);
        assertEquals(2, activities.size());
        QuestionableActivityListingDTO activity = findActivityOfType(activities,
                QuestionableActivityTriggerConcept.CERTIFICATION_STATUS_EDITED_HISTORY);
        assertNotNull(activity);
        assertEquals(1, activity.getListingId().longValue());

        String timezoneDisplayDst = TimeZone.getDefault().getDisplayName(true, TimeZone.SHORT);
        assertEquals("[Withdrawn by Developer (Sun Sep 20 13:14:00 " + timezoneDisplayDst + " 2015)]", activity.getBefore());
        assertEquals("[Withdrawn by ONC-ACB (Sun Sep 20 13:14:00 " + timezoneDisplayDst + " 2015)]", activity.getAfter());
        assertEquals(REASON, activity.getReason());
        assertEquals(QuestionableActivityTriggerConcept.CERTIFICATION_STATUS_EDITED_HISTORY.getName(),
                activity.getTrigger().getName());

        activity = findActivityOfType(activities,
                QuestionableActivityTriggerConcept.EDITION_2014_EDITED);
        assertNotNull(activity);
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    @Test
    @Transactional
    @Rollback
    public void testAddCqm() throws
    EntityCreationException, EntityRetrievalException, InvalidArgumentsException, JsonProcessingException,
    MissingReasonException, IOException, ValidationException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);

        final long cms82Id = 60L;
        Date beforeActivity = new Date();
        CertifiedProductSearchDetails listing = cpdManager.getCertifiedProductDetails(1L);
        for (CertificationResult result : listing.getCertificationResults()) {
            result.setSed(Boolean.FALSE);
            result.setGap(Boolean.FALSE);
        }
        CQMResultDetails addedCqm = new CQMResultDetails();
        addedCqm.setId(cms82Id);
        addedCqm.setCmsId("CMS82");
        addedCqm.setSuccess(Boolean.TRUE);
        Set<String> successVersions = new HashSet<String>();
        successVersions.add("v0");
        addedCqm.setSuccessVersions(successVersions);
        listing.getCqmResults().add(addedCqm);
        ListingUpdateRequest updateRequest = new ListingUpdateRequest();
        updateRequest.setListing(listing);
        updateRequest.setReason(REASON);
        cpController.updateCertifiedProduct(updateRequest);
        Date afterActivity = new Date();

        List<QuestionableActivityListingDTO> activities =
                qaDao.findListingActivityBetweenDates(beforeActivity, afterActivity);
        assertNotNull(activities);
        assertEquals(2, activities.size());
        QuestionableActivityListingDTO activity = findActivityOfType(activities,
                QuestionableActivityTriggerConcept.CQM_ADDED);
        assertNotNull(activity);
        assertEquals(1, activity.getListingId().longValue());
        assertNull(activity.getBefore());
        assertEquals("CMS82", activity.getAfter());
        assertEquals(REASON, activity.getReason());
        assertEquals(QuestionableActivityTriggerConcept.CQM_ADDED.getName(), activity.getTrigger().getName());

        activity = findActivityOfType(activities,
                QuestionableActivityTriggerConcept.EDITION_2014_EDITED);
        assertNotNull(activity);
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    @Test
    @Transactional
    @Rollback
    public void testRemoveCqmIncludesReason() throws
    EntityCreationException, EntityRetrievalException, InvalidArgumentsException, JsonProcessingException,
    MissingReasonException, IOException, ValidationException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);

        Date beforeActivity = new Date();
        CertifiedProductSearchDetails listing = cpdManager.getCertifiedProductDetails(1L);
        for (CertificationResult result : listing.getCertificationResults()) {
            result.setSed(Boolean.FALSE);
            result.setGap(Boolean.FALSE);
        }
        for (CQMResultDetails cqm : listing.getCqmResults()) {
            if (cqm.getCmsId() != null && cqm.getCmsId().equals("CMS146")) {
                cqm.setSuccess(Boolean.FALSE);
                cqm.setSuccessVersions(null);
            }
        }

        ListingUpdateRequest updateRequest = new ListingUpdateRequest();
        updateRequest.setListing(listing);
        updateRequest.setReason(REASON);
        cpController.updateCertifiedProduct(updateRequest);
        Date afterActivity = new Date();

        List<QuestionableActivityListingDTO> activities =
                qaDao.findListingActivityBetweenDates(beforeActivity, afterActivity);
        assertNotNull(activities);
        assertEquals(2, activities.size());
        QuestionableActivityListingDTO activity = findActivityOfType(activities,
                QuestionableActivityTriggerConcept.CQM_REMOVED);
        assertNotNull(activity);
        assertEquals(1, activity.getListingId().longValue());
        assertEquals("CMS146", activity.getBefore());
        assertNull(activity.getAfter());
        assertNotNull(activity.getReason());
        assertEquals(REASON, activity.getReason());
        assertEquals(QuestionableActivityTriggerConcept.CQM_REMOVED.getName(), activity.getTrigger().getName());

        activity = findActivityOfType(activities,
                QuestionableActivityTriggerConcept.EDITION_2014_EDITED);
        assertNotNull(activity);
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    @Test
    @Transactional
    @Rollback
    public void testAddCriteria() throws EntityCreationException, EntityRetrievalException,
    InvalidArgumentsException, JsonProcessingException, MissingReasonException, IOException, ValidationException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);

        final int critId = 4;
        Date beforeActivity = new Date();
        CertifiedProductSearchDetails listing = cpdManager.getCertifiedProductDetails(1L);
        for (CertificationResult result : listing.getCertificationResults()) {
            result.setSed(Boolean.FALSE);
            result.setGap(Boolean.FALSE);
        }
        for (CertificationResult cert : listing.getCertificationResults()) {
            if (cert.getId().longValue() == critId) {
                cert.setSuccess(Boolean.TRUE);
            }
        }
        ListingUpdateRequest updateRequest = new ListingUpdateRequest();
        updateRequest.setListing(listing);
        updateRequest.setReason(REASON);
        cpController.updateCertifiedProduct(updateRequest);
        Date afterActivity = new Date();

        List<QuestionableActivityListingDTO> activities = qaDao.findListingActivityBetweenDates(beforeActivity,
                afterActivity);
        assertNotNull(activities);
        assertEquals(2, activities.size());
        QuestionableActivityListingDTO activity = findActivityOfType(activities,
                QuestionableActivityTriggerConcept.CRITERIA_ADDED);
        assertNotNull(activity);
        assertEquals(1, activity.getListingId().longValue());
        assertNull(activity.getBefore());
        assertEquals("170.314 (a)(4)", activity.getAfter());
        assertEquals(REASON, activity.getReason());
        assertEquals(QuestionableActivityTriggerConcept.CRITERIA_ADDED.getName(), activity.getTrigger().getName());

        activity = findActivityOfType(activities,
                QuestionableActivityTriggerConcept.EDITION_2014_EDITED);
        assertNotNull(activity);
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    @Test
    @Transactional
    @Rollback
    public void testRemoveCriteriaIncludesReason() throws EntityCreationException,
    EntityRetrievalException, ValidationException,
    InvalidArgumentsException, JsonProcessingException, MissingReasonException, IOException, ValidationException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);

        Date beforeActivity = new Date();
        CertifiedProductSearchDetails listing = cpdManager.getCertifiedProductDetails(1L);
        for (CertificationResult cert : listing.getCertificationResults()) {
            if (cert.getId().longValue() == 1) {
                cert.setSuccess(Boolean.FALSE);
                UcdProcess ucdToRemove = null;
                if (listing.getSed() != null && listing.getSed().getUcdProcesses() != null
                        && listing.getSed().getUcdProcesses().size() > 0) {
                    for (UcdProcess ucd : listing.getSed().getUcdProcesses()) {
                        for (CertificationCriterion ucdCriteria : ucd.getCriteria()) {
                            if (ucdCriteria.getNumber() != null && ucdCriteria.getNumber().equals("170.314 (a)(1)")) {
                                ucdToRemove = ucd;
                            }
                        }
                    }
                }
                listing.getSed().getUcdProcesses().remove(ucdToRemove);
                cert.setGap(Boolean.FALSE);
                cert.setSed(Boolean.FALSE);
                cert.setTestStandards(new ArrayList<CertificationResultTestStandard>());
                cert.setAdditionalSoftware(new ArrayList<CertificationResultAdditionalSoftware>());
                cert.setTestProcedures(new ArrayList<CertificationResultTestProcedure>());
                cert.setTestDataUsed(new ArrayList<CertificationResultTestData>());
            }
            if (cert.getId().longValue() == 2) {
                cert.setSed(Boolean.FALSE);
            }
        }
        ListingUpdateRequest updateRequest = new ListingUpdateRequest();
        updateRequest.setListing(listing);
        updateRequest.setReason(REASON);
        cpController.updateCertifiedProduct(updateRequest);
        Date afterActivity = new Date();

        List<QuestionableActivityListingDTO> activities = qaDao.findListingActivityBetweenDates(beforeActivity,
                afterActivity);
        assertNotNull(activities);
        assertEquals(2, activities.size());
        QuestionableActivityListingDTO activity = findActivityOfType(activities,
                QuestionableActivityTriggerConcept.CRITERIA_REMOVED);
        assertNotNull(activity);
        assertEquals(1, activity.getListingId().longValue());
        assertEquals("170.314 (a)(1)", activity.getBefore());
        assertNull(activity.getAfter());
        assertNotNull(activity.getReason());
        assertEquals(REASON, activity.getReason());
        assertEquals(QuestionableActivityTriggerConcept.CRITERIA_REMOVED.getName(), activity.getTrigger().getName());

        activity = findActivityOfType(activities,
                QuestionableActivityTriggerConcept.EDITION_2014_EDITED);
        assertNotNull(activity);
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    //TODO: add test for surveillance deleted. Need surveillance associated with
    //product ID 10 to pass surveillance validation

    /**
     * Editing a 2014 listing will now add 2 pieces of questionable activity to the db
     * so we need to be able to pick from those 2 for the type of activity we are testing for.
     * @param activities
     * @param type
     * @return
     */
    private QuestionableActivityListingDTO findActivityOfType(
            final List<QuestionableActivityListingDTO> activities,
            final QuestionableActivityTriggerConcept type) {
        QuestionableActivityListingDTO foundActivity = null;
        for (QuestionableActivityListingDTO activity : activities) {
            if (activity.getTrigger().getName().equals(type.getName())) {
                foundActivity = activity;
            }
        }
        return foundActivity;
    }
}
