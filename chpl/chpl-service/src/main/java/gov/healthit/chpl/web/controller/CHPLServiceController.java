package gov.healthit.chpl.web.controller;

import gov.healthit.chpl.auth.interceptor.Bean;
import gov.healthit.chpl.auth.interceptor.CheckAuthorization;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CHPLServiceController {
	
	@Autowired
	private Bean bean;
	
	
	//@CheckAuthorization
	@RequestMapping(value="/hello/{firstName}/{lastName}", method= RequestMethod.GET, produces="application/json; charset=utf-8")
	public String validateCode(@PathVariable String firstName, @PathVariable String lastName) {
		System.out.println("hello world");
		
		bean.foo();
		
		return "{\"firstName\" : \""+firstName+"\", \"lastName\" : \""+lastName+"\" }";
		
	}
	
	public Bean getBean(){
		return bean;
	}
	
	public void setBean(Bean bn){
		this.bean = bn;
	}
	
	
}
