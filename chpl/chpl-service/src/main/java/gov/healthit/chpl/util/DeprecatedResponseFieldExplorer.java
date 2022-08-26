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

import gov.healthit.chpl.api.deprecatedUsage.DeprecatedResponseField;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class DeprecatedResponseFieldExplorer {
    public static final String FIELD_SEPARATOR = " -> ";
    private static final String CHPL_PKG_BEGIN = "gov.healthit.chpl";
    private Map<Class<?>, Set<DeprecatedField>> classToDeprecatedFieldsMap;
    private Map<Class<?>, Set<DeprecatedMethod>> classToDeprecatedMethodsMap;

    public DeprecatedResponseFieldExplorer() {
        classToDeprecatedFieldsMap = new LinkedHashMap<Class<?>, Set<DeprecatedField>>();
        classToDeprecatedMethodsMap = new LinkedHashMap<Class<?>, Set<DeprecatedMethod>>();
    }

    public LinkedHashMap<String, Object> getUniqueDeprecatedItemsForClass(Class<?> clazz) {
        LinkedHashMap<String, Object> uniqueDeprecatedItems = new LinkedHashMap<String, Object>();
        Set<DeprecatedField> deprecatedFields = getDeprecatedFieldsForClass(clazz);
        deprecatedFields.stream()
            .forEach(deprecatedField -> uniqueDeprecatedItems.put(deprecatedField.getFriendlyName(), deprecatedField.getField()));

        Set<DeprecatedMethod> deprecatedMethods = getDeprecatedMethodsForClass(clazz);
        deprecatedMethods.stream()
            .filter(deprecatedMethod -> uniqueDeprecatedItems.get(deprecatedMethod.getFriendlyName()) == null)
            .forEach(deprecatedMethod -> uniqueDeprecatedItems.put(deprecatedMethod.getFriendlyName(), deprecatedMethod.getMethod()));
        return uniqueDeprecatedItems;
    }

    public Set<DeprecatedField> getDeprecatedFieldsForClass(Class<?> clazz) {
        if (classToDeprecatedFieldsMap.get(clazz) == null) {
            LOGGER.debug("Finding all deprecated fields for class " + clazz.getName());
            Set<DeprecatedField> deprecatedFields = new LinkedHashSet<DeprecatedField>();
            getAllDeprecatedFields(clazz, deprecatedFields, "");
            classToDeprecatedFieldsMap.put(clazz, deprecatedFields);
        }
        return classToDeprecatedFieldsMap.get(clazz);
    }

    public Set<DeprecatedMethod> getDeprecatedMethodsForClass(Class<?> clazz) {
        if (classToDeprecatedMethodsMap.get(clazz) == null) {
            LOGGER.debug("Finding all deprecated methods for class " + clazz.getName());
            Set<DeprecatedMethod> deprecatedMethods = new LinkedHashSet<DeprecatedMethod>();
            getAllDeprecatedMethods(clazz, deprecatedMethods, "");
            classToDeprecatedMethodsMap.put(clazz, deprecatedMethods);
        }
        return classToDeprecatedMethodsMap.get(clazz);
    }

    private void getAllDeprecatedFields(Class<?> clazz, Set<DeprecatedField> allDeprecatedFields, String fieldNamePrefix) {
        if (clazz == null) {
            return;
        } else {
            LOGGER.debug("Getting all deprecated fields for " + fieldNamePrefix + ": " + clazz.getName());
        }

        getAllDeprecatedFields(clazz.getSuperclass(), allDeprecatedFields, fieldNamePrefix);

        //get any normal field that is deprecated and could be returned in the JSON
        List<DeprecatedField> deprecatedVisibleFields = Arrays.stream(clazz.getDeclaredFields())
                .filter(f -> !hasJsonIgnorableAnnotation(f))
                .filter(f -> f.getAnnotation(DeprecatedResponseField.class) != null)
                .map(f -> DeprecatedField.builder()
                        .field(f)
                        .friendlyName(fieldNamePrefix + f.getName())
                        .build())
                .toList();
        allDeprecatedFields.addAll(deprecatedVisibleFields);

        Map<String, Class<?>> nestedClassesToCheckForDeprecatedFields = getNestedClasses(clazz, fieldNamePrefix);
        nestedClassesToCheckForDeprecatedFields.keySet().stream()
            .forEach(nestedClassPrefix ->
                getAllDeprecatedFields(nestedClassesToCheckForDeprecatedFields.get(nestedClassPrefix), allDeprecatedFields, nestedClassPrefix));
    }

    private void getAllDeprecatedMethods(Class<?> clazz, Set<DeprecatedMethod> allDeprecatedMethods, String fieldNamePrefix) {
        if (clazz == null) {
            return;
        } else {
            LOGGER.debug("Getting all deprecated methods for " + clazz.getName());
        }

        getAllDeprecatedMethods(clazz.getSuperclass(), allDeprecatedMethods, fieldNamePrefix);

        //get the property associated with any deprecated "getter" method that could also be returned in the json
        try {
            for (PropertyDescriptor propertyDescriptor
                    : Introspector.getBeanInfo(clazz).getPropertyDescriptors()) {
                Method readMethod = propertyDescriptor.getReadMethod();
                if (readMethod != null && !hasJsonIgnorableAnnotation(readMethod)
                        && readMethod.getAnnotation(DeprecatedResponseField.class) != null) {
                    allDeprecatedMethods.add(DeprecatedMethod.builder()
                            .method(readMethod)
                            .friendlyName(fieldNamePrefix + propertyDescriptor.getDisplayName())
                            .build());
                }
            }
        } catch (IntrospectionException ex) {
            LOGGER.error("Could not introspect the class " + clazz.getName(), ex);
        }

        Map<String, Class<?>> nestedClassesToCheckForDeprecatedFields = getNestedClasses(clazz, fieldNamePrefix);
        nestedClassesToCheckForDeprecatedFields.keySet().stream()
            .forEach(nestedClassPrefix -> getAllDeprecatedMethods(nestedClassesToCheckForDeprecatedFields.get(nestedClassPrefix), allDeprecatedMethods, nestedClassPrefix));
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

    private Map<String, Class<?>> getNestedClasses(Class<?> clazz, String fieldNamePrefix) {
        //this gets the classes that are regular non-deprecated non-primitive non-JDK types of objects
        Map<String, Class<?>> nestedClassesToCheckForDeprecatedFields = new LinkedHashMap<String, Class<?>>();
        Arrays.stream(clazz.getDeclaredFields())
            .filter(field -> isNotDeprecatedOrIgnoredUponSerialization(field))
            .filter(field -> isFieldAChplClass(field))
            .forEach(field -> addFieldToNestedClasses(field, fieldNamePrefix, nestedClassesToCheckForDeprecatedFields));

        //this gets the classes that are nested in parameterized Collections (i.e. List<T>)
        Arrays.stream(clazz.getDeclaredFields())
            .filter(field -> isNotDeprecatedOrIgnoredUponSerialization(field))
            .filter(field ->  field.getGenericType() != null && field.getGenericType() instanceof ParameterizedType)
            .forEach(field -> addParameterizedFieldToNestedClassesIfApplicable(field, fieldNamePrefix, nestedClassesToCheckForDeprecatedFields));
        return nestedClassesToCheckForDeprecatedFields;
    }

    private boolean isNotDeprecatedOrIgnoredUponSerialization(Field field) {
        return field.getAnnotation(DeprecatedResponseField.class) == null
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

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    @Builder
    public static class DeprecatedField {
        private Field field;
        private String friendlyName;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    @Builder
    public static class DeprecatedMethod {
        private Method method;
        private String friendlyName;
    }
}
