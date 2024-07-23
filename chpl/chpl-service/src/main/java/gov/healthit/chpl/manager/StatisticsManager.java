package gov.healthit.chpl.manager;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ApplicationObjectSupport;
import org.springframework.stereotype.Service;

import gov.healthit.chpl.certificationCriteria.CertificationCriterionComparator;
import gov.healthit.chpl.dao.AgeRangeDAO;
import gov.healthit.chpl.dao.CriterionProductStatisticsDAO;
import gov.healthit.chpl.dao.EducationTypeDAO;
import gov.healthit.chpl.dao.IncumbentDevelopersStatisticsDAO;
import gov.healthit.chpl.dao.ListingCountStatisticsDAO;
import gov.healthit.chpl.dao.ParticipantAgeStatisticsDAO;
import gov.healthit.chpl.dao.ParticipantEducationStatisticsDAO;
import gov.healthit.chpl.dao.ParticipantExperienceStatisticsDAO;
import gov.healthit.chpl.dao.ParticipantGenderStatisticsDAO;
import gov.healthit.chpl.dao.SedParticipantStatisticsCountDAO;
import gov.healthit.chpl.dao.statistics.NonconformityTypeStatisticsDAO;
import gov.healthit.chpl.domain.CriterionProductStatistics;
import gov.healthit.chpl.domain.IncumbentDevelopersStatistics;
import gov.healthit.chpl.domain.ListingCountStatistics;
import gov.healthit.chpl.domain.NonconformityTypeStatistics;
import gov.healthit.chpl.domain.ParticipantAgeStatistics;
import gov.healthit.chpl.domain.ParticipantEducationStatistics;
import gov.healthit.chpl.domain.ParticipantExperienceStatistics;
import gov.healthit.chpl.domain.TestParticipant.TestParticipantAge;
import gov.healthit.chpl.domain.TestParticipant.TestParticipantEducation;
import gov.healthit.chpl.dto.IncumbentDevelopersStatisticsDTO;
import gov.healthit.chpl.dto.ListingCountStatisticsDTO;
import gov.healthit.chpl.dto.NonconformityTypeStatisticsDTO;
import gov.healthit.chpl.dto.ParticipantAgeStatisticsDTO;
import gov.healthit.chpl.dto.ParticipantEducationStatisticsDTO;
import gov.healthit.chpl.dto.ParticipantExperienceStatisticsDTO;
import gov.healthit.chpl.dto.ParticipantGenderStatisticsDTO;
import gov.healthit.chpl.dto.SedParticipantStatisticsCountDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;

@Service
public class StatisticsManager extends ApplicationObjectSupport {

    private CriterionProductStatisticsDAO criterionProductStatisticsDAO;
    private EducationTypeDAO educationTypeDAO;
    private IncumbentDevelopersStatisticsDAO incumbentDevelopersStatisticsDAO;
    private ListingCountStatisticsDAO listingCountStatisticsDAO;
    private NonconformityTypeStatisticsDAO nonconformityTypeStatisticsDAO;
    private ParticipantAgeStatisticsDAO participantAgeStatisticsDAO;
    private ParticipantEducationStatisticsDAO participantEducationStatisticsDAO;
    private ParticipantExperienceStatisticsDAO participantExperienceStatisticsDAO;
    private ParticipantGenderStatisticsDAO participantGenderStatisticsCountDAO;
    private SedParticipantStatisticsCountDAO sedParticipantStatisticsCountDAO;
    private AgeRangeDAO ageRangeDao;
    private CertificationCriterionComparator certificationCriterionComparator;

    @Autowired
    public StatisticsManager(CriterionProductStatisticsDAO criterionProductStatisticsDAO, EducationTypeDAO educationTypeDAO,
            IncumbentDevelopersStatisticsDAO incumbentDevelopersStatisticsDAO, ListingCountStatisticsDAO listingCountStatisticsDAO,
            NonconformityTypeStatisticsDAO nonconformityTypeStatisticsDAO, ParticipantAgeStatisticsDAO participantAgeStatisticsDAO,
            ParticipantEducationStatisticsDAO participantEducationStatisticsDAO, ParticipantExperienceStatisticsDAO participantExperienceStatisticsDAO,
            ParticipantGenderStatisticsDAO participantGenderStatisticsCountDAO, SedParticipantStatisticsCountDAO sedParticipantStatisticsCountDAO,
            AgeRangeDAO ageRangeDao, CertificationCriterionComparator certificationCriterionComparator) {

        this.criterionProductStatisticsDAO = criterionProductStatisticsDAO;
        this.educationTypeDAO = educationTypeDAO;
        this.incumbentDevelopersStatisticsDAO = incumbentDevelopersStatisticsDAO;
        this.listingCountStatisticsDAO = listingCountStatisticsDAO;
        this.nonconformityTypeStatisticsDAO = nonconformityTypeStatisticsDAO;
        this.participantAgeStatisticsDAO = participantAgeStatisticsDAO;
        this.participantEducationStatisticsDAO = participantEducationStatisticsDAO;
        this.participantExperienceStatisticsDAO = participantExperienceStatisticsDAO;
        this.participantGenderStatisticsCountDAO = participantGenderStatisticsCountDAO;
        this.sedParticipantStatisticsCountDAO = sedParticipantStatisticsCountDAO;
        this.ageRangeDao = ageRangeDao;
        this.certificationCriterionComparator = certificationCriterionComparator;
    }


    public List<NonconformityTypeStatistics> getAllNonconformitiesByCriterion() {
        List<NonconformityTypeStatisticsDTO> dtos = nonconformityTypeStatisticsDAO.getAllNonconformityStatistics();

        List<NonconformityTypeStatistics> ret = new ArrayList<NonconformityTypeStatistics>();
        for (NonconformityTypeStatisticsDTO dto : dtos) {
            NonconformityTypeStatistics stat = new NonconformityTypeStatistics(dto);
            ret.add(stat);
        }

        return ret;
    }

    public List<SedParticipantStatisticsCountDTO> getAllSedParticipantCounts() {
        return sedParticipantStatisticsCountDAO.findAll();
    }

    public List<CriterionProductStatistics> getCriterionProductStatisticsResult() {
        List<CriterionProductStatistics> criterionProductStatistics = criterionProductStatisticsDAO.findAll().stream()
                .sorted(Comparator.comparing(CriterionProductStatistics::getCriterion, certificationCriterionComparator))
                .toList();

        for (int i = 0; i < criterionProductStatistics.size(); i++) {
            criterionProductStatistics.get(i).setSortOrder(i);
        }
        return criterionProductStatistics;
    }

    public List<IncumbentDevelopersStatistics> getIncumbentDevelopersStatisticsResult() {
        List<IncumbentDevelopersStatistics> result = new ArrayList<IncumbentDevelopersStatistics>();
        List<IncumbentDevelopersStatisticsDTO> dtos = incumbentDevelopersStatisticsDAO.findAll();

        for (IncumbentDevelopersStatisticsDTO dto : dtos) {
            IncumbentDevelopersStatistics ids = new IncumbentDevelopersStatistics(dto);
            result.add(ids);
        }

        return result;
    }

    public List<ListingCountStatistics> getListingCountStatisticsResult() {
        List<ListingCountStatistics> result = new ArrayList<ListingCountStatistics>();
        List<ListingCountStatisticsDTO> dtos = listingCountStatisticsDAO.findAll();

        for (ListingCountStatisticsDTO dto : dtos) {
            ListingCountStatistics cps = new ListingCountStatistics(dto);
            result.add(cps);
        }
        return result;
    }

    public ParticipantGenderStatisticsDTO getParticipantGenderStatisticsDTO() {
        // There should only ever be one active record.
        List<ParticipantGenderStatisticsDTO> stats = participantGenderStatisticsCountDAO.findAll();
        if (stats != null && stats.size() > 0) {
            return stats.get(0);
        } else {
            return new ParticipantGenderStatisticsDTO();
        }
    }

    public List<ParticipantAgeStatistics> getParticipantAgeStatisticsResult() {
        List<ParticipantAgeStatistics> result = new ArrayList<ParticipantAgeStatistics>();
        List<ParticipantAgeStatisticsDTO> dtos = participantAgeStatisticsDAO.findAll();

        for (ParticipantAgeStatisticsDTO dto : dtos) {
            ParticipantAgeStatistics pas = new ParticipantAgeStatistics(dto);
            TestParticipantAge ageRange = ageRangeDao.getById(dto.getTestParticipantAgeId());
            if (ageRange != null && ageRange.getName() != null) {
                pas.setAgeRange(ageRange.getName());
            }
            result.add(pas);
        }
        return result;
    }

    public List<ParticipantEducationStatistics> getParticipantEducationStatisticsResult() {
        List<ParticipantEducationStatistics> result = new ArrayList<ParticipantEducationStatistics>();
        List<ParticipantEducationStatisticsDTO> dtos = participantEducationStatisticsDAO.findAll();

        for (ParticipantEducationStatisticsDTO dto : dtos) {
            ParticipantEducationStatistics pes = new ParticipantEducationStatistics(dto);
            try {
                TestParticipantEducation educationType = educationTypeDAO.getById(dto.getEducationTypeId());
                if (educationType != null && educationType.getName() != null) {
                    pes.setEducation(educationType.getName());
                }
            } catch (EntityRetrievalException e) {
                pes.setEducation("Unknown");
            }
            result.add(pes);
        }
        return result;
    }

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
