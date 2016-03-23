package gov.healthit.chpl.domain;

import gov.healthit.chpl.dto.CertificationResultTestParticipantDTO;

public class CertificationResultTestParticipant {
	private Long id;
	private Long testParticipantId;
	private String gender;
	private Integer age;
	private Long educationTypeId;
	private String educationTypeName;
	private String occupation;
	private Integer professionalExperienceMonths;
	private Integer computerExperienceMonths;
	private Integer productExperienceMonths;
	private String assistiveTechnologyNeeds;

	public CertificationResultTestParticipant() {
		super();
	}
	
	public CertificationResultTestParticipant(CertificationResultTestParticipantDTO dto) {
		this.id = dto.getId();
		this.testParticipantId = dto.getTestParticipantId();
		if(dto.getTestParticipant() != null) {
			this.age = dto.getTestParticipant().getAge();
			this.gender = dto.getTestParticipant().getGender();
			this.educationTypeId = dto.getTestParticipant().getEducationTypeId();
			if(dto.getTestParticipant().getEducationType() != null) {
				this.educationTypeName = dto.getTestParticipant().getEducationType().getName();
			}
			this.occupation = dto.getTestParticipant().getOccupation();
			this.professionalExperienceMonths = dto.getTestParticipant().getProductExperienceMonths();
			this.computerExperienceMonths = dto.getTestParticipant().getComputerExperienceMonths();
			this.productExperienceMonths = dto.getTestParticipant().getProductExperienceMonths();
			this.assistiveTechnologyNeeds = dto.getTestParticipant().getAssistiveTechnologyNeeds();
		}
	}
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getTestParticipantId() {
		return testParticipantId;
	}

	public void setTestParticipantId(Long testParticipantId) {
		this.testParticipantId = testParticipantId;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public Integer getAge() {
		return age;
	}

	public void setAge(Integer age) {
		this.age = age;
	}

	public Long getEducationTypeId() {
		return educationTypeId;
	}

	public void setEducationTypeId(Long educationTypeId) {
		this.educationTypeId = educationTypeId;
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

	public String getEducationTypeName() {
		return educationTypeName;
	}

	public void setEducationTypeName(String educationTypeName) {
		this.educationTypeName = educationTypeName;
	}

}
