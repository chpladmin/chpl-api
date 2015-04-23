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
public class GlobalAdminOnlyInterceptor implements MethodInterceptor {

	@Autowired
	JWTUserConverter retriever;
	
	@Override
	public Object invoke(MethodInvocation invocation) throws Throwable {
		System.out.println("hello from the Global Admin Only interceptor.");
		
		String jwt = (String) invocation.getArguments()[0];
		User user = retriever.getUser(jwt);
		//if (Authorizer.authorize(user, "global-group", "admin")){
		if (true){
			return invocation.proceed();
		} else {
			throw new AuthorizationException();
		}
	}

}
