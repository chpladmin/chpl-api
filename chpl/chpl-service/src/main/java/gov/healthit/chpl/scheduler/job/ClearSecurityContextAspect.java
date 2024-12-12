package gov.healthit.chpl.scheduler.job;

import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Aspect
//@Component
public class ClearSecurityContextAspect {

    //@After("execution(* *.*(..)) && @annotation(clearSecurityContext)")
    //public void clearSecurityContext(JoinPoint joinPoint, ClearSecurityContext clearSecurityContext) {
    //    LOGGER.info("This code was run!!!");
    //}

    @Pointcut("execution(* gov.healthit.chpl.scheduler.job.DirectReviewCacheRefreshJob.execute(..))")
    private void selectExecute(){}

    @Before("selectExecute()")
    public void beforeAdvice(){
        LOGGER.info("This code was run 1 !!!");
    }

    @After("selectExecute()")
    public void afterAdvice(){
        LOGGER.info("This code was run 2 !!!");
    }
}
