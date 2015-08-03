package gov.healthit.chpl.web.controller;

import java.util.List;

import gov.healthit.chpl.acb.CertificationBodyManager;
import gov.healthit.chpl.auth.user.UserRetrievalException;
import gov.healthit.chpl.domain.CQMResult;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.entity.CertificationBody;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CHPLServiceController {
	
	
	@Autowired
	private CertificationBodyManager certificationBodyManager;
	
	
	@RequestMapping(value="/hello/{firstName}/{lastName}", method= RequestMethod.GET, produces="application/json; charset=utf-8")
	public String hello(@PathVariable String firstName, @PathVariable String lastName) {
		
		return "{\"firstName\" : \""+firstName+"\", \"lastName\" : \""+lastName+"\" }";
		
	}
	
	
	@RequestMapping(value="/adminACB/{acbID}", method= RequestMethod.GET, produces="application/json; charset=utf-8")
	public String adminACB(@PathVariable String acbID) {
		
		List<CertificationBody> authorizedACBs = certificationBodyManager.getAll();
		
		if (authorizedACBs.size() > 0){
			return "{\"AdminsteringACB:\" : \""+acbID+"\"}";
		} else {
			return "{\"AdminsteringACB:\" : \"-1\"}";
		}
	}
	
	@RequestMapping(value="/createACB/{acbName}", method= RequestMethod.GET, produces="application/json; charset=utf-8")
	public String createACB(@PathVariable String acbName) {
		
		System.out.println("Claims:");
		System.out.println(SecurityContextHolder.getContext().getAuthentication().getAuthorities());
		
		CertificationBody acb = new CertificationBody();
		acb.setName(acbName);
		acb.setWebsite("www.zombo.com");
		certificationBodyManager.create(acb);
		
		return acb.toString();
		
	}
	
	@RequestMapping(value="/renameACB/{acbId}/{acbName}", method= RequestMethod.GET, produces="application/json; charset=utf-8")
	public String updateACBName(@PathVariable Long acbId, @PathVariable String acbName) {
		
		CertificationBody acb = certificationBodyManager.getById(acbId);
		acb.setName(acbName);
		certificationBodyManager.update(acb);
		
		return acb.toString();
		
	}
	
	@RequestMapping(value="/listMyACBs", method= RequestMethod.GET, produces="application/json; charset=utf-8")
	public String listMyACBs() {
		
		List<CertificationBody> acbs = certificationBodyManager.getAll();
		
		System.out.println("found: ");
		System.out.println(acbs.size());
		System.out.println("ACBs");
		
		for (CertificationBody cb : acbs){
			
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
