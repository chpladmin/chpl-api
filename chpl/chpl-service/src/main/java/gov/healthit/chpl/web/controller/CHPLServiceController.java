package gov.healthit.chpl.web.controller;

import java.util.List;



import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.manager.CertificationBodyManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CHPLServiceController {
	
	
	@Autowired
	private CertificationBodyManager certificationBodyManager;
	
	
	@RequestMapping(value="/hello/{firstName}/{lastName}", method= RequestMethod.GET, produces="application/json; charset=utf-8")
	public String hello(@PathVariable String firstName, @PathVariable String lastName) {
		
		return "{\"firstName\" : \""+firstName+"\", \"lastName\" : \""+lastName+"\" }";
		
	}
	
	
	@RequestMapping(value="/createACB/{acbName}", method= RequestMethod.GET, produces="application/json; charset=utf-8")
	public String createACB(@PathVariable String acbName) throws EntityCreationException {
		
		System.out.println("Claims:");
		System.out.println(SecurityContextHolder.getContext().getAuthentication().getAuthorities());
		
		CertificationBodyDTO acb = new CertificationBodyDTO();
		acb.setName(acbName);
		acb.setWebsite("www.zombo.com");
		certificationBodyManager.create(acb);
		
		return acb.toString();
		
	}
	
	@RequestMapping(value="/listMyACBs", method= RequestMethod.GET, produces="application/json; charset=utf-8")
	public String listMyACBs() {
		
		List<CertificationBodyDTO> acbs = certificationBodyManager.getAll();
		
		System.out.println("found: ");
		System.out.println(acbs.size());
		System.out.println("ACBs");
		
		for (CertificationBodyDTO cb : acbs){
			
			System.out.println(cb.toString());
			System.out.println(cb.getName());
			
		}
		
		return certificationBodyManager.getAll().toArray().toString();
		
	}
	
	public CertificationBodyManager getCertificationBodyManager() {
		return certificationBodyManager;
	}


	public void setCertificationBodyManager(
			CertificationBodyManager certificationBodyManager) {
		this.certificationBodyManager = certificationBodyManager;
	}
	
}
