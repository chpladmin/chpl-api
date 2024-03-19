package gov.healthit.chpl.certifiedproduct.service;

import static gov.healthit.chpl.util.LambdaExceptionUtil.rethrowConsumer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.dao.CertificationResultDAO;
import gov.healthit.chpl.dao.TestParticipantDAO;
import gov.healthit.chpl.dao.TestTaskDAO;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.TestParticipant;
import gov.healthit.chpl.domain.TestTask;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class SedSynchronizationService {
    private CertificationResultDAO certResultDao;
    private TestTaskDAO testTaskDao;
    private TestParticipantDAO testParticipantDao;

    @Autowired
    public SedSynchronizationService(CertificationResultDAO certResultDao,
            TestTaskDAO testTaskDao,
            TestParticipantDAO testParticipantDao) {
        this.certResultDao = certResultDao;
        this.testTaskDao = testTaskDao;
        this.testParticipantDao = testParticipantDao;
    }

    //TODO: Add synchronization of UCD Processes here some time in the future when
    //that is going to regression tested.

    public int synchronizeTestTasks(CertifiedProductSearchDetails existingListing,
            CertifiedProductSearchDetails updatedListing, List<TestTask> origTestTasks,
            List<TestTask> newTestTasks) throws EntityCreationException, EntityRetrievalException {

        createNewTestTasksAndParticipants(newTestTasks);

        List<TestTask> updatedTestTasks = new ArrayList<TestTask>();
        List<TestTask> addedTestTasks = new ArrayList<TestTask>();
        List<TestTask> removedTestTasks = new ArrayList<TestTask>();

        //Find the updated test tasks
        if (!CollectionUtils.isEmpty(newTestTasks)) {
            updatedTestTasks = newTestTasks.stream()
                    .filter(tt -> {
                        Optional<TestTask> found = getMatchingItemInList(tt, origTestTasks);
                        return found.isPresent();
                    })
                    .toList();

            updatedTestTasks.forEach(rethrowConsumer(updatedTt ->
                updateTestTask(updatedListing, getMatchingItemInList(updatedTt, origTestTasks).get(), updatedTt)));
        }

        //Find the added test tasks
        if (!CollectionUtils.isEmpty(newTestTasks)) {
            addedTestTasks = newTestTasks.stream()
                    .filter(cr -> getMatchingItemInList(cr, origTestTasks).isEmpty())
                    .toList();

            addedTestTasks.forEach(rethrowConsumer(addedTt -> associateNewTestTaskWithCertificationResults(updatedListing, addedTt, newTestTasks)));
        }

        //Find the removed test tasks
        if (!CollectionUtils.isEmpty(origTestTasks)) {
            removedTestTasks = origTestTasks.stream()
                    .filter(tt -> getMatchingItemInList(tt, newTestTasks).isEmpty())
                    .toList();

            removedTestTasks.forEach(x -> LOGGER.info("Removed Test Task: {}", x.getId()));

            removedTestTasks.forEach(removedTt -> deleteTestTask(updatedListing, removedTt));
        }

        return updatedTestTasks.size() + addedTestTasks.size() + removedTestTasks.size();
    }

    private void createNewTestTasksAndParticipants(List<TestTask> updatedTestTasks)
        throws EntityCreationException {
        updatedTestTasks.stream()
            .forEach(rethrowConsumer(task -> createNewTestTaskAndParticipants(task, updatedTestTasks)));
    }

    private void createNewTestTaskAndParticipants(TestTask testTask, List<TestTask> allTestTasks)
        throws EntityCreationException {
        // With Angular listing edit: Any with a negative ID are new and need to be added and the negative ID could be repeated
        // so a task/participant with a negative id needs to be added once and then that ID needs to be replaced with
        // the created item's ID anywhere else that it is found.
        // With React listing edit: Any null ID is a new task/participant and we will use the passed-in uniqueId to determine
        // if that task is re-used across criteria or that participant is re-used across tasks.
        if (testTask.getId() != null && testTask.getId() < 0) {
            Long prevId = testTask.getId();
            Long createdTaskId = testTaskDao.create(testTask);
            for (TestTask otherTask : allTestTasks) {
                if (otherTask.getId() != null && otherTask.getId().equals(prevId)) {
                    otherTask.setId(createdTaskId);
                }
            }
        } else if (testTask.getId() == null && !StringUtils.isEmpty(testTask.getUniqueId())) {
            String prevId = testTask.getUniqueId();
            Long createdTaskId = testTaskDao.create(testTask);
            for (TestTask otherTask : allTestTasks) {
                if (otherTask.getId() == null && !StringUtils.isEmpty(otherTask.getUniqueId())
                        && StringUtils.equals(prevId, otherTask.getUniqueId())) {
                    otherTask.setId(createdTaskId);
                }
            }
        }

        for (TestParticipant participant : testTask.getTestParticipants()) {
            //get rid of this "if" when we remove Angular listing edit but leave the "else if"
            if (participant.getId() != null && participant.getId() < 0) {
                Long prevId = participant.getId();
                Long createdParticipantId = testParticipantDao.create(participant);
                for (TestTask otherTask : allTestTasks) {
                    for (TestParticipant otherParticipant : otherTask.getTestParticipants()) {
                        if (otherParticipant.getId() != null && otherParticipant.getId().equals(prevId)) {
                            otherParticipant.setId(createdParticipantId);
                        }
                    }
                }
            } else if (participant.getId() == null && !StringUtils.isEmpty(participant.getUniqueId())) {
                String prevId = participant.getUniqueId();
                Long createdParticipantId = testParticipantDao.create(participant);
                for (TestTask otherTask : allTestTasks) {
                    for (TestParticipant otherParticipant : otherTask.getTestParticipants()) {
                        if (otherParticipant.getId() == null && !StringUtils.isEmpty(otherParticipant.getUniqueId())
                                && StringUtils.equals(prevId, otherParticipant.getUniqueId())) {
                            otherParticipant.setId(createdParticipantId);
                        }
                    }
                }
            }
        }
    }

    private void updateTestTask(CertifiedProductSearchDetails listing, TestTask origTestTask, TestTask updatedTestTask)
            throws EntityCreationException {
        boolean isDifferent = false;
        if (!StringUtils.equals(origTestTask.getDescription(), updatedTestTask.getDescription())
                || !StringUtils.equals(origTestTask.getTaskRatingScale(), updatedTestTask.getTaskRatingScale())
                || !Objects.equals(origTestTask.getTaskErrors(), updatedTestTask.getTaskErrors())
                || !Objects.equals(origTestTask.getTaskErrorsStddev(), updatedTestTask.getTaskErrorsStddev())
                || !Objects.equals(origTestTask.getTaskPathDeviationObserved(),
                        updatedTestTask.getTaskPathDeviationObserved())
                || !Objects.equals(origTestTask.getTaskPathDeviationOptimal(),
                        updatedTestTask.getTaskPathDeviationOptimal())
                || !Objects.equals(origTestTask.getTaskRating(), updatedTestTask.getTaskRating())
                || !StringUtils.equals(origTestTask.getTaskRatingScale(), updatedTestTask.getTaskRatingScale())
                || !Objects.equals(origTestTask.getTaskRatingStddev(), updatedTestTask.getTaskRatingStddev())
                || !Objects.equals(origTestTask.getTaskSuccessAverage(), updatedTestTask.getTaskSuccessAverage())
                || !Objects.equals(origTestTask.getTaskSuccessStddev(), updatedTestTask.getTaskSuccessStddev())
                || !Objects.equals(origTestTask.getTaskTimeAvg(), updatedTestTask.getTaskTimeAvg())
                || !Objects.equals(origTestTask.getTaskTimeDeviationObservedAvg(),
                        updatedTestTask.getTaskTimeDeviationObservedAvg())
                || !Objects.equals(origTestTask.getTaskTimeDeviationOptimalAvg(),
                        updatedTestTask.getTaskTimeDeviationOptimalAvg())
                || !Objects.equals(origTestTask.getTaskTimeStddev(), updatedTestTask.getTaskTimeStddev())) {
            isDifferent = true;
        }

        if (isDifferent) {
            updatedTestTask.setId(origTestTask.getId());
            try {
                testTaskDao.update(updatedTestTask);
            } catch (Exception ex) {
                LOGGER.error("Error updating test task " + updatedTestTask.getId(), ex);
            }
        }

        updateAssociatedCertificationResults(listing, origTestTask, updatedTestTask);

        try {
            updateTaskParticipants(origTestTask, origTestTask.getTestParticipants(),
                updatedTestTask.getTestParticipants());
        } catch (Exception ex) {
            LOGGER.error("Error updating participants for task " + updatedTestTask.getId(), ex);
        }
    }

    private void updateAssociatedCertificationResults(CertifiedProductSearchDetails listing,
            TestTask origTestTask, TestTask updatedTestTask) throws EntityCreationException {
        List<Long> certResultIdsAssociatedWithOrigTask = origTestTask.getCriteria().stream()
                    .map(criterion -> getCertResultForCriterion(listing, criterion))
                    .filter(certResult -> certResult.isPresent())
                    .map(certResult -> certResult.get().getId())
                    .collect(Collectors.toList());

        List<Long> certResultIdsAssociatedWithUpdatedTask = updatedTestTask.getCriteria().stream()
                    .map(criterion -> getCertResultForCriterion(listing, criterion))
                    .filter(certResult -> certResult.isPresent())
                    .map(certResult -> certResult.get().getId())
                    .collect(Collectors.toList());

        //find added cert results
        if (!CollectionUtils.isEmpty(certResultIdsAssociatedWithUpdatedTask)) {
            List<Long> addedCertResultIds = new ArrayList<Long>();
            addedCertResultIds = certResultIdsAssociatedWithUpdatedTask.stream()
                    .filter(crId -> getMatchingItemInList(crId, certResultIdsAssociatedWithOrigTask).isEmpty())
                    .toList();

            addedCertResultIds.forEach(rethrowConsumer(crId -> certResultDao.createTestTaskMapping(crId, updatedTestTask, listing.getSed().getTestTasks())));
        }

        //find removed cert results
        if (!CollectionUtils.isEmpty(certResultIdsAssociatedWithOrigTask)) {
            List<Long> removedCertResultIds = new ArrayList<Long>();
            removedCertResultIds = certResultIdsAssociatedWithOrigTask.stream()
                    .filter(crId -> getMatchingItemInList(crId, certResultIdsAssociatedWithUpdatedTask).isEmpty())
                    .toList();

            removedCertResultIds.forEach(rethrowConsumer(crId -> certResultDao.deleteTestTaskMapping(crId, updatedTestTask.getId())));
        }
    }

    private void updateTaskParticipants(TestTask testTask, Collection<TestParticipant> origParticipants,
            Collection<TestParticipant> updatedParticipants) throws EntityCreationException, EntityRetrievalException {
        Set<TestParticipant> participantsToAdd = new HashSet<TestParticipant>();
        Set<TestParticipant> participantsToUpdate = new HashSet<TestParticipant>();
        Set<Long> idsToRemove = new HashSet<Long>();

        // figure out which participants to add
        if (!CollectionUtils.isEmpty(updatedParticipants)) {
            // fill in potentially missing participant id
            for (TestParticipant updatedParticipant : updatedParticipants) {
                if (updatedParticipant.getId() == null) {
                    Long addedParticipantId = testParticipantDao.create(updatedParticipant);
                    updatedParticipant.setId(addedParticipantId);
                }
            }

            if (CollectionUtils.isEmpty(origParticipants)) {
                // existing listing has none, add all from the update
                for (TestParticipant updatedItem : updatedParticipants) {
                    if (updatedItem.getId() != null) {
                        participantsToAdd.add(updatedItem);
                    }
                }
            } else if (!CollectionUtils.isEmpty(origParticipants)) {
                // existing listing has some, compare to the update to see if any are different
                for (TestParticipant updatedParticipant : updatedParticipants) {
                    boolean inExistingListing = false;
                    for (TestParticipant existingParticipant : origParticipants) {
                        inExistingListing = !inExistingListing ? updatedParticipant.matches(existingParticipant) : inExistingListing;
                        if (updatedParticipant.getId().equals(existingParticipant.getId())) {
                            participantsToUpdate.add(updatedParticipant);
                        }
                    }

                    if (!inExistingListing) {
                        if (updatedParticipant.getId() != null) {
                            participantsToAdd.add(updatedParticipant);
                        }
                    }
                }
            }
        }

        // figure out which participants to remove
        if (!CollectionUtils.isEmpty(origParticipants)) {
            // if the updated listing has none, remove them all from existing
            if (CollectionUtils.isEmpty(updatedParticipants)) {
                for (TestParticipant existingParticipant : origParticipants) {
                    idsToRemove.add(existingParticipant.getId());
                }
            } else if (updatedParticipants.size() > 0) {
                for (TestParticipant existingParticipant : origParticipants) {
                    boolean inUpdatedListing = false;
                    for (TestParticipant updatedItem : updatedParticipants) {
                        inUpdatedListing = !inUpdatedListing ? existingParticipant.matches(updatedItem) : inUpdatedListing;
                    }
                    if (!inUpdatedListing) {
                        idsToRemove.add(existingParticipant.getId());
                    }
                }
            }
        }

        for (TestParticipant participantToAdd : participantsToAdd) {
            certResultDao.addTestParticipantMapping(testTask, participantToAdd);
        }

        for (TestParticipant toUpdate : participantsToUpdate) {
            boolean isDifferent = false;
            TestParticipant origParticipant = getMatchingItemInList(toUpdate, origParticipants).get();
            TestParticipant updatedParticipant = toUpdate;
            if (!StringUtils.equals(origParticipant.getAgeRange(), updatedParticipant.getAgeRange())
                    || !StringUtils.equals(origParticipant.getAge().getName(), updatedParticipant.getAge().getName())
                    || !StringUtils.equals(origParticipant.getAssistiveTechnologyNeeds(),
                            updatedParticipant.getAssistiveTechnologyNeeds())
                    || !Objects.equals(origParticipant.getComputerExperienceMonths(),
                            updatedParticipant.getComputerExperienceMonths())
                    || !StringUtils.equals(origParticipant.getEducationTypeName(), updatedParticipant.getEducationTypeName())
                    || !StringUtils.equals(origParticipant.getEducationType().getName(), updatedParticipant.getEducationType().getName())
                    || !StringUtils.equals(origParticipant.getGender(), updatedParticipant.getGender())
                    || !StringUtils.equals(origParticipant.getOccupation(), updatedParticipant.getOccupation())
                    || !Objects.equals(origParticipant.getProductExperienceMonths(),
                            updatedParticipant.getProductExperienceMonths())
                    || !Objects.equals(origParticipant.getProfessionalExperienceMonths(),
                            updatedParticipant.getProfessionalExperienceMonths())) {
                isDifferent = true;
            }

            if (isDifferent) {
                testParticipantDao.update(updatedParticipant);
            }
        }

        for (Long idToRemove : idsToRemove) {
            certResultDao.deleteTestParticipantMapping(testTask.getId(), idToRemove);
        }
    }

    private void associateNewTestTaskWithCertificationResults(CertifiedProductSearchDetails listing, TestTask testTask, List<TestTask> allTestTasks)
        throws EntityCreationException {
        List<Long> certResultIdsAssociatedWithTask = new ArrayList<Long>();
        if (!CollectionUtils.isEmpty(testTask.getCriteria())) {
            certResultIdsAssociatedWithTask = testTask.getCriteria().stream()
                    .map(criterion -> getCertResultForCriterion(listing, criterion))
                    .filter(certResult -> certResult.isPresent())
                    .map(certResult -> certResult.get().getId())
                    .collect(Collectors.toList());
        }

        if (!CollectionUtils.isEmpty(certResultIdsAssociatedWithTask)) {
            certResultIdsAssociatedWithTask.stream()
                .forEach(rethrowConsumer(certResultId -> certResultDao.createTestTaskMapping(certResultId, testTask, allTestTasks)));
        }
    }

    private void deleteTestTask(CertifiedProductSearchDetails listing, TestTask testTask) {
        List<Long> certResultIdsAssociatedWithTask = new ArrayList<Long>();
        if (!CollectionUtils.isEmpty(testTask.getCriteria())) {
            certResultIdsAssociatedWithTask = testTask.getCriteria().stream()
                    .map(criterion -> getCertResultForCriterion(listing, criterion))
                    .filter(certResult -> certResult.isPresent())
                    .map(certResult -> certResult.get().getId())
                    .collect(Collectors.toList());
        }

        if (!CollectionUtils.isEmpty(certResultIdsAssociatedWithTask)) {
            certResultIdsAssociatedWithTask.stream()
                .forEach(rethrowConsumer(certResultId -> certResultDao.deleteTestTaskMapping(certResultId, testTask.getId())));
        }
    }

    private Optional<CertificationResult> getCertResultForCriterion(CertifiedProductSearchDetails listing, CertificationCriterion criterion) {
        return listing.getCertificationResults().stream()
                .filter(certResult -> certResult.getCriterion().getId().equals(criterion.getId()))
                .findAny();
    }

    private Optional<TestTask> getMatchingItemInList(TestTask testTask, List<TestTask> testTasks) {
        if (CollectionUtils.isEmpty(testTasks)) {
            return Optional.empty();
        }
        return testTasks.stream()
                .filter(tt -> tt != null && tt.getId() != null
                            ? tt.getId().equals(testTask.getId())
                            : false)
                .findAny();
    }

    private Optional<TestParticipant> getMatchingItemInList(TestParticipant testParticipant, Collection<TestParticipant> testParticipants) {
        if (CollectionUtils.isEmpty(testParticipants)) {
            return Optional.empty();
        }
        return testParticipants.stream()
                .filter(tp -> tp != null && tp.getId() != null
                            ? tp.getId().equals(testParticipant.getId())
                            : false)
                .findAny();
    }

    private Optional<Long> getMatchingItemInList(Long idToMatch, Collection<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return Optional.empty();
        }
        return ids.stream()
                .filter(id -> id != null
                            ? id.equals(idToMatch)
                            : false)
                .findAny();
    }
}
