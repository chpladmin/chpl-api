package gov.healthit.chpl.sharedstore.user;

import java.lang.reflect.Method;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.expression.AnnotatedElementKey;
import org.springframework.expression.EvaluationContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.sharedstore.ExpressionEvaluator;
import lombok.extern.log4j.Log4j2;

@Component
@Aspect
@Log4j2
public class UserStoreRemoveAspect {
    private ExpressionEvaluator<String> evaluator = new ExpressionEvaluator<String>();
    private SharedUserStoreProvider sharedUserStoreProvider;

    @Autowired
    public UserStoreRemoveAspect(SharedUserStoreProvider sharedUserStoreProvider) {
        this.sharedUserStoreProvider = sharedUserStoreProvider;
    }

    @AfterReturning("execution(* *.*(..)) && @annotation(userStoreRemove)")
    @Transactional
    public void userStoreRemove(JoinPoint joinPoint, UserStoreRemove userStoreRemove) {
        if (userStoreRemove.removeBy().equals(RemoveUserBy.ALL)) {
            removeAllUsersFromStore();
        } else {
            String id = getValue(joinPoint, userStoreRemove.id());
            removeUsersFromStore(userStoreRemove.removeBy(), id);
        }
    }

    private void removeAllUsersFromStore() {
        sharedUserStoreProvider.removeAll();
    }

    private void removeUsersFromStore(RemoveUserBy removeBy, String id) {
        if (id == null) {
            LOGGER.error("Attempting to remove user(s) from the shared store by " + removeBy.name()
                + " but the 'id' field passed into the removeUsersFromStore method was null. "
                + "Nothing will be removed from the store.");
        }

        switch (removeBy) {
            case USER_ID:
                removeUsersFromStoreByUserId(id);
                break;
            //leaving space to possibly "remove by" developer id, acb id later
            default:
        }
    }

    private void removeUsersFromStoreByUserId(String userId) {
        sharedUserStoreProvider.remove(userId);
    }

    private String getValue(JoinPoint joinPoint, String condition) {
        return getValue(joinPoint.getTarget(), joinPoint.getArgs(),
                        joinPoint.getTarget().getClass(),
                        ((MethodSignature) joinPoint.getSignature()).getMethod(), condition);
      }

    private String getValue(Object object, Object[] args, Class clazz, Method method, String condition) {
        if (args == null) {
          return null;
        }
        EvaluationContext evaluationContext = evaluator.createEvaluationContext(object, clazz, method, args);
        AnnotatedElementKey methodKey = new AnnotatedElementKey(method, clazz);
        return evaluator.condition(condition, methodKey, evaluationContext, String.class);
    }
}
