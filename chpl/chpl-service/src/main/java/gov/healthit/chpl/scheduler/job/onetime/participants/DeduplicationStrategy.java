package gov.healthit.chpl.scheduler.job.onetime.participants;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.CertifiedProductSed;
import gov.healthit.chpl.domain.TestParticipant;
import gov.healthit.chpl.domain.TestTask;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2(topic = "updateParticipantsJobLogger")
public class DeduplicationStrategy implements ParticipantUpdateStrategy {
    private static final String ID_PREFIX = "ID";

    public boolean updateParticipants(CertifiedProductSearchDetails listing) {
        List<TestParticipant> allParticipants = listing.getSed().getTestTasks().stream()
            .flatMap(testTask -> testTask.getTestParticipants().stream())
            .collect(Collectors.toList());

        // set all participant IDs to null
        allParticipants.stream()
            .forEach(participant -> participant.setId(null));

        //try to figure out if there was more than 1 individual that had the same participant data
        //and if there was then this strategy cannot be used
        boolean areAllParticipantsUnique = true;
        Iterator<TestParticipant> participantsIter = allParticipants.iterator();
        while (participantsIter.hasNext() && areAllParticipantsUnique) {
            TestParticipant currParticipant = participantsIter.next();
            long numTimesParticipantIsInListing = getNumTimesParticipantIsInListingSed(currParticipant, listing.getSed());
            long numTasksUsingParticipant = getNumTasksUsingParticipant(currParticipant, listing.getSed());
            if (numTimesParticipantIsInListing != numTasksUsingParticipant) {
                LOGGER.warn("There may be more than 1 individual with the same participant data: " + currParticipant.toString());
                areAllParticipantsUnique = false;
            }
        }
        if (!areAllParticipantsUnique) {
            return false;
        }

        //give all the participants that are the "same" the same uniqueId
        setParticipantUniqueIds(listing.getSed());

        long numParticipantsMissingUniqueId = getNumParticipantsWithoutUniqueId(listing.getSed());
        if (numParticipantsMissingUniqueId > 0) {
            LOGGER.warn(numParticipantsMissingUniqueId + " participants did not get a unique ID. Update cannot continue.");
            return false;
        }
        return true;
    }

    private long getNumTimesParticipantIsInListingSed(TestParticipant testParticipant, CertifiedProductSed sed) {
        return sed.getTestTasks().stream()
            .flatMap(testTask -> testTask.getTestParticipants().stream())
            .filter(listingParticipant -> testParticipantMatches(testParticipant, listingParticipant))
            .count();
    }

    private long getNumTasksUsingParticipant(TestParticipant testParticipant, CertifiedProductSed sed) {
        int count = 0;
        for (TestTask task : sed.getTestTasks()) {
            boolean taskUsesParticipant = task.getTestParticipants().stream()
                .filter(taskParticipant -> testParticipantMatches(testParticipant, taskParticipant))
                .findAny().isPresent();
            if (taskUsesParticipant) {
                count++;
            }
        }
        return count;
    }

    private long getNumParticipantsWithoutUniqueId(CertifiedProductSed sed) {
        return sed.getTestTasks().stream()
            .flatMap(testTask -> testTask.getTestParticipants().stream())
            .filter(listingParticipant -> StringUtils.isEmpty(listingParticipant.getUniqueId()))
            .count();
    }

    private boolean testParticipantMatches(TestParticipant testParticipant1, TestParticipant testParticipant2) {
        boolean result = false;
        if (StringUtils.equals(testParticipant1.getAgeRange(), testParticipant2.getAgeRange())
                && Objects.equals(testParticipant1.getAgeRangeId(), testParticipant2.getAgeRangeId())
                && StringUtils.equals(testParticipant1.getAssistiveTechnologyNeeds(),
                        testParticipant2.getAssistiveTechnologyNeeds())
                && Objects.equals(testParticipant1.getComputerExperienceMonths(),
                        testParticipant2.getComputerExperienceMonths())
                && StringUtils.equals(testParticipant1.getEducationTypeName(), testParticipant2.getEducationTypeName())
                && Objects.equals(testParticipant1.getEducationTypeId(), testParticipant2.getEducationTypeId())
                && StringUtils.equals(testParticipant1.getGender(), testParticipant2.getGender())
                && StringUtils.equals(testParticipant1.getOccupation(), testParticipant2.getOccupation())
                && Objects.equals(testParticipant1.getProductExperienceMonths(),
                        testParticipant2.getProductExperienceMonths())
                && Objects.equals(testParticipant1.getProfessionalExperienceMonths(),
                        testParticipant2.getProfessionalExperienceMonths())) {
            result = true;
        }
        return result;
    }

    private void setParticipantUniqueIds(CertifiedProductSed sed) {
        int idCount = 1;
        for (TestTask currTask : sed.getTestTasks()) {
            for (TestParticipant currParticipant : currTask.getTestParticipants()) {
                if (StringUtils.isEmpty(currParticipant.getUniqueId())) {
                    String uniqueId = ID_PREFIX + idCount;
                    currParticipant.setUniqueId(uniqueId);
                    //set the same unique ID on all identical participants
                    sed.getTestTasks().stream()
                        .flatMap(task -> task.getTestParticipants().stream())
                        .filter(participant -> StringUtils.isEmpty(participant.getUniqueId())
                                && testParticipantMatches(currParticipant, participant))
                        .forEach(matchedParticipant -> matchedParticipant.setUniqueId(uniqueId));
                    idCount++;
                }
            }
        }
    }
}
