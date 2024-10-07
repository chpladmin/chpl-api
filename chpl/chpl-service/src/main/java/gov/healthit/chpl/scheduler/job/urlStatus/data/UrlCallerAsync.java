package gov.healthit.chpl.scheduler.job.urlStatus.data;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;

import org.apache.logging.log4j.Logger;
import org.htmlunit.FailingHttpStatusCodeException;
import org.htmlunit.Page;
import org.htmlunit.WebClient;
import org.springframework.stereotype.Service;

@Service("urlCallerAsync")
public class UrlCallerAsync {

    public CompletableFuture<Integer> getUrlResponseCodeFuture(
            UrlResult urlToCheck, WebClient webClient, ExecutorService executorService, Logger logger) throws Exception {
        CompletableFuture<Integer> future =
                CompletableFuture.supplyAsync(() -> getUrlResponseCode(webClient, urlToCheck, logger), executorService);
        return future;
    }

    private Integer getUrlResponseCode(WebClient webClient, UrlResult urlToCheck, Logger logger) throws CompletionException {
        logger.info("Checking URL " + urlToCheck.getUrl());
        Integer statusCode = null;
        try {
            Page page = webClient.getPage(urlToCheck.getUrl());
            statusCode = page.getWebResponse().getStatusCode();
        } catch (FailingHttpStatusCodeException ex) {
            logger.info("Request to " + urlToCheck.getUrl() + " failed with status code " + ex.getStatusCode());
            statusCode = ex.getStatusCode();
        } catch (Exception ex) {
            logger.info("Error making request to " + urlToCheck.getUrl(), ex);
            if (urlToCheck.getUrlType().equals(UrlType.CERTIFICATION_CRITERION)) {
                logger.error("A certification criterion Companion Guide URL may be bad: "
                        + urlToCheck.getUrl() + ". The error was: " + ex.getMessage(), ex);
            }
            throw new CompletionException(ex);
        }
        return statusCode;
    }
}
