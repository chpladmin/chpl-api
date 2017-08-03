package gov.healthit.chpl.web.controller;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import gov.healthit.chpl.domain.DeveloperTransparency;
import gov.healthit.chpl.manager.DeveloperManager;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(value = "collections")
@RestController
@RequestMapping("/collections")
public class CollectionsController {
	private static final Logger logger = LogManager.getLogger(CollectionsController.class);
	@Autowired private DeveloperManager developerManager;
		
	@ApiOperation(value="Get a list of all developers with transparency attestation URLs"
			+ "and ACB attestations.", 
			notes="")
	@RequestMapping(value="/developers", method=RequestMethod.GET,
			produces="application/json; charset=utf-8")
	public @ResponseBody List<DeveloperTransparency> getAllCertifiedProducts() {
		List<DeveloperTransparency> developerResults = developerManager.getDeveloperCollection();
		return developerResults;
	}
}
