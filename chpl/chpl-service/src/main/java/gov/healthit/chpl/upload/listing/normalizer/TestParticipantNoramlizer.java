package gov.healthit.chpl.upload.listing.normalizer;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.AgeRangeDAO;
import gov.healthit.chpl.dao.EducationTypeDAO;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.TestParticipant;
import gov.healthit.chpl.domain.TestTask;
import gov.healthit.chpl.dto.AgeRangeDTO;
import gov.healthit.chpl.dto.EducationTypeDTO;

@Component
public class TestParticipantNoramlizer {
    private AgeRangeDAO ageRangeDao;
    private EducationTypeDAO educationTypeDao;

    @Autowired
    public TestParticipantNoramlizer(AgeRangeDAO ageRangeDao, EducationTypeDAO educationTypeDao) {
        this.ageRangeDao = ageRangeDao;
        this.educationTypeDao = educationTypeDao;
    }

    public void normalize(CertifiedProductSearchDetails listing) {
        if (listing.getSed() != null && listing.getSed().getTestTasks() != null
                && listing.getSed().getTestTasks().size() > 0) {
            listing.getSed().getTestTasks().stream()
                .forEach(testTask -> {
                    //The "set to null if negative" code is done because of a complicated issue
                    //with React/Angular using negative IDs to track task and particpants.
                    //The "set to null if negative" code could potentially be removed in the future with OCD-2838
                    setTestTaskIdToNullIfNegative(testTask);
                    setTestParticipantIdsNullIfNegative(testTask);
                    populateTestParticipantAges(testTask);
                    populateTestParticipantEducationTypes(testTask);
                });
        }
    }

    private void setTestTaskIdToNullIfNegative(TestTask testTask) {
        if (testTask.getId() != null && testTask.getId() < 0) {
            testTask.setId(null);
        }
    }

    private void setTestParticipantIdsNullIfNegative(TestTask testTask) {
        if (testTask.getTestParticipants() != null && testTask.getTestParticipants().size() > 0) {
            testTask.getTestParticipants().stream()
                .forEach(participant -> setTestParticipantIdToNullIfNegative(participant));
        }
    }

    private void setTestParticipantIdToNullIfNegative(TestParticipant testParticipant) {
        if (testParticipant.getId() != null && testParticipant.getId() < 0) {
            testParticipant.setId(null);
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
            AgeRangeDTO ageRangeDto = ageRangeDao.getByName(participant.getAgeRange());
            if (ageRangeDto != null) {
                participant.setAgeRangeId(ageRangeDto.getId());
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
        if (participant != null && !StringUtils.isEmpty(participant.getAgeRange())) {
            EducationTypeDTO educationTypeDto = educationTypeDao.getByName(participant.getEducationTypeName());
            if (educationTypeDto != null) {
                participant.setEducationTypeId(educationTypeDto.getId());
            }
        }
    }
}
