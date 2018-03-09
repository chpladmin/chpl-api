package gov.healthit.chpl.manager.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ApplicationObjectSupport;
import org.springframework.stereotype.Service;

import gov.healthit.chpl.dao.SedParticipantStatisticsCountDAO;
import gov.healthit.chpl.dto.SedParticipantStatisticsCountDTO;
import gov.healthit.chpl.manager.StatisticsManager;

/**
 * Implementation of the StatisticsManager interface.
 * @author TYoung
 *
 */
@Service
public class StatisticsManagerImpl extends ApplicationObjectSupport implements StatisticsManager {
    @Autowired
    private SedParticipantStatisticsCountDAO sedParticipantStatisticsCountDAO;

    @Override
    public List<SedParticipantStatisticsCountDTO> getAllSedParticipantCounts() {
        return sedParticipantStatisticsCountDAO.findAll();
    }
}
