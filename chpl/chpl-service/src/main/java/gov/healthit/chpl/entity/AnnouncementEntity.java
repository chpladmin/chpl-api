package gov.healthit.chpl.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import gov.healthit.chpl.domain.Announcement;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@ToString
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "announcement")
public class AnnouncementEntity extends EntityAudit {
    private static final long serialVersionUID = 6927035351584714349L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "announcement_id", nullable = false)
    private Long id;

    @Column(name = "announcement_title", nullable = false)
    private String title;

    @Column(name = "announcement_text", nullable = false)
    private String text;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    @Column(name = "ispublic", nullable = false)
    private Boolean isPublic;

    public Announcement toDomain() {
        return Announcement.builder()
                .id(this.getId())
                .title(this.getTitle())
                .text(this.getText())
                .startDateTime(this.getStartDate())
                .endDateTime(this.getEndDate())
                .isPublic(this.getIsPublic())
                .creationDate(this.getCreationDate())
                .lastModifiedDate(this.getLastModifiedDate())
                .lastModifiedUser(this.getLastModifiedUser())
                .deleted(this.getDeleted())
                .build();
    }
}
