package gov.healthit.chpl.entity.notification;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.Where;


@Entity
@Immutable
@Table(name = "notification_recipient")
public class RecipientWithSubscriptionsEntity {
	
	@Id 
	@Column(name = "id")
	private Long id;
	
	@Column(name = "email")
	private String email;
	
	@OneToMany( fetch = FetchType.LAZY, mappedBy = "recipientId" )
	@Column( name = "recipient_id", nullable = false  )
	@Where(clause="deleted <> 'true'")
	private Set<NotificationTypeRecipientMapEntity> subscriptions = new HashSet<NotificationTypeRecipientMapEntity>();
	
	@Column(name = "deleted")
	private Boolean deleted;
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public Set<NotificationTypeRecipientMapEntity> getSubscriptions() {
		return subscriptions;
	}

	public void setSubscriptions(Set<NotificationTypeRecipientMapEntity> subscriptions) {
		this.subscriptions = subscriptions;
	}

	public Boolean getDeleted() {
		return deleted;
	}

	public void setDeleted(Boolean deleted) {
		this.deleted = deleted;
	}
}
