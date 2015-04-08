package gov.healthit.chpl.auth.interceptor;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

public class RestAuthInterceptor implements MethodInterceptor {

	public Object invoke(MethodInvocation invocation) throws Throwable {
		
	    System.out.println("method "+invocation.getMethod()+" is called on "+
                invocation.getThis()+" with args "+invocation.getArguments());
	    return invocation.proceed();
	}

}
