package gov.healthit.chpl.domain;

import java.io.Serializable;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.annotation.JsonIgnore;

import gov.healthit.chpl.dto.TestParticipantDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder(toBuilder = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TestParticipant implements Serializable {
    private static final long serialVersionUID = -3771155258451736516L;
    private static final Logger LOGGER = LogManager.getLogger(TestParticipant.class);

    @Schema(description = "Participant internal ID")
    private Long id;

    @Schema(description = "An ONC-ACB designated identifier for an individual SED participant. This "
            + "variable is a string variable only applicable to 2015 Edition, but must "
            + "be unique to a particular participant. It is for internal use within an "
            + "upload file only.")
    private String uniqueId;

    @Schema(description = "Self-reported gender of the corresponding participant. This variable is "
            + "only applicable for 2015 Edition.",
            allowableValues = {"Male", "Female", "Unknown"})
    private String gender;

    @Schema(description = "Education internal ID")
    private Long educationTypeId;

    @Schema(description = "Highest education level attained by corresponding participant. This "
            + "variable is only applicable for 2015 Edition.",
            allowableValues = {"No high school degree", "High school graduate, diploma or the equivalent (for example: GED)",
            "Some college credit, no degree", "Trade/technical/vocational training", "Associate degre", "Bachelor's degree",
            "Master's degree",  "Doctorate degree (e.g., MD,DNP, DMD, PhD)"})
    private String educationTypeName;

    @Schema(description = "Age range internal ID")
    private Long ageRangeId;

    @Schema(description = "The age range for the corresponding participant.",
            allowableValues = {"0-9", "10-19", "20-29", "30-39", "40-49", "50-59", "60-69", "70-79", "80-89", "90-99", "100+"})
    private String ageRange;

    @Schema(description = "This variable illustrates occupation or role of corresponding "
            + "participant. It is only applicable to 2015 Edition and a string variable "
            + "that does not take any restrictions on formatting or values.")
    private String occupation;

    @Schema(description = "Professional experience of the corresponding participant, in number of "
            + "months. This variable is only applicable to 2015 Edition, and takes only "
            + "positive integers (i.e. no decimals) values.")
    private Integer professionalExperienceMonths;

    @JsonIgnore
    private String professionalExperienceMonthsStr;

    @Schema(description = "The corresponding participant's experience with computers (in general), "
            + " in number of months. It is only applicable for 2015 Edition and takes "
            + "only positive integers (i.e. no decimals).")
    private Integer computerExperienceMonths;

    @JsonIgnore
    private String computerExperienceMonthsStr;

    @Schema(description = "The corresponding participant's experience with the certified product/ "
            + "health IT capabilities (SED criterion) being tested, in number of months. "
            + "This variable is applicable to 2015 Edition, and only takes positive "
            + "integers (i.e. no decimals are allowed) values.")
    private Integer productExperienceMonths;

    @JsonIgnore
    private String productExperienceMonthsStr;

    @Schema(description = "Any assistive technology needs as identified by the corresponding "
            + "participant. This variable is a string variable that does not take any "
            + "restrictions on formatting or values and is only applicable for 2015 "
            + "Edition.")
    private String assistiveTechnologyNeeds;

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

    public void setProfessionalExperienceMonths(final String value) {
        if (!StringUtils.isEmpty(value)) {
            try {
                professionalExperienceMonths = Math.round(Float.valueOf(value));
            } catch (final Exception e) {
                LOGGER.error("can't parse " + value + " as a float.");
            }
        }
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

    public void setProductExperienceMonths(final String value) {
        if (!StringUtils.isEmpty(value)) {
            try {
                productExperienceMonths = Math.round(Float.valueOf(value));
            } catch (final Exception e) {
                LOGGER.error("can't parse " + value + " as a float.");
            }
        }
    }
}
