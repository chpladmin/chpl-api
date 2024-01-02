package gov.healthit.chpl.scheduler.job.telligen;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
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
        try (BufferedReader br = new BufferedReader(new InputStreamReader(getClass().getClassLoader().getResourceAsStream("telligen.csv")))) {
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
                callEndpoint("https://chpl-stg.healthit.gov" + record.get(0).toString());
            } catch (InterruptedException | IOException | URISyntaxException e) {
                LOGGER.error(e);
            }
        }
    }

    private void callEndpoint(String endpoint) throws IOException, InterruptedException, URISyntaxException {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(endpoint);
            request.addHeader("API-Key", "405935904f3dddecc10328e689d69f77");
            CloseableHttpResponse response = client.execute(request);

            String body = new String(response.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8);
            LOGGER.info("URI: {}", request.toString());
            LOGGER.info("Http Code: {}", response.getStatusLine().getStatusCode());
            LOGGER.info("Bytes Received: {}", body.length());
            LOGGER.info("Body: {}", body);
        }

    }
}
