package gov.healthit.chpl.certificationCriteria;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.service.UrlFormatter;
import gov.healthit.chpl.util.Util;
import lombok.extern.log4j.Log4j2;

@Component
@Aspect
@Log4j2
public class UrlFormattingNeededAspect {

    private UrlFormatter urlFormatter;

    @Autowired
    public UrlFormattingNeededAspect(UrlFormatter urlFormatter) {
        this.urlFormatter = urlFormatter;
    }

    @Around("@annotation(UrlFormattingNeeded)")
    public CertificationCriterion formatCompanionGuideUrl(ProceedingJoinPoint joinPoint) throws Throwable {
        Object result = joinPoint.proceed();
        if (result != null && result instanceof CertificationCriterion) {
            CertificationCriterion crit = (CertificationCriterion) result;
            LOGGER.info("Formatting Companion Guide Link for " + Util.formatCriteriaNumber(crit));
            crit.setCompanionGuideLink(urlFormatter.format(crit.getCompanionGuideLink()));
            return crit;
        } else {
            LOGGER.error("Attempting to apply 'UrlFormattingNeeded' annotation to object of class " + result.getClass().getName());
        }
        return null;
    }
}
