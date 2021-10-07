package gov.healthit.chpl.util;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class DeprecatedFieldExplorer {
    private static final String CHPL_PKG_BEGIN = "gov.healthit.chpl";

    //TODO: maybe store all the deprecated fields per class in a hashmap in here the first time they are looked up
    //so we don't add any time to requests
    public void getAllDeprecatedFields(Class<?> clazz, Set<String> allDeprecatedFieldNames, String fieldPrefix) {
        if (clazz == null) {
            return;
        }

        getAllDeprecatedFields(clazz.getSuperclass(), allDeprecatedFieldNames, fieldPrefix);

        //get any normal field that is deprecated and could be returned in the JSON
        List<String> filteredFieldNames = Arrays.stream(clazz.getDeclaredFields())
          .filter(f -> f.getAnnotation(Deprecated.class) != null)
          .map(f -> fieldPrefix + f.getName())
          .collect(Collectors.toList());
        allDeprecatedFieldNames.addAll(filteredFieldNames);

        //get the property associated with any deprecated "getter" method that could also be returned in the json
        try {
            for (PropertyDescriptor propertyDescriptor
                    : Introspector.getBeanInfo(clazz).getPropertyDescriptors()) {
                Method readMethod = propertyDescriptor.getReadMethod();
                if (readMethod != null && readMethod.getAnnotation(Deprecated.class) != null) {
                    allDeprecatedFieldNames.add(fieldPrefix + propertyDescriptor.getDisplayName());
                }
            }
        } catch (IntrospectionException ex) {
            LOGGER.error("Could not introspect the class " + clazz.getName(), ex);
        }

        Map<String, Class<?>> nestedClassesToCheckForDeprecatedFields = getNestedClasses(clazz, fieldPrefix);
        for (String nestedClassPrefix : nestedClassesToCheckForDeprecatedFields.keySet()) {
            getAllDeprecatedFields(nestedClassesToCheckForDeprecatedFields.get(nestedClassPrefix), allDeprecatedFieldNames, nestedClassPrefix);
        }
    }

    private Map<String, Class<?>> getNestedClasses(Class<?> clazz, String fieldPrefix) {
        //this gets the classes that are regular non-deprecated non-primitive non-JDK types of objects
        Map<String, Class<?>> nestedClassesToCheckForDeprecatedFields = Arrays.stream(clazz.getDeclaredFields())
                .filter(field -> field.getAnnotation(Deprecated.class) == null)
                .filter(field -> field.getClass().getPackage().getName().startsWith(CHPL_PKG_BEGIN))
                .collect(Collectors.toMap(field -> fieldPrefix + field.getName() + ".", Field::getClass));

        //this gets the classes that are nested in parameterized Collections (i.e. List<T>)
        Arrays.stream(clazz.getDeclaredFields())
                .filter(field -> field.getAnnotation(Deprecated.class) == null)
                .filter(field ->  field.getGenericType() != null && field.getGenericType() instanceof ParameterizedType)
                .forEach(field -> addParameterizedFieldToNestedClassesIfApplicable(field, fieldPrefix, nestedClassesToCheckForDeprecatedFields));
        return nestedClassesToCheckForDeprecatedFields;
    }

    private void addParameterizedFieldToNestedClassesIfApplicable(Field field, String fieldPrefix, Map<String, Class<?>> nestedClassesToCheckForDeprecatedFields) {
        ParameterizedType ptype = (ParameterizedType) field.getGenericType();
        Type[] typeArgs = ptype.getActualTypeArguments();
        for (Type type : typeArgs) {
            if (type != null && type instanceof Class<?>) {
                Class<?> clazz = (Class<?>) type;
                if (clazz.getPackage().getName().startsWith(CHPL_PKG_BEGIN)) {
                    nestedClassesToCheckForDeprecatedFields.put(fieldPrefix + field.getName() + ".", clazz.getClass());
                }
            }
        }
    }
}
