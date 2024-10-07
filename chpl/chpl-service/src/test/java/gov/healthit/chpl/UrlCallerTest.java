package gov.healthit.chpl;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

import org.apache.http.ProtocolException;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.htmlunit.BrowserVersion;
import org.htmlunit.FailingHttpStatusCodeException;
import org.htmlunit.Page;
import org.htmlunit.WebClient;
import org.junit.Test;

public class UrlCallerTest {
    private static final String USER_AGENT_CHROME = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Safari/537.36";

    //I would like to leave this test here in the event we need to diagnose any issues with the Questionable URL Report.
   // @Ignore
    @Test
    public void testUrlHttpClient() throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException,
        IOException {
        //System.setProperty("javax.net.debug", "all");

        String url = "https://drummondgroup.com/wp-content/uploads/2018/08/OTEMR-2.2-Test-Lab-Test-Report_v2014.1.1_1-14March2017.pdf";

        try (WebClient webClient = new WebClient(BrowserVersion.CHROME, false, null, -1)) {
            webClient.getOptions().setRedirectEnabled(true);
            webClient.getOptions().setTimeout(30000); //milliseconds
            webClient.getOptions().setThrowExceptionOnScriptError(false);
            webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
            webClient.getOptions().setUseInsecureSSL(true);
            Page page = webClient.getPage(url);
            System.out.println(page.getWebResponse().getStatusCode());

//            final WebResponse response = webClient.getWebConnection().getResponse(new WebRequest(new URL(url)));
//            System.out.println(response.getStatusCode());

        } catch (FailingHttpStatusCodeException ex) {
            System.out.println("FAILED with status code " + ex.getStatusCode());
            ex.printStackTrace();
        } catch (Exception ex) {
            System.out.println("FAILED for some other reason " + ex.getMessage());
            ex.printStackTrace();
        }


//        TrustStrategy acceptingTrustStrategy = (X509Certificate[] chain, String authType) -> true;
//        SSLContext sslContext = org.apache.http.ssl.SSLContexts.custom().loadTrustMaterial(null, acceptingTrustStrategy).build();
//        SSLConnectionSocketFactory csf = new SSLConnectionSocketFactory(sslContext != null ? sslContext : SSLContexts.createDefault());
//        CloseableHttpClient httpClient = HttpClients.custom()
//                .setSSLSocketFactory(csf)
//                .setRedirectStrategy(new CustomRedirectStrategy())
//                .setDefaultRequestConfig(RequestConfig.custom()
//                        .setConnectionRequestTimeout(30000)
//                        .setConnectTimeout(30000)
//                        .setSocketTimeout(30000)
//                        .build())
//                .setUserAgent(USER_AGENT_CHROME)
//                .build();
//
//        CloseableHttpResponse response = httpClient.execute(new HttpGet(url));
//        String responseBody = EntityUtils.toString(response.getEntity());
//        System.out.println(responseBody);
//        int statusCode = response.getStatusLine().getStatusCode();
//        response.close();
//        System.out.println(HttpStatus.valueOf(statusCode));
    }

    private class CustomRedirectStrategy extends DefaultRedirectStrategy {
        @Override
        protected URI createLocationURI(final String location) throws ProtocolException {
            try {
                System.out.println("Redirecting to " + location);
                //replace spaces with %20
                String encodedLocation = location.replace(" ", "%20");
                if (!location.equals(encodedLocation)) {
                    System.out.println("Encoded " + location + " as " + encodedLocation);
                }
                return new URI(encodedLocation);
            } catch (final URISyntaxException ex) {
                throw new ProtocolException("Invalid redirect URI: " + location, ex);
            }
        }
    }
}
