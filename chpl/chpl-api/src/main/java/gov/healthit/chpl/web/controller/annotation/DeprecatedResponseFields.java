package gov.healthit.chpl.web.controller.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface DeprecatedResponseFields {

    /**
     *  The class containing deprecated fields that is being returned from the method.
     */
    Class<?> responseClass() default Object.class;
}

