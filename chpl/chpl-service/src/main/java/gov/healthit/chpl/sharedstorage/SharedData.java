package gov.healthit.chpl.sharedstorage;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class SharedData {
    private String domain;
    private String key;
    private String value;
    private LocalDateTime putDate;
}
