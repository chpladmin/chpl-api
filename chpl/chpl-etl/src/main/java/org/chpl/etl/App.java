package org.chpl.etl;


/**
 * Hello world!
 *
 */
public class App 
{
	public static void main( String[] args )
	{
		EtlGraph etlGraph = new EtlGraph();
		etlGraph.setGraph("/chpl.grf");
        etlGraph.setInputFile("c:/Users/alarned/git/chpl-api/chpl/chpl-etl/src/main/resources/CHPL_pipe.csv");
        etlGraph.execute();
	}
}
