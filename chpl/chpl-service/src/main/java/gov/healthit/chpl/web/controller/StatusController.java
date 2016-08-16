package gov.healthit.chpl.web.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api
@RestController
public class StatusController {
	private static final Logger logger = LogManager.getLogger(StatusController.class);

	@ApiOperation(value="Check that the rest services are up and running.", 
			notes="")
	@RequestMapping(value="/status", method=RequestMethod.GET,
			produces="application/json; charset=utf-8")
	public @ResponseBody String getStatus() {
		logger.warn("/status called");
		return "{\"status\": \"OK\"}";
	}
}
