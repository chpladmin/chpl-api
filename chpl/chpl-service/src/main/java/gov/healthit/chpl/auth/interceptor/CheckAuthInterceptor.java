package gov.healthit.chpl.auth.interceptor;

import gov.healthit.chpl.auth.User;
import gov.healthit.chpl.auth.authorization.AuthorizationException;
import gov.healthit.chpl.auth.authorization.Authorizer;
import gov.healthit.chpl.auth.authorization.JWTUserConverter;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class CheckAuthInterceptor implements MethodInterceptor {

	@Autowired
	JWTUserConverter retriever;
	
	@Override
	public Object invoke(MethodInvocation invocation) throws Throwable {
		System.out.println("hello from the interceptor.");
	    System.out.println("method "+invocation.getMethod()+" is called on "+
                invocation.getThis()+" with args "+invocation.getArguments());
	    
		String jwt = (String) invocation.getArguments()[0];
		
		//try {
		/*
		User user = retriever.getUser(jwt);
		Authorizer.setAuthorization(user);
		*/
		//} catch JWTValidationException(e){
		//}
		/*
		if (Authorizer.authorize(user, "group", group)){
			return invocation.proceed();
		} else {
			throw new AuthorizationException();
		}
		*/
		return invocation.proceed();
	}

}
