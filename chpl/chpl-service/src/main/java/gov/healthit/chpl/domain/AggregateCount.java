package gov.healthit.chpl.domain;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import gov.healthit.chpl.app.ReflectiveHelper;

/** Description: Provides aggregate counts for objects
 * 
 * @author dlucas
 *
 */
public class AggregateCount {
	private Integer totalCount;
	private List<? extends Object> objectList = new ArrayList<Object>();
	
	public AggregateCount(){
	}
	
	public AggregateCount(List<? extends Object> objectList){
		this.objectList = objectList;
		if(objectList != null){
			this.totalCount = objectList.size();
		}
	}
	
	public Integer getTotalCount(){
		return this.totalCount;
	}

	public Integer getCountDuringPeriodUsingField(Date startDate, Date endDate, String fieldName) {
		Integer counter = 0;
		for(Object obj : objectList){
				Date objectDate = ReflectiveHelper.get(obj, fieldName);
				if((objectDate.after(startDate) || objectDate.equals(startDate)) && (objectDate.before(endDate) || objectDate.equals(endDate))){
					counter++;
				}
		}	
		return counter;
	}
	
	public Integer getCountBeforeEndDateUsingField(Date endDate, String fieldName) {
		Integer counter = 0;
		for(Object obj : objectList){
				Date objectDate = ReflectiveHelper.get(obj, fieldName);
				if((objectDate.before(endDate) || objectDate.equals(endDate))){
					counter++;
				}
		}	
		return counter;
	}
	
}