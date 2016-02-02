package gov.healthit.chpl.domain;

import org.springframework.security.acls.domain.BasePermission;
import org.springframework.security.acls.model.Permission;

public enum ChplPermission {
	READ(),
	DELETE(),
	ADMIN();
	
	public static Permission toPermission(ChplPermission cbp) {
		Permission permission = null;
		if(cbp == ChplPermission.READ) {
			permission = BasePermission.READ;
		} else if(cbp == ChplPermission.ADMIN) {
			permission = BasePermission.ADMINISTRATION;
		} else if(cbp == ChplPermission.DELETE) {
			permission = BasePermission.DELETE;
		}
		return permission;
	}
	
	public static ChplPermission fromPermission(Permission perm) {
		ChplPermission permission = null;
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
