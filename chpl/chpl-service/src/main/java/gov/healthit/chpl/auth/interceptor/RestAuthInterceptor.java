package gov.healthit.chpl.auth.interceptor;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.stereotype.Component;


@Component
public class RestAuthInterceptor implements MethodInterceptor {

	@Override
	public Object invoke(MethodInvocation invocation) throws Throwable {
		System.out.println("hello.");
	    //System.out.println("method "+invocation.getMethod()+" is called on "+
        //        invocation.getThis()+" with args "+invocation.getArguments());
		invocation.getArguments();
	    return invocation.proceed();
	}

}
