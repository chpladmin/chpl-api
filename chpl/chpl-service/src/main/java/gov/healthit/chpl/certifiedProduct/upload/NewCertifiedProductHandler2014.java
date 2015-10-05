package gov.healthit.chpl.certifiedProduct.upload;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CQMCriterion;
import gov.healthit.chpl.entity.CQMCriterionEntity;
import gov.healthit.chpl.entity.PendingCqmCriterionEntity;
import gov.healthit.chpl.web.controller.InvalidArgumentsException;

@Component("newCertifiedProductHandler2014")
public class NewCertifiedProductHandler2014 extends NewCertifiedProductHandler {
	
	public List<CQMCriterion> getApplicableCqmCriterion(List<CQMCriterion> allCqms) {
		List<CQMCriterion> criteria = new ArrayList<CQMCriterion>();
		for (CQMCriterion criterion : allCqms) {
			if (criterion.getNumber().startsWith("CMS")) {
				criteria.add(criterion);
			}
		}
		return criteria;
	}
	
	/**
	 * 2014 products don't have NQF results
	 * @param criterionNum
	 * @param column
	 * @return
	 * @throws InvalidArgumentsException
	 */
	public PendingCqmCriterionEntity handleCqmNqfCriterion(String criterionNum, int column) throws InvalidArgumentsException {
		return null;
	}
	
	/**
	 * look up a CQM CMS criteria by number and version. throw an error if we can't find it
	 * @param criterionNum
	 * @param column
	 * @return
	 * @throws InvalidArgumentsException
	 */
	public PendingCqmCriterionEntity handleCqmCmsCriterion(String criterionNum, int column) throws InvalidArgumentsException {
		String version = getRecord().get(column);
		if(version != null) {
			version = version.trim();
		}
		
		CQMCriterionEntity cqmEntity = cqmDao.getEntityByNumberAndVersion(criterionNum, version);
		if(cqmEntity == null) {
			throw new InvalidArgumentsException("Could not find a CQM CMS criterion matching " + criterionNum + " and version " + version);
		}
		
		PendingCqmCriterionEntity result = new PendingCqmCriterionEntity();
		result.setMappedCriterion(cqmEntity);
		result.setMeetsCriteria(true);	

		return result;
	}
}
