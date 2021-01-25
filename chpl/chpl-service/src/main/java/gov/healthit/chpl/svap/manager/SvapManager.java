package gov.healthit.chpl.svap.manager;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.svap.dao.SvapDAO;
import gov.healthit.chpl.svap.domain.SvapCriteriaMap;

@Component
public class SvapManager {
    private SvapDAO svapDao;

    @Autowired
    public SvapManager(SvapDAO svapDao) {
        this.svapDao = svapDao;
    }

    public List<SvapCriteriaMap> getAllSvapCriteriaMaps() throws EntityRetrievalException {
        return svapDao.getAllSvapCriteriaMap();
    }
}
