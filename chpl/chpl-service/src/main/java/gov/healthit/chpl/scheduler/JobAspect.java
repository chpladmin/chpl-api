package gov.healthit.chpl.scheduler;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.context.SecurityContextHolder;

import lombok.extern.log4j.Log4j2;

@Aspect
@Log4j2
public class JobAspect {
    //@Before("execution(public void  org.quartz.Job.execute(..))")
    //public void logBefore(JoinPoint joinPOint) {
    //    LOGGER.info("BEFORE!! - {}", joinPOint.getSignature().getName());
    //}

    @After("execution(public void  org.quartz.Job.execute(..))")
    public void logAfter(JoinPoint joinPoint) {
        LOGGER.info("AFTER!! - {}", joinPoint.getSignature().getName());
        LOGGER.info("{}", Thread.currentThread().getName());
        SecurityContextHolder.clearContext();
    }

}
