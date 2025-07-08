package gov.healthit.chpl.targeteduser;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class TargetedUserManager {

    private TargetedUserDAO targetedUserDao;

    @Autowired
    public TargetedUserManager(TargetedUserDAO targetedUserDao) {
        this.targetedUserDao = targetedUserDao;
    }

    @Transactional
    public List<TargetedUser> getAll() {
        return targetedUserDao.findAll();
    }
}
