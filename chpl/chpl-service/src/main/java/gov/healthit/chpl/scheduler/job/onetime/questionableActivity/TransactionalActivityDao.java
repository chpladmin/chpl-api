package gov.healthit.chpl.scheduler.job.onetime.questionableActivity;

import java.util.Date;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.ActivityDAO;
import gov.healthit.chpl.domain.activity.ActivityConcept;
import gov.healthit.chpl.dto.ActivityDTO;
import gov.healthit.chpl.util.UserMapper;

@Component("transactionalActivityDao")
public class TransactionalActivityDao extends ActivityDAO {

    @Autowired
    TransactionalActivityDao(UserMapper userMapper) {
        super(userMapper);
    }

    @Transactional
    @Override
    public List<ActivityDTO> findByConcept(ActivityConcept concept, Date startDate, Date endDate) {
        return super.findByConcept(concept, startDate, endDate);
    }

    @Transactional
    @Override
    public List<ActivityDTO> findPageByConcept(ActivityConcept concept, Date startDate, Date endDate,
            Integer pageNum, Integer pageSize) {
        return super.findPageByConcept(concept, startDate, endDate, pageNum, pageSize);
    }

    @Transactional
    @Override
    public Long findResultSetSizeByConcept(ActivityConcept concept, Date startDate, Date endDate) {
        return super.findResultSetSizeByConcept(concept, startDate, endDate);
    }
}
