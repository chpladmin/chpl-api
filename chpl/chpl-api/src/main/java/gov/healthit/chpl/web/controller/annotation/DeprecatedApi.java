package gov.healthit.chpl.web.controller.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface DeprecatedApi {

    /**
     *  The targeted date of removal of the API in yyyy-MM-dd format.
     *  The API shall not be removed before this date but may be removed at any point after.
     */
    String removalDate() default "";

    /**
     * A message to the user indicating what API endpoint(s) they may want to use instead
     * if a replacement exists.
     */
    String message() default "";

    /**
     * friendlyUrl provides a way to consolidate similar endpoints that have different URLs.
     * For example, /certified_product/111 should map to the same friendlyUrl as /certified_product/CHP-12345
     * and the same friendlyUrl as /certified_product/15.04.04.TREE. Their usage may all be counted together.
     */
    String friendlyUrl() default "";
}

