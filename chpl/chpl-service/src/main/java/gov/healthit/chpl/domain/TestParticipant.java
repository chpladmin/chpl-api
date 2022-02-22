package gov.healthit.chpl.domain;

import java.io.Serializable;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.annotation.JsonIgnore;

import gov.healthit.chpl.dto.TestParticipantDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * Participant in a given test task.
 */
@XmlType(namespace = "http://chpl.healthit.gov/listings")
@XmlAccessorType(XmlAccessType.FIELD)
@Builder(toBuilder = true)
@AllArgsConstructor
public class TestParticipant implements Serializable {
    private static final long serialVersionUID = -3771155258451736516L;
    private static final Logger LOGGER = LogManager.getLogger(TestParticipant.class);

    /**
     * Participant internal ID
     */
    @XmlElement(required = true)
    private Long id;

    /**
     * An ONC-ACB designated identifier for an individual SED participant. This
     * variable is a string variable only applicable to 2015 Edition, but must
     * be unique to a particular participant. It is for internal use within an
     * upload file only.
     */
    @XmlTransient
    private String uniqueId;

    /**
     * Self-reported gender of the corresponding participant. This variable is
     * only applicable for 2015 Edition. The following are allowable values for
     * the 'Participant Gender' field: Male, Female, Unknown.
     */
    @XmlElement(required = true)
    private String gender;

    /**
     * Education internal ID
     */
    @XmlElement(required = true)
    private Long educationTypeId;

    /**
     * Highest education level attained by corresponding participant. This
     * variable is only applicable for 2015 Edition. The following are allowable
     * values for the 'Participant Education' field: No high school degree; High
     * school graduate, diploma or the equivalent (for example: GED); Some
     * college credit, no degree; Trade/technical/vocational training; Associate
     * degree; Bachelor's degree; Master's degree; Doctorate degree (e.g., MD,
     * DNP, DMD, PhD).
     */
    @XmlElement(required = true)
    private String educationTypeName;

    /**
     * Age range internal ID
     */
    @XmlElement(required = true)
    private Long ageRangeId;

    /**
     * The age range for the corresponding participant. The following are
     * allowable values for the 'Participant Age' field: 0-9, 10-19, 20-29,
     * 30-39, 40-49, 50-59, 60-69, 70-79, 80-89, 90-99, 100+
     */
    @XmlElement(required = true)
    private String ageRange;

    /**
     * This variable illustrates occupation or role of corresponding
     * participant. It is only applicable to 2015 Edition and a string variable
     * that does not take any restrictions on formatting or values.
     */
    @XmlElement(required = true)
    private String occupation;

    /**
     * Professional experience of the corresponding participant, in number of
     * months. This variable is only applicable to 2015 Edition, and takes only
     * positive integers (i.e. no decimals) values.
     */
    @XmlElement(required = true)
    private Integer professionalExperienceMonths;

    @XmlTransient
    @JsonIgnore
    private String professionalExperienceMonthsStr;

    /**
     * The corresponding participant's experience with computers (in general),
     * in number of months. It is only applicable for 2015 Edition and takes
     * only positive integers (i.e. no decimals).
     */
    @XmlElement(required = true)
    private Integer computerExperienceMonths;

    @XmlTransient
    @JsonIgnore
    private String computerExperienceMonthsStr;

    /**
     * The corresponding participant's experience with the certified product/
     * health IT capabilities (SED criterion) being tested, in number of months.
     * This variable is applicable to 2015 Edition, and only takes positive
     * integers (i.e. no decimals are allowed) values.
     */
    @XmlElement(required = true)
    private Integer productExperienceMonths;

    @XmlTransient
    @JsonIgnore
    private String productExperienceMonthsStr;

    /**
     * Any assistive technology needs as identified by the corresponding
     * participant. This variable is a string variable that does not take any
     * restrictions on formatting or values and is only applicable for 2015
     * Edition.
     */
    @XmlElement(required = true)
    private String assistiveTechnologyNeeds;

    public TestParticipant() {
    }

    public TestParticipant(TestParticipantDTO dto) {
        this();
        this.id = dto.getId();
        this.gender = dto.getGender();
        this.educationTypeId = dto.getEducationTypeId();
        if (dto.getEducationType() != null) {
            this.educationTypeName = dto.getEducationType().getName();
        }
        this.ageRangeId = dto.getAgeRangeId();
        if (dto.getAgeRange() != null) {
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
        if (other == null || !(other instanceof TestParticipant)) {
            return false;
        }
        TestParticipant anotherParticipant = (TestParticipant) other;
        return matches(anotherParticipant);
    }

    @Override
    public int hashCode() {
        int hashCode = 0;
        if (this.getId() != null) {
            hashCode = this.getId().hashCode();
        } else {
            if (this.getAgeRange() != null) {
                hashCode += this.getAgeRange().hashCode();
            } else if (this.getAgeRangeId() != null) {
                hashCode += this.getAgeRangeId().hashCode();
            }
            if (this.getAssistiveTechnologyNeeds() != null) {
                hashCode += this.getAssistiveTechnologyNeeds().hashCode();
            }
            if (this.getComputerExperienceMonths() != null) {
                hashCode += this.getComputerExperienceMonths().hashCode();
            }
            if (this.getEducationTypeName() != null) {
                hashCode += this.getEducationTypeName().hashCode();
            } else if (this.getEducationTypeId() != null) {
                hashCode += this.getEducationTypeId().hashCode();
            }
            if (this.getGender() != null) {
                hashCode += this.getGender().hashCode();
            }
            if (this.getOccupation() != null) {
                hashCode += this.getOccupation().hashCode();
            }
            if (this.getProductExperienceMonths() != null) {
                hashCode += this.getProductExperienceMonths().hashCode();
            }
            if (this.getProfessionalExperienceMonths() != null) {
                hashCode += this.getProfessionalExperienceMonths().hashCode();
            }
        }
        return hashCode;
    }

    public boolean matches(TestParticipant anotherParticipant) {
        boolean result = false;
        if (this.getId() != null && anotherParticipant.getId() != null
                && this.getId().longValue() == anotherParticipant.getId().longValue()) {
            result = true;
        } else if (StringUtils.equals(this.getUniqueId(), anotherParticipant.getUniqueId())
                && StringUtils.equals(this.getAgeRange(), anotherParticipant.getAgeRange())
                && Objects.equals(this.getAgeRangeId(), anotherParticipant.getAgeRangeId())
                && StringUtils.equals(this.getAssistiveTechnologyNeeds(),
                        anotherParticipant.getAssistiveTechnologyNeeds())
                && Objects.equals(this.getComputerExperienceMonths(),
                        anotherParticipant.getComputerExperienceMonths())
                && StringUtils.equals(this.getEducationTypeName(), anotherParticipant.getEducationTypeName())
                && Objects.equals(this.getEducationTypeId(), anotherParticipant.getEducationTypeId())
                && StringUtils.equals(this.getGender(), anotherParticipant.getGender())
                && StringUtils.equals(this.getOccupation(), anotherParticipant.getOccupation())
                && Objects.equals(this.getProductExperienceMonths(),
                        anotherParticipant.getProductExperienceMonths())
                && Objects.equals(this.getProfessionalExperienceMonths(),
                        anotherParticipant.getProfessionalExperienceMonths())) {
            result = true;
        }
        return result;
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

    public String getOccupation() {
        return occupation;
    }

    public void setOccupation(final String occupation) {
        this.occupation = occupation;
    }

    public Integer getProfessionalExperienceMonths() {
        return professionalExperienceMonths;
    }

    public void setProfessionalExperienceMonths(final String value) {
        if (!StringUtils.isEmpty(value)) {
            try {
                professionalExperienceMonths = Math.round(Float.valueOf(value));
            } catch (final Exception e) {
                LOGGER.error("can't parse " + value + " as a float.");
            }
        }
    }

    public Integer getComputerExperienceMonths() {
        return computerExperienceMonths;
    }

    public void setComputerExperienceMonths(final String value) {
        if (!StringUtils.isEmpty(value)) {
            try {
                computerExperienceMonths = Math.round(Float.valueOf(value));
            } catch (final Exception e) {
                LOGGER.error("can't parse " + value + " as a float.");
            }
        }
    }

    public Integer getProductExperienceMonths() {
        return productExperienceMonths;
    }

    public void setProductExperienceMonths(final String value) {
        if (!StringUtils.isEmpty(value)) {
            try {
                productExperienceMonths = Math.round(Float.valueOf(value));
            } catch (final Exception e) {
                LOGGER.error("can't parse " + value + " as a float.");
            }
        }
    }

    public String getAssistiveTechnologyNeeds() {
        return assistiveTechnologyNeeds;
    }

    public void setAssistiveTechnologyNeeds(final String assistiveTechnologyNeeds) {
        this.assistiveTechnologyNeeds = assistiveTechnologyNeeds;
    }

    public String getEducationTypeName() {
        return educationTypeName;
    }

    public void setEducationTypeName(final String educationTypeName) {
        this.educationTypeName = educationTypeName;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(final String uniqueId) {
        this.uniqueId = uniqueId;
    }

    public Long getAgeRangeId() {
        return ageRangeId;
    }

    public void setAgeRangeId(final Long ageRangeId) {
        this.ageRangeId = ageRangeId;
    }

    public String getAgeRange() {
        return ageRange;
    }

    public void setAgeRange(final String ageRange) {
        this.ageRange = ageRange;
    }

    public String getProfessionalExperienceMonthsStr() {
        return professionalExperienceMonthsStr;
    }

    public void setProfessionalExperienceMonthsStr(String professionalExperienceMonthsStr) {
        this.professionalExperienceMonthsStr = professionalExperienceMonthsStr;
    }

    public String getComputerExperienceMonthsStr() {
        return computerExperienceMonthsStr;
    }

    public void setComputerExperienceMonthsStr(String computerExperienceMonthsStr) {
        this.computerExperienceMonthsStr = computerExperienceMonthsStr;
    }

    public String getProductExperienceMonthsStr() {
        return productExperienceMonthsStr;
    }

    public void setProductExperienceMonthsStr(String productExperienceMonthsStr) {
        this.productExperienceMonthsStr = productExperienceMonthsStr;
    }

}
