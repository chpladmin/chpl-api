package gov.healthit.chpl.certifiedProduct.upload;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import gov.healthit.chpl.domain.CQMCriterion;
import gov.healthit.chpl.entity.CQMCriterionEntity;
import gov.healthit.chpl.entity.PendingCqmCriterionEntity;
import gov.healthit.chpl.web.controller.InvalidArgumentsException;

@Component("newCertifiedProductHandler2011")
public class NewCertifiedProductHandler2011 extends NewCertifiedProductHandler {
	
	public List<CQMCriterion> getApplicableCqmCriterion(List<CQMCriterion> allCqms) {
		List<CQMCriterion> criteria = new ArrayList<CQMCriterion>();
		for (CQMCriterion criterion : allCqms) {
			if (criterion.getNumber().startsWith("NQF")) {
				criteria.add(criterion);
			}
		}
		return criteria;
	}
	
	/**
	 * look up an NQF type of CQM by name/number. throw an error if we can't find it
	 * @param criterionNum
	 * @param column
	 * @return
	 * @throws InvalidArgumentsException
	 */
	public PendingCqmCriterionEntity handleCqmNqfCriterion(String criterionNum, int column) throws InvalidArgumentsException {
		CQMCriterionEntity cqmEntity = cqmDao.getEntityByNumber(criterionNum);
		if(cqmEntity == null) {
			throw new InvalidArgumentsException("Could not find a CQM NQF criterion matching " + criterionNum);
		}
		
		PendingCqmCriterionEntity result = new PendingCqmCriterionEntity();
		result.setMappedCriterion(cqmEntity);
		boolean meetsCriteria = false;
		if(!StringUtils.isEmpty(getRecord().get(column))) {
			int value = new Integer(getRecord().get(column)).intValue();
			if(value > 0) {
				meetsCriteria = true;
			}
		} 
		result.setMeetsCriteria(meetsCriteria);		
		return result;
	}
	
	/**
	 * 2011 products don't have CMS results
	 * @param criterionNum
	 * @param column
	 * @return
	 * @throws InvalidArgumentsException
	 */
	public PendingCqmCriterionEntity handleCqmCmsCriterion(String criterionNum, int column) throws InvalidArgumentsException {
		return null;
	}
}
