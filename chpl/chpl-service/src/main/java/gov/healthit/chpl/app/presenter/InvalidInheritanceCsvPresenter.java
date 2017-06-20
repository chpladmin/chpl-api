package gov.healthit.chpl.app.presenter;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.ListingGraphDAO;
import gov.healthit.chpl.domain.CertifiedProduct;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.dto.CertificationCriterionDTO;
import gov.healthit.chpl.dto.CertifiedProductDTO;

@Component("invalidInheritanceCsvPresenter")
public class InvalidInheritanceCsvPresenter extends CertifiedProductCsvPresenter {
	private static final Logger logger = LogManager.getLogger(InvalidInheritanceCsvPresenter.class);
	private Properties props;
	private ListingGraphDAO inheritanceDao;
	
	protected List<String> generateHeaderValues() {
		List<String> result = new ArrayList<String>();
		result.add("CHPL ID");
		result.add("Developer");
		result.add("Product");
		result.add("Version");
		result.add("ONC-ACB");
		result.add("URL");
		if(getApplicableCriteria() != null) {
			for(CertificationCriterionDTO criteria : getApplicableCriteria()) {
				result.add(criteria.getNumber());
			}
		}
		return result;
	}
	
	protected List<String> generateRowValue(CertifiedProductSearchDetails data) {
		//determine if this listing is breaking any of the ICS rules we want to trigger on
		if(breaksIcsRules(data)) {
			List<String> result = new ArrayList<String>();
			result.add(data.getChplProductNumber());
			result.add(data.getDeveloper().getName());
			result.add(data.getProduct().getName());
			result.add(data.getVersion().getVersion());
			result.add(data.getCertifyingBody().get("name").toString());
			String productDetailsUrl = props.getProperty("chplUrlBegin").trim();
			if(!productDetailsUrl.endsWith("/")) {
				productDetailsUrl += "/";
			} 
			productDetailsUrl += "#/product/" + data.getId();
			result.add(productDetailsUrl);
			List<String> criteria = generateCriteriaValues(data);
			result.addAll(criteria);
			return result;
		}
		return null;
	}
	
	private boolean breaksIcsRules(CertifiedProductSearchDetails listing) {
		String uniqueId = listing.getChplProductNumber();
		String[] uniqueIdParts = uniqueId.split("\\.");
		if(uniqueIdParts == null || uniqueIdParts.length != CertifiedProductDTO.CHPL_PRODUCT_ID_PARTS) {
			return false;
		}
		String icsCodePart = uniqueIdParts[CertifiedProductDTO.ICS_CODE_INDEX];
		try {
			Integer icsCode = new Integer(icsCodePart);
			boolean hasIcs = icsCode.intValue() == 1 ||
					(listing.getIcs() != null && listing.getIcs().getInherits() == Boolean.TRUE);
			
			//check if listing has ICS but no family ties
			if(hasIcs && (listing.getIcs() == null || listing.getIcs().getParents() == null || 
					listing.getIcs().getParents().size() == 0)) {
				return true;
			}
			
			//check if this listing has correct ICS incrementation
			//this listing's ICS code must be greater than the max of parent ICS codes
			if(hasIcs && listing.getIcs() != null && listing.getIcs().getParents() != null && 
					listing.getIcs().getParents().size() > 0) {
				List<Long> parentIds = new ArrayList<Long>();
				for(CertifiedProduct potentialParent : listing.getIcs().getParents()) {
					parentIds.add(potentialParent.getId());
				}
				
				Integer largestIcs = getInheritanceDao().getLargestIcs(parentIds);
				if(largestIcs != null && icsCode.intValue() != (largestIcs.intValue()+1)) {
					return true;
				}
			}
		} catch(Exception ex) {
			logger.error("Could not compare ICS value " + icsCodePart + " to inherits boolean value", ex);
		}
		return false;
	}
	
	public Properties getProps() {
		return props;
	}

	public void setProps(Properties props) {
		this.props = props;
	}

	public ListingGraphDAO getInheritanceDao() {
		return inheritanceDao;
	}

	public void setInheritanceDao(ListingGraphDAO inheritanceDao) {
		this.inheritanceDao = inheritanceDao;
	}

}
