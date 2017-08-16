package gov.healthit.chpl.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.StringUtils;

import gov.healthit.chpl.dto.CertificationResultTestTaskDTO;
import gov.healthit.chpl.dto.TestParticipantDTO;
import gov.healthit.chpl.dto.TestTaskDTO;

/**
 * A task used for SED testing for a given criteria
 */
@XmlType(namespace = "http://chpl.healthit.gov/listings")
@XmlAccessorType(XmlAccessType.FIELD)
public class TestTask implements Serializable {
	private static final long serialVersionUID = -3761135258451736516L;
	
	private static final Logger logger = LogManager.getLogger(TestTask.class);

	/**
	 * Test task internal ID
	 */
	@XmlElement(required = true)
	private Long id;
	
	/**
	 * An ONC-ACB designated identifier for an individual SED task and 
	 * that must be unique to a particular task. 
	 * This variable is only applicable to 2015 Edition and for internal 
	 * use within an upload file only.
	 */
	@XmlElement(required = false, nillable=true)
	private String uniqueId;
	
	/**
	 * Brief description of task performed during SED/usability testing. This variable is only applicable to 2015 Edition, and a string variable that does not take any restrictions on formatting or values.
	 */
	@XmlElement(required = false, nillable=true)
	private String description;
	
	/**
	 * Mean task success rate (in percentages). 
	 * It is only applicable to 2015 Edition, and takes only positive decimal numbers.
	 */
	@XmlElement(required = false, nillable=true)
	private Float taskSuccessAverage;
	
	/**
	 * Standard deviation of the task success rate (in percentages). 
	 * It is only applicable to 2015 Edition, and takes only positive decimal numbers.
	 */
	@XmlElement(required = false, nillable=true)
	private Float taskSuccessStddev;
	
	/**
	 * This variable indicates observed number of steps taken for the corresponding task. It is applicable to 2015 Edition, and takes only positive integer values. 
	 */
	@XmlElement(required = false, nillable=true)
	private Integer taskPathDeviationObserved;
	
	/**
	 * This variable indicates optimal number of steps for the corresponding task. It is only applicable to 2015 Edition, and only takes positive integer(i.e. no decimals) values. 
	 */
	@XmlElement(required = false, nillable=true)
	private Integer taskPathDeviationOptimal;
	
	/**
	 * Average time of completion for the corresponding task, in seconds. 
	 * It is only applicable to 2015 Edition, and takes only positive integer number values.
	 */
	@XmlElement(required = false, nillable=true)
	private Long taskTimeAvg;
	
	/**
	 * Standard deviation for task time, in seconds. It is only applicable to 2015 Edition, and takes only positive integer number values.
	 */
	@XmlElement(required = false, nillable=true)
	private Integer taskTimeStddev;
	
	/**
	 * Observed number of time (in seconds) taken for the corresponding task.  
	 * It is only applicable to 2015 Edition, and takes only positive integer number values.
	 */
	@XmlElement(required = false, nillable=true)
	private Integer taskTimeDeviationObservedAvg;
	
	/**
	 * Optimal number of time (in seconds) taken for the corresponding task.  
	 * It is only applicable to 2015 Edition, and takes only positive integer number values.
	 */
	@XmlElement(required = false, nillable=true)
	private Integer taskTimeDeviationOptimalAvg;
	
	/**
	 * Mean task error rate (in percentages). It is only applicable to 2015 Edition, and takes only positive decimal numbers.
	 */
	@XmlElement(required = false, nillable=true)
	private Float taskErrors;
	
	/**
	 * Standard deviation of the task error rate (in percentages). This variable is only applicable to 2015 Edition, and takes only positive decimal numbers.
	 */
	@XmlElement(required = false, nillable=true)
	private Float taskErrorsStddev;
	
	/**
	 * This variable indicates the type of scale that was used to rate the usability of the task. 
	 * System Usability Scale is preferred. Likert Scale is also accepted.  
	 * If the scale type is System Usability Scale, only positive integers between 1-100 are allowed. 
	 * If the scale type is the Likert scale, positive decimal numbers are allowed. 
	 * It is only applicable to 2015 Edition.
	 */
	@XmlElement(required = false, nillable=true)
	private String taskRatingScale;
	
	/**
	 * This variable indicates mean usability rating of the corresponding task, 
	 * based on the specified scale type. If the scale type is System Usability Scale, 
	 * only positive integers between 1-100 are allowed. If the scale type is the 
	 * Likert scale, positive decimal numbers are allowed. It is only applicable to 2015 Edition.
	 */
	@XmlElement(required = false, nillable=true)
	private Float taskRating;
	
	/**
	 * Standard deviation of the mean usability rating of the corresponding task, 
	 * based on the specified scale type. 
	 * It is only applicable to 2015 Edition, and takes only positive decimal numbers. 
	 */
	@XmlElement(required = false, nillable=true)
	private Float taskRatingStddev;
	
	/**
	 * The set of criteria within a listing to which this task is applied.
	 */
	@XmlElement(required = false, nillable = true)
	private Set<CertificationCriterion> criteria;
	
	/**
	 * Participants in the test task.
	 */
	@XmlElementWrapper(name = "participants", nillable = true, required = false)
	@XmlElement(name = "participant")
	private List<TestParticipant> testParticipants;
	
	
	public TestTask() {
		super();
		testParticipants = new ArrayList<TestParticipant>();
		criteria = new HashSet<CertificationCriterion>();
	}
	
	public TestTask(TestTaskDTO dto) {
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
		if(dto.getParticipants() != null && dto.getParticipants().size() > 0) {
			for(TestParticipantDTO participantDto : dto.getParticipants()) {
				this.testParticipants.add(new TestParticipant(participantDto));
			}
		}
	}
	
	public TestTask(CertificationResultTestTaskDTO dto) {
		this(dto.getTestTask());
		this.id = dto.getTestTaskId();
	}
	
	@Override
	public boolean equals(Object other) {
		if(other == null || !(other instanceof TestTask)) {
			return false;
		}
		TestTask anotherTask = (TestTask) other;
		return matches(anotherTask);
	}
	
	@Override
	public int hashCode() {
		return this.getId().hashCode();
	}
	
	public boolean matches(TestTask anotherTask) {
		boolean result = false;
		if(this.getId() != null && anotherTask.getId() != null && 
				this.getId().longValue() == anotherTask.getId().longValue()) {
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
	
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Float getTaskSuccessAverage() {
		return taskSuccessAverage;
	}

	public void setTaskSuccessAverage(Float taskSuccessAverage) {
		this.taskSuccessAverage = taskSuccessAverage;
	}

	public Float getTaskSuccessStddev() {
		return taskSuccessStddev;
	}

	public void setTaskSuccessStddev(Float taskSuccessStddev) {
		this.taskSuccessStddev = taskSuccessStddev;
	}

	public Integer getTaskPathDeviationObserved() {
		return taskPathDeviationObserved;
	}

	public void setTaskPathDeviationObserved(String value) {
		if(!StringUtils.isEmpty(value)) {
	        try {
	        	taskPathDeviationObserved = Math.round(new Float(value));
	        } catch (NumberFormatException e) {
	        	logger.error("can't parse " + value + " as a float or integer.");
	        }
		}
    }

	public Integer getTaskPathDeviationOptimal() {
		return taskPathDeviationOptimal;
	}
	
	public void setTaskPathDeviationOptimal(String value) {
		if(!StringUtils.isEmpty(value)) {
	        try {
	        	taskPathDeviationOptimal = Math.round(new Float(value));
	        } catch (NumberFormatException e) {
	        	logger.error("can't parse " + value + " as a float or integer.");
	        }
		}
    }
	
	public Long getTaskTimeAvg() {
		return taskTimeAvg;
	}
	
	public void setTaskTimeAvg(String value) {
		if(!StringUtils.isEmpty(value)) {
	        try {
	        	taskTimeAvg = new Long(Math.round(new Float(value)));
	        } catch (NumberFormatException e) {
	        	logger.error("can't parse " + value + " as a float or integer.");
	        }
		}
    }
	
	public Integer getTaskTimeStddev() {
		return taskTimeStddev;
	}

	public void setTaskTimeStddev(String value) {
		if(!StringUtils.isEmpty(value)) {
	        try {
	        	taskTimeStddev = Math.round(new Float(value));
	        } catch (NumberFormatException e) {
	        	logger.error("can't parse " + value + " as a float or integer.");
	        }
		}
    }
	
	public Integer getTaskTimeDeviationObservedAvg() {
		return taskTimeDeviationObservedAvg;
	}
	
	public void setTaskTimeDeviationObservedAvg(String value) {
		if(!StringUtils.isEmpty(value)) {
	        try {
	        	taskTimeDeviationObservedAvg = Math.round(new Float(value));
	        } catch (NumberFormatException e) {
	        	logger.error("can't parse " + value + " as a float or integer.");
	        }
		}
    }
	
	public Integer getTaskTimeDeviationOptimalAvg() {
		return taskTimeDeviationOptimalAvg;
	}

	public void setTaskTimeDeviationOptimalAvg(String value) {
		if(!StringUtils.isEmpty(value)) {
			try {
	        	taskTimeDeviationOptimalAvg = Math.round(new Float(value));
	        } catch (NumberFormatException e) {
	        	logger.error("can't parse " + value + " as a float or integer.");
	        }	
		}
    }
	
	public Float getTaskErrors() {
		return taskErrors;
	}

	public void setTaskErrors(Float taskErrors) {
		this.taskErrors = taskErrors;
	}

	public Float getTaskErrorsStddev() {
		return taskErrorsStddev;
	}

	public void setTaskErrorsStddev(Float taskErrorsStddev) {
		this.taskErrorsStddev = taskErrorsStddev;
	}

	public String getTaskRatingScale() {
		return taskRatingScale;
	}

	public void setTaskRatingScale(String taskRatingScale) {
		this.taskRatingScale = taskRatingScale;
	}

	public Float getTaskRating() {
		return taskRating;
	}

	public void setTaskRating(Float taskRating) {
		this.taskRating = taskRating;
	}

	public List<TestParticipant> getTestParticipants() {
		return testParticipants;
	}

	public void setTestParticipants(List<TestParticipant> testParticipants) {
		this.testParticipants = testParticipants;
	}

	public String getUniqueId() {
		return uniqueId;
	}

	public void setUniqueId(String uniqueId) {
		this.uniqueId = uniqueId;
	}

	public Float getTaskRatingStddev() {
		return taskRatingStddev;
	}

	public void setTaskRatingStddev(Float taskRatingStddev) {
		this.taskRatingStddev = taskRatingStddev;
	}

	public Set<CertificationCriterion> getCriteria() {
		return criteria;
	}

	public void setCriteria(Set<CertificationCriterion> criteria) {
		this.criteria = criteria;
	}
}
