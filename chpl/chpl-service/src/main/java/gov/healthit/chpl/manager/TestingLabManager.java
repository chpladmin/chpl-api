package gov.healthit.chpl.manager;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;

import gov.healthit.chpl.dao.TestingLabDAO;
import gov.healthit.chpl.domain.TestingLab;
import gov.healthit.chpl.domain.activity.ActivityConcept;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.UserRetrievalException;
import gov.healthit.chpl.manager.impl.SecuredManager;
import gov.healthit.chpl.manager.impl.UpdateTestingLabException;
import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class TestingLabManager extends SecuredManager {
    @Autowired
    private TestingLabDAO testingLabDAO;

    @Autowired
    private ActivityManager activityManager;

    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).TESTING_LAB, "
            + "T(gov.healthit.chpl.permissions.domains.TestingLabDomainPermissions).CREATE)")
    public TestingLab create(TestingLab atl)
            throws UserRetrievalException, EntityCreationException, EntityRetrievalException, JsonProcessingException {
        String maxCode = testingLabDAO.getMaxCode();
        int maxCodeValue = Integer.parseInt(maxCode);
        int nextCodeValue = maxCodeValue + 1;

        String nextAtlCode = "";
        if (nextCodeValue < 10) {
            nextAtlCode = "0" + nextCodeValue;
        } else if (nextCodeValue > 99) {
            throw new EntityCreationException(
                    "Cannot create a 2-digit ATL code since there are more than 99 ATLs in the system.");
        } else {
            nextAtlCode = nextCodeValue + "";
        }
        atl.setAtlCode(nextAtlCode);
        atl.setRetired(false);
        // Create the atl itself
        TestingLab result = testingLabDAO.create(atl);

        LOGGER.debug("Created testing lab " + result + " and granted admin permission to recipient "
                + gov.healthit.chpl.util.AuthUtil.getUsername());

        String activityMsg = "Created Testing Lab " + result.getName();
        activityManager.addActivity(ActivityConcept.TESTING_LAB, result.getId(), activityMsg, null, result);
        return result;
    }

    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).TESTING_LAB, "
            + "T(gov.healthit.chpl.permissions.domains.TestingLabDomainPermissions).UPDATE, #atl)")
    public TestingLab update(TestingLab atl) throws EntityRetrievalException, JsonProcessingException,
            EntityCreationException, UpdateTestingLabException {

        TestingLab beforeAtl = testingLabDAO.getById(atl.getId());
        TestingLab result = testingLabDAO.update(atl);

        String activityMsg = "Updated testing lab " + atl.getName();
        activityManager.addActivity(ActivityConcept.TESTING_LAB, result.getId(), activityMsg, beforeAtl,
                result);
        return result;
    }

    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).TESTING_LAB, "
            + "T(gov.healthit.chpl.permissions.domains.TestingLabDomainPermissions).RETIRE)")
    public TestingLab retire(TestingLab atl) throws EntityRetrievalException, JsonProcessingException,
            EntityCreationException, UpdateTestingLabException {

        if (atl.getRetirementDay() == null || LocalDate.now().isBefore(atl.getRetirementDay())) {
            throw new UpdateTestingLabException("Retirement date is required and must be before \"now\".");
        }
        TestingLab beforeAtl = testingLabDAO.getById(atl.getId());
        TestingLab result = testingLabDAO.update(atl);

        String activityMsg = "Retired atl " + beforeAtl.getName();
        activityManager.addActivity(ActivityConcept.TESTING_LAB, result.getId(), activityMsg,
                beforeAtl, result);
        return result;
    }

    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).TESTING_LAB, "
            + "T(gov.healthit.chpl.permissions.domains.TestingLabDomainPermissions).UNRETIRE)")
    public TestingLab unretire(Long atlId) throws EntityRetrievalException, JsonProcessingException,
            EntityCreationException, UpdateTestingLabException {
        TestingLab beforeAtl = testingLabDAO.getById(atlId);
        TestingLab toUnretire = testingLabDAO.getById(atlId);
        toUnretire.setRetired(false);
        toUnretire.setRetirementDay(null);
        TestingLab result = testingLabDAO.update(toUnretire);

        String activityMsg = "Unretired atl " + toUnretire.getName();
        activityManager.addActivity(ActivityConcept.TESTING_LAB, result.getId(), activityMsg,
                beforeAtl, result);
        return result;
    }

    @Transactional(readOnly = true)
    public List<TestingLab> getAll() {
        return testingLabDAO.findAll();
    }

    @Transactional(readOnly = true)
    public TestingLab getById(Long id) throws EntityRetrievalException {
        return testingLabDAO.getById(id);
    }
}
