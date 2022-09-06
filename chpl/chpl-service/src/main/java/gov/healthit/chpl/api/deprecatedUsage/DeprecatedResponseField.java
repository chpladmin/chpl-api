package gov.healthit.chpl.api.deprecatedUsage;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.FIELD, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface DeprecatedResponseField {

    /**
     *  The targeted date of removal of the field in yyyy-MM-dd format.
     *  The field shall not be removed before this date but may be removed at any point after.
     */
    String removalDate() default "";

    /**
     * A message to the user indicating how they may want to retrieve this data instead, if at all.
     */
    String message() default "";
}

