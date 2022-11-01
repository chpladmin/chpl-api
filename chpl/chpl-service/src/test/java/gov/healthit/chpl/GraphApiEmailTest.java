package gov.healthit.chpl;

import org.junit.Test;

public class GraphApiEmailTest {

    @Test
    public void doGraphStuff() throws Exception {
        Graph graph = new Graph();
        graph.sendMailAsApp("test", "hi!", "kekey@ainq.com");
    }
}