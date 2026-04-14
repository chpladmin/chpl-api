package gov.healthit.chpl.servicebaseurllist;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.transaction.Transactional;

@Component
public class ServiceBaseUrlListManager {

    private ServiceBaseUrlListByDeveloperDao sbulByDeveloperDao;

    @Autowired
    public ServiceBaseUrlListManager(ServiceBaseUrlListByDeveloperDao sbulByDeveloperDao) {
        this.sbulByDeveloperDao = sbulByDeveloperDao;
    }

    @Transactional
    public List<ServiceBaseUrlListByDeveloper> getUrls(Long developerId) {
        return sbulByDeveloperDao.getSbulUrls(developerId);
    }
}
