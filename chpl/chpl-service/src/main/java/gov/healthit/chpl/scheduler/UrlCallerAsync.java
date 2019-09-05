package gov.healthit.chpl.scheduler;

import java.util.concurrent.Future;

import org.apache.logging.log4j.Logger;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.Request;
import org.asynchttpclient.RequestBuilder;
import org.asynchttpclient.Response;
import org.asynchttpclient.util.HttpConstants;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import gov.healthit.chpl.dto.scheduler.UrlResultDTO;

/**
 * Provides asynchronous support to scheduler classes for getting the response from a URL.
 * @author kekey
 *
 */
@Service("urlCallerAsync")
public class UrlCallerAsync {
    /**
     * Retrieves the associated URL Response object as a Future<>.
     * @param urlToCheck url object
     * @param httpClient a configured http client to use
     * @param logger a configured logger
     * @return Response object
     * @throws Exception any one of a number of exceptions (not sure of all the ones that are possible here)
     */
    @Async("jobAsyncDataExecutor")
    public Future<Response> getUrlResponse(
            final UrlResultDTO urlToCheck, final AsyncHttpClient httpClient, final Logger logger) throws Exception {
        logger.info("Checking URL " + urlToCheck.getUrl());
        Request getRequest = new RequestBuilder(HttpConstants.Methods.GET)
                .setFollowRedirect(true)
                .setUrl(urlToCheck.getUrl())
                .build();
        return httpClient.executeRequest(getRequest);
    }
}
