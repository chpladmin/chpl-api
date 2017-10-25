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

public interface QuestionableActivityDAO {
    public QuestionableActivityDTO create(QuestionableActivityDTO activity);
    public List<QuestionableActivityVersionDTO> findVersionActivityBetweenDates(Date start, Date end);
    public List<QuestionableActivityProductDTO> findProductActivityBetweenDates(Date start, Date end);
    public List<QuestionableActivityDeveloperDTO> findDeveloperActivityBetweenDates(Date start, Date end);
    public List<QuestionableActivityListingDTO> findListingActivityBetweenDates(Date start, Date end);
    public List<QuestionableActivityCertificationResultDTO> findCertificationResultActivityBetweenDates(Date start, Date end);
    public List<QuestionableActivityTriggerDTO> getAllTriggers();

}
