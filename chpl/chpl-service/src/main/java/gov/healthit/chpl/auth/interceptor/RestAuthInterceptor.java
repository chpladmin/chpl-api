package gov.healthit.chpl.auth.interceptor;

import gov.healthit.chpl.auth.User;
import gov.healthit.chpl.auth.authorization.Authorizor;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class RestAuthInterceptor implements MethodInterceptor {

	@Autowired
	Authorizor authorizor;
	
	@Override
	public Object invoke(MethodInvocation invocation) throws Throwable {
		//System.out.println("hello.");
	    //System.out.println("method "+invocation.getMethod()+" is called on "+
        //        invocation.getThis()+" with args "+invocation.getArguments());
		//invocation.getArguments();
		String jwt = (String) invocation.getArguments()[0];
		User user = authorizor.getUser(jwt);
		
	    return invocation.proceed();
	}

}
