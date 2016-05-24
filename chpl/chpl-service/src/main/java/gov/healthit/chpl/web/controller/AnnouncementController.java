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
import io.swagger.annotations.ApiOperation;

@Api(value="announcements")
@RestController
@RequestMapping("/announcements")
public class AnnouncementController {
	
	@Autowired AnnouncementManager announcementManager;
	@Autowired UserManager userManager;
	
	private static final Logger logger = LogManager.getLogger(AnnouncementController.class);
	
	@ApiOperation(value="Get all announcements.", 
			notes="The announcement listing is open to anyone, however announcements may be both public and private and "
					+ " only public announcements will be returned if a non-authenticated user calls this method. "
					+ " Both public and private announcements will be returned to an authenticated user."
					+ " Scheduled future announcements can be retrieved by setting the 'future' flag to true "
					+ " and only CHPL users with ROLE_ADMIN will be granted access to that data.")
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
	@ApiOperation(value="Get a specific announcement.")
	@RequestMapping(value="/{announcementId}", method=RequestMethod.GET,
			produces="application/json; charset=utf-8")
	public @ResponseBody Announcement getAnnouncementById(@PathVariable("announcementId") Long announcementId)
		throws EntityRetrievalException {
		AnnouncementDTO announcement = announcementManager.getById(announcementId);
		
		return new Announcement(announcement);
	}
	
	@ApiOperation(value="Create a new announcement.", 
			notes="Only CHPL users with ROLE_ADMIN are able to create announcements.")
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
	

	@ApiOperation(value="Change an existing announcement.", 
			notes="Only CHPL users with ROLE_ADMIN are able to update announcements.")
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
	
	@ApiOperation(value="Delete an existing announcement.", 
			notes="Only CHPL users with ROLE_ADMIN are able to delete announcements.")
	@RequestMapping(value="/{announcementId}/delete", method= RequestMethod.POST,
			produces="application/json; charset=utf-8")
	public String deleteAnnouncement(@PathVariable("announcementId") Long announcementId) 
			throws JsonProcessingException, EntityCreationException, EntityRetrievalException,
			UserRetrievalException {
		
		AnnouncementDTO toDelete = announcementManager.getById(announcementId,false);		
		announcementManager.delete(toDelete);
		return "{\"deletedAnnouncement\" : true }";
	}
}

