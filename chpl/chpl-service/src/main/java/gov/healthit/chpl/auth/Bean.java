package gov.healthit.chpl.auth;

import gov.healthit.chpl.auth.interceptor.CheckAuthorization;

public class Bean {

    @CheckAuthorization
    public void foo() {
        System.out.println("Executing method 'foo'.");
    }
	
}
