package gov.healthit.chpl.manager.impl;

import org.springframework.beans.factory.annotation.Autowired;

import gov.healthit.chpl.dao.QuestionableActivityDAO;
import gov.healthit.chpl.dto.questionableActivity.QuestionableActivityDTO;

public abstract class QuestionableActivityProvider {
    
    @Autowired
    protected QuestionableActivityDAO questionableActivityDao;

    public abstract boolean isQuestionableActivity(Object src, Object dest);

    public abstract QuestionableActivityDTO getQuestionableActivityObject(Object src, Object dest);

    public void handleActivity(Object src, Object dest) {
        if (isQuestionableActivity(src, dest)) {
            QuestionableActivityDTO activity = getQuestionableActivityObject(src, dest);
            questionableActivityDao.create(activity);
        }
    }
}
