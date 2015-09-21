package gov.healthit.chpl.manager;


import java.io.IOException;
import java.util.List;

import org.springframework.security.acls.model.Permission;
import org.springframework.security.acls.model.Sid;
import org.springframework.web.multipart.MultipartFile;

import gov.healthit.chpl.auth.dto.UserDTO;
import gov.healthit.chpl.auth.user.UserRetrievalException;
import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.domain.PendingCertifiedProductDetails;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.dto.PendingCertifiedProductDTO;
import gov.healthit.chpl.entity.PendingCertifiedProductEntity;
import gov.healthit.chpl.web.controller.InvalidArgumentsException;


public interface PendingCertifiedProductManager {
	public List<PendingCertifiedProductDetails> upload( MultipartFile file) 
			throws InvalidArgumentsException, EntityRetrievalException, IOException;
	public List<PendingCertifiedProductDTO> getAll();
	public List<PendingCertifiedProductDetails> getAllDetails();
	public PendingCertifiedProductDTO getById(Long id) throws EntityRetrievalException;
	public PendingCertifiedProductDetails getDetailsById(Long id) throws EntityRetrievalException;
	public List<PendingCertifiedProductDTO> getByAcb(CertificationBodyDTO acb);
	public List<PendingCertifiedProductDetails> getDetailsByAcb(CertificationBodyDTO acb);
		
	public PendingCertifiedProductDTO create(Long acbId, PendingCertifiedProductEntity toCreate) 
			throws EntityRetrievalException, EntityCreationException;
			
	public void delete(PendingCertifiedProductDTO product);
	public void addPermission(CertificationBodyDTO acb, PendingCertifiedProductDTO pcpDto, Long userId, Permission permission) throws UserRetrievalException;
	public void addPermission(CertificationBodyDTO acb, PendingCertifiedProductDTO pcpDto, UserDTO user, Permission permission);
	public void addPermissionToAllPendingCertifiedProductsOnAcb(CertificationBodyDTO acb, UserDTO user, Permission permission);
	public void deleteUserPermissionFromAllPendingCertifiedProductsOnAcb(CertificationBodyDTO acb, Sid user);
	public void deletePermission(PendingCertifiedProductDTO pcpDto, Sid recipient, Permission permission);
	public void deleteUserPermissionsOnPendingCertifiedProduct(PendingCertifiedProductDTO pcpDto, Sid recipient);
	public void deletePermissionsForUser(UserDTO userDto) throws UserRetrievalException;
}
