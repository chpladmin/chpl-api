package org.chpl.etl;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class EtlGraphTest {

	private EtlGraph graph;
	
	@Before
	public void setUp() {
		graph = new EtlGraph();
		graph.setGraph("/chpl.grf");		
	}
	
	@Test
	public void shallGenerateGraphFromResource() {
		assertNotNull(graph.getGraph());
	}
	
	@Test
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
}
