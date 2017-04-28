package gov.healthit.chpl.web.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import gov.healthit.chpl.manager.NotificationManager;
import io.swagger.annotations.Api;

@Api(value="notifications")
@RestController
@RequestMapping("/notifications")
public class NotificationController {
	
	private static final Logger logger = LogManager.getLogger(NotificationController.class);

	
	@Autowired NotificationManager notificationManager;

//	@ApiOperation(value = "Get the list of all registrants and the notifications that they've signed up for that are applicable to the currently logged in user")
//	@RequestMapping(value = "", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
//	public @ResponseBody NotificationResults getAllRegistrantsWithNotifications() {
//		
//	}
}
