package gov.healthit.chpl.manager.impl;

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
import gov.healthit.chpl.domain.ListingCountStatistics;
import gov.healthit.chpl.domain.CriterionProductStatistics;
import gov.healthit.chpl.domain.IncumbentDevelopersStatistics;
import gov.healthit.chpl.domain.ParticipantAgeStatistics;
import gov.healthit.chpl.domain.ParticipantEducationStatistics;
import gov.healthit.chpl.domain.ParticipantExperienceStatistics;
import gov.healthit.chpl.dto.ListingCountStatisticsDTO;
import gov.healthit.chpl.dto.CriterionProductStatisticsDTO;
import gov.healthit.chpl.dto.EducationTypeDTO;
import gov.healthit.chpl.dto.IncumbentDevelopersStatisticsDTO;
import gov.healthit.chpl.dto.ParticipantAgeStatisticsDTO;
import gov.healthit.chpl.dto.ParticipantEducationStatisticsDTO;
import gov.healthit.chpl.dto.ParticipantExperienceStatisticsDTO;
import gov.healthit.chpl.dto.ParticipantGenderStatisticsDTO;
import gov.healthit.chpl.dto.SedParticipantStatisticsCountDTO;
import gov.healthit.chpl.dto.TestParticipantAgeDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.manager.StatisticsManager;
import gov.healthit.chpl.web.controller.results.ListingCountStatisticsResult;
import gov.healthit.chpl.web.controller.results.CriterionProductStatisticsResult;
import gov.healthit.chpl.web.controller.results.IncumbentDevelopersStatisticsResult;
import gov.healthit.chpl.web.controller.results.ParticipantAgeStatisticsResult;
import gov.healthit.chpl.web.controller.results.ParticipantEducationStatisticsResult;
import gov.healthit.chpl.web.controller.results.ParticipantExperienceStatisticsResult;

/**
 * Implementation of the StatisticsManager interface.
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
    public List<SedParticipantStatisticsCountDTO> getAllSedParticipantCounts() {
        return sedParticipantStatisticsCountDAO.findAll();
    }

    @Override
    public CriterionProductStatisticsResult getCriterionProductStatisticsResult() {
        CriterionProductStatisticsResult result = new CriterionProductStatisticsResult();
        List<CriterionProductStatisticsDTO> dtos = criterionProductStatisticsDAO.findAll();

        for (CriterionProductStatisticsDTO dto : dtos) {
            CriterionProductStatistics cps = new CriterionProductStatistics(dto);
            result.getCriterionProductStatisticsResult().add(cps);
        }
        return result;
    }

    @Override
    public IncumbentDevelopersStatisticsResult getIncumbentDevelopersStatisticsResult() {
        IncumbentDevelopersStatisticsResult result = new IncumbentDevelopersStatisticsResult();
        List<IncumbentDevelopersStatisticsDTO> dtos = incumbentDevelopersStatisticsDAO.findAll();

        for (IncumbentDevelopersStatisticsDTO dto : dtos) {
            IncumbentDevelopersStatistics ids = new IncumbentDevelopersStatistics(dto);
            result.getIncumbentDevelopersStatisticsResult().add(ids);
        }

        return result;
    }

    @Override
    public ListingCountStatisticsResult getListingCountStatisticsResult() {
        ListingCountStatisticsResult result = new ListingCountStatisticsResult();
        List<ListingCountStatisticsDTO> dtos = listingCountStatisticsDAO.findAll();

        for (ListingCountStatisticsDTO dto : dtos) {
            ListingCountStatistics cps = new ListingCountStatistics(dto);
            result.getStatisticsResult().add(cps);
        }
        return result;
    }

    @Override
    public ParticipantGenderStatisticsDTO getParticipantGenderStatisticsDTO() {
        //There should only ever be one active record.
        List<ParticipantGenderStatisticsDTO> stats = participantGenderStatisticsCountDAO.findAll();
        if (stats != null && stats.size() > 0) {
            return stats.get(0);
        } else {
            return new ParticipantGenderStatisticsDTO();
        }
    }

    @Override
    public ParticipantAgeStatisticsResult getParticipantAgeStatisticsResult() {
        ParticipantAgeStatisticsResult result = new ParticipantAgeStatisticsResult();
        List<ParticipantAgeStatisticsDTO> dtos = participantAgeStatisticsDAO.findAll();

        for (ParticipantAgeStatisticsDTO dto : dtos) {
            ParticipantAgeStatistics pas = new ParticipantAgeStatistics(dto);
            TestParticipantAgeDTO testParticipantAgeDTO = testParticipantAgeDAO.getById(dto.getTestParticipantAgeId());
            if (testParticipantAgeDTO != null && testParticipantAgeDTO.getAge() != null) {
                pas.setAgeRange(testParticipantAgeDTO.getAge());
            }
            result.getParticipantAgeStatistics().add(pas);
        }
        return result;
    }

    @Override
    public ParticipantEducationStatisticsResult getParticipantEducationStatisticsResult() {
        ParticipantEducationStatisticsResult result = new ParticipantEducationStatisticsResult();
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
            result.getParticipantEducationStatistics().add(pes);
        }
        return result;
    }

    @Override
    public ParticipantExperienceStatisticsResult getParticipantExperienceStatisticsResult(final Long experienceTypeId) {
        ParticipantExperienceStatisticsResult result = new ParticipantExperienceStatisticsResult();
        List<ParticipantExperienceStatisticsDTO> dtos = participantExperienceStatisticsDAO.findAll(experienceTypeId);

        for (ParticipantExperienceStatisticsDTO dto : dtos) {
            ParticipantExperienceStatistics pes = new ParticipantExperienceStatistics(dto);
            result.getParticipantExperienceStatistics().add(pes);
        }
        return result;
    }
}
