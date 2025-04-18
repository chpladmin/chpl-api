package gov.healthit.chpl.sharedstore.user;

import java.lang.reflect.Method;
import java.util.UUID;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.expression.AnnotatedElementKey;
import org.springframework.expression.EvaluationContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.exception.UserRetrievalException;
import gov.healthit.chpl.sharedstore.ExpressionEvaluator;
import gov.healthit.chpl.user.cognito.CognitoApiWrapper;
import lombok.extern.log4j.Log4j2;

@Component
@Aspect
@Log4j2
public class UserStoreReplaceAspect {
    private ExpressionEvaluator<String> evaluator = new ExpressionEvaluator<String>();
    private SharedUserStoreProvider sharedUserStoreProvider;
    private CognitoApiWrapper cognitoApiWrapper;

    @Autowired
    public UserStoreReplaceAspect(SharedUserStoreProvider sharedUserStoreProvider,
            CognitoApiWrapper cognitoApiWrapper) {
        this.sharedUserStoreProvider = sharedUserStoreProvider;
        this.cognitoApiWrapper = cognitoApiWrapper;
    }

    @AfterReturning("execution(* *.*(..)) && @annotation(userStoreReplace)")
    @Transactional
    public void userStoreReplace(JoinPoint joinPoint, UserStoreReplace userStoreReplace) {
        if (userStoreReplace.replaceBy().equals(ReplaceUserBy.USER_ID)) {
            String id = getValue(joinPoint, userStoreReplace.id());
            replaceUserInStore(userStoreReplace.replaceBy(), id);
        }
    }

    private void replaceUserInStore(ReplaceUserBy replaceBy, String id) {
        if (id == null) {
            LOGGER.error("Attempting to replace user in the shared store by " + replaceBy.name()
                + " but the 'id' field passed into the replaceUserInStore method was null. "
                + "Nothing will be replaced in the store.");
        }

        switch (replaceBy) {
            case USER_ID:
                replaceUsersInStoreByUserId(id);
                break;
            //leaving space to possibly "remove by" developer id, acb id later
            default:
        }
    }

    private void replaceUsersInStoreByUserId(String userId) {
        sharedUserStoreProvider.remove(userId);
        try {
            cognitoApiWrapper.getUserInfo(UUID.fromString(userId));
        } catch (UserRetrievalException ex) {
            LOGGER.error("Unable to get replace user in store with ID " + userId, ex);
        }
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
