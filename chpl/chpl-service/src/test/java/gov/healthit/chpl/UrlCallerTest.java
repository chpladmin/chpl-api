package gov.healthit.chpl;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

import org.htmlunit.BrowserVersion;
import org.htmlunit.Page;
import org.htmlunit.WebClient;
import org.junit.Ignore;
import org.junit.Test;

public class UrlCallerTest {
    private static final String USER_AGENT_CHROME = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Safari/537.36";

    //I would like to leave this test here in the event we need to diagnose any issues with the Questionable URL Report.
    @Ignore
    @Test
    public void testUrlHttpClient() throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException,
        IOException {
        //System.setProperty("javax.net.debug", "all");

        String url = "https://mpnproxyfhirstore.blob.core.windows.net/serviceurl/ServiceBaseURLs.csv";

        try (WebClient webClient = new WebClient(BrowserVersion.CHROME, false, null, -1)) {
            webClient.getOptions().setRedirectEnabled(true);
            webClient.getOptions().setTimeout(30000); //milliseconds
            webClient.getOptions().setThrowExceptionOnScriptError(false);
            webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
            webClient.getOptions().setUseInsecureSSL(true);
            Page page = webClient.getPage(url);
            System.out.println(page.getWebResponse().getStatusCode());
        } catch (Exception ex) {
            System.out.println("FAILED for some other reason: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}
