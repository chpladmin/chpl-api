package org.chpl.etl;

import java.net.URISyntaxException;


/**
 * Hello world!
 *
 */
public class App 
{
	public static void main( String[] args )
	{
		EtlGraph etlGraph = null;
//		etlGraph = new EtlGraph();
//		etlGraph.setGraph("/chpl.grf");
//        etlGraph.setInputFile("c:/Users/alarned/git/chpl-api/chpl/chpl-etl/src/main/resources/CHPL_pipe.csv");
//        etlGraph.execute();
        
        try {
			etlGraph = new EtlGraph();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        etlGraph.setGraph("/chpl-xls.grf");
        etlGraph.setInputFile("c:/Users/alarned/git/chpl-api/chpl/chpl-etl/src/main/resources/CHPL_04_17_15.xlsx");
        etlGraph.execute();
	}
}
