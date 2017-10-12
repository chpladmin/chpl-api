package gov.healthit.chpl.dto;

import java.io.Serializable;

import gov.healthit.chpl.entity.PendingTestParticipantEntity;

public class PendingTestParticipantDTO implements Serializable {
    private static final long serialVersionUID = -1972103510475701834L;
    private Long id;
    private String uniqueId;
    private String gender;
    private Long educationTypeId;
    private String userEnteredEducationType;
    private EducationTypeDTO educationType;
    private Long ageRangeId;
    private String userEnteredAgeRange;
    private AgeRangeDTO ageRange;
    private String occupation;
    private Integer professionalExperienceMonths;
    private Integer computerExperienceMonths;
    private Integer productExperienceMonths;
    private String assistiveTechnologyNeeds;

    public PendingTestParticipantDTO() {
    }

    public PendingTestParticipantDTO(PendingTestParticipantEntity entity) {
        this.setId(entity.getId());
        this.uniqueId = entity.getUniqueId();
        this.gender = entity.getGender();
        this.educationTypeId = entity.getEducationTypeId();
        this.userEnteredEducationType = entity.getUserEnteredEducation();
        if (entity.getEducation() != null) {
            this.educationType = new EducationTypeDTO(entity.getEducation());
        }
        this.ageRangeId = entity.getAgeRangeId();
        this.userEnteredAgeRange = entity.getUserEnteredAge();
        if (entity.getAgeRange() != null) {
            this.ageRange = new AgeRangeDTO(entity.getAgeRange());
        }
        this.occupation = entity.getOccupation();
        this.professionalExperienceMonths = entity.getProfessionalExperienceMonths();
        this.computerExperienceMonths = entity.getComputerExperienceMonths();
        this.productExperienceMonths = entity.getProductExperienceMonths();
        this.assistiveTechnologyNeeds = entity.getAssistiveTechnologyNeeds();
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(final String uniqueId) {
        this.uniqueId = uniqueId;
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

    public EducationTypeDTO getEducationType() {
        return educationType;
    }

    public void setEducationType(final EducationTypeDTO educationType) {
        this.educationType = educationType;
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

    public String getUserEnteredEducationType() {
        return userEnteredEducationType;
    }

    public void setUserEnteredEducationType(final String userEnteredEducationType) {
        this.userEnteredEducationType = userEnteredEducationType;
    }

    public String getUserEnteredAgeRange() {
        return userEnteredAgeRange;
    }

    public void setUserEnteredAgeRange(final String userEnteredAgeRange) {
        this.userEnteredAgeRange = userEnteredAgeRange;
    }
}
