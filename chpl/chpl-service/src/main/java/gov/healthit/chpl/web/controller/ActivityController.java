package gov.healthit.chpl.web.controller;

import java.util.List;

import gov.healthit.chpl.domain.ActivityConcept;
import gov.healthit.chpl.domain.ActivityEvent;
import gov.healthit.chpl.manager.ActivityManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/activity")
public class ActivityController {
	
	@Autowired
	private ActivityManager activityManager;
	
	@RequestMapping(value="/acb", method=RequestMethod.GET, produces="application/json; charset=utf-8")
	public List<ActivityEvent> activityForACBs(@RequestParam(required=false) Integer lastNDays){
		
		if (lastNDays == null){
			return getActivityEventsForACBs();
		} else {
			return getActivityEventsForACBs(lastNDays);
		}
	}
	
	@RequestMapping(value="/acb/{id}", method=RequestMethod.GET, produces="application/json; charset=utf-8")
	public List<ActivityEvent> activityForACBById(@PathVariable("id") Long id, @RequestParam(required=false) Integer lastNDays){
		
		if (lastNDays == null){
			return getActivityEventsForACBs(id);
		} else {
			return getActivityEventsForACBs(id, lastNDays);
		}
	}
	
	/*
	 * 	@RequestMapping(value="/", method=RequestMethod.GET,
			produces="application/json; charset=utf-8")
	public @ResponseBody List<ProductVersion> getVersionsByProduct(@RequestParam(required=true) Long productId) {
		List<ProductVersionDTO> versionList = null;
		
		if(productId != null && productId > 0) {
			versionList = pvManager.getByProduct(productId);	
		} else {
			versionList = pvManager.getAll();
		}
		
		List<ProductVersion> versions = new ArrayList<ProductVersion>();
		if(versionList != null && versionList.size() > 0) {
			for(ProductVersionDTO dto : versionList) {
				ProductVersion result = new ProductVersion(dto);
				versions.add(result);
			}
		}
		return versions;
	}
	
	@RequestMapping(value="/{versionId}", method=RequestMethod.GET,
			produces="application/json; charset=utf-8")
	public @ResponseBody ProductVersion getProductVersionById(@PathVariable("versionId") Long versionId) throws EntityRetrievalException {
		ProductVersionDTO version = pvManager.getById(versionId);
		
		ProductVersion result = null;
		if(version != null) {
			result = new ProductVersion(version);
		}
		return result;
	}
	
	 */
	
	
	
	
	private List<ActivityEvent> getActivityEventsForACBs(Long id){
		
		List<ActivityEvent> events = null;
		ActivityConcept concept = ActivityConcept.ACTIVITY_CONCEPT_CERTIFICATION_BODY;
		events = getActivityEventsForObject(concept, id);
		
		return events;
	}
	
	private List<ActivityEvent> getActivityEventsForCertifications(Long id){
		
		List<ActivityEvent> events = null;
		ActivityConcept concept = ActivityConcept.ACTIVITY_CONCEPT_CERTIFICATION;
		events = getActivityEventsForObject(concept, id);
		
		return events;
	}
	
	private List<ActivityEvent> getActivityEventsForCertifiedProducts(Long id){
		
		List<ActivityEvent> events = null;
		ActivityConcept concept = ActivityConcept.ACTIVITY_CONCEPT_CERTIFIED_PRODUCT;
		events = getActivityEventsForObject(concept, id);
		
		return events;
	}
	
	private List<ActivityEvent> getActivityEventsForPendingCertifiedProducts(Long id){
		
		List<ActivityEvent> events = null;
		ActivityConcept concept = ActivityConcept.ACTIVITY_CONCEPT_PENDING_CERTIFIED_PRODUCT;
		events = getActivityEventsForObject(concept, id);
		
		return events;
	}
	
	private List<ActivityEvent> getActivityEventsForProducts(Long id){
		
		List<ActivityEvent> events = null;
		ActivityConcept concept = ActivityConcept.ACTIVITY_CONCEPT_PRODUCT;
		events = getActivityEventsForObject(concept, id);
		
		return events;
	}
	
	private List<ActivityEvent> getActivityEventsForUsers(Long id){
		
		List<ActivityEvent> events = null;
		ActivityConcept concept = ActivityConcept.ACTIVITY_CONCEPT_USER;
		events = getActivityEventsForObject(concept, id);
		
		return events;
	}
	
	private List<ActivityEvent> getActivityEventsForVendors(Long id){
		
		List<ActivityEvent> events = null;
		ActivityConcept concept = ActivityConcept.ACTIVITY_CONCEPT_VENDOR;
		events = getActivityEventsForObject(concept, id);
		
		return events;
	}
	
	private List<ActivityEvent> getActivityEventsForVersions(Long id){
		
		List<ActivityEvent> events = null;
		ActivityConcept concept = ActivityConcept.ACTIVITY_CONCEPT_VERSION;
		events = getActivityEventsForObject(concept, id);
		return events;
		
	}
	
	
	
	
	
	
	private List<ActivityEvent> getActivityEventsForACBs(Long id, Integer lastNDays){
		
		List<ActivityEvent> events = null;
		ActivityConcept concept = ActivityConcept.ACTIVITY_CONCEPT_CERTIFICATION_BODY;
		events = getActivityEventsForObject(concept, id, lastNDays);
		return events;
	}
	
	
	private List<ActivityEvent> getActivityEventsForCertifications(Long id, Integer lastNDays){
		
		List<ActivityEvent> events = null;
		ActivityConcept concept = ActivityConcept.ACTIVITY_CONCEPT_CERTIFICATION;
		events = getActivityEventsForObject(concept, id, lastNDays);
		return events;
	}
	
	
	private List<ActivityEvent> getActivityEventsForCertifiedProducts(Long id, Integer lastNDays){
		
		List<ActivityEvent> events = null;
		ActivityConcept concept = ActivityConcept.ACTIVITY_CONCEPT_CERTIFIED_PRODUCT;
		events = getActivityEventsForObject(concept, id, lastNDays);
		return events;
	}
	
	private List<ActivityEvent> getActivityEventsForPendingCertifiedProducts(Long id, Integer lastNDays){
		
		List<ActivityEvent> events = null;
		ActivityConcept concept = ActivityConcept.ACTIVITY_CONCEPT_PENDING_CERTIFIED_PRODUCT;
		events = getActivityEventsForObject(concept, id, lastNDays);
		return events;
	}
	
	private List<ActivityEvent> getActivityEventsForProducts(Long id, Integer lastNDays){
		
		List<ActivityEvent> events = null;
		ActivityConcept concept = ActivityConcept.ACTIVITY_CONCEPT_PRODUCT;
		events = getActivityEventsForObject(concept, id, lastNDays);
		return events;
	}
	
	private List<ActivityEvent> getActivityEventsForUsers(Long id, Integer lastNDays){
		
		List<ActivityEvent> events = null;
		ActivityConcept concept = ActivityConcept.ACTIVITY_CONCEPT_USER;
		events = getActivityEventsForObject(concept, id, lastNDays);
		return events;
	}
	
	private List<ActivityEvent> getActivityEventsForVendors(Long id, Integer lastNDays){
		
		List<ActivityEvent> events = null;
		ActivityConcept concept = ActivityConcept.ACTIVITY_CONCEPT_VENDOR;
		events = getActivityEventsForObject(concept, id, lastNDays);
		return events;
	}
	
	private List<ActivityEvent> getActivityEventsForVersions(Long id, Integer lastNDays){
		
		List<ActivityEvent> events = null;
		ActivityConcept concept = ActivityConcept.ACTIVITY_CONCEPT_VERSION;
		events = getActivityEventsForObject(concept, id, lastNDays);
		return events;
		
	}
	
	
	
	
	
	
	
	
	private List<ActivityEvent> getActivityEventsForACBs(Integer lastNDays){
		
		List<ActivityEvent> events = null;
		ActivityConcept concept = ActivityConcept.ACTIVITY_CONCEPT_CERTIFICATION_BODY;
		events = getActivityEventsForConcept(concept, lastNDays);
		
		return events;
	}
	
	
	private List<ActivityEvent> getActivityEventsForCertifications(Integer lastNDays){
		
		List<ActivityEvent> events = null;
		ActivityConcept concept = ActivityConcept.ACTIVITY_CONCEPT_CERTIFICATION;
		events = getActivityEventsForConcept(concept, lastNDays);
		
		return events;
	}
	
	
	private List<ActivityEvent> getActivityEventsForCertifiedProducts(Integer lastNDays){
		
		List<ActivityEvent> events = null;
		ActivityConcept concept = ActivityConcept.ACTIVITY_CONCEPT_CERTIFIED_PRODUCT;
		events = getActivityEventsForConcept(concept, lastNDays);
		
		return events;
	}
	
	private List<ActivityEvent> getActivityEventsForPendingCertifiedProducts(Integer lastNDays){
		
		List<ActivityEvent> events = null;
		ActivityConcept concept = ActivityConcept.ACTIVITY_CONCEPT_PENDING_CERTIFIED_PRODUCT;
		events = getActivityEventsForConcept(concept, lastNDays);
		
		return events;
	}
	
	private List<ActivityEvent> getActivityEventsForProducts(Integer lastNDays){
		
		List<ActivityEvent> events = null;
		ActivityConcept concept = ActivityConcept.ACTIVITY_CONCEPT_PRODUCT;
		events = getActivityEventsForConcept(concept, lastNDays);
		
		return events;
	}
	
	private List<ActivityEvent> getActivityEventsForUsers(Integer lastNDays){
		
		List<ActivityEvent> events = null;
		ActivityConcept concept = ActivityConcept.ACTIVITY_CONCEPT_USER;
		events = getActivityEventsForConcept(concept, lastNDays);
		
		return events;
	}
	
	private List<ActivityEvent> getActivityEventsForVendors(Integer lastNDays){
		
		List<ActivityEvent> events = null;
		ActivityConcept concept = ActivityConcept.ACTIVITY_CONCEPT_VENDOR;
		events = getActivityEventsForConcept(concept, lastNDays);
		
		return events;
	}
	
	private List<ActivityEvent> getActivityEventsForVersions(Integer lastNDays){
		
		List<ActivityEvent> events = null;
		ActivityConcept concept = ActivityConcept.ACTIVITY_CONCEPT_VERSION;
		events = getActivityEventsForConcept(concept, lastNDays);
		return events;
		
	}
	
	
	
	
	
	
	
	private List<ActivityEvent> getActivityEventsForACBs(){
		
		List<ActivityEvent> events = null;
		ActivityConcept concept = ActivityConcept.ACTIVITY_CONCEPT_CERTIFICATION_BODY;
		events = getActivityEventsForConcept(concept);
		
		return events;
	}
	
	
	private List<ActivityEvent> getActivityEventsForCertifications(){
		
		List<ActivityEvent> events = null;
		ActivityConcept concept = ActivityConcept.ACTIVITY_CONCEPT_CERTIFICATION;
		events = getActivityEventsForConcept(concept);
		
		return events;
	}
	
	
	private List<ActivityEvent> getActivityEventsForCertifiedProducts(){
		
		List<ActivityEvent> events = null;
		ActivityConcept concept = ActivityConcept.ACTIVITY_CONCEPT_CERTIFIED_PRODUCT;
		events = getActivityEventsForConcept(concept);
		
		return events;
	}
	
	private List<ActivityEvent> getActivityEventsForPendingCertifiedProducts(){
		
		List<ActivityEvent> events = null;
		ActivityConcept concept = ActivityConcept.ACTIVITY_CONCEPT_PENDING_CERTIFIED_PRODUCT;
		events = getActivityEventsForConcept(concept);
		
		return events;
	}
	
	private List<ActivityEvent> getActivityEventsForProducts(){
		
		List<ActivityEvent> events = null;
		ActivityConcept concept = ActivityConcept.ACTIVITY_CONCEPT_PRODUCT;
		events = getActivityEventsForConcept(concept);
		
		return events;
	}
	
	private List<ActivityEvent> getActivityEventsForUsers(){
		
		List<ActivityEvent> events = null;
		ActivityConcept concept = ActivityConcept.ACTIVITY_CONCEPT_USER;
		events = getActivityEventsForConcept(concept);
		
		return events;
	}
	
	private List<ActivityEvent> getActivityEventsForVendors(){
		
		List<ActivityEvent> events = null;
		ActivityConcept concept = ActivityConcept.ACTIVITY_CONCEPT_VENDOR;
		events = getActivityEventsForConcept(concept);
		
		return events;
	}
	
	private List<ActivityEvent> getActivityEventsForVersions(){
		
		List<ActivityEvent> events = null;
		ActivityConcept concept = ActivityConcept.ACTIVITY_CONCEPT_VERSION;
		events = getActivityEventsForConcept(concept);
		return events;
		
	}
	
	
	
	private List<ActivityEvent> getActivityEventsForConcept(ActivityConcept concept){
		return activityManager.getActivityForConcept(concept);
	}
	
	private List<ActivityEvent> getActivityEventsForConcept(ActivityConcept concept, Integer lastNDays){
		return activityManager.getActivityForConcept(concept);
	}
	
	private List<ActivityEvent> getActivityEventsForObject(ActivityConcept concept, Long objectId){
		return activityManager.getActivityForObject(concept, objectId);
	}
	
	private List<ActivityEvent> getActivityEventsForObject(ActivityConcept concept, Long objectId, Integer lastNDays){
		return activityManager.getActivityForObject(concept, objectId, lastNDays);
	}
	
	private List<ActivityEvent> getActivityEvents(Integer lastNDays){
		return activityManager.getAllActivityInLastNDays(lastNDays);
	}
	
	private List<ActivityEvent> getActivityEvents(){
		return activityManager.getAllActivity();
	}
	
	
	private List<ActivityEvent> getActivityEventsForConcept(Long conceptId){
		
		List<ActivityEvent> events = null;
		
		for (ActivityConcept concept : ActivityConcept.values()) {
			if(concept.getId().equals(conceptId)){
				events =  getActivityEventsForConcept(concept);
				break;
			}
		}
		return events;
	}
	
	private List<ActivityEvent> getActivityEventsForConcept(String conceptName){
		
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
