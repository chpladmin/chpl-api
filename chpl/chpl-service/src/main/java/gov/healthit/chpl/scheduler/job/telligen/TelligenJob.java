package gov.healthit.chpl.scheduler.job.telligen;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.http.client.utils.URIBuilder;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import gov.healthit.chpl.scheduler.job.QuartzJob;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class TelligenJob extends QuartzJob {
    private static final String COMMA = ",";
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        List<List<String>> records = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(getClass().getClassLoader().getResourceAsStream("todd.csv")))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(COMMA);
                records.add(Arrays.asList(values));
            }
        } catch (IOException e) {
            LOGGER.error(e);
        }

        for (List<String> record : records) {
            try {
                callEndpoint("localhost:3000" + record.get(0).toString().replaceAll("/$", ""));
            } catch (InterruptedException | IOException | URISyntaxException e) {
                LOGGER.error(e);
            }
        }
    }

    private void callEndpoint(String endpoint) throws IOException, InterruptedException, URISyntaxException {
        URIBuilder uriBuilder = new URIBuilder()
                .setScheme("https")
                .setHost(endpoint);

        HttpClient client = HttpClient.newBuilder().build();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uriBuilder.build())
                .header("API-Key", "405935904f3dddecc10328e689d69f77")
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        LOGGER.info("Http Code: {}", response.statusCode());
        LOGGER.info("Bytes Received: {}", response.body().length());
        LOGGER.info("Body: {}", response.body());
    }
}
