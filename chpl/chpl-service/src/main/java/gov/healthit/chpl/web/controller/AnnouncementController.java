package gov.healthit.chpl.web.controller;


import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;

import gov.healthit.chpl.auth.manager.UserManager;
import gov.healthit.chpl.auth.user.UserRetrievalException;
import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.domain.Announcement;
import gov.healthit.chpl.dto.AnnouncementDTO;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.manager.AnnouncementManager;
import gov.healthit.chpl.manager.impl.UpdateCertifiedBodyException;
import gov.healthit.chpl.web.controller.results.AnnouncementResults;
import io.swagger.annotations.Api;

@Api(value="announcements")
@RestController
@RequestMapping("/announcements")
public class AnnouncementController {
	
	@Autowired AnnouncementManager announcementManager;
	@Autowired UserManager userManager;
	
	private static final Logger logger = LogManager.getLogger(AnnouncementController.class);
	
	@RequestMapping(value="/", method=RequestMethod.GET,produces="application/json; charset=utf-8")
	public @ResponseBody AnnouncementResults getAnnouncements(
			@RequestParam(required=false, defaultValue="false") boolean future) {
		AnnouncementResults results = new AnnouncementResults();
		List<AnnouncementDTO> announcements = null;
		if(!future){
			announcements = announcementManager.getAll();
		}else{
			announcements = announcementManager.getAllCurrentAndFuture();
		}
		if(announcements != null) {
			for(AnnouncementDTO announcement : announcements) {
				results.getAnnouncements().add(new Announcement(announcement));
			}
		}
		return results;
	}
	
	@RequestMapping(value="/{announcementId}", method=RequestMethod.GET,
			produces="application/json; charset=utf-8")
	public @ResponseBody Announcement getAnnouncementById(@PathVariable("announcementId") Long announcementId)
		throws EntityRetrievalException {
		AnnouncementDTO announcement = announcementManager.getById(announcementId);
		
		return new Announcement(announcement);
	}
	
	@RequestMapping(value="/create", method= RequestMethod.POST, 
			consumes= MediaType.APPLICATION_JSON_VALUE,
			produces="application/json; charset=utf-8")
	public Announcement createAnnouncement(@RequestBody Announcement announcementInfo) throws InvalidArgumentsException, UserRetrievalException, EntityRetrievalException, EntityCreationException, JsonProcessingException {
		AnnouncementDTO toCreate = new AnnouncementDTO();
		if(StringUtils.isEmpty(announcementInfo.getTitle())) {
			throw new InvalidArgumentsException("A title is required for a new announcement");
		}else{
			toCreate.setTitle(announcementInfo.getTitle());
		}
		toCreate.setText(announcementInfo.getText());
		if(StringUtils.isEmpty(announcementInfo.getIsPublic())) {
			throw new InvalidArgumentsException("A public permission is required for a new announcement");
		}else{
			toCreate.setIsPublic(announcementInfo.getIsPublic());
		}
		if(StringUtils.isEmpty(announcementInfo.getStartDate())) {
			throw new InvalidArgumentsException("A start date is required for a new announcement");
		}else{
			toCreate.setStartDate(announcementInfo.getStartDate());
		}
		if(StringUtils.isEmpty(announcementInfo.getEndDate())) {
			throw new InvalidArgumentsException("An end date is required for a new announcement");
		}else{
			toCreate.setEndDate(announcementInfo.getEndDate());
		}
		
		toCreate = announcementManager.create(toCreate);
		return new Announcement(toCreate);
	}
	

	@RequestMapping(value="/update", method= RequestMethod.POST, 
			consumes= MediaType.APPLICATION_JSON_VALUE,
			produces="application/json; charset=utf-8")
	public Announcement updateAnnouncement(@RequestBody Announcement announcementInfo) throws InvalidArgumentsException, EntityRetrievalException, JsonProcessingException, EntityCreationException, UpdateCertifiedBodyException {
		AnnouncementDTO toUpdate = new AnnouncementDTO();
		toUpdate.setId(announcementInfo.getId());
		toUpdate.setTitle(announcementInfo.getTitle());
		toUpdate.setText(announcementInfo.getText());
		toUpdate.setIsPublic(announcementInfo.getIsPublic());
		toUpdate.setStartDate(announcementInfo.getStartDate());
		toUpdate.setEndDate(announcementInfo.getEndDate());

		
		AnnouncementDTO result = announcementManager.update(toUpdate);
		return new Announcement(result);
	}
	
	
	@RequestMapping(value="/{announcementId}/delete", method= RequestMethod.POST,
			produces="application/json; charset=utf-8")
	public String deleteAnnouncement(@PathVariable("announcementId") Long announcementId) 
			throws JsonProcessingException, EntityCreationException, EntityRetrievalException,
			UserRetrievalException {
		
		AnnouncementDTO toDelete = announcementManager.getById(announcementId,false);		
		announcementManager.delete(toDelete);
		return "{\"deletedAnnouncement\" : true }";
	}
	
	@RequestMapping(value="/{announcementId}/undelete", method= RequestMethod.POST,
			produces="application/json; charset=utf-8")
	public String undeleteAnnouncement(@PathVariable("announcementId") Long announcementId) 
			throws JsonProcessingException, EntityCreationException, EntityRetrievalException,
			UserRetrievalException {
		
		AnnouncementDTO toResurrect = announcementManager.getById(announcementId,true);		
		announcementManager.undelete(toResurrect);
		return "{\"resurrectedAnnouncement\" : true }";
	}
	
}

