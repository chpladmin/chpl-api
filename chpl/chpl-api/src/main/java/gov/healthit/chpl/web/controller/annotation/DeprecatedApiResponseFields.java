package gov.healthit.chpl.web.controller.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface DeprecatedApiResponseFields {

    /**
     *  The class containing deprecated fields that is being returned from the method.
     *  Any field in this class with the DeprecatedResponseField annotation will give information on its individual removal date.
     */
    Class<?> responseClass() default Object.class;

    /**
     * The HTTP Method (GET, POST, etc.) of the API endpoint with deprecated response field(s).
     * @return
     */
    String httpMethod() default "GET";

    /**
     * friendlyUrl provides a way to consolidate similar endpoints that have different URLs.
     * For example, /certified_product/111 should map to the same friendlyUrl as /certified_product/CHP-12345
     * and the same friendlyUrl as /certified_product/15.04.04.TREE. Their usage may all be counted together.
     */
    String friendlyUrl() default "";
}

