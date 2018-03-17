package gov.healthit.chpl.dao;

import gov.healthit.chpl.dto.TestParticipantAgeDTO;

/**
 * Data access object for the test_participant_age table.
 * @author TYoung
 *
 */
public interface TestParticipantAgeDAO {
    /**
     * Retrieves all of the test_participant_age record as a TestPartcipantAgeDTO objects.
     * @param id The id of the test_participant_age record to be retrieved
     * @return TestParticipantAgeDTO representing the test_participant_age record
     */
    TestParticipantAgeDTO getById(Long id);
}
