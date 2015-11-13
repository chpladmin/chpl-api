package gov.healthit.chpl.web.controller;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import gov.healthit.chpl.domain.ActivityConcept;
import gov.healthit.chpl.domain.ActivityEvent;
import gov.healthit.chpl.manager.ActivityManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonParseException;

@RestController
@RequestMapping("/activity")
public class ActivityController {
	
	@Autowired
	private ActivityManager activityManager;
	
	
	@RequestMapping(value="/", method=RequestMethod.GET, produces="application/json; charset=utf-8")
	public List<ActivityEvent> activity(@RequestParam(required=false) Integer lastNDays) throws JsonParseException, IOException{
		
		if (lastNDays == null){
			return getActivityEvents();
		} else {
			return getActivityEvents(lastNDays);
		}
	}
	
	@RequestMapping(value="/acbs", method=RequestMethod.GET, produces="application/json; charset=utf-8")
	public List<ActivityEvent> activityForACBs(@RequestParam(required=false) Integer lastNDays) throws JsonParseException, IOException{
		
		if (lastNDays == null){
			return getActivityEventsForACBs();
		} else {
			return getActivityEventsForACBs(lastNDays);
		}
	}
	
	@RequestMapping(value="/acbs/{id}", method=RequestMethod.GET, produces="application/json; charset=utf-8")
	public List<ActivityEvent> activityForACBById(@PathVariable("id") Long id, @RequestParam(required=false) Integer lastNDays) throws JsonParseException, IOException{
		
		if (lastNDays == null){
			return getActivityEventsForACBs(id);
		} else {
			return getActivityEventsForACBs(id, lastNDays);
		}
	}
	
	
	@RequestMapping(value="/certified_products", method=RequestMethod.GET, produces="application/json; charset=utf-8")
	public List<ActivityEvent> activityForCertifiedProducts(@RequestParam(required=false) Integer lastNDays) throws JsonParseException, IOException{
		
		if (lastNDays == null){
			return getActivityEventsForCertifiedProducts();
		} else {
			return getActivityEventsForCertifiedProducts(lastNDays);
		}
	}
	
	@RequestMapping(value="/certified_products/{id}", method=RequestMethod.GET, produces="application/json; charset=utf-8")
	public List<ActivityEvent> activityForCertifiedProductById(@PathVariable("id") Long id, @RequestParam(required=false) Integer lastNDays) throws JsonParseException, IOException{
		
		if (lastNDays == null){
			return getActivityEventsForCertifiedProducts(id);
		} else {
			return getActivityEventsForCertifiedProducts(id, lastNDays);
		}
	}
	
	
	@RequestMapping(value="/certifications", method=RequestMethod.GET, produces="application/json; charset=utf-8")
	public List<ActivityEvent> activityForCertifications(@RequestParam(required=false) Integer lastNDays) throws JsonParseException, IOException{
		
		if (lastNDays == null){
			return getActivityEventsForCertifications();
		} else {
			return getActivityEventsForCertifications(lastNDays);
		}
	}
	
	@RequestMapping(value="/certifications/{id}", method=RequestMethod.GET, produces="application/json; charset=utf-8")
	public List<ActivityEvent> activityForCertificationById(@PathVariable("id") Long id, @RequestParam(required=false) Integer lastNDays) throws JsonParseException, IOException{
		
		if (lastNDays == null){
			return getActivityEventsForCertifications(id);
		} else {
			return getActivityEventsForCertifications(id, lastNDays);
		}
	}
	
	
	@RequestMapping(value="/pending_certified_products", method=RequestMethod.GET, produces="application/json; charset=utf-8")
	public List<ActivityEvent> activityForPendingCertifiedProducts(@RequestParam(required=false) Integer lastNDays) throws JsonParseException, IOException{
		
		if (lastNDays == null){
			return getActivityEventsForPendingCertifiedProducts();
		} else {
			return getActivityEventsForPendingCertifiedProducts(lastNDays);
		}
	}
	
	@RequestMapping(value="/pending_certified_products/{id}", method=RequestMethod.GET, produces="application/json; charset=utf-8")
	public List<ActivityEvent> activityForPendingCertifiedProducts(@PathVariable("id") Long id, @RequestParam(required=false) Integer lastNDays) throws JsonParseException, IOException{
		
		if (lastNDays == null){
			return getActivityEventsForPendingCertifiedProducts(id);
		} else {
			return getActivityEventsForPendingCertifiedProducts(id, lastNDays);
		}
	}
	
	@RequestMapping(value="/products", method=RequestMethod.GET, produces="application/json; charset=utf-8")
	public List<ActivityEvent> activityForProducts(@RequestParam(required=false) Integer lastNDays) throws JsonParseException, IOException{
		
		if (lastNDays == null){
			return getActivityEventsForProducts();
		} else {
			return getActivityEventsForProducts(lastNDays);
		}
	}
	
	@RequestMapping(value="/products/{id}", method=RequestMethod.GET, produces="application/json; charset=utf-8")
	public List<ActivityEvent> activityForProducts(@PathVariable("id") Long id, @RequestParam(required=false) Integer lastNDays) throws JsonParseException, IOException{
		
		if (lastNDays == null){
			return getActivityEventsForProducts(id);
		} else {
			return getActivityEventsForProducts(id, lastNDays);
		}
	}
	
	@RequestMapping(value="/users", method=RequestMethod.GET, produces="application/json; charset=utf-8")
	public List<ActivityEvent> activityForUsers(@RequestParam(required=false) Integer lastNDays) throws JsonParseException, IOException{
		
		if (lastNDays == null){
			return getActivityEventsForUsers();
		} else {
			return getActivityEventsForUsers(lastNDays);
		}
	}
	
	@RequestMapping(value="/users/{id}", method=RequestMethod.GET, produces="application/json; charset=utf-8")
	public List<ActivityEvent> activityForUsers(@PathVariable("id") Long id, @RequestParam(required=false) Integer lastNDays) throws JsonParseException, IOException{
		
		if (lastNDays == null){
			return getActivityEventsForUsers(id);
		} else {
			return getActivityEventsForUsers(id, lastNDays);
		}
	}
	
	
	@RequestMapping(value="/vendors", method=RequestMethod.GET, produces="application/json; charset=utf-8")
	public List<ActivityEvent> activityForVendors(@RequestParam(required=false) Integer lastNDays) throws JsonParseException, IOException{
		
		if (lastNDays == null){
			return getActivityEventsForVendors();
		} else {
			return getActivityEventsForVendors(lastNDays);
		}
	}
	
	
	@RequestMapping(value="/vendors/{id}", method=RequestMethod.GET, produces="application/json; charset=utf-8")
	public List<ActivityEvent> activityForVendors(@PathVariable("id") Long id, @RequestParam(required=false) Integer lastNDays) throws JsonParseException, IOException{
		
		if (lastNDays == null){
			return getActivityEventsForVendors(id);
		} else {
			return getActivityEventsForVendors(id, lastNDays);
		}
	}
	
	
	@RequestMapping(value="/user_activity", method=RequestMethod.GET, produces="application/json; charset=utf-8")
	public Map<Long, List<ActivityEvent> > activityByUser(@RequestParam(required=false) Integer lastNDays) throws JsonParseException, IOException{
		
		if (lastNDays == null){
			return activityManager.getActivityByUser();
		} else {
			return activityManager.getActivityByUserInLastNDays(lastNDays);
		}
	}
	

	@RequestMapping(value="/user_activity/{id}", method=RequestMethod.GET, produces="application/json; charset=utf-8")
	public List<ActivityEvent> activityByUser(@PathVariable("id") Long id, @RequestParam(required=false) Integer lastNDays) throws JsonParseException, IOException{
		
		if (lastNDays == null){
			return activityManager.getActivityForUser(id);
		} else {
			return activityManager.getActivityForUserInLastNDays(id, lastNDays);
		}
	}
	
	
	private List<ActivityEvent> getActivityEventsForACBs(Long id) throws JsonParseException, IOException{
		
		List<ActivityEvent> events = null;
		ActivityConcept concept = ActivityConcept.ACTIVITY_CONCEPT_CERTIFICATION_BODY;
		events = getActivityEventsForObject(concept, id);
		
		return events;
	}
	
	private List<ActivityEvent> getActivityEventsForCertifications(Long id) throws JsonParseException, IOException{
		
		List<ActivityEvent> events = null;
		ActivityConcept concept = ActivityConcept.ACTIVITY_CONCEPT_CERTIFICATION;
		events = getActivityEventsForObject(concept, id);
		
		return events;
	}
	
	private List<ActivityEvent> getActivityEventsForCertifiedProducts(Long id) throws JsonParseException, IOException{
		
		List<ActivityEvent> events = null;
		ActivityConcept concept = ActivityConcept.ACTIVITY_CONCEPT_CERTIFIED_PRODUCT;
		events = getActivityEventsForObject(concept, id);
		
		return events;
	}
	
	private List<ActivityEvent> getActivityEventsForPendingCertifiedProducts(Long id) throws JsonParseException, IOException{
		
		List<ActivityEvent> events = null;
		ActivityConcept concept = ActivityConcept.ACTIVITY_CONCEPT_PENDING_CERTIFIED_PRODUCT;
		events = getActivityEventsForObject(concept, id);
		
		return events;
	}
	
	private List<ActivityEvent> getActivityEventsForProducts(Long id) throws JsonParseException, IOException{
		
		List<ActivityEvent> events = null;
		ActivityConcept concept = ActivityConcept.ACTIVITY_CONCEPT_PRODUCT;
		events = getActivityEventsForObject(concept, id);
		
		return events;
	}
	
	private List<ActivityEvent> getActivityEventsForUsers(Long id) throws JsonParseException, IOException{
		
		List<ActivityEvent> events = null;
		ActivityConcept concept = ActivityConcept.ACTIVITY_CONCEPT_USER;
		events = getActivityEventsForObject(concept, id);
		
		return events;
	}
	
	private List<ActivityEvent> getActivityEventsForVendors(Long id) throws JsonParseException, IOException{
		
		List<ActivityEvent> events = null;
		ActivityConcept concept = ActivityConcept.ACTIVITY_CONCEPT_VENDOR;
		events = getActivityEventsForObject(concept, id);
		
		return events;
	}
	
	private List<ActivityEvent> getActivityEventsForVersions(Long id) throws JsonParseException, IOException{
		
		List<ActivityEvent> events = null;
		ActivityConcept concept = ActivityConcept.ACTIVITY_CONCEPT_VERSION;
		events = getActivityEventsForObject(concept, id);
		return events;
		
	}
	
	
	
	
	
	
	private List<ActivityEvent> getActivityEventsForACBs(Long id, Integer lastNDays) throws JsonParseException, IOException{
		
		List<ActivityEvent> events = null;
		ActivityConcept concept = ActivityConcept.ACTIVITY_CONCEPT_CERTIFICATION_BODY;
		events = getActivityEventsForObject(concept, id, lastNDays);
		return events;
	}
	
	
	private List<ActivityEvent> getActivityEventsForCertifications(Long id, Integer lastNDays) throws JsonParseException, IOException{
		
		List<ActivityEvent> events = null;
		ActivityConcept concept = ActivityConcept.ACTIVITY_CONCEPT_CERTIFICATION;
		events = getActivityEventsForObject(concept, id, lastNDays);
		return events;
	}
	
	
	private List<ActivityEvent> getActivityEventsForCertifiedProducts(Long id, Integer lastNDays) throws JsonParseException, IOException{
		
		List<ActivityEvent> events = null;
		ActivityConcept concept = ActivityConcept.ACTIVITY_CONCEPT_CERTIFIED_PRODUCT;
		events = getActivityEventsForObject(concept, id, lastNDays);
		return events;
	}
	
	private List<ActivityEvent> getActivityEventsForPendingCertifiedProducts(Long id, Integer lastNDays) throws JsonParseException, IOException{
		
		List<ActivityEvent> events = null;
		ActivityConcept concept = ActivityConcept.ACTIVITY_CONCEPT_PENDING_CERTIFIED_PRODUCT;
		events = getActivityEventsForObject(concept, id, lastNDays);
		return events;
	}
	
	private List<ActivityEvent> getActivityEventsForProducts(Long id, Integer lastNDays) throws JsonParseException, IOException{
		
		List<ActivityEvent> events = null;
		ActivityConcept concept = ActivityConcept.ACTIVITY_CONCEPT_PRODUCT;
		events = getActivityEventsForObject(concept, id, lastNDays);
		return events;
	}
	
	private List<ActivityEvent> getActivityEventsForUsers(Long id, Integer lastNDays) throws JsonParseException, IOException{
		
		List<ActivityEvent> events = null;
		ActivityConcept concept = ActivityConcept.ACTIVITY_CONCEPT_USER;
		events = getActivityEventsForObject(concept, id, lastNDays);
		return events;
	}
	
	private List<ActivityEvent> getActivityEventsForVendors(Long id, Integer lastNDays) throws JsonParseException, IOException{
		
		List<ActivityEvent> events = null;
		ActivityConcept concept = ActivityConcept.ACTIVITY_CONCEPT_VENDOR;
		events = getActivityEventsForObject(concept, id, lastNDays);
		return events;
	}
	
	private List<ActivityEvent> getActivityEventsForVersions(Long id, Integer lastNDays) throws JsonParseException, IOException{
		
		List<ActivityEvent> events = null;
		ActivityConcept concept = ActivityConcept.ACTIVITY_CONCEPT_VERSION;
		events = getActivityEventsForObject(concept, id, lastNDays);
		return events;
		
	}
	
	
	
	
	
	
	
	
	private List<ActivityEvent> getActivityEventsForACBs(Integer lastNDays) throws JsonParseException, IOException{
		
		List<ActivityEvent> events = null;
		ActivityConcept concept = ActivityConcept.ACTIVITY_CONCEPT_CERTIFICATION_BODY;
		events = getActivityEventsForConcept(concept, lastNDays);
		
		return events;
	}
	
	
	private List<ActivityEvent> getActivityEventsForCertifications(Integer lastNDays) throws JsonParseException, IOException{
		
		List<ActivityEvent> events = null;
		ActivityConcept concept = ActivityConcept.ACTIVITY_CONCEPT_CERTIFICATION;
		events = getActivityEventsForConcept(concept, lastNDays);
		
		return events;
	}
	
	
	private List<ActivityEvent> getActivityEventsForCertifiedProducts(Integer lastNDays) throws JsonParseException, IOException{
		
		List<ActivityEvent> events = null;
		ActivityConcept concept = ActivityConcept.ACTIVITY_CONCEPT_CERTIFIED_PRODUCT;
		events = getActivityEventsForConcept(concept, lastNDays);
		
		return events;
	}
	
	private List<ActivityEvent> getActivityEventsForPendingCertifiedProducts(Integer lastNDays) throws JsonParseException, IOException{
		
		List<ActivityEvent> events = null;
		ActivityConcept concept = ActivityConcept.ACTIVITY_CONCEPT_PENDING_CERTIFIED_PRODUCT;
		events = getActivityEventsForConcept(concept, lastNDays);
		
		return events;
	}
	
	private List<ActivityEvent> getActivityEventsForProducts(Integer lastNDays) throws JsonParseException, IOException{
		
		List<ActivityEvent> events = null;
		ActivityConcept concept = ActivityConcept.ACTIVITY_CONCEPT_PRODUCT;
		events = getActivityEventsForConcept(concept, lastNDays);
		
		return events;
	}
	
	private List<ActivityEvent> getActivityEventsForUsers(Integer lastNDays) throws JsonParseException, IOException{
		
		List<ActivityEvent> events = null;
		ActivityConcept concept = ActivityConcept.ACTIVITY_CONCEPT_USER;
		events = getActivityEventsForConcept(concept, lastNDays);
		
		return events;
	}
	
	private List<ActivityEvent> getActivityEventsForVendors(Integer lastNDays) throws JsonParseException, IOException{
		
		List<ActivityEvent> events = null;
		ActivityConcept concept = ActivityConcept.ACTIVITY_CONCEPT_VENDOR;
		events = getActivityEventsForConcept(concept, lastNDays);
		
		return events;
	}
	
	private List<ActivityEvent> getActivityEventsForVersions(Integer lastNDays) throws JsonParseException, IOException{
		
		List<ActivityEvent> events = null;
		ActivityConcept concept = ActivityConcept.ACTIVITY_CONCEPT_VERSION;
		events = getActivityEventsForConcept(concept, lastNDays);
		return events;
		
	}
	
	
	
	
	
	
	
	private List<ActivityEvent> getActivityEventsForACBs() throws JsonParseException, IOException{
		
		List<ActivityEvent> events = null;
		ActivityConcept concept = ActivityConcept.ACTIVITY_CONCEPT_CERTIFICATION_BODY;
		events = getActivityEventsForConcept(concept);
		
		return events;
	}
	
	
	private List<ActivityEvent> getActivityEventsForCertifications() throws JsonParseException, IOException{
		
		List<ActivityEvent> events = null;
		ActivityConcept concept = ActivityConcept.ACTIVITY_CONCEPT_CERTIFICATION;
		events = getActivityEventsForConcept(concept);
		
		return events;
	}
	
	
	private List<ActivityEvent> getActivityEventsForCertifiedProducts() throws JsonParseException, IOException{
		
		List<ActivityEvent> events = null;
		ActivityConcept concept = ActivityConcept.ACTIVITY_CONCEPT_CERTIFIED_PRODUCT;
		events = getActivityEventsForConcept(concept);
		
		return events;
	}
	
	private List<ActivityEvent> getActivityEventsForPendingCertifiedProducts() throws JsonParseException, IOException{
		
		List<ActivityEvent> events = null;
		ActivityConcept concept = ActivityConcept.ACTIVITY_CONCEPT_PENDING_CERTIFIED_PRODUCT;
		events = getActivityEventsForConcept(concept);
		
		return events;
	}
	
	private List<ActivityEvent> getActivityEventsForProducts() throws JsonParseException, IOException{
		
		List<ActivityEvent> events = null;
		ActivityConcept concept = ActivityConcept.ACTIVITY_CONCEPT_PRODUCT;
		events = getActivityEventsForConcept(concept);
		
		return events;
	}
	
	private List<ActivityEvent> getActivityEventsForUsers() throws JsonParseException, IOException{
		
		List<ActivityEvent> events = null;
		ActivityConcept concept = ActivityConcept.ACTIVITY_CONCEPT_USER;
		events = getActivityEventsForConcept(concept);
		
		return events;
	}
	
	private List<ActivityEvent> getActivityEventsForVendors() throws JsonParseException, IOException{
		
		List<ActivityEvent> events = null;
		ActivityConcept concept = ActivityConcept.ACTIVITY_CONCEPT_VENDOR;
		events = getActivityEventsForConcept(concept);
		
		return events;
	}
	
	private List<ActivityEvent> getActivityEventsForVersions() throws JsonParseException, IOException{
		
		List<ActivityEvent> events = null;
		ActivityConcept concept = ActivityConcept.ACTIVITY_CONCEPT_VERSION;
		events = getActivityEventsForConcept(concept);
		return events;
		
	}
	
	
	
	private List<ActivityEvent> getActivityEventsForConcept(ActivityConcept concept) throws JsonParseException, IOException{
		return activityManager.getActivityForConcept(concept);
	}
	
	private List<ActivityEvent> getActivityEventsForConcept(ActivityConcept concept, Integer lastNDays) throws JsonParseException, IOException{
		return activityManager.getActivityForConcept(concept);
	}
	
	private List<ActivityEvent> getActivityEventsForObject(ActivityConcept concept, Long objectId) throws JsonParseException, IOException{
		return activityManager.getActivityForObject(concept, objectId);
	}
	
	private List<ActivityEvent> getActivityEventsForObject(ActivityConcept concept, Long objectId, Integer lastNDays) throws JsonParseException, IOException{
		return activityManager.getActivityForObject(concept, objectId, lastNDays);
	}
	
	private List<ActivityEvent> getActivityEvents(Integer lastNDays) throws JsonParseException, IOException{
		return activityManager.getAllActivityInLastNDays(lastNDays);
	}
	
	private List<ActivityEvent> getActivityEvents() throws JsonParseException, IOException{
		return activityManager.getAllActivity();
	}
	
	private List<ActivityEvent> getActivityEventsByUser(Integer lastNDays) throws JsonParseException, IOException{
		return activityManager.getAllActivityInLastNDays(lastNDays);
	}
	
	private List<ActivityEvent> getActivityByUserInLastNDays() throws JsonParseException, IOException{
		return activityManager.getAllActivity();
	}
	
	
	
	private List<ActivityEvent> getActivityEventsForConcept(Long conceptId) throws JsonParseException, IOException{
		
		List<ActivityEvent> events = null;
		
		for (ActivityConcept concept : ActivityConcept.values()) {
			if(concept.getId().equals(conceptId)){
				events =  getActivityEventsForConcept(concept);
				break;
			}
		}
		return events;
	}
	
	private List<ActivityEvent> getActivityEventsForConcept(String conceptName) throws JsonParseException, IOException{
		
		List<ActivityEvent> events = null;
		
		for (ActivityConcept concept : ActivityConcept.values()) {
			if(concept.getName().equals(conceptName)){
				events =  getActivityEventsForConcept(concept);
				break;
			}
		}
		return events;
	}
	
}
