package gov.healthit.chpl.manager;

import java.util.List;

import gov.healthit.chpl.dto.ParticipantGenderStatisticsDTO;
import gov.healthit.chpl.dto.SedParticipantStatisticsCountDTO;
import gov.healthit.chpl.web.controller.results.ListingCountStatisticsResult;
import gov.healthit.chpl.web.controller.results.CriterionProductStatisticsResult;
import gov.healthit.chpl.web.controller.results.IncumbentDevelopersStatisticsResult;
import gov.healthit.chpl.web.controller.results.ParticipantAgeStatisticsResult;
import gov.healthit.chpl.web.controller.results.ParticipantEducationStatisticsResult;
import gov.healthit.chpl.web.controller.results.ParticipantExperienceStatisticsResult;

/**
 * The StatisticsManager class is used to retrieve data that will be used for charting.
 * @author TYoung
 *
 */
public interface StatisticsManager {
    /**
     * Retrieves the data used for populating Developers & Products charts.
     * @return object with data
     */
    ListingCountStatisticsResult getListingCountStatisticsResult();

    /**
     * Retrieves that data that will be used for the SED and participant counts chart.
     * @return List of SedParticipantStatisticsCountDTO objects
     */
    List<SedParticipantStatisticsCountDTO> getAllSedParticipantCounts();

    /**
     * Retrieves data used for Criterion/Product count chart.
     * @return object with data
     */
    CriterionProductStatisticsResult getCriterionProductStatisticsResult();

    /**
     * Retrieves data used for Incumbent Developers count chart.
     * @return object with data
     */
    IncumbentDevelopersStatisticsResult getIncumbentDevelopersStatisticsResult();

    /**
     * Retrieves that data that will be used for the SED/participant/gender counts chart.
     * @return ParticipantGenderStatisticsDTO object
     *
     */
    ParticipantGenderStatisticsDTO getParticipantGenderStatisticsDTO();

    /**
     * Retrieves that data that will be used for the SED/participant/age counts chart.
     * @return ParticipantAgeStatisticsResult object
     *
     */
    ParticipantAgeStatisticsResult getParticipantAgeStatisticsResult();

    /**
     * Retrieves that data that will be used for the SED/participant/education counts chart.
     * @return ParticipantEducationStatisticsResult object
     *
     */
    ParticipantEducationStatisticsResult getParticipantEducationStatisticsResult();

    /**
     * Retrieves that data that will be used for the SED/participant/experience counts chart based on the
     * parameter passed in.
     * @param experienceTypeId 1 - Professional, 2 - Product, 3 - Computer
     * @return ParticipantExperienceStatisticsResult object
     */
    ParticipantExperienceStatisticsResult getParticipantExperienceStatisticsResult(Long experienceTypeId);

}
