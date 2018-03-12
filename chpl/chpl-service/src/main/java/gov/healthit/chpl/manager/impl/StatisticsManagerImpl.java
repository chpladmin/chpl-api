package gov.healthit.chpl.manager.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ApplicationObjectSupport;
import org.springframework.stereotype.Service;

import gov.healthit.chpl.dao.ParticipantAgeStatisticsDAO;
import gov.healthit.chpl.dao.ParticipantGenderStatisticsDAO;
import gov.healthit.chpl.dao.SedParticipantStatisticsCountDAO;
import gov.healthit.chpl.dto.ParticipantAgeStatisticsDTO;
import gov.healthit.chpl.dto.ParticipantGenderStatisticsDTO;
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

    @Autowired
    private ParticipantGenderStatisticsDAO participantGenderStatisticsCountDAO;
    
    @Autowired
    private ParticipantAgeStatisticsDAO participantAgeStatisticsDAO;
    
    @Override
    public List<SedParticipantStatisticsCountDTO> getAllSedParticipantCounts() {
        return sedParticipantStatisticsCountDAO.findAll();
    }
    
    @Override
    public ParticipantGenderStatisticsDTO getParticipantGenderStatisticsDTO() {
        //There sould only ever be one active record.
        List<ParticipantGenderStatisticsDTO> stats = participantGenderStatisticsCountDAO.findAll();
        if (stats != null && stats.size() >0) {
            return stats.get(0);
        }
        else {
            return new ParticipantGenderStatisticsDTO();
        }
    }

    @Override
    public List<ParticipantAgeStatisticsDTO> getParticipantAgerStatisticsDTO() {
        return participantAgeStatisticsDAO.findAll();
    }
    
    
}
