package gov.healthit.chpl.entity;

import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import gov.healthit.chpl.domain.Announcement;
import gov.healthit.chpl.util.DateUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
@Table(name = "announcement")
public class AnnouncementEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "announcement_id", nullable = false)
    private Long id;

    @Basic(optional = false)
    @Column(name = "announcement_title", nullable = false)
    private String title;

    @Basic(optional = true)
    @Column(name = "announcement_text", nullable = false)
    private String text;

    @Basic(optional = false)
    @Column(name = "start_date", nullable = false)
    private Date startDate;

    @Basic(optional = false)
    @Column(name = "end_date", nullable = false)
    private Date endDate;

    @Basic(optional = false)
    @Column(name = "ispublic", nullable = false)
    private Boolean isPublic;

    @Basic(optional = false)
    @Column(name = "creation_date", nullable = false)
    private Date creationDate;

    @Basic(optional = false)
    @Column(name = "last_modified_date", nullable = false)
    private Date lastModifiedDate;

    @Basic(optional = false)
    @Column(name = "last_modified_user", nullable = false)
    private Long lastModifiedUser;

    @Basic(optional = false)
    @Column(nullable = false, name = "deleted")
    private Boolean deleted;

    public Announcement toDomain() {
        return Announcement.builder()
                .id(this.getId())
                .title(this.getTitle())
                .text(this.getText())
                .startDateTime(DateUtil.toLocalDateTime(this.getStartDate().getTime()))
                .endDateTime(DateUtil.toLocalDateTime(this.getEndDate().getTime()))
                .startDate(this.getStartDate())
                .endDate(this.getEndDate())
                .isPublic(this.getIsPublic())
                .build();
    }
}
