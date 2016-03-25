package gov.healthit.chpl.web.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api
@RestController
public class StatusController {
	
	@ApiOperation(value="Check that the rest services are up and running.", 
			notes="")
	@RequestMapping(value="/status", method=RequestMethod.GET,
			produces="application/json; charset=utf-8")
	public @ResponseBody String getStatus() {
		return "{\"status\": \"OK\"}";
	}
}
