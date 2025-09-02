package gov.healthit.chpl.report.questionableurl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class QuestionableUrlReportService {
    private QuestionableUrlReportDao questionableUrlReportDao;

    @Autowired
    public QuestionableUrlReportService(QuestionableUrlReportDao questionableUrlReportDao) {
        this.questionableUrlReportDao = questionableUrlReportDao;
    }

    @Transactional
    public List<QuestionableUrlReport> getQuestionableUrlReports() {
        return questionableUrlReportDao.getQuestionableUrlReports();
    }

    @Transactional
    public List<QuestionableUrlDetailReport> getQuestionableUrlDetailReports() {
       return questionableUrlReportDao.getQuestionableUrlDetails();
    }
}
