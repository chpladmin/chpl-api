package gov.healthit.chpl.domain;

import java.io.Serializable;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;

import gov.healthit.chpl.api.deprecatedUsage.DeprecatedResponseField;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Builder(toBuilder = true)
@Data
@Log4j2
@NoArgsConstructor
@AllArgsConstructor
public class TestParticipant implements Serializable {
    private static final long serialVersionUID = -3771155258451736516L;

    @Schema(description = "Participant internal ID")
    private Long id;

    @Schema(description = "An ONC-ACB designated identifier for an individual SED participant. "
            + "The value must be unique to a particular participant. "
            + "It is for internal use within an upload file only.")
    private String uniqueId;

    @Schema(description = "Self-reported gender of the corresponding participant.",
            allowableValues = {"Male", "Female", "Unknown"})
    private String gender;

    @Schema(description = "The education level for the corresponding participant.")
    @Builder.Default
    private TestParticipantEducation educationType = new TestParticipantEducation();

    @Deprecated
    @DeprecatedResponseField(message = "Please use education.id.", removalDate = "2024-09-01")
    @Schema(description = "Education internal ID")
    private Long educationTypeId;

    @Deprecated
    @DeprecatedResponseField(message = "Please use education.name.", removalDate = "2024-09-01")
    @Schema(description = "Highest education level attained by corresponding participant.",
            allowableValues = {"No high school degree", "High school graduate, diploma or the equivalent (for example: GED)",
            "Some college credit, no degree", "Trade/technical/vocational training", "Associate degre", "Bachelor's degree",
            "Master's degree",  "Doctorate degree (e.g., MD,DNP, DMD, PhD)"})
    private String educationTypeName;

    @Schema(description = "The age range for the corresponding participant.")
    @Builder.Default
    private TestParticipantAge age = new TestParticipantAge();

    @Deprecated
    @DeprecatedResponseField(message = "Please use age.name.", removalDate = "2024-09-01")
    @Schema(description = "Age range internal ID")
    private Long ageRangeId;

    @Deprecated
    @DeprecatedResponseField(message = "Please use age.name.", removalDate = "2024-09-01")
    @Schema(description = "The age range for the corresponding participant.",
            allowableValues = {"0-9", "10-19", "20-29", "30-39", "40-49", "50-59", "60-69", "70-79", "80-89", "90-99", "100+"})
    private String ageRange;

    @Schema(description = "This variable illustrates occupation or role of corresponding participant. "
            + "It is a string variable that does not take any restrictions on formatting or values.")
    private String occupation;

    @Schema(description = "Professional experience of the corresponding participant, in number of "
            + "months. This variable only takes positive integers (i.e. no decimals) values.")
    private Integer professionalExperienceMonths;

    @JsonIgnore
    private String professionalExperienceMonthsStr;

    @Schema(description = "The corresponding participant's experience with computers (in general), "
            + "in number of months. It only takes positive integers (i.e. no decimals).")
    private Integer computerExperienceMonths;

    @JsonIgnore
    private String computerExperienceMonthsStr;

    @Schema(description = "The corresponding participant's experience with the certified product/health "
            + "IT capabilities (SED criterion) being tested, in number of months. "
            + "This variable only takes positive integers (i.e. no decimals are allowed) values.")
    private Integer productExperienceMonths;

    @JsonIgnore
    private String productExperienceMonthsStr;

    @Schema(description = "Any assistive technology needs as identified by the corresponding "
            + "participant. This variable is a string variable that does not take any "
            + "restrictions on formatting or values.")
    private String assistiveTechnologyNeeds;

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
            if (this.getAge() != null && this.getAge().getId() != null) {
                hashCode += this.getAge().getId().hashCode();
            }
            if (this.getAge() != null && this.getAge().getName() != null) {
                hashCode += this.getAge().getName().hashCode();
            }
            if (this.getAssistiveTechnologyNeeds() != null) {
                hashCode += this.getAssistiveTechnologyNeeds().hashCode();
            }
            if (this.getComputerExperienceMonths() != null) {
                hashCode += this.getComputerExperienceMonths().hashCode();
            }
            if (this.getEducationType() != null && this.getEducationType().getId() != null) {
                hashCode += this.getEducationType().getId().hashCode();
            }
            if (this.getEducationType() != null && this.getEducationType().getName() != null) {
                hashCode += this.getEducationType().getName().hashCode();
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
                && Objects.equals(this.getAge().getId(), anotherParticipant.getAge().getId())
                && StringUtils.equals(this.getAge().getName(), anotherParticipant.getAge().getName())
                && StringUtils.equals(this.getAssistiveTechnologyNeeds(),
                        anotherParticipant.getAssistiveTechnologyNeeds())
                && Objects.equals(this.getComputerExperienceMonths(),
                        anotherParticipant.getComputerExperienceMonths())
                && Objects.equals(this.getEducationType().getId(), anotherParticipant.getEducationType().getId())
                && StringUtils.equals(this.getEducationType().getName(), anotherParticipant.getEducationType().getName())
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

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static final class TestParticipantAge {
        private Long id;
        private String name;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static final class TestParticipantEducation {
        private Long id;
        private String name;
    }
}
