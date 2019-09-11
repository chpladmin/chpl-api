package gov.healthit.chpl.scheduler.brokenUrlJob;

import java.util.concurrent.Future;

import org.apache.logging.log4j.Logger;
import org.asynchttpclient.AsyncHandler;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.HttpResponseBodyPart;
import org.asynchttpclient.HttpResponseStatus;
import org.asynchttpclient.Request;
import org.asynchttpclient.RequestBuilder;
import org.asynchttpclient.util.HttpConstants;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import io.netty.handler.codec.http.HttpHeaders;

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
    public Future<Integer> getUrlResponseCode(
            final UrlResult urlToCheck, final AsyncHttpClient httpClient, final Logger logger) throws Exception {
        logger.info("Checking URL " + urlToCheck.getUrl());
        Request getRequest = new RequestBuilder(HttpConstants.Methods.GET)
                .setFollowRedirect(false)
                .setUrl(urlToCheck.getUrl())
                .build();
        return httpClient.executeRequest(getRequest, new AsyncHandler<Integer>() {
            private int responseCode = -1;

            /**
             * All we care about is the response status code, so we will save that here
             * and return the ABORT state to tell the http client that it does not need
             * to continue handling the request.
             */
            @Override
            public State onStatusReceived(final HttpResponseStatus responseStatus) throws Exception {
                this.responseCode = responseStatus.getStatusCode();
                return AsyncHandler.State.ABORT;
            }

            @Override
            public State onHeadersReceived(final HttpHeaders headers) throws Exception {
                return AsyncHandler.State.CONTINUE;
            }

            @Override
            public State onBodyPartReceived(final HttpResponseBodyPart bodyPart) throws Exception {
                return AsyncHandler.State.CONTINUE;
            }

            @Override
            public void onThrowable(final Throwable t) {
            }

            @Override
            public Integer onCompleted() throws Exception {
                return responseCode;
            }
        });
    }
}
