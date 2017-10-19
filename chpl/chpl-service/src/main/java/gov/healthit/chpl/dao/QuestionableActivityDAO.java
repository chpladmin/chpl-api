package gov.healthit.chpl.dao;

import java.util.Date;
import java.util.List;

import gov.healthit.chpl.dto.questionableActivity.QuestionableActivityDTO;
import gov.healthit.chpl.dto.questionableActivity.QuestionableActivityTriggerDTO;

public interface QuestionableActivityDAO {
    public QuestionableActivityDTO create(QuestionableActivityDTO activity);
    public List<QuestionableActivityDTO> findBetweenDates(Date start, Date end);
    public List<QuestionableActivityTriggerDTO> getAllTriggers();

}
