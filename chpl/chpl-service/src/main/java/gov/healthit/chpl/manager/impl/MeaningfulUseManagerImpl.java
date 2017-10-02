package gov.healthit.chpl.manager.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.dao.MeaningfulUseDAO;
import gov.healthit.chpl.dto.MeaningfulUseAccurateAsOfDTO;
import gov.healthit.chpl.manager.MeaningfulUseManager;

@Service
public class MeaningfulUseManagerImpl implements MeaningfulUseManager {
    @Autowired
    MeaningfulUseDAO meaningfulUseDao;

    @Transactional(readOnly = true)
    public MeaningfulUseAccurateAsOfDTO getMeaningfulUseAccurateAsOf() {
        return meaningfulUseDao.getMeaningfulUseAccurateAsOf();
    }

    @Transactional(readOnly = false)
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_CMS_STAFF')")
    public MeaningfulUseAccurateAsOfDTO updateMeaningfulUseAccurateAsOf(
            MeaningfulUseAccurateAsOfDTO meaningfulUseAccurateAsOfDTO) {
        return meaningfulUseDao.updateAccurateAsOf(meaningfulUseAccurateAsOfDTO);
    }
}
