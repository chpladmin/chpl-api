package gov.healthit.chpl.scheduler.job.urluptime;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ServiceBasedUrl {
    private String url;
    private Long developerId;
    private List<String> chplProductNumbers;

    public String getDatadogFormattedUrl() {
        if (url == null) {
            return null;
        }
        String[] splitUrl = url.split(":");
        StringBuffer updateUrl = new StringBuffer();
        return updateUrl.append(splitUrl[0].toLowerCase())
                .append(":")
                .append(Arrays.stream(splitUrl, 1, splitUrl.length).collect(Collectors.joining()))
                .toString();
    }
}
