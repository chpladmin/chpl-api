package org.chpl.etl;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jetel.graph.Result;
import org.jetel.graph.TransformationGraph;
import org.jetel.graph.TransformationGraphXMLReaderWriter;
import org.jetel.graph.runtime.EngineInitializer;
import org.jetel.graph.runtime.GraphRuntimeContext;
import org.jetel.main.runGraph;

public class EtlGraph {

	private FileInputStream graphResource;
	private GraphRuntimeContext runtimeContext;
	private TransformationGraph graph;
	private TransformationGraphXMLReaderWriter graphReader;

	public EtlGraph() throws URISyntaxException {
		this("/plugins");
	}

	public EtlGraph(String pluginDir) throws URISyntaxException {
		EngineInitializer.initEngine(App.class.getResource(pluginDir).toURI().getPath(), null, null);
	}

	public void setGraph(String graphResource) {
		try {
			this.graphResource = new FileInputStream(App.class.getResource(graphResource).toURI().getPath());
		} catch (FileNotFoundException e) {
			Logger.getLogger(EtlGraph.class.getName()).log(Level.SEVERE, null, e);
		} catch (URISyntaxException e) {
			Logger.getLogger(EtlGraph.class.getName()).log(Level.SEVERE, null, e);
		}
		//prepare runtime parameters - JMX is turned off
		runtimeContext = new GraphRuntimeContext();
		runtimeContext.setUseJMX(false);

		//create transformation graph from xml file
		graph = new TransformationGraph();
		graphReader = new TransformationGraphXMLReaderWriter(runtimeContext);

		try {
			graph = graphReader.read(this.graphResource);
		} catch (Exception e) {
			Logger.getLogger(EtlGraph.class.getName()).log(Level.SEVERE, null, e);
			return;
		}
	}

	public TransformationGraph getGraph() {
		return this.graph;
	}

	public boolean execute() {
		Future<Result> result;
		try {
			result = runGraph.executeGraph(this.graph, this.runtimeContext);
			if (!result.get().equals(Result.FINISHED_OK)) {
				Logger.getLogger(EtlGraph.class.getName()).log(Level.SEVERE, "Failed graph execution!\n");
				return false;
			}
		} catch (Exception e) {
			Logger.getLogger(EtlGraph.class.getName()).log(Level.SEVERE, null, e);
			return false;
		}
		return true;
	}
}
