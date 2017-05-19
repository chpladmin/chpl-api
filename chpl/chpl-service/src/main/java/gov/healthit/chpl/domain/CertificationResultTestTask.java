package gov.healthit.chpl.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.StringUtils;

import gov.healthit.chpl.dto.CertificationResultTestTaskDTO;

@XmlType(namespace = "http://chpl.healthit.gov/listings")
@XmlAccessorType(XmlAccessType.FIELD)
public class CertificationResultTestTask implements Serializable {
	private static final long serialVersionUID = -5239080984125704737L;

	private static final Logger logger = LogManager.getLogger(CertificationResultTestTask.class);

	@XmlElement(required = true)
	private Long id;

	@XmlElement(required = false, nillable=true)
	private String uniqueId;
	
	@XmlElement(required = true)
	private Long testTaskId;
	
	@XmlElement(required = false, nillable=true)
	private String description;
	
	@XmlElement(required = false, nillable=true)
	private Float taskSuccessAverage;
	
	@XmlElement(required = false, nillable=true)
	private Float taskSuccessStddev;
	
	@XmlElement(required = false, nillable=true)
	private Integer taskPathDeviationObserved;
	
	@XmlElement(required = false, nillable=true)
	private Integer taskPathDeviationOptimal;
	
	@XmlElement(required = false, nillable=true)
	private Long taskTimeAvg;
	
	@XmlElement(required = false, nillable=true)
	private Integer taskTimeStddev;
	
	@XmlElement(required = false, nillable=true)
	private Integer taskTimeDeviationObservedAvg;
	
	@XmlElement(required = false, nillable=true)
	private Integer taskTimeDeviationOptimalAvg;
	
	@XmlElement(required = false, nillable=true)
	private Float taskErrors;
	
	@XmlElement(required = false, nillable=true)
	private Float taskErrorsStddev;
	
	@XmlElement(required = false, nillable=true)
	private String taskRatingScale;
	
	@XmlElement(required = false, nillable=true)
	private Float taskRating;
	
	@XmlElement(required = false, nillable=true)
	private Float taskRatingStddev;
	
	@XmlElementWrapper(name = "participants", nillable = true, required = false)
	@XmlElement(name = "participant")
	private List<CertificationResultTestParticipant> testParticipants;

	public CertificationResultTestTask() {
		super();
		testParticipants = new ArrayList<CertificationResultTestParticipant>();
	}
	
	public CertificationResultTestTask(CertificationResultTestTaskDTO dto) {
		this();
		this.id = dto.getId();
		this.testTaskId = dto.getTestTaskId();
		if(dto.getTestTask() != null) {
			this.description = dto.getTestTask().getDescription();
			this.taskSuccessAverage = dto.getTestTask().getTaskSuccessAverage();
			this.taskSuccessStddev = dto.getTestTask().getTaskSuccessStddev();
			this.taskPathDeviationObserved = dto.getTestTask().getTaskPathDeviationObserved();
			this.taskPathDeviationOptimal = dto.getTestTask().getTaskPathDeviationOptimal();
			this.taskTimeAvg = dto.getTestTask().getTaskTimeAvg();
			this.taskTimeStddev = dto.getTestTask().getTaskTimeStddev();
			this.taskTimeDeviationObservedAvg = dto.getTestTask().getTaskTimeDeviationObservedAvg();
			this.taskTimeDeviationOptimalAvg = dto.getTestTask().getTaskTimeDeviationOptimalAvg();
			this.taskErrors = dto.getTestTask().getTaskErrors();
			this.taskErrorsStddev = dto.getTestTask().getTaskErrorsStddev();
			this.taskRatingScale = dto.getTestTask().getTaskRatingScale();
			this.taskRating = dto.getTestTask().getTaskRating();
			this.taskRatingStddev = dto.getTestTask().getTaskRatingStddev();
		}
	}
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getTestTaskId() {
		return testTaskId;
	}

	public void setTestTaskId(Long testTaskId) {
		this.testTaskId = testTaskId;
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

	public List<CertificationResultTestParticipant> getTestParticipants() {
		return testParticipants;
	}

	public void setTestParticipants(List<CertificationResultTestParticipant> testParticipants) {
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
}
