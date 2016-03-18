package gov.healthit.chpl.domain;

import gov.healthit.chpl.dto.CertificationResultTestTaskDTO;

public class CertificationResultTestTask {
	private Long id;
	private Long testTaskId;
	private String description;
	private Float taskSuccessAverage;
	private Float taskSuccessStddev;
	private Integer taskPathDeviationObserved;
	private Integer taskPathDeviationOptimal;
	private Long taskTimeAvg;
	private Integer taskTimeStddev;
	private Integer taskTimeDeviationObservedAvg;
	private Integer taskTimeDeviationOptimalAvg;
	private Float taskErrors;
	private Float taskErrorsStddev;
	private String taskRatingScale;
	private Float taskRating;

	public CertificationResultTestTask() {
		super();
	}
	
	public CertificationResultTestTask(CertificationResultTestTaskDTO dto) {
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

	public void setTaskPathDeviationObserved(Integer taskPathDeviationObserved) {
		this.taskPathDeviationObserved = taskPathDeviationObserved;
	}

	public Integer getTaskPathDeviationOptimal() {
		return taskPathDeviationOptimal;
	}

	public void setTaskPathDeviationOptimal(Integer taskPathDeviationOptimal) {
		this.taskPathDeviationOptimal = taskPathDeviationOptimal;
	}

	public Long getTaskTimeAvg() {
		return taskTimeAvg;
	}

	public void setTaskTimeAvg(Long taskTimeAvg) {
		this.taskTimeAvg = taskTimeAvg;
	}

	public Integer getTaskTimeStddev() {
		return taskTimeStddev;
	}

	public void setTaskTimeStddev(Integer taskTimeStddev) {
		this.taskTimeStddev = taskTimeStddev;
	}

	public Integer getTaskTimeDeviationObservedAvg() {
		return taskTimeDeviationObservedAvg;
	}

	public void setTaskTimeDeviationObservedAvg(Integer taskTimeDeviationObservedAvg) {
		this.taskTimeDeviationObservedAvg = taskTimeDeviationObservedAvg;
	}

	public Integer getTaskTimeDeviationOptimalAvg() {
		return taskTimeDeviationOptimalAvg;
	}

	public void setTaskTimeDeviationOptimalAvg(Integer taskTimeDeviationOptimalAvg) {
		this.taskTimeDeviationOptimalAvg = taskTimeDeviationOptimalAvg;
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
}
