package gov.healthit.chpl.web.controller;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import gov.healthit.chpl.domain.SedParticipantStatisticsCount;
import gov.healthit.chpl.dto.SedParticipantStatisticsCountDTO;
import gov.healthit.chpl.manager.StatisticsManager;
import gov.healthit.chpl.web.controller.results.SedParticipantStatisticsCountResults;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(value = "statistics")
@RestController
@RequestMapping("/statistics")
public class StatisticsController {
	
	private static final Logger LOGGER = LogManager.getLogger(StatisticsController.class);
	
	@Autowired
	private StatisticsManager statisticsManager;
	
	@ApiOperation(value = "Get all Sed/Participant counts.",
            notes = "Need to enter notes... ")
    @RequestMapping(value = "/sed_participant_count", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody SedParticipantStatisticsCountResults getSedParticipantStatisticsCounts() {
		SedParticipantStatisticsCountResults results = new SedParticipantStatisticsCountResults();
        List<SedParticipantStatisticsCountDTO> sedParticipantStatisticsCountDTOs = statisticsManager.getAllSedParticipantCounts();
        if (sedParticipantStatisticsCountDTOs != null) {
            for (SedParticipantStatisticsCountDTO sedParticipantStatisticsCountDTO : sedParticipantStatisticsCountDTOs) {
                results.getSedParticipantStatisticsCounts().add(new SedParticipantStatisticsCount(sedParticipantStatisticsCountDTO));
            }
        }
        return results;
    }
}
