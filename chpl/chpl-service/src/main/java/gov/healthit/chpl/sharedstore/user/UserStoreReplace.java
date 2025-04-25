package gov.healthit.chpl.sharedstore.user;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface UserStoreReplace  {
    ReplaceUserBy replaceBy() default ReplaceUserBy.USER_ID;
    String id() default "";
}
