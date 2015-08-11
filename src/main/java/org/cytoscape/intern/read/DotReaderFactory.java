package org.cytoscape.intern.read;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.cytoscape.intern.FileHandlerManager;
import org.cytoscape.io.CyFileFilter;
import org.cytoscape.io.read.InputStreamTaskFactory;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.work.TaskIterator;

/**
 * Allows the input stream to be set for reader task factories
 * 
 * @author Massoud Maher
 * @author Braxton Fitts
 * @author Ziran Zhang
 */

public class DotReaderFactory implements InputStreamTaskFactory {
    
	
	//debug logger declaration 
	private static final Logger LOGGER = Logger.getLogger("org.cytoscape.intern.read.DotReaderFactory");
	private static final FileHandlerManager FILE_HANDLER_MGR = FileHandlerManager.getManager();

	
	//Variable Declarations
	private CyFileFilter fileFilter;
	private CyNetworkViewFactory netViewFact;
	private CyNetworkFactory netFact;
	private CyNetworkManager netMgr;
	private CyRootNetworkManager rootNetMgr;
	private VisualMappingManager vizMapMgr;
	private VisualStyleFactory vizStyleFact;
	
	
	/**
	 * Sets the DotReaderFactory with associate fileFilter
	 * 
	 * @param fileFilter CyFileFilter associated with this factory
	 * @param netViewFact CyNetworkViewFactory needed for DotReaderTask
	 * @param netFact CyNetworkFactory needed for DotReaderTask
	 * @param netMgr CyNetworkManager needed for DotReaderTask
	 * @param rootNetMgr CyRootNetworkManager needed for DotReaderTask
	 * @param vizMapMgr VisualMappingManager needed for DotReaderTask
	 * @param vizStyleFact VisualStyleFactory needed for DotReaderTask
	 */
	public DotReaderFactory(CyFileFilter fileFilter, CyNetworkViewFactory netViewFact,
			CyNetworkFactory netFact, CyNetworkManager netMgr, CyRootNetworkManager rootNetMgr,
			VisualMappingManager vizMapMgr, VisualStyleFactory vizStyleFact) {

		// make logger write to file
		FileHandler handler = null;
		try {
			handler = new FileHandler("log_DotReaderFactory.txt");
			handler.setLevel(Level.ALL);
			handler.setFormatter(new SimpleFormatter());
		}
		catch(IOException e) {
			// to prevent compiler error
		}
		LOGGER.addHandler(handler);
		FILE_HANDLER_MGR.registerFileHandler(handler);
		
		this.fileFilter = fileFilter;
		this.netViewFact = netViewFact;
		this.netFact = netFact;
		this.netMgr = netMgr;
		this.rootNetMgr = rootNetMgr;
		this.vizMapMgr = vizMapMgr;
		this.vizStyleFact = vizStyleFact;

	}
	
	
	/**
	 * Returns CyFileFilter associated with this factory
	 * 
	 * @return CyFileFilter for this factory
	 */
	@Override
	public CyFileFilter getFileFilter() {
		return fileFilter;
	}

	
	/**
	 * Sets the input stream that will be read by the Reader created from this factory
	 * 
	 * @param inStream The InputStream to be read
	 * @param inputName The name of the input
	 * 
	 * @return TaskIterator created by calling DotReaderTask()
	 */
	@Override
	public TaskIterator createTaskIterator(InputStream inStream, String inputName) {
		LOGGER.info("create TaskIterator with params");
		
		return new TaskIterator(new DotReaderTask(inStream, netViewFact,
				netFact, netMgr, rootNetMgr, vizMapMgr, vizStyleFact));
	}

	
	/**
	 * Returns true if the factory is ready to produce a TaskIterator and false otherwise.
	 * 
	 * @param inStream The InputStream to be read
	 * @param inputName The name of the input
	 * 
	 * @return Boolean indicating the factory is ready to produce a TaskIterator
	 */
	@Override
	public boolean isReady(InputStream inStream, String inputName) {
		
		// check file extension
		if (inStream != null && inputName != null) {
			LOGGER.info("Valid input is found");
			
			String[] parts = inputName.split(".");
			String extension = parts[parts.length-1];
			if (extension.matches(("gv|dot"))) {
				
				LOGGER.info("gv|dot extention is matched");
				return true;
			}
		}
		
		return false;
	}

}
