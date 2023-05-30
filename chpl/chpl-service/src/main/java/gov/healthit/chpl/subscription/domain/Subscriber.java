package gov.healthit.chpl.subscription.domain;

import java.util.UUID;

import org.apache.commons.lang3.StringUtils;

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
    private SubscriberRole role;
    private String email;

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        } else if (!(o instanceof Subscriber)) {
            return false;
        }
        Subscriber otherSubscriber = (Subscriber) o;
        if (otherSubscriber.id == null || this.id == null) {
            return false;
        }
        return StringUtils.equals(this.id.toString(), otherSubscriber.id.toString());
    }

    @Override
    public int hashCode() {
        if (this.id == null) {
            return -1;
        }
        return this.id.hashCode();
    }
}
