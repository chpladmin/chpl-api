package gov.healthit.chpl.domain;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.StringUtils;

import gov.healthit.chpl.dto.CertificationResultTestTaskParticipantDTO;

/**
 * Participant in a given test task.
 */
@XmlType(namespace = "http://chpl.healthit.gov/listings")
@XmlAccessorType(XmlAccessType.FIELD)
public class CertificationResultTestParticipant implements Serializable {
	private static final long serialVersionUID = -5404897845633867927L;

	private static final Logger logger = LogManager.getLogger(CertificationResultTestParticipant.class);

	/**
	 * Test task to participant mapping internal ID
	 */
	@XmlElement(required = true)
	private Long id;
	
	/**
	 * An ONC-ACB designated identifier for an individual SED participant. 
	 * This variable is a string variable only applicable to 2015 Edition, 
	 * but must be unique to a particular participant. 
	 * It is for internal use within an upload file only.
	 */
	@XmlElement(required = false, nillable=true)
	private String uniqueId;
	
	/**
	 * Participant internal ID
	 */
	@XmlElement(required = true)
	private Long testParticipantId;
	
	/**
	 * Self-reported gender of the corresponding participant. 
	 * This variable is only applicable for 2015 Edition. 
	 * The following are allowable values for the 'Participant Gender' field: Male, Female, Unknown.
	 */
	@XmlElement(required = false, nillable=true)
	private String gender;
	
	/**
	 * Education internal ID
	 */
	@XmlElement(required = false, nillable=true)
	private Long educationTypeId;
	
	/**
	 * Highest education level attained by corresponding participant. This variable is only applicable for 2015 Edition. 
	 * The following are allowable values for the 'Participant Education' field: 
	 * No high school degree; High school graduate, diploma or the equivalent (for example: GED); 
	 * Some college credit, no degree; Trade/technical/vocational training; Associate degree; 
	 * Bachelor's degree; Master's degree; Doctorate degree (e.g., MD, DNP, DMD, PhD).
	 */
	@XmlElement(required = false, nillable=true)
	private String educationTypeName;
	
	/**
	 * Age range internal ID
	 */
	@XmlElement(required = false, nillable=true)
	private Long ageRangeId;
	
	/**
	 * The age range for the corresponding participant. 
	 * The following are allowable values for the 'Participant Age' field: 0-9, 10-19, 20-29, 30-39, 40-49, 50-59, 60-69, 70-79, 80-89, 90-99, 100+
	 */
	@XmlElement(required = false, nillable=true)
	private String ageRange;
	
	/**
	 * This variable illustrates occupation or role of corresponding participant. It is only applicable to 2015 Edition and a string variable that does not take any restrictions on formatting or values. 
	 */
	@XmlElement(required = false, nillable=true)
	private String occupation;
	
	/**
	 * Professional experience of the corresponding participant, in number of months. 
	 * This variable is only applicable to 2015 Edition, and takes only positive 
	 * integers (i.e. no decimals) values. 
	 */
	@XmlElement(required = false, nillable=true)
	private Integer professionalExperienceMonths;
	
	/**
	 * The corresponding participant's experience with computers (in general), in number of months. It is only applicable for 2015 Edition and takes only positive integers (i.e. no decimals). 
	 */
	@XmlElement(required = false, nillable=true)
	private Integer computerExperienceMonths;
	
	/**
	 * The corresponding participant's experience with the certified product/ health IT 
	 * capabilities (SED criterion) being tested, in number of months. This variable is 
	 * applicable to 2015 Edition, and only takes positive integers 
	 * (i.e. no decimals are allowed) values. 
	 */
	@XmlElement(required = false, nillable=true)
	private Integer productExperienceMonths;
	
	/**
	 * Any assistive technology needs as identified by the corresponding participant. This variable is a string variable that does not take any restrictions on formatting or values and is only applicable for 2015 Edition. 
	 */
	@XmlElement(required = false, nillable=true)
	private String assistiveTechnologyNeeds;

	public CertificationResultTestParticipant() {
		super();
	}
	
	public CertificationResultTestParticipant(CertificationResultTestTaskParticipantDTO dto) {
		this.id = dto.getId();
		this.testParticipantId = dto.getTestParticipantId();
		if(dto.getTestParticipant() != null) {
			this.gender = dto.getTestParticipant().getGender();
			this.educationTypeId = dto.getTestParticipant().getEducationTypeId();
			if(dto.getTestParticipant().getEducationType() != null) {
				this.educationTypeName = dto.getTestParticipant().getEducationType().getName();
			}
			this.ageRangeId = dto.getTestParticipant().getAgeRangeId();
			if(dto.getTestParticipant().getAgeRange() != null) {
				this.ageRange = dto.getTestParticipant().getAgeRange().getAge();
			}
			this.occupation = dto.getTestParticipant().getOccupation();
			this.professionalExperienceMonths = dto.getTestParticipant().getProfessionalExperienceMonths();
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

	public void setProfessionalExperienceMonths(String value) {
		if(!StringUtils.isEmpty(value)) {
	        try {
	        	professionalExperienceMonths = Math.round(new Float(value));
	        } catch (NumberFormatException e) {
	           logger.error("can't parse " + value + " as a float or integer.");
	        }
		}
    }
	
	public Integer getComputerExperienceMonths() {
		return computerExperienceMonths;
	}
	
	public void setComputerExperienceMonths(String value) {
		if(!StringUtils.isEmpty(value)) {
	        try {
	        	computerExperienceMonths = Math.round(new Float(value));
	        } catch (NumberFormatException e) {
	           logger.error("can't parse " + value + " as a float or integer.");
	        }
		}
    }
	
	public Integer getProductExperienceMonths() {
		return productExperienceMonths;
	}

	public void setProductExperienceMonths(String value) {
		if(!StringUtils.isEmpty(value)) {
	        try {
	        	productExperienceMonths = Math.round(new Float(value));
	        } catch (NumberFormatException e) {
	           logger.error("can't parse " + value + " as a float or integer.");
	        }
		}
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

	public String getUniqueId() {
		return uniqueId;
	}

	public void setUniqueId(String uniqueId) {
		this.uniqueId = uniqueId;
	}

	public Long getAgeRangeId() {
		return ageRangeId;
	}

	public void setAgeRangeId(Long ageRangeId) {
		this.ageRangeId = ageRangeId;
	}

	public String getAgeRange() {
		return ageRange;
	}

	public void setAgeRange(String ageRange) {
		this.ageRange = ageRange;
	}

}
