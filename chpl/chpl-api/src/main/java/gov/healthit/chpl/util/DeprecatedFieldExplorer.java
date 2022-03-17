package gov.healthit.chpl.util;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class DeprecatedFieldExplorer {
    public static final String FIELD_SEPARATOR = " -> ";
    private static final String CHPL_PKG_BEGIN = "gov.healthit.chpl";
    private Map<Class<?>, Set<String>> classToDeprecatedFieldNamesMap;

    public DeprecatedFieldExplorer() {
        classToDeprecatedFieldNamesMap = new LinkedHashMap<Class<?>, Set<String>>();
    }

    public Set<String> getDeprecatedFieldsForClass(Class<?> clazz) {
        if (classToDeprecatedFieldNamesMap.get(clazz) == null) {
            LOGGER.debug("Finding all deprecated fields for class " + clazz.getName());
            Set<String> deprecatedFieldNames = new LinkedHashSet<String>();
            getAllDeprecatedFields(clazz, deprecatedFieldNames, "");
            classToDeprecatedFieldNamesMap.put(clazz, deprecatedFieldNames);
        }
        return classToDeprecatedFieldNamesMap.get(clazz);
    }

    private void getAllDeprecatedFields(Class<?> clazz, Set<String> allDeprecatedFieldNames, String fieldPrefix) {
        if (clazz == null) {
            return;
        } else {
            LOGGER.debug("Getting all deprecated fields for " + fieldPrefix + ": " + clazz.getName());
        }

        getAllDeprecatedFields(clazz.getSuperclass(), allDeprecatedFieldNames, fieldPrefix);

        //get any normal field that is deprecated and could be returned in the JSON
        List<String> filteredFieldNames = Arrays.stream(clazz.getDeclaredFields())
                .filter(f -> !hasJsonIgnorableAnnotation(f))
                .filter(f -> f.getAnnotation(Deprecated.class) != null)
                .map(f -> fieldPrefix + f.getName())
                .toList();
        allDeprecatedFieldNames.addAll(filteredFieldNames);

        //get the property associated with any deprecated "getter" method that could also be returned in the json
        try {
            for (PropertyDescriptor propertyDescriptor
                    : Introspector.getBeanInfo(clazz).getPropertyDescriptors()) {
                Method readMethod = propertyDescriptor.getReadMethod();
                if (readMethod != null && !hasJsonIgnorableAnnotation(readMethod)
                        && readMethod.getAnnotation(Deprecated.class) != null) {
                    allDeprecatedFieldNames.add(fieldPrefix + propertyDescriptor.getDisplayName());
                }
            }
        } catch (IntrospectionException ex) {
            LOGGER.error("Could not introspect the class " + clazz.getName(), ex);
        }

        Map<String, Class<?>> nestedClassesToCheckForDeprecatedFields = getNestedClasses(clazz, fieldPrefix);
        nestedClassesToCheckForDeprecatedFields.keySet().stream()
            .forEach(nestedClassPrefix -> getAllDeprecatedFields(nestedClassesToCheckForDeprecatedFields.get(nestedClassPrefix), allDeprecatedFieldNames, nestedClassPrefix));
    }

    private boolean hasJsonIgnorableAnnotation(Field field) {
        return field.getAnnotation(JsonIgnore.class) != null
                || (field.getAnnotation(JsonProperty.class) != null
                        && field.getAnnotation(JsonProperty.class).access().equals(Access.WRITE_ONLY));
    }

    private boolean hasJsonIgnorableAnnotation(Method method) {
        return method.getAnnotation(JsonIgnore.class) != null
                || (method.getAnnotation(JsonProperty.class) != null
                        && method.getAnnotation(JsonProperty.class).access().equals(Access.WRITE_ONLY));
    }

    private Map<String, Class<?>> getNestedClasses(Class<?> clazz, String fieldPrefix) {
        //this gets the classes that are regular non-deprecated non-primitive non-JDK types of objects
        Map<String, Class<?>> nestedClassesToCheckForDeprecatedFields = new LinkedHashMap<String, Class<?>>();
        Arrays.stream(clazz.getDeclaredFields())
            .filter(field -> isNotDeprecatedOrIgnoredUponSerialization(field))
            .filter(field -> isFieldAChplClass(field))
            .forEach(field -> addFieldToNestedClasses(field, fieldPrefix, nestedClassesToCheckForDeprecatedFields));

        //this gets the classes that are nested in parameterized Collections (i.e. List<T>)
        Arrays.stream(clazz.getDeclaredFields())
            .filter(field -> isNotDeprecatedOrIgnoredUponSerialization(field))
            .filter(field ->  field.getGenericType() != null && field.getGenericType() instanceof ParameterizedType)
            .forEach(field -> addParameterizedFieldToNestedClassesIfApplicable(field, fieldPrefix, nestedClassesToCheckForDeprecatedFields));
        return nestedClassesToCheckForDeprecatedFields;
    }

    private boolean isNotDeprecatedOrIgnoredUponSerialization(Field field) {
        return field.getAnnotation(Deprecated.class) == null
                && field.getAnnotation(JsonIgnore.class) == null;
    }

    private boolean isFieldAChplClass(Field field) {
        return field.getType() != null && field.getType().getPackage() != null
                && field.getType().getPackage().getName().startsWith(CHPL_PKG_BEGIN);
    }

    private void addFieldToNestedClasses(Field field, String fieldPrefix, Map<String, Class<?>> nestedClassesToCheckForDeprecatedFields) {
        if (field != null && field.getType() instanceof Class<?>
            && !((Class<?>) field.getType()).isEnum()) {
            nestedClassesToCheckForDeprecatedFields.put(fieldPrefix + field.getName() + FIELD_SEPARATOR, field.getType());
        }
    }

    private void addParameterizedFieldToNestedClassesIfApplicable(Field field, String fieldPrefix, Map<String, Class<?>> nestedClassesToCheckForDeprecatedFields) {
        ParameterizedType ptype = (ParameterizedType) field.getGenericType();
        Type[] typeArgs = ptype.getActualTypeArguments();
        for (Type type : typeArgs) {
            if (type != null && type instanceof Class<?> && !((Class<?>) type).isEnum()) {
                Class<?> clazz = (Class<?>) type;
                if (clazz.getPackage().getName().startsWith(CHPL_PKG_BEGIN)) {
                    nestedClassesToCheckForDeprecatedFields.put(fieldPrefix + field.getName() + FIELD_SEPARATOR, clazz);
                }
            }
        }
    }
}
