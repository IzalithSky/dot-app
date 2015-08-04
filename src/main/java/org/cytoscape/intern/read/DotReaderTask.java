package org.cytoscape.intern.read;

import org.cytoscape.intern.FileHandlerManager;

import org.cytoscape.io.read.AbstractCyNetworkReader;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;

import org.cytoscape.model.subnetwork.CyRootNetworkManager;

import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;

import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.VisualStyleFactory;

import org.cytoscape.work.TaskMonitor;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;

import java.util.HashMap;

import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/*import com.alexmerz.graphviz.Parser;
import com.alexmerz.graphviz.ParseException;*/
import com.alexmerz.graphviz.objects.*;

/**
 * Task object that reads a dot file into a network/ network view
 * 
 * @author Massoud Maher
 * @author Braxton Fitts
 * @author Ziran Zhang
 */

public class DotReaderTask extends AbstractCyNetworkReader {
	
	// debug logger
	private static final Logger LOGGER = Logger.getLogger("org.cytoscape.intern.read.DotReaderTask");
	private FileHandler handler;
	
	private static final FileHandlerManager FILE_HANDLER_MGR = FileHandlerManager.getManager();

	/*
	 * InputStreamReader used as input to the JPGD Parser
	 */
	private InputStreamReader inStreamReader;
	
	/*
	 * HashMap that maps the created CyNetworks to their JPGD Graph object
	 */
	private HashMap<CyNetwork, Graph> dotGraphs;
	
	/*
	 * VisualMappingManager to which the new visual style will be added
	 */
	private VisualMappingManager vizMapMgr;
	
	/*
	 * VisualStyleFactory that will create the VisualStyle for the CyNetwork
	 */
	private VisualStyleFactory vizStyleFact;
	
	
	/**
	 * Constructs a DotReaderTask object for importing a dot file
	 * 
	 * @param inStream the stream to be read from
	 * @param netViewFact instance of CyNetworkViewFactory
	 * @param netFact instance of CyNetworkFactory
	 * @param netMgr instance of CyNetworkManager
	 * @param rootNetMgr instance of CyRootNetworkManager
	 * @param vizMapMgr instance of VisualMappingManager
	 * @param vizStyleFact instance of VisualStyleFactory
	 */
	public DotReaderTask(InputStream inStream, CyNetworkViewFactory netViewFact,
			CyNetworkFactory netFact, CyNetworkManager netMgr,
			CyRootNetworkManager rootNetMgr, VisualMappingManager vizMapMgr, VisualStyleFactory vizStyleFact) {
		super(inStream, netViewFact, netFact, netMgr, rootNetMgr);
		// Make logger write to file
		handler = null;
		try {
			handler = new FileHandler("log_DotReaderTask.txt");
			handler.setLevel(Level.ALL);
			
			handler.setFormatter(new SimpleFormatter());
		}
		catch(IOException e) {
			// to prevent compiler error
		}
		LOGGER.addHandler(handler);
		FILE_HANDLER_MGR.registerFileHandler(handler);

		// Init
		inStreamReader = new InputStreamReader(inStream);
		dotGraphs = null;
		this.vizMapMgr = vizMapMgr;
		this.vizStyleFact = vizStyleFact;
	}

	
	/**
	 * Causes the task to begin execution.
	 * 
	 * @param taskMonitor The TaskMonitor provided by TaskManager to allow the
	 * Task to modify its user interface.
	 */
	@Override
	public void run(TaskMonitor monitor) throws Exception {
		// TODO Auto-generated method stub
		/*
		 * Steps:
		 * 1. Use Parser to generate Graph objects representing
		 * the graphs from the InputStreamReader
		 * 2. Create CyNetwork[] "networks" the size equaling the number of Graph objects
		 * 2. For each graph object do the following:
		 * 		a. Create a CyNetwork
		 * 		b. Set the name of the CyNetwork to the Graph's Id
		 * 		c. Retrieve the list of Node objects
		 * 		d. For each node do the following:
		 * 			- Create a CyNode
		 * 			- Set the name and shared_name of the CyNode to the Node's Id
		 * 		e. For each edge do the following:
		 *			- Create a CyEdge
		 *			- If digraph, set interaction to "interaction", else set to undirected
		 *			- set the name and shared_name of CyEdge to Sourcename (interaction) Targetname
		 * 3. Add CyNetwork to "networks"
		 * 4. Add <CyNetwork, Graph> pair to HashMap
		 */

	}
	
	
	/**
	 * build an instance of CyNetworkView based on the passed in CyNetwork instance
	 * 
	 * @param CyNetwork network from which we want to build the CyNetworkView
	 */
	@Override
	public CyNetworkView buildCyNetworkView(CyNetwork network) {
		// TODO Auto-generated method stub
		/*
		 * Steps:
		 * Retrieve Graph object corresponding to "arg0" from HashMap
		 * Create new VisualStyle with VisualStyleFactory
		 * Create new CyNetworkView with CyNetworkViewFactory
		 * Pass Graph Object, VisualStyle, CyNetworkView into Reader
		 * in order to set Visual Properties
		 * add VisualStyle to VisualMappingManager
		 * return CyNetworkView
		 */
		return null;
	}

}
