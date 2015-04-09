package gov.healthit.chpl.auth.interceptor;

import java.lang.reflect.Method;

import org.aopalliance.aop.Advice;
import org.springframework.aop.Pointcut;
import org.springframework.aop.support.AbstractPointcutAdvisor;
import org.springframework.aop.support.StaticMethodMatcherPointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RestAuthAdvisor extends AbstractPointcutAdvisor {
	
	private static final long serialVersionUID = 1L;
	
	private final StaticMethodMatcherPointcut pointcut = new
			StaticMethodMatcherPointcut() {
				@Override
				public boolean matches(Method method, Class<?> targetClass) {
					return method.isAnnotationPresent(CheckAuthorization.class);
				}
			};
			
	@Autowired
	private RestAuthInterceptor interceptor;
	
	@Override
	public Pointcut getPointcut() {
	        return this.pointcut;
	}
	
	@Override
	public Advice getAdvice() {
		return this.interceptor;
	}
}
