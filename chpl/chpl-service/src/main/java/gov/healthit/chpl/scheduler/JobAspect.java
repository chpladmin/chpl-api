package gov.healthit.chpl.scheduler;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.context.SecurityContextHolder;

@Aspect
public class JobAspect {

    @After("execution(public void  org.quartz.Job.execute(..))")
    public void logAfter(JoinPoint joinPoint) {
        SecurityContextHolder.clearContext();
    }

}
