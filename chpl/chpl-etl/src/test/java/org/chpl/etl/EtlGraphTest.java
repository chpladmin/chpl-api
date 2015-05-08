package org.chpl.etl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class EtlGraphTest {

	private EtlGraph graph;
	
	@Before
	public void setUp() {
		try {
			graph = new EtlGraph();
		} catch (URISyntaxException e) {
			Logger.getLogger(EtlGraphTest.class.getName()).log(Level.SEVERE, null, e);
		}
		graph.setGraph("/graphs/create_vendor-product.grf");
	}
	
	@Test
	public void shallGenerateGraphFromResource() {
		assertNotNull(graph.getGraph());
	}
	
	@Test
	@Ignore
	public void shallExecuteGraph() {
		assertTrue(graph.execute());
	}
	
	@Test (expected = NullPointerException.class)
	public void shallThrowExceptionIfNoPluginsDirectory() throws URISyntaxException {
		String pluginDir = "/badDirectory";
		graph = new EtlGraph(pluginDir);
	}
}
