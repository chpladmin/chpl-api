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
                CompletableFuture.supplyAsync(() -> getUrlResponseCode(httpClient, urlToCheck.getUrl(), logger), executorService);
        return future;
    }

    private Integer getUrlResponseCode(CloseableHttpClient httpClient, String url, Logger logger) throws CompletionException {
        logger.info("Checking URL " + url);
        CloseableHttpResponse response = null;
        Integer statusCode = null;
        try {
            response = httpClient.execute(new HttpGet(url));
            statusCode = response.getStatusLine().getStatusCode();
        } catch (Exception ex) {
            logger.info("Error making request to " + url, ex);
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
