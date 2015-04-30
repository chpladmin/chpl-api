package org.chpl.etl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.net.URISyntaxException;

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
			e.printStackTrace();
		}
		graph.setGraph("/chpl.grf");		
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
	
	@Test
	public void shallSetInputFile() {
		String newFile = "c:/Users/alarned/git/chpl-api/chpl/chpl-etl/src/main/resources/CHPL_pipe.csv";
		assertNotEquals(newFile, graph.getInputFile());
		graph.setInputFile(newFile);
		assertEquals(newFile, graph.getInputFile());
	}
	
	@Test (expected = NullPointerException.class)
	public void shallThrowExceptionIfNoPluginsDirectory() throws URISyntaxException {
		String pluginDir = "/badDirectory";
		graph = new EtlGraph(pluginDir);
	}
}
