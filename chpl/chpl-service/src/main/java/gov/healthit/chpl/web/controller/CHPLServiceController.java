package gov.healthit.chpl.web.controller;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class CHPLServiceController {
	
	
	@RequestMapping(value="/hello/{firstName}/{lastName}", method= RequestMethod.GET, produces="application/json; charset=utf-8")
	public String validateCode(@PathVariable String firstName, @PathVariable String lastName) {
		
		return "{\"firstName\" : \""+firstName+"\", \"lastName\" : \""+lastName+"\" }";
		
	}
	
}
