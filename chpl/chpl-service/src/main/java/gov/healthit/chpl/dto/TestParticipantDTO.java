package gov.healthit.chpl.dto;

import java.io.Serializable;

import gov.healthit.chpl.entity.TestParticipantEntity;

public class TestParticipantDTO implements Serializable {
    private static final long serialVersionUID = 5294870595258371654L;
    private Long id;
    private String gender;
    private Long educationTypeId;
    private EducationTypeDTO educationType;
    private Long ageRangeId;
    private AgeRangeDTO ageRange;
    private String occupation;
    private Integer professionalExperienceMonths;
    private Integer computerExperienceMonths;
    private Integer productExperienceMonths;
    private String assistiveTechnologyNeeds;

    private String pendingUniqueId;

    public TestParticipantDTO() {
    }

    public TestParticipantDTO(TestParticipantEntity entity) {
        if (entity != null) {
            this.id = entity.getId();
            this.gender = entity.getGender();
            this.educationTypeId = entity.getEducationTypeId();
            if (entity.getEducation() != null) {
                this.educationType = new EducationTypeDTO(entity.getEducation());
            }
            this.ageRangeId = entity.getAgeRangeId();
            if (entity.getAgeRange() != null) {
                this.ageRange = new AgeRangeDTO(entity.getAgeRange());
            }
            this.occupation = entity.getOccupation();
            this.professionalExperienceMonths = entity.getProfessionalExperienceMonths();
            this.computerExperienceMonths = entity.getComputerExperienceMonths();
            this.productExperienceMonths = entity.getProductExperienceMonths();
            this.assistiveTechnologyNeeds = entity.getAssistiveTechnologyNeeds();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(final String gender) {
        this.gender = gender;
    }

    public Long getEducationTypeId() {
        return educationTypeId;
    }

    public void setEducationTypeId(final Long educationTypeId) {
        this.educationTypeId = educationTypeId;
    }

    public EducationTypeDTO getEducationType() {
        return educationType;
    }

    public void setEducationType(final EducationTypeDTO educationType) {
        this.educationType = educationType;
    }

    public String getOccupation() {
        return occupation;
    }

    public void setOccupation(final String occupation) {
        this.occupation = occupation;
    }

    public Integer getProfessionalExperienceMonths() {
        return professionalExperienceMonths;
    }

    public void setProfessionalExperienceMonths(final Integer professionalExperienceMonths) {
        this.professionalExperienceMonths = professionalExperienceMonths;
    }

    public Integer getComputerExperienceMonths() {
        return computerExperienceMonths;
    }

    public void setComputerExperienceMonths(final Integer computerExperienceMonths) {
        this.computerExperienceMonths = computerExperienceMonths;
    }

    public Integer getProductExperienceMonths() {
        return productExperienceMonths;
    }

    public void setProductExperienceMonths(final Integer productExperienceMonths) {
        this.productExperienceMonths = productExperienceMonths;
    }

    public String getAssistiveTechnologyNeeds() {
        return assistiveTechnologyNeeds;
    }

    public void setAssistiveTechnologyNeeds(final String assistiveTechnologyNeeds) {
        this.assistiveTechnologyNeeds = assistiveTechnologyNeeds;
    }

    public String getPendingUniqueId() {
        return pendingUniqueId;
    }

    public void setPendingUniqueId(final String pendingUniqueId) {
        this.pendingUniqueId = pendingUniqueId;
    }

    public Long getAgeRangeId() {
        return ageRangeId;
    }

    public void setAgeRangeId(final Long ageRangeId) {
        this.ageRangeId = ageRangeId;
    }

    public AgeRangeDTO getAgeRange() {
        return ageRange;
    }

    public void setAgeRange(final AgeRangeDTO ageRange) {
        this.ageRange = ageRange;
    }

}
