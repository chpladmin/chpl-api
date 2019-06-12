package gov.healthit.chpl.manager.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ApplicationObjectSupport;
import org.springframework.stereotype.Service;

import gov.healthit.chpl.dao.ListingCountStatisticsDAO;
import gov.healthit.chpl.dao.CriterionProductStatisticsDAO;
import gov.healthit.chpl.dao.EducationTypeDAO;
import gov.healthit.chpl.dao.IncumbentDevelopersStatisticsDAO;
import gov.healthit.chpl.dao.ParticipantAgeStatisticsDAO;
import gov.healthit.chpl.dao.ParticipantEducationStatisticsDAO;
import gov.healthit.chpl.dao.ParticipantExperienceStatisticsDAO;
import gov.healthit.chpl.dao.ParticipantGenderStatisticsDAO;
import gov.healthit.chpl.dao.SedParticipantStatisticsCountDAO;
import gov.healthit.chpl.dao.TestParticipantAgeDAO;
import gov.healthit.chpl.dao.statistics.NonconformityTypeStatisticsDAO;
import gov.healthit.chpl.domain.CriterionProductStatistics;
import gov.healthit.chpl.domain.IncumbentDevelopersStatistics;
import gov.healthit.chpl.domain.ListingCountStatistics;
import gov.healthit.chpl.domain.NonconformityTypeStatistics;
import gov.healthit.chpl.domain.ParticipantAgeStatistics;
import gov.healthit.chpl.domain.ParticipantEducationStatistics;
import gov.healthit.chpl.domain.ParticipantExperienceStatistics;
import gov.healthit.chpl.dto.ListingCountStatisticsDTO;
import gov.healthit.chpl.dto.CriterionProductStatisticsDTO;
import gov.healthit.chpl.dto.EducationTypeDTO;
import gov.healthit.chpl.dto.IncumbentDevelopersStatisticsDTO;
import gov.healthit.chpl.dto.NonconformityTypeStatisticsDTO;
import gov.healthit.chpl.dto.ParticipantAgeStatisticsDTO;
import gov.healthit.chpl.dto.ParticipantEducationStatisticsDTO;
import gov.healthit.chpl.dto.ParticipantExperienceStatisticsDTO;
import gov.healthit.chpl.dto.ParticipantGenderStatisticsDTO;
import gov.healthit.chpl.dto.SedParticipantStatisticsCountDTO;
import gov.healthit.chpl.dto.TestParticipantAgeDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.manager.StatisticsManager;

/**
 * Implementation of the StatisticsManager interface.
 *
 * @author TYoung
 *
 */
@Service
public class StatisticsManagerImpl extends ApplicationObjectSupport implements StatisticsManager {

    @Autowired
    private CriterionProductStatisticsDAO criterionProductStatisticsDAO;

    @Autowired
    private EducationTypeDAO educationTypeDAO;

    @Autowired
    private IncumbentDevelopersStatisticsDAO incumbentDevelopersStatisticsDAO;

    @Autowired
    private ListingCountStatisticsDAO listingCountStatisticsDAO;

    @Autowired
    private NonconformityTypeStatisticsDAO nonconformityTypeStatisticsDAO;

    @Autowired
    private ParticipantAgeStatisticsDAO participantAgeStatisticsDAO;

    @Autowired
    private ParticipantEducationStatisticsDAO participantEducationStatisticsDAO;

    @Autowired
    private ParticipantExperienceStatisticsDAO participantExperienceStatisticsDAO;

    @Autowired
    private ParticipantGenderStatisticsDAO participantGenderStatisticsCountDAO;

    @Autowired
    private SedParticipantStatisticsCountDAO sedParticipantStatisticsCountDAO;

    @Autowired
    private TestParticipantAgeDAO testParticipantAgeDAO;

    @Override
    public List<NonconformityTypeStatistics> getAllNonconformitiesByCriterion() {
        List<NonconformityTypeStatisticsDTO> dtos = nonconformityTypeStatisticsDAO.getAllNonconformityStatistics();

        List<NonconformityTypeStatistics> ret = new ArrayList<NonconformityTypeStatistics>();
        for (NonconformityTypeStatisticsDTO dto : dtos) {
            NonconformityTypeStatistics stat = new NonconformityTypeStatistics(dto);
            ret.add(stat);
        }

        return ret;
    }

    @Override
    public List<SedParticipantStatisticsCountDTO> getAllSedParticipantCounts() {
        return sedParticipantStatisticsCountDAO.findAll();
    }

    @Override
    public List<CriterionProductStatistics> getCriterionProductStatisticsResult() {
        List<CriterionProductStatistics> result = new ArrayList<CriterionProductStatistics>();
        List<CriterionProductStatisticsDTO> dtos = criterionProductStatisticsDAO.findAll();

        for (CriterionProductStatisticsDTO dto : dtos) {
            CriterionProductStatistics cps = new CriterionProductStatistics(dto);
            result.add(cps);
        }
        return result;
    }

    @Override
    public List<IncumbentDevelopersStatistics> getIncumbentDevelopersStatisticsResult() {
        List<IncumbentDevelopersStatistics> result = new ArrayList<IncumbentDevelopersStatistics>();
        List<IncumbentDevelopersStatisticsDTO> dtos = incumbentDevelopersStatisticsDAO.findAll();

        for (IncumbentDevelopersStatisticsDTO dto : dtos) {
            IncumbentDevelopersStatistics ids = new IncumbentDevelopersStatistics(dto);
            result.add(ids);
        }

        return result;
    }

    @Override
    public List<ListingCountStatistics> getListingCountStatisticsResult() {
        List<ListingCountStatistics> result = new ArrayList<ListingCountStatistics>();
        List<ListingCountStatisticsDTO> dtos = listingCountStatisticsDAO.findAll();

        for (ListingCountStatisticsDTO dto : dtos) {
            ListingCountStatistics cps = new ListingCountStatistics(dto);
            result.add(cps);
        }
        return result;
    }

    @Override
    public ParticipantGenderStatisticsDTO getParticipantGenderStatisticsDTO() {
        // There should only ever be one active record.
        List<ParticipantGenderStatisticsDTO> stats = participantGenderStatisticsCountDAO.findAll();
        if (stats != null && stats.size() > 0) {
            return stats.get(0);
        } else {
            return new ParticipantGenderStatisticsDTO();
        }
    }

    @Override
    public List<ParticipantAgeStatistics> getParticipantAgeStatisticsResult() {
        List<ParticipantAgeStatistics> result = new ArrayList<ParticipantAgeStatistics>();
        List<ParticipantAgeStatisticsDTO> dtos = participantAgeStatisticsDAO.findAll();

        for (ParticipantAgeStatisticsDTO dto : dtos) {
            ParticipantAgeStatistics pas = new ParticipantAgeStatistics(dto);
            TestParticipantAgeDTO testParticipantAgeDTO = testParticipantAgeDAO.getById(dto.getTestParticipantAgeId());
            if (testParticipantAgeDTO != null && testParticipantAgeDTO.getAge() != null) {
                pas.setAgeRange(testParticipantAgeDTO.getAge());
            }
            result.add(pas);
        }
        return result;
    }

    @Override
    public List<ParticipantEducationStatistics> getParticipantEducationStatisticsResult() {
        List<ParticipantEducationStatistics> result = new ArrayList<ParticipantEducationStatistics>();
        List<ParticipantEducationStatisticsDTO> dtos = participantEducationStatisticsDAO.findAll();

        for (ParticipantEducationStatisticsDTO dto : dtos) {
            ParticipantEducationStatistics pes = new ParticipantEducationStatistics(dto);
            try {
                EducationTypeDTO educationTypeDTO = educationTypeDAO.getById(dto.getEducationTypeId());
                if (educationTypeDTO != null && educationTypeDTO.getName() != null) {
                    pes.setEducation(educationTypeDTO.getName());
                }
            } catch (EntityRetrievalException e) {
                pes.setEducation("Unknown");
            }
            result.add(pes);
        }
        return result;
    }

    @Override
    public List<ParticipantExperienceStatistics> getParticipantExperienceStatisticsResult(final Long experienceTypeId) {
        List<ParticipantExperienceStatistics> result = new ArrayList<ParticipantExperienceStatistics>();
        List<ParticipantExperienceStatisticsDTO> dtos = participantExperienceStatisticsDAO.findAll(experienceTypeId);

        for (ParticipantExperienceStatisticsDTO dto : dtos) {
            ParticipantExperienceStatistics pes = new ParticipantExperienceStatistics(dto);
            result.add(pes);
        }
        return result;
    }
}
