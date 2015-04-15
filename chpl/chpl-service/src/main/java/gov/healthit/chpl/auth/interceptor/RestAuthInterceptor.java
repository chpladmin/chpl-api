package gov.healthit.chpl.auth.interceptor;

import gov.healthit.chpl.auth.User;
import gov.healthit.chpl.auth.authorization.AuthorizationException;
import gov.healthit.chpl.auth.authorization.Authorizer;
import gov.healthit.chpl.auth.authorization.JWTUserRetriever;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class RestAuthInterceptor implements MethodInterceptor {

	@Autowired
	JWTUserRetriever retriever;
	
	@Override
	public Object invoke(MethodInvocation invocation) throws Throwable {
		System.out.println("hello from the interceptor.");
	    System.out.println("method "+invocation.getMethod()+" is called on "+
                invocation.getThis()+" with args "+invocation.getArguments());
	    
		String jwt = (String) invocation.getArguments()[0];
		User user = retriever.getUser(jwt);
		String group = (String) invocation.getArguments()[1];
		
		if (Authorizer.authorize(user, "group", group)){
			return invocation.proceed();
		} else {
			throw new AuthorizationException();
		}
		
	    
	}

}
