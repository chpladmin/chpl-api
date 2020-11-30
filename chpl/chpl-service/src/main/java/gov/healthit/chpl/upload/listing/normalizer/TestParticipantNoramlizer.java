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
                    lookupTestParticipantAges(testTask);
                    lookupTestParticipantEducationTypes(testTask);
                });
        }
    }

    private void lookupTestParticipantAges(TestTask testTask) {
        if (testTask.getTestParticipants() != null && testTask.getTestParticipants().size() > 0) {
            testTask.getTestParticipants().stream()
                .forEach(participant -> lookupTestParticipantAge(participant));
        }
    }

    private void lookupTestParticipantAge(TestParticipant participant) {
        if (participant != null && !StringUtils.isEmpty(participant.getAgeRange())) {
            AgeRangeDTO ageRangeDto = ageRangeDao.getByName(participant.getAgeRange());
            if (ageRangeDto != null) {
                participant.setAgeRangeId(ageRangeDto.getId());
            }
        }
    }

    private void lookupTestParticipantEducationTypes(TestTask testTask) {
        if (testTask.getTestParticipants() != null && testTask.getTestParticipants().size() > 0) {
            testTask.getTestParticipants().stream()
                .forEach(participant -> lookupTestParticipantEducationType(participant));
        }
    }

    private void lookupTestParticipantEducationType(TestParticipant participant) {
        if (participant != null && !StringUtils.isEmpty(participant.getAgeRange())) {
            EducationTypeDTO educationTypeDto = educationTypeDao.getByName(participant.getEducationTypeName());
            if (educationTypeDto != null) {
                participant.setEducationTypeId(educationTypeDto.getId());
            }
        }
    }
}
