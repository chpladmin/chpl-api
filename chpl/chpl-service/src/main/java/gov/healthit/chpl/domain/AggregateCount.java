package gov.healthit.chpl.domain;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
				Date objectDate = get(obj, fieldName);
				if((objectDate.after(startDate) || objectDate.equals(startDate)) && (objectDate.before(endDate) || objectDate.equals(endDate))){
					counter++;
				}
		}	
		return counter;
	}
	
	public Integer getCountBeforeEndDateUsingField(Date endDate, String fieldName) {
		Integer counter = 0;
		for(Object obj : objectList){
				Date objectDate = get(obj, fieldName);
				if((objectDate.before(endDate) || objectDate.equals(endDate))){
					counter++;
				}
		}	
		return counter;
	}
	
	@SuppressWarnings("unchecked")
	public static <V> V get(Object object, String fieldName) {
	    Class<?> clazz = object.getClass();
	    while (clazz != null) {
	        try {
	            Field field = clazz.getDeclaredField(fieldName);
	            field.setAccessible(true);
	            return (V) field.get(object);
	        } catch (NoSuchFieldException e) {
	            clazz = clazz.getSuperclass();
	        } catch (Exception e) {
	            throw new IllegalStateException(e);
	        }
	    }
	    return null;
	}
	
}