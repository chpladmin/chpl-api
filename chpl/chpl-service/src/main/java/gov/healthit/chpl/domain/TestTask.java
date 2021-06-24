package gov.healthit.chpl.domain;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import gov.healthit.chpl.dto.CertificationResultTestTaskDTO;
import gov.healthit.chpl.dto.TestParticipantDTO;
import gov.healthit.chpl.dto.TestTaskDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Singular;
import lombok.ToString;

/**
 * A task used for SED testing for a given criteria.
 */
@XmlType(namespace = "http://chpl.healthit.gov/listings")
@XmlAccessorType(XmlAccessType.FIELD)
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder(toBuilder = true)
@AllArgsConstructor
@ToString
public class TestTask implements Serializable {
    private static final long serialVersionUID = -3761135258451736516L;

    private static final Logger LOGGER = LogManager.getLogger(TestTask.class);

    /**
     * Test task internal ID
     */
    @XmlElement(required = true)
    private Long id;

    /**
     * An ONC-ACB designated identifier for an individual SED task and that must
     * be unique to a particular task. This variable is only applicable to 2015
     * Edition and for internal use within an upload file only.
     */
    @XmlTransient
    private String uniqueId;

    /**
     * Brief description of task performed during SED/usability testing. This
     * variable is only applicable to 2015 Edition, and a string variable that
     * does not take any restrictions on formatting or values.
     */
    @XmlElement(required = true)
    private String description;

    /**
     * Mean task success rate (in percentages). It is only applicable to 2015
     * Edition, and takes only positive decimal numbers.
     */
    @XmlElement(required = true)
    private Float taskSuccessAverage;

    @XmlTransient
    @JsonIgnore
    private String taskSuccessAverageStr;

    /**
     * Standard deviation of the task success rate (in percentages). It is only
     * applicable to 2015 Edition, and takes only positive decimal numbers.
     */
    @XmlElement(required = true)
    private Float taskSuccessStddev;

    @XmlTransient
    @JsonIgnore
    private String taskSuccessStddevStr;

    /**
     * This variable indicates observed number of steps taken for the
     * corresponding task. It is applicable to 2015 Edition, and takes only
     * positive integer values.
     */
    @XmlElement(required = true)
    private Integer taskPathDeviationObserved;

    @XmlTransient
    @JsonIgnore
    private String taskPathDeviationObservedStr;

    /**
     * This variable indicates optimal number of steps for the corresponding
     * task. It is only applicable to 2015 Edition, and only takes positive
     * integer(i.e. no decimals) values.
     */
    @XmlElement(required = true)
    private Integer taskPathDeviationOptimal;

    @XmlTransient
    @JsonIgnore
    private String taskPathDeviationOptimalStr;

    /**
     * Average time of completion for the corresponding task, in seconds. It is
     * only applicable to 2015 Edition, and takes only positive integer number
     * values.
     */
    @XmlElement(required = true)
    private Long taskTimeAvg;

    @XmlTransient
    @JsonIgnore
    private String taskTimeAvgStr;

    /**
     * Standard deviation for task time, in seconds. It is only applicable to
     * 2015 Edition, and takes only positive integer number values.
     */
    @XmlElement(required = true)
    private Integer taskTimeStddev;

    @XmlTransient
    @JsonIgnore
    private String taskTimeStddevStr;

    /**
     * Observed number of time (in seconds) taken for the corresponding task. It
     * is only applicable to 2015 Edition, and takes only positive integer
     * number values.
     */
    @XmlElement(required = true)
    private Integer taskTimeDeviationObservedAvg;

    @XmlTransient
    @JsonIgnore
    private String taskTimeDeviationObservedAvgStr;

    /**
     * Optimal number of time (in seconds) taken for the corresponding task. It
     * is only applicable to 2015 Edition, and takes only positive integer
     * number values.
     */
    @XmlElement(required = true)
    private Integer taskTimeDeviationOptimalAvg;

    @XmlTransient
    @JsonIgnore
    private String taskTimeDeviationOptimalAvgStr;

    /**
     * Mean task error rate (in percentages). It is only applicable to 2015
     * Edition, and takes only positive decimal numbers.
     */
    @XmlElement(required = true)
    private Float taskErrors;

    @XmlTransient
    @JsonIgnore
    private String taskErrorsStr;

    /**
     * Standard deviation of the task error rate (in percentages). This variable
     * is only applicable to 2015 Edition, and takes only positive decimal
     * numbers.
     */
    @XmlElement(required = true)
    private Float taskErrorsStddev;

    @XmlTransient
    @JsonIgnore
    private String taskErrorsStddevStr;

    /**
     * This variable indicates the type of scale that was used to rate the
     * usability of the task. System Usability Scale is preferred. Likert Scale
     * is also accepted. If the scale type is System Usability Scale, only
     * positive integers between 1-100 are allowed. If the scale type is the
     * Likert scale, positive decimal numbers are allowed. It is only applicable
     * to 2015 Edition.
     */
    @XmlElement(required = true)
    private String taskRatingScale;

    /**
     * This variable indicates mean usability rating of the corresponding task,
     * based on the specified scale type. If the scale type is System Usability
     * Scale, only positive integers between 1-100 are allowed. If the scale
     * type is the Likert scale, positive decimal numbers are allowed. It is
     * only applicable to 2015 Edition.
     */
    @XmlElement(required = true)
    private Float taskRating;

    @XmlTransient
    @JsonIgnore
    private String taskRatingStr;

    /**
     * Standard deviation of the mean usability rating of the corresponding
     * task, based on the specified scale type. It is only applicable to 2015
     * Edition, and takes only positive decimal numbers.
     */
    @XmlElement(required = true)
    private Float taskRatingStddev;

    @XmlTransient
    @JsonIgnore
    private String taskRatingStddevStr;

    /**
     * The set of criteria within a listing to which this task is applied.
     */
    @Singular("criterion")
    @XmlElementWrapper(name = "criteriaList", nillable = true, required = false)
    @XmlElement(name = "criteria")
    private Set<CertificationCriterion> criteria;

    /**
     * Participants in the test task.
     */
    @XmlElementWrapper(name = "participants", required = true)
    @XmlElement(name = "participant")
    @Singular
    private Set<TestParticipant> testParticipants;

    public TestTask() {
        super();
        testParticipants = new HashSet<TestParticipant>();
        criteria = new HashSet<CertificationCriterion>();
    }

    public TestTask(final TestTaskDTO dto) {
        this();
        this.id = dto.getId();
        this.description = dto.getDescription();
        this.taskSuccessAverage = dto.getTaskSuccessAverage();
        this.taskSuccessStddev = dto.getTaskSuccessStddev();
        this.taskPathDeviationObserved = dto.getTaskPathDeviationObserved();
        this.taskPathDeviationOptimal = dto.getTaskPathDeviationOptimal();
        this.taskTimeAvg = dto.getTaskTimeAvg();
        this.taskTimeStddev = dto.getTaskTimeStddev();
        this.taskTimeDeviationObservedAvg = dto.getTaskTimeDeviationObservedAvg();
        this.taskTimeDeviationOptimalAvg = dto.getTaskTimeDeviationOptimalAvg();
        this.taskErrors = dto.getTaskErrors();
        this.taskErrorsStddev = dto.getTaskErrorsStddev();
        this.taskRatingScale = dto.getTaskRatingScale();
        this.taskRating = dto.getTaskRating();
        this.taskRatingStddev = dto.getTaskRatingStddev();
        if (dto.getParticipants() != null && dto.getParticipants().size() > 0) {
            for (TestParticipantDTO participantDto : dto.getParticipants()) {
                this.testParticipants.add(new TestParticipant(participantDto));
            }
        }
    }

    public TestTask(final CertificationResultTestTaskDTO dto) {
        this(dto.getTestTask());
        this.id = dto.getTestTaskId();
    }

    @Override
    public boolean equals(final Object other) {
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

    public boolean matches(final TestTask anotherTask) {
        boolean result = false;
        if (this.getId() != null && anotherTask.getId() != null
                && this.getId().longValue() == anotherTask.getId().longValue()) {
            result = true;
        } else if (StringUtils.equals(this.getDescription(), anotherTask.getDescription())
                && ObjectUtils.equals(this.getTaskErrors(), anotherTask.getTaskErrors())
                && ObjectUtils.equals(this.getTaskErrorsStddev(), anotherTask.getTaskErrorsStddev())
                && ObjectUtils.equals(this.getTaskPathDeviationObserved(), anotherTask.getTaskPathDeviationObserved())
                && ObjectUtils.equals(this.getTaskPathDeviationOptimal(), anotherTask.getTaskPathDeviationOptimal())
                && ObjectUtils.equals(this.getTaskRating(), anotherTask.getTaskRating())
                && StringUtils.equals(this.getTaskRatingScale(), anotherTask.getTaskRatingScale())
                && ObjectUtils.equals(this.getTaskRatingStddev(), anotherTask.getTaskRatingStddev())
                && ObjectUtils.equals(this.getTaskSuccessAverage(), anotherTask.getTaskSuccessAverage())
                && ObjectUtils.equals(this.getTaskSuccessStddev(), anotherTask.getTaskSuccessStddev())
                && ObjectUtils.equals(this.getTaskTimeAvg(), anotherTask.getTaskTimeAvg())
                && ObjectUtils.equals(this.getTaskTimeDeviationObservedAvg(),
                        anotherTask.getTaskTimeDeviationObservedAvg())
                && ObjectUtils.equals(this.getTaskTimeDeviationOptimalAvg(),
                        anotherTask.getTaskTimeDeviationOptimalAvg())
                && ObjectUtils.equals(this.getTaskTimeStddev(), anotherTask.getTaskTimeStddev())) {
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

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public Float getTaskSuccessAverage() {
        return taskSuccessAverage;
    }

    public void setTaskSuccessAverage(final String value) {
        if (!StringUtils.isEmpty(value)) {
            try {
                taskSuccessAverage = Float.valueOf(value);
            } catch (final Exception e) {
                LOGGER.error("can't parse " + value + " as a float.");
            }
        }
    }

    public Float getTaskSuccessStddev() {
        return taskSuccessStddev;
    }

    public void setTaskSuccessStddev(final String value) {
        if (!StringUtils.isEmpty(value)) {
            try {
                taskSuccessStddev = Float.valueOf(value);
            } catch (final Exception e) {
                LOGGER.error("can't parse " + value + " as a float.");
            }
        }
    }

    public Integer getTaskPathDeviationObserved() {
        return taskPathDeviationObserved;
    }

    public void setTaskPathDeviationObserved(final String value) {
        if (!StringUtils.isEmpty(value)) {
            try {
                taskPathDeviationObserved = Math.round(Float.valueOf(value));
            } catch (final Exception e) {
                LOGGER.error("can't parse " + value + " as a float.");
            }
        }
    }

    public Integer getTaskPathDeviationOptimal() {
        return taskPathDeviationOptimal;
    }

    public void setTaskPathDeviationOptimal(final String value) {
        if (!StringUtils.isEmpty(value)) {
            try {
                taskPathDeviationOptimal = Math.round(Float.valueOf(value));
            } catch (final Exception e) {
                LOGGER.error("can't parse " + value + " as a float.");
            }
        }
    }

    public Long getTaskTimeAvg() {
        return taskTimeAvg;
    }

    public void setTaskTimeAvg(final String value) {
        if (!StringUtils.isEmpty(value)) {
            try {
                taskTimeAvg = (long) Math.round(Float.valueOf(value));
            } catch (final Exception e) {
                LOGGER.error("can't parse " + value + " as a float.");
            }
        }
    }

    public Integer getTaskTimeStddev() {
        return taskTimeStddev;
    }

    public void setTaskTimeStddev(final String value) {
        if (!StringUtils.isEmpty(value)) {
            try {
                taskTimeStddev = Math.round(Float.valueOf(value));
            } catch (final Exception e) {
                LOGGER.error("can't parse " + value + " as a float.");
            }
        }
    }

    public Integer getTaskTimeDeviationObservedAvg() {
        return taskTimeDeviationObservedAvg;
    }

    public void setTaskTimeDeviationObservedAvg(final String value) {
        if (!StringUtils.isEmpty(value)) {
            try {
                taskTimeDeviationObservedAvg = Math.round(Float.valueOf(value));
            } catch (final Exception e) {
                LOGGER.error("can't parse " + value + " as a float.");
            }
        }
    }

    public Integer getTaskTimeDeviationOptimalAvg() {
        return taskTimeDeviationOptimalAvg;
    }

    public void setTaskTimeDeviationOptimalAvg(final String value) {
        if (!StringUtils.isEmpty(value)) {
            try {
                taskTimeDeviationOptimalAvg = Math.round(Float.valueOf(value));
            } catch (final Exception e) {
                LOGGER.error("can't parse " + value + " as a float.");
            }
        }
    }

    public Float getTaskErrors() {
        return taskErrors;
    }

    public void setTaskErrors(final String value) {
        if (!StringUtils.isEmpty(value)) {
            try {
                taskErrors = Float.valueOf(value);
            } catch (final Exception e) {
                LOGGER.error("can't parse " + value + " as a float.");
            }
        }
    }

    public Float getTaskErrorsStddev() {
        return taskErrorsStddev;
    }

    public void setTaskErrorsStddev(final String value) {
        if (!StringUtils.isEmpty(value)) {
            try {
                taskErrorsStddev = Float.valueOf(value);
            } catch (final Exception e) {
                LOGGER.error("can't parse " + value + " as a float.");
            }
        }
    }

    public String getTaskRatingScale() {
        return taskRatingScale;
    }

    public void setTaskRatingScale(final String taskRatingScale) {
        this.taskRatingScale = taskRatingScale;
    }

    public Float getTaskRating() {
        return taskRating;
    }

    public void setTaskRating(final String value) {
        if (!StringUtils.isEmpty(value)) {
            try {
                taskRating = Float.valueOf(value);
            } catch (final Exception e) {
                LOGGER.error("can't parse " + value + " as a float.");
            }
        }
    }

    public Set<TestParticipant> getTestParticipants() {
        return testParticipants;
    }

    public void setTestParticipants(final Set<TestParticipant> testParticipants) {
        this.testParticipants = testParticipants;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(final String uniqueId) {
        this.uniqueId = uniqueId;
    }

    public Float getTaskRatingStddev() {
        return taskRatingStddev;
    }

    public void setTaskRatingStddev(final String value) {
        if (!StringUtils.isEmpty(value)) {
            try {
                taskRatingStddev = Float.valueOf(value);
            } catch (final Exception e) {
                LOGGER.error("can't parse " + value + " as a float or integer.");
            }
        }
    }

    public Set<CertificationCriterion> getCriteria() {
        return criteria;
    }

    public void setCriteria(final Set<CertificationCriterion> criteria) {
        this.criteria = criteria;
    }

    public String getTaskSuccessAverageStr() {
        return taskSuccessAverageStr;
    }

    public void setTaskSuccessAverageStr(String taskSuccessAverageStr) {
        this.taskSuccessAverageStr = taskSuccessAverageStr;
    }

    public String getTaskSuccessStddevStr() {
        return taskSuccessStddevStr;
    }

    public void setTaskSuccessStddevStr(String taskSuccessStddevStr) {
        this.taskSuccessStddevStr = taskSuccessStddevStr;
    }

    public String getTaskPathDeviationObservedStr() {
        return taskPathDeviationObservedStr;
    }

    public void setTaskPathDeviationObservedStr(String taskPathDeviationObservedStr) {
        this.taskPathDeviationObservedStr = taskPathDeviationObservedStr;
    }

    public String getTaskPathDeviationOptimalStr() {
        return taskPathDeviationOptimalStr;
    }

    public void setTaskPathDeviationOptimalStr(String taskPathDeviationOptimalStr) {
        this.taskPathDeviationOptimalStr = taskPathDeviationOptimalStr;
    }

    public String getTaskTimeAvgStr() {
        return taskTimeAvgStr;
    }

    public void setTaskTimeAvgStr(String taskTimeAvgStr) {
        this.taskTimeAvgStr = taskTimeAvgStr;
    }

    public String getTaskTimeStddevStr() {
        return taskTimeStddevStr;
    }

    public void setTaskTimeStddevStr(String taskTimeStddevStr) {
        this.taskTimeStddevStr = taskTimeStddevStr;
    }

    public String getTaskTimeDeviationObservedAvgStr() {
        return taskTimeDeviationObservedAvgStr;
    }

    public void setTaskTimeDeviationObservedAvgStr(String taskTimeDeviationObservedAvgStr) {
        this.taskTimeDeviationObservedAvgStr = taskTimeDeviationObservedAvgStr;
    }

    public String getTaskTimeDeviationOptimalAvgStr() {
        return taskTimeDeviationOptimalAvgStr;
    }

    public void setTaskTimeDeviationOptimalAvgStr(String taskTimeDeviationOptimalAvgStr) {
        this.taskTimeDeviationOptimalAvgStr = taskTimeDeviationOptimalAvgStr;
    }

    public String getTaskErrorsStr() {
        return taskErrorsStr;
    }

    public void setTaskErrorsStr(String taskErrorsStr) {
        this.taskErrorsStr = taskErrorsStr;
    }

    public String getTaskErrorsStddevStr() {
        return taskErrorsStddevStr;
    }

    public void setTaskErrorsStddevStr(String taskErrorsStddevStr) {
        this.taskErrorsStddevStr = taskErrorsStddevStr;
    }

    public String getTaskRatingStddevStr() {
        return taskRatingStddevStr;
    }

    public void setTaskRatingStddevStr(String taskRatingStddevStr) {
        this.taskRatingStddevStr = taskRatingStddevStr;
    }
}
