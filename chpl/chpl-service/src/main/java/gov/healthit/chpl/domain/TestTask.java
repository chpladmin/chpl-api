package gov.healthit.chpl.domain;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.extern.log4j.Log4j2;

@JsonIgnoreProperties(ignoreUnknown = true)
@Builder(toBuilder = true)
@AllArgsConstructor
@Data
@Log4j2
public class TestTask implements Serializable {
    private static final long serialVersionUID = -3761135258451736516L;

    @Schema(description = "Test task internal ID")
    private Long id;

    @Schema(description = "An ONC-ACB designated identifier for an individual SED task and that must "
            + "be unique to a particular task. This is for internal use within an upload file only.")
    private String uniqueId;

    @Schema(description = "Brief description of task performed during SED/usability testing. "
            + "This is a string variable that does not take any restrictions on formatting or values.")
    private String description;

    @Schema(description = "Mean task success rate (in percentages). It is only takes positive decimal numbers.")
    private Float taskSuccessAverage;

    @JsonIgnore
    private String taskSuccessAverageStr;

    @Schema(description = "Standard deviation of the task success rate (in percentages). It is only "
            + "takes positive decimal numbers")
    private Float taskSuccessStddev;

    @JsonIgnore
    private String taskSuccessStddevStr;

    @Schema(description = "This variable indicates observed number of steps taken for the corresponding task. "
            + "It only takes positive integer values.")
    private Integer taskPathDeviationObserved;

    @JsonIgnore
    private String taskPathDeviationObservedStr;

    @Schema(description = "This variable indicates optimal number of steps for the corresponding task. "
            + "It only takes positive integer(i.e. no decimals) values.")
    private Integer taskPathDeviationOptimal;

    @JsonIgnore
    private String taskPathDeviationOptimalStr;

    @Schema(description = "Average time of completion for the corresponding task, in seconds. "
            + "It is only takes positive integer number values.")
    private Long taskTimeAvg;

    @JsonIgnore
    private String taskTimeAvgStr;

    @Schema(description = "Standard deviation for task time, in seconds. It only takes positive integer number values.")
    private Integer taskTimeStddev;

    @JsonIgnore
    private String taskTimeStddevStr;

    @Schema(description = "Observed number of time (in seconds) taken for the corresponding task. "
            + "It is only takes positive integer number values.")
    private Integer taskTimeDeviationObservedAvg;

    @JsonIgnore
    private String taskTimeDeviationObservedAvgStr;

    @Schema(description = "Optimal number of time (in seconds) taken for the corresponding task. "
            + "It only takes positive integer number values.")
    private Integer taskTimeDeviationOptimalAvg;

    @JsonIgnore
    private String taskTimeDeviationOptimalAvgStr;

    @Schema(description = "Mean task error rate (in percentages). It only takes positive decimal numbers.")
    private Float taskErrors;

    @JsonIgnore
    private String taskErrorsStr;

    @Schema(description = "Standard deviation of the task error rate (in percentages). "
            + "This takes only positive decimal numbers.")
    private Float taskErrorsStddev;

    @JsonIgnore
    private String taskErrorsStddevStr;

    @Schema(description = "This variable indicates the type of scale that was used to rate the "
            + "usability of the task. System Usability Scale is preferred. Likert Scale "
            + "is also accepted. If the scale type is System Usability Scale, only "
            + "positive integers between 1-100 are allowed. If the scale type is the "
            + "Likert scale, positive decimal numbers are allowed.")
    private String taskRatingScale;

    @Schema(description = "This variable indicates mean usability rating of the corresponding task, "
            + "based on the specified scale type. If the scale type is System Usability "
            + "Scale, only positive integers between 1-100 are allowed. If the scale "
            + "type is the Likert scale, positive decimal numbers are allowed.")
    private Float taskRating;

    @JsonIgnore
    private String taskRatingStr;

    @Schema(description = "Standard deviation of the mean usability rating of the corresponding "
            + "task, based on the specified scale type. It is only takes positive decimal numbers.")
    private Float taskRatingStddev;

    @JsonIgnore
    private String taskRatingStddevStr;

    @Schema(description = "The set of criteria within a listing to which this task is applied.")
    @Builder.Default
    private LinkedHashSet<CertificationCriterion> criteria = new LinkedHashSet<CertificationCriterion>();

    @Schema(description = "Participants in the test task.")
    @Builder.Default
    private LinkedHashSet<TestParticipant> testParticipants = new LinkedHashSet<TestParticipant>();

    public TestTask() {
        super();
        testParticipants = new LinkedHashSet<TestParticipant>();
        criteria = new LinkedHashSet<CertificationCriterion>();
    }

    @Override
    public boolean equals(Object other) {
        if (other == null || !(other instanceof TestTask)) {
            return false;
        }
        TestTask anotherTask = (TestTask) other;
        return matches(anotherTask);
    }

    @Override
    public int hashCode() {
        int hashCode = 0;
        if (this.getId() != null) {
            hashCode = this.getId().hashCode();
        } else {
            if (this.getDescription() != null) {
                hashCode += this.getDescription().hashCode();
            }
            if (this.getTaskErrors() != null) {
                hashCode += this.getTaskErrors().hashCode();
            }
            if (this.getTaskErrorsStddev() != null) {
                hashCode += this.getTaskErrorsStddev().hashCode();
            }
            if (this.getTaskPathDeviationObserved() != null) {
                hashCode += this.getTaskPathDeviationObserved().hashCode();
            }
            if (this.getTaskPathDeviationOptimal() != null) {
                hashCode += this.getTaskPathDeviationOptimal().hashCode();
            }
            if (this.getTaskRating() != null) {
                hashCode += this.getTaskRating().hashCode();
            }
            if (this.getTaskRatingScale() != null) {
                hashCode += this.getTaskRatingScale().hashCode();
            }
            if (this.getTaskRatingStddev() != null) {
                hashCode += this.getTaskRatingStddev().hashCode();
            }
            if (this.getTaskSuccessAverage() != null) {
                hashCode += this.getTaskSuccessAverage().hashCode();
            }
            if (this.getTaskSuccessStddev() != null) {
                hashCode += this.getTaskSuccessStddev().hashCode();
            }
            if (this.getTaskTimeAvg() != null) {
                hashCode += this.getTaskTimeAvg().hashCode();
            }
            if (this.getTaskTimeDeviationObservedAvg() != null) {
                hashCode += this.getTaskTimeDeviationObservedAvg().hashCode();
            }
            if (this.getTaskTimeDeviationOptimalAvg() != null) {
                hashCode += this.getTaskTimeDeviationOptimalAvg().hashCode();
            }
            if (this.getTaskTimeStddev() != null) {
                hashCode += this.getTaskTimeStddev().hashCode();
            }
        }
        return hashCode;
    }

    public boolean matches(TestTask anotherTask) {
        boolean result = false;
        if (this.getId() != null && anotherTask.getId() != null
                && this.getId().longValue() == anotherTask.getId().longValue()) {
            result = true;
        } else if (StringUtils.equals(this.getDescription(), anotherTask.getDescription())
                && Objects.equals(this.getTaskErrors(), anotherTask.getTaskErrors())
                && Objects.equals(this.getTaskErrorsStddev(), anotherTask.getTaskErrorsStddev())
                && Objects.equals(this.getTaskPathDeviationObserved(), anotherTask.getTaskPathDeviationObserved())
                && Objects.equals(this.getTaskPathDeviationOptimal(), anotherTask.getTaskPathDeviationOptimal())
                && Objects.equals(this.getTaskRating(), anotherTask.getTaskRating())
                && StringUtils.equals(this.getTaskRatingScale(), anotherTask.getTaskRatingScale())
                && Objects.equals(this.getTaskRatingStddev(), anotherTask.getTaskRatingStddev())
                && Objects.equals(this.getTaskSuccessAverage(), anotherTask.getTaskSuccessAverage())
                && Objects.equals(this.getTaskSuccessStddev(), anotherTask.getTaskSuccessStddev())
                && Objects.equals(this.getTaskTimeAvg(), anotherTask.getTaskTimeAvg())
                && Objects.equals(this.getTaskTimeDeviationObservedAvg(),
                        anotherTask.getTaskTimeDeviationObservedAvg())
                && Objects.equals(this.getTaskTimeDeviationOptimalAvg(),
                        anotherTask.getTaskTimeDeviationOptimalAvg())
                && Objects.equals(this.getTaskTimeStddev(), anotherTask.getTaskTimeStddev())) {
            result = doAllParticipantsMatch(anotherTask);
        }
        return result;
    }

    public boolean doAllParticipantsMatch(TestTask anotherTask) {
        if (CollectionUtils.isEmpty(anotherTask.getTestParticipants())
                && !CollectionUtils.isEmpty(this.getTestParticipants())) {
            return false;
        } else if (!CollectionUtils.isEmpty(anotherTask.getTestParticipants())
                && CollectionUtils.isEmpty(this.getTestParticipants())) {
            return false;
        } else if (CollectionUtils.isEmpty(anotherTask.getTestParticipants())
                && CollectionUtils.isEmpty(this.getTestParticipants())) {
            return true;
        } else {
            return CollectionUtils.isEmpty(subtractSets(this.getTestParticipants(), anotherTask.getTestParticipants()))
                    && CollectionUtils.isEmpty(subtractSets(anotherTask.getTestParticipants(), this.getTestParticipants()));
        }
    }

    private List<TestParticipant> subtractSets(Set<TestParticipant> listA, Set<TestParticipant> listB) {
        Predicate<TestParticipant> notInListB = certFromA -> !listB.stream()
                .anyMatch(cert -> certFromA.matches(cert));

        return listA.stream()
                .filter(notInListB)
                .collect(Collectors.toList());
    }

    public void setTaskSuccessAverage(String value) {
        if (!StringUtils.isEmpty(value)) {
            try {
                taskSuccessAverage = Float.valueOf(value);
            } catch (Exception e) {
                LOGGER.debug("can't parse " + value + " as a float.");
            }
        }
    }

    public void setTaskSuccessStddev(String value) {
        if (!StringUtils.isEmpty(value)) {
            try {
                taskSuccessStddev = Float.valueOf(value);
            } catch (Exception e) {
                LOGGER.debug("can't parse " + value + " as a float.");
            }
        }
    }

    public void setTaskPathDeviationObserved(String value) {
        if (!StringUtils.isEmpty(value)) {
            try {
                taskPathDeviationObserved = Math.round(Float.valueOf(value));
            } catch (Exception e) {
                LOGGER.debug("can't parse " + value + " as a float.");
            }
        }
    }

    public void setTaskPathDeviationOptimal(String value) {
        if (!StringUtils.isEmpty(value)) {
            try {
                taskPathDeviationOptimal = Math.round(Float.valueOf(value));
            } catch (Exception e) {
                LOGGER.debug("can't parse " + value + " as a float.");
            }
        }
    }

    public void setTaskTimeAvg(String value) {
        if (!StringUtils.isEmpty(value)) {
            try {
                taskTimeAvg = (long) Math.round(Float.valueOf(value));
            } catch (Exception e) {
                LOGGER.debug("can't parse " + value + " as a float.");
            }
        }
    }

    public void setTaskTimeStddev(String value) {
        if (!StringUtils.isEmpty(value)) {
            try {
                taskTimeStddev = Math.round(Float.valueOf(value));
            } catch (Exception e) {
                LOGGER.debug("can't parse " + value + " as a float.");
            }
        }
    }

    public void setTaskTimeDeviationObservedAvg(String value) {
        if (!StringUtils.isEmpty(value)) {
            try {
                taskTimeDeviationObservedAvg = Math.round(Float.valueOf(value));
            } catch (Exception e) {
                LOGGER.debug("can't parse " + value + " as a float.");
            }
        }
    }

    public void setTaskTimeDeviationOptimalAvg(String value) {
        if (!StringUtils.isEmpty(value)) {
            try {
                taskTimeDeviationOptimalAvg = Math.round(Float.valueOf(value));
            } catch (Exception e) {
                LOGGER.debug("can't parse " + value + " as a float.");
            }
        }
    }

    public void setTaskErrors(String value) {
        if (!StringUtils.isEmpty(value)) {
            try {
                taskErrors = Float.valueOf(value);
            } catch (Exception e) {
                LOGGER.debug("can't parse " + value + " as a float.");
            }
        }
    }

    public void setTaskErrorsStddev(String value) {
        if (!StringUtils.isEmpty(value)) {
            try {
                taskErrorsStddev = Float.valueOf(value);
            } catch (Exception e) {
                LOGGER.debug("can't parse " + value + " as a float.");
            }
        }
    }

    public void setTaskRating(String value) {
        if (!StringUtils.isEmpty(value)) {
            try {
                taskRating = Float.valueOf(value);
            } catch (Exception e) {
                LOGGER.debug("can't parse " + value + " as a float.");
            }
        }
    }

    public void setTaskRatingStddev(String value) {
        if (!StringUtils.isEmpty(value)) {
            try {
                taskRatingStddev = Float.valueOf(value);
            } catch (Exception e) {
                LOGGER.debug("can't parse " + value + " as a float or integer.");
            }
        }
    }
}
