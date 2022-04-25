package gov.healthit.chpl.sharedstore;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class SharedStore {
    private String domain;
    private String key;
    private String value;
    private LocalDateTime putDate;
}
