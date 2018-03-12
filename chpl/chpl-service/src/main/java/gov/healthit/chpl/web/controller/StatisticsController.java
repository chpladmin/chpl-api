package gov.healthit.chpl.web.controller;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import gov.healthit.chpl.domain.ParticipantGenderStatistics;
import gov.healthit.chpl.domain.SedParticipantStatisticsCount;
import gov.healthit.chpl.dto.ParticipantGenderStatisticsDTO;
import gov.healthit.chpl.dto.SedParticipantStatisticsCountDTO;
import gov.healthit.chpl.manager.StatisticsManager;
import gov.healthit.chpl.web.controller.results.SedParticipantStatisticsCountResults;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * The StatisticsController is used to return data that can be used to charting.
 * @author TYoung
 *
 */
@Api(value = "statistics")
@RestController
@RequestMapping("/statistics")
public class StatisticsController {

    private static final Logger LOGGER = LogManager.getLogger(StatisticsController.class);

    @Autowired
    private StatisticsManager statisticsManager;

    /**
     * Retrieves and returns the SED/Participant counts.
     * @return a JSON representation of a SedParticipantStatisticsCountResults object
     */
    @ApiOperation(value = "Get all Sed/Participant counts.",
            notes = "Retrieves and returns the SED/Participant counts.")
    @RequestMapping(value = "/sed_participant_count", method = RequestMethod.GET,
            produces = "application/json; charset=utf-8")
    public @ResponseBody SedParticipantStatisticsCountResults getSedParticipantStatisticsCounts() {
        SedParticipantStatisticsCountResults results = new SedParticipantStatisticsCountResults();
        List<SedParticipantStatisticsCountDTO> sedParticipantStatisticsCountDTOs = statisticsManager
                .getAllSedParticipantCounts();
        if (sedParticipantStatisticsCountDTOs != null) {
            for (SedParticipantStatisticsCountDTO sedParticipantStatisticsCountDTO
                    : sedParticipantStatisticsCountDTOs) {
                results.getSedParticipantStatisticsCounts()
                        .add(new SedParticipantStatisticsCount(sedParticipantStatisticsCountDTO));
            }
        }
        return results;
    }
    
    /**
     * Retrieves and returns the SED/Participant/Gender counts.
     * @return a JSON representation of a ParticipantGenderStatistics object
     */
    @ApiOperation(value = "Get all Sed/Participant/Gender counts.",
            notes = "Retrieves and returns the SED/Participant/Gender counts.")
    @RequestMapping(value = "/participant_gender_count", method = RequestMethod.GET,
            produces = "application/json; charset=utf-8")
    public @ResponseBody ParticipantGenderStatistics getParticipantGenderStatistics() {
        ParticipantGenderStatisticsDTO stats = statisticsManager.getParticipantGenderStatisticsDTO();
        return new ParticipantGenderStatistics(stats);
    }
    
    /**
     * Retrieves and returns the SED/Participant/Age counts.
     * @return a JSON representation of a XXXXXXX object
     */
    @ApiOperation(value = "Get all Sed/Participant/Age counts.",
            notes = "Retrieves and returns the SED/Participant/Age counts.")
    @RequestMapping(value = "/participant_age_count", method = RequestMethod.GET,
            produces = "application/json; charset=utf-8")
    public @ResponseBody ParticipantGenderStatistics getParticipantAgeStatistics() {
        ParticipantGenderStatisticsDTO stats = statisticsManager.getParticipantGenderStatisticsDTO();
        return new ParticipantGenderStatistics(stats);
    }
}
