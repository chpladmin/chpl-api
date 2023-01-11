package gov.healthit.chpl.qmsStandard;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
@Table(name = "qms_standard")
public class QmsStandardEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "qms_standard_id")
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "deleted", insertable = false)
    private Boolean deleted;

    @Column(name = "last_modified_user")
    private Long lastModifiedUser;

    @Column(name = "creation_date", insertable = false, updatable = false)
    private Date creationDate;

    @Column(name = "last_modified_date", insertable = false, updatable = false)
    private Date lastModifiedDate;

    public QmsStandard toDomain() {
        return QmsStandard.builder()
                .id(this.getId())
                .name(this.getName())
                .build();
    }
}
