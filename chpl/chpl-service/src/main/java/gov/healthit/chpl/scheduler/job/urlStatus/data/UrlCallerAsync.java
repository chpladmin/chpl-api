package gov.healthit.chpl.scheduler.job.urlStatus.data;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

@Service("urlCallerAsync")
public class UrlCallerAsync {

    public CompletableFuture<Integer> getUrlResponseCodeFuture(
            UrlResult urlToCheck, CloseableHttpClient httpClient, ExecutorService executorService, Logger logger) throws Exception {
        CompletableFuture<Integer> future =
                CompletableFuture.supplyAsync(() -> getUrlResponseCode(httpClient, urlToCheck, logger), executorService);
        return future;
    }

    private Integer getUrlResponseCode(CloseableHttpClient httpClient, UrlResult urlToCheck, Logger logger) throws CompletionException {
        logger.info("Checking URL " + urlToCheck.getUrl());
        CloseableHttpResponse response = null;
        Integer statusCode = null;
        try {
            response = httpClient.execute(new HttpGet(urlToCheck.getUrl()));
            statusCode = response.getStatusLine().getStatusCode();
        } catch (Exception ex) {
            logger.info("Error making request to " + urlToCheck.getUrl(), ex);
            if (urlToCheck.getUrlType().equals(UrlType.CERTIFICATION_CRITERION)) {
                logger.error("A certification criterion Companion Guide URL may be bad: "
                        + urlToCheck.getUrl() + ". The error was: " + ex.getMessage(), ex);
            }
            throw new CompletionException(ex);
        } finally {
            try {
                if (response != null) {
                    response.close();
                }
            } catch (Exception ignorable) {
                logger.warn("Could not close response.", ignorable);
            }
        }
        return statusCode;
    }
}
