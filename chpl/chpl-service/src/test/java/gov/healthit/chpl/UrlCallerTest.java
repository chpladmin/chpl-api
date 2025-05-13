package gov.healthit.chpl;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.htmlunit.BrowserVersion;
import org.htmlunit.FailingHttpStatusCodeException;
import org.htmlunit.Page;
import org.htmlunit.WebClient;
import org.joda.time.LocalDateTime;
import org.junit.Ignore;
import org.junit.Test;

public class UrlCallerTest {
    private WebClient webClient;
    private ExecutorService executorService;
    private Map<String, Future<Integer>> urlResponseCodeFuturesMap;

    //I would like to leave this test here in the event we need to diagnose any issues with the Questionable URL Report.
    @Ignore
    @Test
    public void testUrlHttpClient() throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException,
        IOException {
        //System.setProperty("javax.net.debug", "all");
        webClient = new WebClient(BrowserVersion.CHROME, false, null, -1);
        webClient.getOptions().setRedirectEnabled(true);
        webClient.getOptions().setTimeout(30000);
        //if we throw exceptions on any error then many websites don't load because they have some
        //javascript error or some link, like GTM, that doesn't work, so we have to ignore these (as the browser does)
        webClient.getOptions().setThrowExceptionOnScriptError(false);
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
        //many websites also have weird issues with their certificates and browsers seem to let you view them anyway
        webClient.getOptions().setUseInsecureSSL(true);
        executorService = Executors.newFixedThreadPool(5);
        urlResponseCodeFuturesMap = new LinkedHashMap<String, Future<Integer>>();

        int batchSize = 5;
        List<String>  urls = Arrays.asList(
                "https://chpl-dev.healthit.gov/rest/report-data/withdrawn-listings",
                "https://chpl-dev.healthit.gov/rest/report-data/active-listings",
                "https://chpl-dev.healthit.gov/rest/report-data/suspended-listings",
                "https://chpl-dev.healthit.gov/rest/report-data/listing-count",
                "https://chpl-dev.healthit.gov/rest/report-data/withdrawn-listing-counts-by-acb",
                "https://chpl-dev.healthit.gov/rest/report-data/active-listing-counts-by-acb",
                "https://chpl-dev.healthit.gov/rest/report-data/suspended-listing-counts-by-acb"
        );

        int batchCount = (urls.size() / batchSize) + 1;
        System.out.println("Querying all urls in " + batchCount + " batches.");
        for (int currBatch = 0; currBatch < batchCount; currBatch++) {
            int batchBegin = currBatch * batchSize;
            int batchEnd = Math.min(batchBegin + batchSize, urls.size());
            System.out.println("*** Batch " + currBatch + " (" + batchBegin + " - " + batchEnd + " ) ***");
            makeRequests(currBatch, urls.subList(batchBegin, batchEnd));
        }
    }

    private void makeRequests(int batchNum, List<String> urls) {
        for (int batchIndex = 0; batchIndex < urls.size(); batchIndex++) {
            String url = urls.get(batchIndex);
            try {
               CompletableFuture<Integer> responseCodeFuture =
                       getUrlResponseCodeFuture(url, webClient, executorService);
               urlResponseCodeFuturesMap.put(url, responseCodeFuture);
            } catch (final Exception ex) {
                System.out.println("Could not check URL " + url + " due to exception " + ex.getMessage());
                ex.printStackTrace();
            }
        }

        int completedUrls = 0;
        for (String activeRequest : urlResponseCodeFuturesMap.keySet()) {
            Future<Integer> futureResponseCode = urlResponseCodeFuturesMap.get(activeRequest);
            try {
                Integer responseCode = futureResponseCode.get();
                System.out.println("Completed " + batchNum + "[" + completedUrls + "] check of URL "
                        + activeRequest + " with status " + responseCode + " at " + LocalDateTime.now());
            } catch (Exception ex) {
                System.out.println("Error checking URL " +  activeRequest + " " + ex.getMessage());
                ex.printStackTrace();
            } finally {
                completedUrls++;
            }
        }
        urlResponseCodeFuturesMap.clear();
    }

    private CompletableFuture<Integer> getUrlResponseCodeFuture(
            String urlToCheck, WebClient webClient, ExecutorService executorService) throws Exception {
        CompletableFuture<Integer> future =
                CompletableFuture.supplyAsync(() -> getUrlResponseCode(webClient, urlToCheck), executorService);
        return future;
    }

    private Integer getUrlResponseCode(WebClient webClient, String urlToCheck) throws CompletionException {
        System.out.println("Checking URL " + urlToCheck + " " + LocalDateTime.now());
        Integer statusCode = null;
        try {
            Page page = webClient.getPage(urlToCheck);
            statusCode = page.getWebResponse().getStatusCode();
        } catch (FailingHttpStatusCodeException ex) {
            System.out.println("Request to " + urlToCheck + " failed with status code " + ex.getStatusCode());
            ex.printStackTrace();
            statusCode = ex.getStatusCode();
        } catch (Exception ex) {
            System.out.println("Error making request to " + urlToCheck);
            ex.printStackTrace();
        }
        return statusCode;
    }
}
