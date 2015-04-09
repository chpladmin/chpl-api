package gov.healthit.chpl.auth.interceptor;

import org.springframework.stereotype.Service;

@Service
public class BeanImpl implements Bean {

	
	@Override
	@CheckAuthorization
	public void foo() {
		System.out.println("Executing method 'foo'.");
	}

}
