package gov.healthit.chpl.domain.compliance;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class DirectReview implements Serializable {
    private static final long serialVersionUID = 7018071377912371691L;

    @JsonProperty(value = "key")
    private String key;

    @JsonProperty(value = "chplProductNumber")
    @JsonAlias("customfield_10213")
    private String chplProductNumber;

    @JsonProperty(value = "startDate")
    @JsonAlias("customfield_10919")
    @JsonDeserialize(using = DateDeserializer.class)
    private LocalDate startDate;

    @JsonProperty(value = "endDate")
    @JsonAlias("customfield_10920")
    @JsonDeserialize(using = DateDeserializer.class)
    private LocalDate endDate;

    @JsonProperty(value = "circumstances")
    @JsonAlias("customfield_10932")
    @JsonDeserialize(using = CircumstancesDeserializer.class)
    private List<String> circumstances = new ArrayList<String>();

    @JsonProperty(value = "lastUpdated")
    @JsonAlias("updated")
    @JsonDeserialize(using = TimestampDeserializer.class)
    private Date lastUpdated;

    @JsonProperty(value = "created")
    @JsonDeserialize(using = TimestampDeserializer.class)
    private Date created;
}
