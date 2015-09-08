package gov.healthit.chpl.domain;

import org.springframework.security.acls.domain.BasePermission;
import org.springframework.security.acls.model.Permission;

public enum CertificationBodyPermission {
	READ(),
	DELETE(),
	ADMIN();
	
	public static Permission toPermission(CertificationBodyPermission cbp) {
		Permission permission = null;
		if(cbp == CertificationBodyPermission.READ) {
			permission = BasePermission.READ;
		} else if(cbp == CertificationBodyPermission.ADMIN) {
			permission = BasePermission.ADMINISTRATION;
		} else if(cbp == CertificationBodyPermission.DELETE) {
			permission = BasePermission.DELETE;
		}
		return permission;
	}
	
	public static CertificationBodyPermission fromPermission(Permission perm) {
		CertificationBodyPermission permission = null;
		if(perm == BasePermission.READ) {
			return READ;
		} else if(perm == BasePermission.ADMINISTRATION) {
			return ADMIN;
		} else if(perm == BasePermission.DELETE) {
			return DELETE;
		}
		return permission;
	}
}
