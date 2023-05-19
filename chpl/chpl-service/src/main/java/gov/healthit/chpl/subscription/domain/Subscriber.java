package gov.healthit.chpl.subscription.domain;

import java.util.UUID;

import javax.xml.bind.annotation.XmlTransient;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Subscriber {
    private UUID id;
    private SubscriberStatus status;
    private String email;

    @JsonIgnore
    @XmlTransient
    private String token;
}
