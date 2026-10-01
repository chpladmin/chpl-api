package gov.healthit.chpl.report.questionableActivity;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class QuestionableActivityReportService {
    private QuestionableActivityReportDao questionableActivityReportDao;

    @Autowired
    public QuestionableActivityReportService(QuestionableActivityReportDao questionableActivityReportDao) {
        this.questionableActivityReportDao = questionableActivityReportDao;
    }

    @Transactional
    public List<QuestionableActivityReport> getQuestionableActivityReports() {
        return questionableActivityReportDao.getQuestionableActivityReports();
    }
}
