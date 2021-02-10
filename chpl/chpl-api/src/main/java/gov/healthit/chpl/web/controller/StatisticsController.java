package gov.healthit.chpl.web.controller;

import gov.healthit.chpl.domain.ParticipantGenderStatistics;
import gov.healthit.chpl.domain.SedParticipantStatisticsCount;
import gov.healthit.chpl.dto.ParticipantGenderStatisticsDTO;
import gov.healthit.chpl.dto.SedParticipantStatisticsCountDTO;
import gov.healthit.chpl.logging.Loggable;
import gov.healthit.chpl.manager.StatisticsManager;
import gov.healthit.chpl.scheduler.job.chartdata.ExperienceType;
import gov.healthit.chpl.web.controller.results.CriterionProductStatisticsResult;
import gov.healthit.chpl.web.controller.results.IncumbentDevelopersStatisticsResult;
import gov.healthit.chpl.web.controller.results.ListingCountStatisticsResult;
import gov.healthit.chpl.web.controller.results.NonconformityTypeStatisticsResult;
import gov.healthit.chpl.web.controller.results.ParticipantAgeStatisticsResult;
import gov.healthit.chpl.web.controller.results.ParticipantEducationStatisticsResult;
import gov.healthit.chpl.web.controller.results.ParticipantExperienceStatisticsResult;
import gov.healthit.chpl.web.controller.results.SedParticipantStatisticsCountResults;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * The StatisticsController is used to return data that can be used to charting.
 *
 * @author TYoung
 *
 */
@Api(value = "statistics")
@RestController
@RequestMapping("/statistics")
@Loggable
public class StatisticsController {

    @Autowired
    private StatisticsManager statisticsManager;

    /**
     * Retrieves and returns the Listing counts.
     *
     * @return a JSON representation of an NonconformityTypeStatisticsResult
     *         object
     */
    @ApiOperation(value = "Get count of nonconformities by criteria.", notes = "Retrieves and returns the counts.")
    @RequestMapping(value = "/nonconformity_criteria_count", method = RequestMethod.GET,
            produces = "application/json; charset=utf-8")
    public NonconformityTypeStatisticsResult getNonconformityCountByCriteria() {
        NonconformityTypeStatisticsResult response = new NonconformityTypeStatisticsResult();
        response.setNonconformityStatisticsResult(statisticsManager.getAllNonconformitiesByCriterion());
        return response;
    }

    /**
     * Retrieves and returns the Listing counts.
     *
     * @return a JSON representation of an ListingCountStatisticsResult object
     */
    @ApiOperation(value = "Get count of Developers and Products with listings.",
            notes = "Retrieves and returns the counts.")
    @RequestMapping(value = "/listing_count", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public ListingCountStatisticsResult getListingCountStatistics() {
        ListingCountStatisticsResult response = new ListingCountStatisticsResult();
        response.setStatisticsResult(statisticsManager.getListingCountStatisticsResult());
        return response;
    }

    /**
     * Retrieves and returns the Criterion/Product counts.
     *
     * @return a JSON representation of a CriterionProductStatisticsResult
     *         object
     */
    @ApiOperation(value = "Get count of Criteria certified to by unique Product.",
            notes = "Retrieves and returns the Criterion/Product counts.")
    @RequestMapping(value = "/criterion_product", method = RequestMethod.GET,
            produces = "application/json; charset=utf-8")
    public @ResponseBody CriterionProductStatisticsResult getCriterionProductStatistics() {
        CriterionProductStatisticsResult response = new CriterionProductStatisticsResult();
        response.setCriterionProductStatisticsResult(statisticsManager.getCriterionProductStatisticsResult());
        return response;
    }

    /**
     * Retrieves and returns the Incumbent Developer counts.
     *
     * @return a JSON representation of an IncumbentDevelopersStatisticsResult
     *         object
     */
    @ApiOperation(value = "Get count of new vs. incumbent Developers.",
            notes = "Retrieves and returns counts grouped by Edition.")
    @RequestMapping(value = "incumbent_developers", method = RequestMethod.GET,
            produces = "application/json; charset=utf-8")
    public @ResponseBody IncumbentDevelopersStatisticsResult getIncumbentDevelopersStatistics() {
        IncumbentDevelopersStatisticsResult response = new IncumbentDevelopersStatisticsResult();
        response.setIncumbentDevelopersStatisticsResult(statisticsManager.getIncumbentDevelopersStatisticsResult());
        return response;
    }

    /**
     * Retrieves and returns the SED/Participant counts.
     *
     * @return a JSON representation of a SedParticipantStatisticsCountResults
     *         object
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
                results.getSedParticipantStatisticsCounts().add(
                        new SedParticipantStatisticsCount(sedParticipantStatisticsCountDTO));
            }
        }
        return results;
    }

    /**
     * Retrieves and returns the SED/Participant/Gender counts.
     *
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
     *
     * @return a JSON representation of a ParticipantAgeStatisticsResult object
     */
    @ApiOperation(value = "Get all Sed/Participant/Age counts.",
            notes = "Retrieves and returns the SED/Participant/Age counts.")
    @RequestMapping(value = "/participant_age_count", method = RequestMethod.GET,
            produces = "application/json; charset=utf-8")
    public @ResponseBody ParticipantAgeStatisticsResult getParticipantAgeStatistics() {
        ParticipantAgeStatisticsResult response = new ParticipantAgeStatisticsResult();
        response.setParticipantAgeStatistics(statisticsManager.getParticipantAgeStatisticsResult());
        return response;
    }

    /**
     * Retrieves and returns the SED/Participant/Education counts.
     *
     * @return a JSON representation of a ParticipantEducationStatisticsResult
     *         object
     */
    @ApiOperation(value = "Get all Sed/Participant/Education counts.",
            notes = "Retrieves and returns the SED/Participant/Education counts.")
    @RequestMapping(value = "/participant_education_count", method = RequestMethod.GET,
            produces = "application/json; charset=utf-8")
    public @ResponseBody ParticipantEducationStatisticsResult getParticipantEducationStatistics() {
        ParticipantEducationStatisticsResult response = new ParticipantEducationStatisticsResult();
        response.setParticipantEducationStatistics(statisticsManager.getParticipantEducationStatisticsResult());
        return response;
    }

    /**
     * Retrieves and returns the SED/Participant/Professional Experience counts.
     *
     * @return a JSON representation of a ParticipantExperienceStatisticsResult
     *         object
     */
    @ApiOperation(value = "Get all Sed/Participant/Professional Experience counts.",
            notes = "Retrieves and returns the SED/Participant/Preofessional Experience counts.")
    @RequestMapping(value = "/participant_professional_experience_count", method = RequestMethod.GET,
            produces = "application/json; charset=utf-8")
    public @ResponseBody ParticipantExperienceStatisticsResult getParticipantProfessionalExperienceStatistics() {
        ParticipantExperienceStatisticsResult response = new ParticipantExperienceStatisticsResult();
        response.setParticipantExperienceStatistics(statisticsManager
                .getParticipantExperienceStatisticsResult(ExperienceType.PROFESSIONAL_EXPERIENCE));
        return response;
    }

    /**
     * Retrieves and returns the SED/Participant/Computer Experience counts.
     *
     * @return a JSON representation of a ParticipantExperienceStatisticsResult
     *         object
     */
    @ApiOperation(value = "Get all Sed/Participant/Computer Experience counts.",
            notes = "Retrieves and returns the SED/Participant/Computer Experience counts.")
    @RequestMapping(value = "/participant_computer_experience_count", method = RequestMethod.GET,
            produces = "application/json; charset=utf-8")
    public @ResponseBody ParticipantExperienceStatisticsResult getParticipantComputerExperienceStatistics() {
        ParticipantExperienceStatisticsResult response = new ParticipantExperienceStatisticsResult();
        response.setParticipantExperienceStatistics(statisticsManager
                .getParticipantExperienceStatisticsResult(ExperienceType.COMPUTER_EXPERIENCE));
        return response;
    }

    /**
     * Retrieves and returns the SED/Participant/Product Experience counts.
     *
     * @return a JSON representation of a ParticipantExperienceStatisticsResult
     *         object
     */
    @ApiOperation(value = "Get all Sed/Participant/Product Experience counts.",
            notes = "Retrieves and returns the SED/Participant/Product Experience counts.")
    @RequestMapping(value = "/participant_product_experience_count", method = RequestMethod.GET,
            produces = "application/json; charset=utf-8")
    public @ResponseBody ParticipantExperienceStatisticsResult getParticipantProductExperienceStatistics() {
        ParticipantExperienceStatisticsResult response = new ParticipantExperienceStatisticsResult();
        response.setParticipantExperienceStatistics(statisticsManager
                .getParticipantExperienceStatisticsResult(ExperienceType.PRODUCT_EXPERIENCE));
        return response;
    }
}
