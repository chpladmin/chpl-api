package gov.healthit.chpl.domain;

import java.io.Serializable;

public class AccurateAsOfDate implements Serializable {
	private static final long serialVersionUID = 8162304032624386529L;
	private Long accurateAsOfDate;
	
	public AccurateAsOfDate(){};
	
	public AccurateAsOfDate(Long accurateAsOfDate){
		this.accurateAsOfDate = accurateAsOfDate;
	};
	
	public Long getAccurateAsOfDate() {
		return accurateAsOfDate;
	}
	public void setAccurateAsOfDate(Long accurateAsOfDate) {
		this.accurateAsOfDate = accurateAsOfDate;
	}
}
