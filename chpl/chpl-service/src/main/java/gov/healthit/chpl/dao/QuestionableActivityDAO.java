package gov.healthit.chpl.dao;

import java.util.Date;
import java.util.List;

import gov.healthit.chpl.dto.questionableActivity.QuestionableActivityCertificationResultDTO;
import gov.healthit.chpl.dto.questionableActivity.QuestionableActivityDTO;
import gov.healthit.chpl.dto.questionableActivity.QuestionableActivityDeveloperDTO;
import gov.healthit.chpl.dto.questionableActivity.QuestionableActivityListingDTO;
import gov.healthit.chpl.dto.questionableActivity.QuestionableActivityProductDTO;
import gov.healthit.chpl.dto.questionableActivity.QuestionableActivityTriggerDTO;
import gov.healthit.chpl.dto.questionableActivity.QuestionableActivityVersionDTO;

/**
 * Interface for database access to questionable activity.
 * @author kekey
 *
 */
public interface QuestionableActivityDAO {
    QuestionableActivityDTO create(QuestionableActivityDTO activity);
    List<QuestionableActivityVersionDTO> findVersionActivityBetweenDates(Date start, Date end);
    List<QuestionableActivityProductDTO> findProductActivityBetweenDates(Date start, Date end);
    List<QuestionableActivityDeveloperDTO> findDeveloperActivityBetweenDates(Date start, Date end);
    List<QuestionableActivityListingDTO> findListingActivityBetweenDates(Date start, Date end);
    List<QuestionableActivityCertificationResultDTO> findCertificationResultActivityBetweenDates(Date start, Date end);
    List<QuestionableActivityTriggerDTO> getAllTriggers();

}
