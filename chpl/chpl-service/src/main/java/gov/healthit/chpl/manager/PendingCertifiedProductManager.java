package gov.healthit.chpl.manager;


import java.util.List;

import org.springframework.security.acls.model.Permission;
import org.springframework.security.acls.model.Sid;

import com.fasterxml.jackson.core.JsonProcessingException;

import gov.healthit.chpl.auth.dto.UserDTO;
import gov.healthit.chpl.auth.user.UserRetrievalException;
import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.domain.CQMCriterion;
import gov.healthit.chpl.domain.PendingCertifiedProductDetails;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.dto.PendingCertifiedProductDTO;
import gov.healthit.chpl.entity.PendingCertifiedProductEntity;


public interface PendingCertifiedProductManager {
	public PendingCertifiedProductDetails getById(Long id) throws EntityRetrievalException;
	public List<PendingCertifiedProductDTO> getByAcb(CertificationBodyDTO acb);
	public List<PendingCertifiedProductDetails> getDetailsByAcb(CertificationBodyDTO acb);
	public List<PendingCertifiedProductDTO> getPending();
	public List<CQMCriterion> getApplicableCriteria(PendingCertifiedProductDTO pendingCpDto);
	
	public PendingCertifiedProductDTO createOrReplace(Long acbId, PendingCertifiedProductEntity toCreate) 
			throws EntityRetrievalException, EntityCreationException, JsonProcessingException;
			
	public void addAllVersionsToCmsCriterion(PendingCertifiedProductDetails pcpDetails);
	public void reject(Long pendingProductId) throws EntityRetrievalException, JsonProcessingException, EntityCreationException;
	public void confirm(Long pendingProductId) throws EntityRetrievalException, JsonProcessingException, EntityCreationException;
	public void addPermission(CertificationBodyDTO acb, PendingCertifiedProductDTO pcpDto, Long userId, Permission permission) throws UserRetrievalException;
	public void addPermission(CertificationBodyDTO acb, PendingCertifiedProductDTO pcpDto, UserDTO user, Permission permission);
	public void addPermissionToAllPendingCertifiedProductsOnAcb(CertificationBodyDTO acb, UserDTO user, Permission permission);
	public void deleteUserPermissionFromAllPendingCertifiedProductsOnAcb(CertificationBodyDTO acb, Sid user);
	public void deletePermission(PendingCertifiedProductDTO pcpDto, Sid recipient, Permission permission);
	public void deleteUserPermissionsOnPendingCertifiedProduct(PendingCertifiedProductDTO pcpDto, Sid recipient);
	public void deletePermissionsForUser(UserDTO userDto) throws UserRetrievalException;
}
