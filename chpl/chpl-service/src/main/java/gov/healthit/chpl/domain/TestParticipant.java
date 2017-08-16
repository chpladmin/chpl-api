package gov.healthit.chpl.domain;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.StringUtils;

import gov.healthit.chpl.dto.TestParticipantDTO;

/**
 * Participant in a given test task.
 */
@XmlType(namespace = "http://chpl.healthit.gov/listings")
@XmlAccessorType(XmlAccessType.FIELD)
public class TestParticipant implements Serializable {
	private static final long serialVersionUID = -3771155258451736516L;
	private static final Logger logger = LogManager.getLogger(TestParticipant.class);

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
	private Long id;
	
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
	
	public TestParticipant() {}
	
	public TestParticipant(TestParticipantDTO dto) {
		this();
		this.id = dto.getId();
		this.gender = dto.getGender();
		this.educationTypeId = dto.getEducationTypeId();
		if(dto.getEducationType() != null) {
			this.educationTypeName = dto.getEducationType().getName();
		}
		this.ageRangeId = dto.getAgeRangeId();
		if(dto.getAgeRange() != null) {
			this.ageRange = dto.getAgeRange().getAge();
		}
		this.occupation = dto.getOccupation();
		this.professionalExperienceMonths = dto.getProfessionalExperienceMonths();
		this.computerExperienceMonths = dto.getComputerExperienceMonths();
		this.productExperienceMonths = dto.getProductExperienceMonths();
		this.assistiveTechnologyNeeds = dto.getAssistiveTechnologyNeeds();
	}
	
	@Override
	public boolean equals(Object other) {
		if(other == null || !(other instanceof TestParticipant)) {
			return false;
		}
		TestParticipant anotherTask = (TestParticipant) other;
		return matches(anotherTask);
	}
	
	@Override
	public int hashCode() {
		return this.getId().hashCode();
	}
	
	public boolean matches(TestParticipant anotherParticipant) {
		boolean result = false;
		if(this.getId() != null && anotherParticipant.getId() != null && 
				this.getId().longValue() == anotherParticipant.getId().longValue()) {
			result = true;
		} 
		//TODO: should we compare all the values??
		return result;
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
