package gov.healthit.chpl.web.controller;

import java.util.List;

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
import gov.healthit.chpl.scheduler.job.chartdata.ExperienceType;
import gov.healthit.chpl.util.SwaggerSecurityRequirement;
import gov.healthit.chpl.web.controller.results.CriterionProductStatisticsResult;
import gov.healthit.chpl.web.controller.results.IncumbentDevelopersStatisticsResult;
import gov.healthit.chpl.web.controller.results.ListingCountStatisticsResult;
import gov.healthit.chpl.web.controller.results.NonconformityTypeStatisticsResult;
import gov.healthit.chpl.web.controller.results.ParticipantAgeStatisticsResult;
import gov.healthit.chpl.web.controller.results.ParticipantEducationStatisticsResult;
import gov.healthit.chpl.web.controller.results.ParticipantExperienceStatisticsResult;
import gov.healthit.chpl.web.controller.results.SedParticipantStatisticsCountResults;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "statistics", description = "Gets statistics.")
@RestController
@RequestMapping("/statistics")
public class StatisticsController {

    @Autowired
    private StatisticsManager statisticsManager;

    @Operation(summary = "Get count of non-conformities by criteria.",
            description = "Retrieves and returns the counts.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "/nonconformity_criteria_count", method = RequestMethod.GET,
            produces = "application/json; charset=utf-8")
    public NonconformityTypeStatisticsResult getNonconformityCountByCriteria() {
        NonconformityTypeStatisticsResult response = new NonconformityTypeStatisticsResult();
        response.setNonconformityStatisticsResult(statisticsManager.getAllNonconformitiesByCriterion());
        return response;
    }

    @Operation(summary = "Get count of Developers and Products with listings.",
            description = "Retrieves and returns the counts.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "/listing_count", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public ListingCountStatisticsResult getListingCountStatistics() {
        ListingCountStatisticsResult response = new ListingCountStatisticsResult();
        response.setStatisticsResult(statisticsManager.getListingCountStatisticsResult());
        return response;
    }

    @Operation(summary = "Get count of Criteria certified to by unique Product.",
            description = "Retrieves and returns the Criterion/Product counts.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "/criterion_product", method = RequestMethod.GET,
            produces = "application/json; charset=utf-8")
    public @ResponseBody CriterionProductStatisticsResult getCriterionProductStatistics() {
        CriterionProductStatisticsResult response = new CriterionProductStatisticsResult();
        response.setCriterionProductStatisticsResult(statisticsManager.getCriterionProductStatisticsResult());
        return response;
    }

    @Operation(summary = "Get count of new vs. incumbent Developers.",
            description = "Retrieves and returns counts grouped by Edition.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "incumbent_developers", method = RequestMethod.GET,
            produces = "application/json; charset=utf-8")
    public @ResponseBody IncumbentDevelopersStatisticsResult getIncumbentDevelopersStatistics() {
        IncumbentDevelopersStatisticsResult response = new IncumbentDevelopersStatisticsResult();
        response.setIncumbentDevelopersStatisticsResult(statisticsManager.getIncumbentDevelopersStatisticsResult());
        return response;
    }

    @Operation(summary = "Get all Sed/Participant counts.",
            description = "Retrieves and returns the SED/Participant counts.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "/sed_participant_count", method = RequestMethod.GET,
            produces = "application/json; charset=utf-8")
    public @ResponseBody SedParticipantStatisticsCountResults getSedParticipantStatisticsCounts() {
        SedParticipantStatisticsCountResults results = new SedParticipantStatisticsCountResults();
        List<SedParticipantStatisticsCountDTO> sedParticipantStatisticsCountDTOs = statisticsManager
                .getAllSedParticipantCounts();
        if (sedParticipantStatisticsCountDTOs != null) {
            for (SedParticipantStatisticsCountDTO sedParticipantStatisticsCountDTO : sedParticipantStatisticsCountDTOs) {
                results.getSedParticipantStatisticsCounts().add(
                        new SedParticipantStatisticsCount(sedParticipantStatisticsCountDTO));
            }
        }
        return results;
    }

    @Operation(summary = "Get all Sed/Participant/Gender counts.",
            description = "Retrieves and returns the SED/Participant/Gender counts.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "/participant_gender_count", method = RequestMethod.GET,
            produces = "application/json; charset=utf-8")
    public @ResponseBody ParticipantGenderStatistics getParticipantGenderStatistics() {
        ParticipantGenderStatisticsDTO stats = statisticsManager.getParticipantGenderStatisticsDTO();
        return new ParticipantGenderStatistics(stats);
    }

    @Operation(summary = "Get all Sed/Participant/Age counts.",
            description = "Retrieves and returns the SED/Participant/Age counts.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "/participant_age_count", method = RequestMethod.GET,
            produces = "application/json; charset=utf-8")
    public @ResponseBody ParticipantAgeStatisticsResult getParticipantAgeStatistics() {
        ParticipantAgeStatisticsResult response = new ParticipantAgeStatisticsResult();
        response.setParticipantAgeStatistics(statisticsManager.getParticipantAgeStatisticsResult());
        return response;
    }

    @Operation(summary = "Get all Sed/Participant/Education counts.",
            description = "Retrieves and returns the SED/Participant/Education counts.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "/participant_education_count", method = RequestMethod.GET,
            produces = "application/json; charset=utf-8")
    public @ResponseBody ParticipantEducationStatisticsResult getParticipantEducationStatistics() {
        ParticipantEducationStatisticsResult response = new ParticipantEducationStatisticsResult();
        response.setParticipantEducationStatistics(statisticsManager.getParticipantEducationStatisticsResult());
        return response;
    }

    @Operation(summary = "Get all Sed/Participant/Professional Experience counts.",
            description = "Retrieves and returns the SED/Participant/Preofessional Experience counts.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "/participant_professional_experience_count", method = RequestMethod.GET,
            produces = "application/json; charset=utf-8")
    public @ResponseBody ParticipantExperienceStatisticsResult getParticipantProfessionalExperienceStatistics() {
        ParticipantExperienceStatisticsResult response = new ParticipantExperienceStatisticsResult();
        response.setParticipantExperienceStatistics(statisticsManager
                .getParticipantExperienceStatisticsResult(ExperienceType.PROFESSIONAL_EXPERIENCE));
        return response;
    }

    @Operation(summary = "Get all Sed/Participant/Computer Experience counts.",
            description = "Retrieves and returns the SED/Participant/Computer Experience counts.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "/participant_computer_experience_count", method = RequestMethod.GET,
            produces = "application/json; charset=utf-8")
    public @ResponseBody ParticipantExperienceStatisticsResult getParticipantComputerExperienceStatistics() {
        ParticipantExperienceStatisticsResult response = new ParticipantExperienceStatisticsResult();
        response.setParticipantExperienceStatistics(statisticsManager
                .getParticipantExperienceStatisticsResult(ExperienceType.COMPUTER_EXPERIENCE));
        return response;
    }

    @Operation(summary = "Get all Sed/Participant/Product Experience counts.",
            description = "Retrieves and returns the SED/Participant/Product Experience counts.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "/participant_product_experience_count", method = RequestMethod.GET,
            produces = "application/json; charset=utf-8")
    public @ResponseBody ParticipantExperienceStatisticsResult getParticipantProductExperienceStatistics() {
        ParticipantExperienceStatisticsResult response = new ParticipantExperienceStatisticsResult();
        response.setParticipantExperienceStatistics(statisticsManager
                .getParticipantExperienceStatisticsResult(ExperienceType.PRODUCT_EXPERIENCE));
        return response;
    }
}
