package gov.healthit.chpl.auth.user;

import gov.healthit.chpl.auth.permission.UserPermission;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.ManyToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;


@Entity
@Table(name="global_user_permission_map")
@IdClass(UserPermissionUserMappingId.class)
//@SQLDelete(sql = "UPDATE global_user_permission_map SET deleted = true WHERE user_permission_id = ?")
@Where(clause = "deleted = false")
public class UserPermissionUserMapping {

	  @Id
	  private long userId;
	  @Id
	  private long permissionId;
	  
	  @Column(name="deleted")
	  private boolean deleted;
	  
	  
	  @ManyToOne
	  @PrimaryKeyJoinColumn(name="user_id", referencedColumnName="user_id")
	  /* if this JPA model doesn't create a table for the "PROJ_EMP" entity,
	  *  please comment out the @PrimaryKeyJoinColumn, and use the ff:
	  *  @JoinColumn(name = "employeeId", updatable = false, insertable = false)
	  * or @JoinColumn(name = "employeeId", updatable = false, insertable = false, referencedColumnName = "id")
	  */
	  private UserImpl user;
	  
	  @ManyToOne
	  @PrimaryKeyJoinColumn(name="user_permission_id_user_permission", referencedColumnName="user_permission_id")
	  /* the same goes here:
	  *  if this JPA model doesn't create a table for the "PROJ_EMP" entity,
	  *  please comment out the @PrimaryKeyJoinColumn, and use the ff:
	  *  @JoinColumn(name = "projectId", updatable = false, insertable = false)
	  * or @JoinColumn(name = "projectId", updatable = false, insertable = false, referencedColumnName = "id")
	  */
	  private UserPermission permission;
	

}
