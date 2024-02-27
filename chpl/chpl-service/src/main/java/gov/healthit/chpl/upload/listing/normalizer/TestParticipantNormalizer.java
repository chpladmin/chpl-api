package gov.healthit.chpl.upload.listing.normalizer;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.dao.AgeRangeDAO;
import gov.healthit.chpl.dao.EducationTypeDAO;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.TestParticipant;
import gov.healthit.chpl.domain.TestParticipant.TestParticipantAge;
import gov.healthit.chpl.domain.TestParticipant.TestParticipantEducation;
import gov.healthit.chpl.domain.TestTask;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class TestParticipantNormalizer {
    private AgeRangeDAO ageRangeDao;
    private EducationTypeDAO educationTypeDao;

    @Autowired
    public TestParticipantNormalizer(AgeRangeDAO ageRangeDao, EducationTypeDAO educationTypeDao) {
        this.ageRangeDao = ageRangeDao;
        this.educationTypeDao = educationTypeDao;
    }

    public void normalize(CertifiedProductSearchDetails listing) {
        if (listing.getSed() != null && listing.getSed().getTestTasks() != null
                && listing.getSed().getTestTasks().size() > 0) {
            clearDataForUnattestedCriteria(listing);
            listing.getSed().getTestTasks().stream()
                .forEach(testTask -> {
                    setTestTaskUniqueIdToIdIfNegative(testTask);
                    setTestParticipantUniqueIdsToIdIfNegative(testTask);
                    populateTestParticipantAges(testTask);
                    populateTestParticipantEducationTypes(testTask);
                });
        }
    }

    private void clearDataForUnattestedCriteria(CertifiedProductSearchDetails listing) {
        List<Long> unattestedCriteriaIds = listing.getCertificationResults().stream()
                .filter(certResult -> certResult.getSuccess() == null || BooleanUtils.isFalse(certResult.getSuccess()))
                .map(unattestedCertResult -> unattestedCertResult.getCriterion().getId())
                .toList();

        listing.getSed().getTestTasks().stream()
            .forEach(testTask -> removeUnattestedCriteriaFromTestTask(unattestedCriteriaIds, testTask));
    }

    private void removeUnattestedCriteriaFromTestTask(List<Long> unattestedCriteriaIds, TestTask testTask) {
        List<CertificationCriterion> testTaskCriteria = testTask.getCriteria().stream().toList();
        List<CertificationCriterion> testTaskCriteriaToRemove = new ArrayList<CertificationCriterion>();
        testTaskCriteria.stream()
            .filter(criterion -> unattestedCriteriaIds.contains(criterion.getId()))
            .forEach(unattestedCriterion -> testTaskCriteriaToRemove.add(unattestedCriterion));

        testTask.getCriteria().removeAll(testTaskCriteriaToRemove);
    }

    //remove when we turn off Angular listing edit
    @Deprecated
    private void setTestTaskUniqueIdToIdIfNegative(TestTask testTask) {
        if ((testTask.getId() != null && testTask.getId() < 0)
                || StringUtils.isEmpty(testTask.getUniqueId())) {
            testTask.setUniqueId(testTask.getId() + "");
        }
    }

    //remove when we turn off Angular listing edit
    @Deprecated
    private void setTestParticipantUniqueIdsToIdIfNegative(TestTask testTask) {
        if (testTask.getTestParticipants() != null && testTask.getTestParticipants().size() > 0) {
            testTask.getTestParticipants().stream()
                .forEach(participant -> setTestParticipantUniqueIdToIdIfNegative(participant));
        }
    }

    private void setTestParticipantUniqueIdToIdIfNegative(TestParticipant testParticipant) {
        if (testParticipant.getId() != null && testParticipant.getId() < 0) {
            testParticipant.setUniqueId(testParticipant.getId() + "");
        }
    }

    private void populateTestParticipantAges(TestTask testTask) {
        if (testTask.getTestParticipants() != null && testTask.getTestParticipants().size() > 0) {
            testTask.getTestParticipants().stream()
                .forEach(participant -> populateTestParticipantAge(participant));
        }
    }

    private void populateTestParticipantAge(TestParticipant participant) {
        if (participant != null && !StringUtils.isEmpty(participant.getAgeRange())) {
            TestParticipantAge ageRange = ageRangeDao.getByName(participant.getAgeRange());
            if (ageRange != null) {
                participant.setAgeRangeId(ageRange.getId());
                participant.getAge().setId(ageRange.getId());
            }
        } else if (participant != null && participant.getAge() != null && !StringUtils.isEmpty(participant.getAge().getName())) {
            TestParticipantAge ageRange = ageRangeDao.getByName(participant.getAge().getName());
            if (ageRange != null) {
                participant.setAgeRangeId(ageRange.getId());
                participant.getAge().setId(ageRange.getId());
            }
        }
    }

    private void populateTestParticipantEducationTypes(TestTask testTask) {
        if (testTask.getTestParticipants() != null && testTask.getTestParticipants().size() > 0) {
            testTask.getTestParticipants().stream()
                .forEach(participant -> populateTestParticipantEducationType(participant));
        }
    }

    private void populateTestParticipantEducationType(TestParticipant participant) {
        if (participant != null && !StringUtils.isEmpty(participant.getEducationTypeName())) {
            TestParticipantEducation educationType = educationTypeDao.getByName(participant.getEducationTypeName());
            if (educationType != null) {
                participant.setEducationTypeId(educationType.getId());
                participant.getEducationType().setId(educationType.getId());
            }
        } else if (participant != null && participant.getEducationType() != null
                 && !StringUtils.isEmpty(participant.getEducationType().getName())) {
            TestParticipantEducation educationType = educationTypeDao.getByName(participant.getEducationType().getName());
            if (educationType != null) {
                participant.setEducationTypeId(educationType.getId());
                participant.getEducationType().setId(educationType.getId());
            }
        }
    }
}
