package gov.healthit.chpl.manager;

import java.util.List;

import gov.healthit.chpl.domain.CriterionProductStatistics;
import gov.healthit.chpl.domain.IncumbentDevelopersStatistics;
import gov.healthit.chpl.domain.ListingCountStatistics;
import gov.healthit.chpl.domain.ParticipantAgeStatistics;
import gov.healthit.chpl.domain.ParticipantEducationStatistics;
import gov.healthit.chpl.domain.ParticipantExperienceStatistics;
import gov.healthit.chpl.dto.ParticipantGenderStatisticsDTO;
import gov.healthit.chpl.dto.SedParticipantStatisticsCountDTO;

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
    List<ListingCountStatistics> getListingCountStatisticsResult();

    /**
     * Retrieves that data that will be used for the SED and participant counts chart.
     * @return List of SedParticipantStatisticsCountDTO objects
     */
    List<SedParticipantStatisticsCountDTO> getAllSedParticipantCounts();

    /**
     * Retrieves data used for Criterion/Product count chart.
     * @return object with data
     */
    List<CriterionProductStatistics> getCriterionProductStatisticsResult();

    /**
     * Retrieves data used for Incumbent Developers count chart.
     * @return object with data
     */
    List<IncumbentDevelopersStatistics> getIncumbentDevelopersStatisticsResult();

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
    List<ParticipantAgeStatistics> getParticipantAgeStatisticsResult();

    /**
     * Retrieves that data that will be used for the SED/participant/education counts chart.
     * @return ParticipantEducationStatisticsResult object
     *
     */
    List<ParticipantEducationStatistics> getParticipantEducationStatisticsResult();

    /**
     * Retrieves that data that will be used for the SED/participant/experience counts chart based on the
     * parameter passed in.
     * @param experienceTypeId 1 - Professional, 2 - Product, 3 - Computer
     * @return ParticipantExperienceStatisticsResult object
     */
    List<ParticipantExperienceStatistics> getParticipantExperienceStatisticsResult(Long experienceTypeId);

}
