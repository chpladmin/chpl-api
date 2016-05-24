package gov.healthit.chpl.dto;

import gov.healthit.chpl.entity.TestParticipantEntity;

public class TestParticipantDTO {

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
	
	public TestParticipantDTO(){}
	
	public TestParticipantDTO(TestParticipantEntity entity)
	{
		if(entity != null) {
			this.id = entity.getId();
			this.gender = entity.getGender();
			this.educationTypeId = entity.getEducationTypeId();
			if(entity.getEducation() != null) {
				this.educationType = new EducationTypeDTO(entity.getEducation());
			}
			this.ageRangeId = entity.getAgeRangeId();
			if(entity.getAgeRange() != null) {
				this.ageRange = new AgeRangeDTO(entity.getAgeRange());
			}
			this.occupation = entity.getOccupation();
			this.professionalExperienceMonths = entity.getProductExperienceMonths();
			this.computerExperienceMonths = entity.getComputerExperienceMonths();
			this.productExperienceMonths = entity.getProductExperienceMonths();
			this.assistiveTechnologyNeeds = entity.getAssistiveTechnologyNeeds();
		}
	}
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public Long getEducationTypeId() {
		return educationTypeId;
	}

	public void setEducationTypeId(Long educationTypeId) {
		this.educationTypeId = educationTypeId;
	}

	public EducationTypeDTO getEducationType() {
		return educationType;
	}

	public void setEducationType(EducationTypeDTO educationType) {
		this.educationType = educationType;
	}

	public String getOccupation() {
		return occupation;
	}

	public void setOccupation(String occupation) {
		this.occupation = occupation;
	}

	public Integer getProfessionalExperienceMonths() {
		return professionalExperienceMonths;
	}

	public void setProfessionalExperienceMonths(Integer professionalExperienceMonths) {
		this.professionalExperienceMonths = professionalExperienceMonths;
	}

	public Integer getComputerExperienceMonths() {
		return computerExperienceMonths;
	}

	public void setComputerExperienceMonths(Integer computerExperienceMonths) {
		this.computerExperienceMonths = computerExperienceMonths;
	}

	public Integer getProductExperienceMonths() {
		return productExperienceMonths;
	}

	public void setProductExperienceMonths(Integer productExperienceMonths) {
		this.productExperienceMonths = productExperienceMonths;
	}

	public String getAssistiveTechnologyNeeds() {
		return assistiveTechnologyNeeds;
	}

	public void setAssistiveTechnologyNeeds(String assistiveTechnologyNeeds) {
		this.assistiveTechnologyNeeds = assistiveTechnologyNeeds;
	}

	public String getPendingUniqueId() {
		return pendingUniqueId;
	}

	public void setPendingUniqueId(String pendingUniqueId) {
		this.pendingUniqueId = pendingUniqueId;
	}

	public Long getAgeRangeId() {
		return ageRangeId;
	}

	public void setAgeRangeId(Long ageRangeId) {
		this.ageRangeId = ageRangeId;
	}

	public AgeRangeDTO getAgeRange() {
		return ageRange;
	}

	public void setAgeRange(AgeRangeDTO ageRange) {
		this.ageRange = ageRange;
	}

}
