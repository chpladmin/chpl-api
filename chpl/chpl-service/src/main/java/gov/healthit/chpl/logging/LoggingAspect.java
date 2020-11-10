package gov.healthit.chpl.logging;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingAspect {

    @Around(value = "@within(gov.healthit.chpl.logging.Loggable) || @annotation(gov.healthit.chpl.logging.Loggable)")
    public Object around(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {


        MethodSignature signature = (MethodSignature) proceedingJoinPoint.getSignature();
        Method method = signature.getMethod();
        Loggable loggableMethod = method.getAnnotation(Loggable.class);

        Loggable loggableClass = proceedingJoinPoint.getTarget().getClass().getAnnotation(Loggable.class);

        StringBuilder sb = new StringBuilder();
        sb.append("Start Execution of " + Modifier.toString(method.getModifiers()) + " "
                + method.getDeclaringClass().getSimpleName() + "." + method.getName() + "()");

        //show params
        boolean showParams = loggableMethod != null ? loggableMethod.params() : loggableClass.params();
        if (showParams) {
            if (proceedingJoinPoint.getArgs() != null && proceedingJoinPoint.getArgs().length > 0) {
                sb.append("[");
                for (int i = 0; i < proceedingJoinPoint.getArgs().length; i++) {
                    sb.append(method.getParameterTypes()[i].getSimpleName() + ":" + proceedingJoinPoint.getArgs()[i].toString());
                    if (i < proceedingJoinPoint.getArgs().length - 1) {
                        sb.append(", ");
                    }
                }
                sb.append("]");
            }
        }
        LogWriter.write(proceedingJoinPoint.getTarget().getClass(), sb.toString());

        long startTime = System.currentTimeMillis();

        //start method execution
        Object result = proceedingJoinPoint.proceed();

        long endTime = System.currentTimeMillis();

        //show results
        StringBuilder value = new StringBuilder();
        if (result != null) {
            boolean showResults = loggableMethod != null ? loggableMethod.result() : loggableClass.result();
            if (showResults) {
                value.append("Result : " + result.toString());
            }
        }

        //show after
        LogWriter.write(proceedingJoinPoint.getTarget().getClass(),
                "Compl Execution of " + Modifier.toString(method.getModifiers()) + " "
                        + method.getDeclaringClass().getSimpleName() + "."
                        + method.getName() + "() took " + (endTime - startTime)
                        + "ms " + value.toString());

        return result;
    }

}
