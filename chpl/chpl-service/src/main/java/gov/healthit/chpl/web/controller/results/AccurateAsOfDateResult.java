package gov.healthit.chpl.web.controller.results;

import java.io.Serializable;

public class AccurateAsOfDateResult implements Serializable {
	private static final long serialVersionUID = 8162304032624386529L;
	private Long accurateAsOfDate;
	
	public AccurateAsOfDateResult(){};
	
	public AccurateAsOfDateResult(Long accurateAsOfDate){
		this.accurateAsOfDate = accurateAsOfDate;
	};
	
	public Long getAccurateAsOfDate() {
		return accurateAsOfDate;
	}
	public void setAccurateAsOfDate(Long accurateAsOfDate) {
		this.accurateAsOfDate = accurateAsOfDate;
	}
}
