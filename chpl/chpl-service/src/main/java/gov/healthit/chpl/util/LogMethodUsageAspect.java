package gov.healthit.chpl.util;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import lombok.extern.log4j.Log4j2;

@Component
@Aspect
@Log4j2
public class LogMethodUsageAspect {

    @Before("@annotation(LogMethodUsage)")
    public void logMethodStart(JoinPoint joinPoint) {
        LOGGER.info(joinPoint.getSignature().getDeclaringTypeName() + " started");
    }

    @AfterReturning("@annotation(LogMethodUsage)")
    public void logMethodCompletion(JoinPoint joinPoint) {
        LOGGER.info(joinPoint.getSignature().getDeclaringTypeName() + " completed");
    }
}
