package gov.healthit.chpl.report.questionableurl;

import java.util.Date;

import org.hibernate.annotations.Immutable;

import gov.healthit.chpl.scheduler.job.urlStatus.data.UrlType;
import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Immutable
@Table(name = "questionable_url_details")
public class QuestionableUrlDetailEntity {
    private static final long serialVersionUID = 50014152563463240L;

    @Id
    @Column(name = "id")
    private Long id;

    @Column(name = "item_id")
    private Long itemId;

    @Column(name = "item_name")
    private String itemName;

    @Column(name = "url")
    private String url;

    @Column(name = "url_type")
    private String urlType;

    @Column(name = "response_code")
    private Integer responseCode;

    @Column(name = "response_message")
    private String responseMessage;

    @Basic(optional = false)
    @Column(name = "checked_date")
    private Date lastChecked;

    public QuestionableUrlDetailReport toDomain() {
        return QuestionableUrlDetailReport.builder()
                .lastChecked(lastChecked)
                .relatedItem(itemName)
                .responseCode(responseCode)
                .responseMessage(responseMessage)
                .url(url)
                .urlType(UrlType.findByName(urlType))
                .build();
    }
}
