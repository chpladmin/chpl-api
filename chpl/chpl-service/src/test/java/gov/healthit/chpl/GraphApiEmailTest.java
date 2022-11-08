package gov.healthit.chpl;

import org.junit.Ignore;
import org.junit.Test;

public class GraphApiEmailTest {
    @Ignore
    @Test
    public void doGraphStuff() throws Exception {
        Graph graph = new Graph();
        graph.sendMailAsApp("test", "hi!", "kekey@ainq.com");
    }
}