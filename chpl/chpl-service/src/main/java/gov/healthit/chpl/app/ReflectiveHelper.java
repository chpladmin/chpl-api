package gov.healthit.chpl.app;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

/**
 * Provides reflective utility methods
 * @author dlucas
 *
 */
@Component("reflectiveHelper")
public class ReflectiveHelper {
	
	public ReflectiveHelper(){}
	
	/**
	 * Uses an object and field name to return the value of the object for the given field
	 * @param object
	 * @param fieldName
	 * @return
	 */
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
	
	/**
	 * Gets all fields for a given class
	 * @param type
	 * @return
	 */
	public static List<Field> getInheritedPrivateFields(Class<?> type) {
	    List<Field> result = new ArrayList<Field>();

	    Class<?> i = type;
	    while (i != null && i != Object.class) {
	    	for (Field field : i.getDeclaredFields()){
	    		if(!field.isSynthetic()){
	    			result.add(field);
	    		}
	    	}
	        i = i.getSuperclass();
	    }

	    return result;
	}
}
