package org.cytoscape.intern.read.reader;

import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NETWORK_BACKGROUND_PAINT;

import java.awt.Color;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.tuple.Pair;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.vizmap.VisualStyle;

import com.alexmerz.graphviz.objects.Graph;

/**
 * Class that contains definitions and some implementation for converting a
 * dot graph to a CyNetwork. Data is passed in as a JPGD Graph object.
 * This subclass handles importing of network/graph properties
 * 
 * @author Massoud Maher
 * @author Braxton Fitts
 * @author Ziran Zhang
 */
public class NetworkReader extends Reader{


	// JPGD object that contains visual information for this network view
	private Graph graph;

	/**
	 * Constructs an object of type Reader.
	 * 
	 * 
	 * @param networkView view of network we are creating/modifying
	 * @param vizStyle VisualStyle that we are applying to the network
	 * @param defaultAttrs Map that contains default attributes
	 * @param elementMap Map of which the keys are JPGD Edge objects and the 
	 * values are corresponding Cytoscape CyEdge objects 
	 */
	public NetworkReader(CyNetworkView networkView, VisualStyle vizStyle, Map<String, String> defaultAttrs, Graph graph) {
		super(networkView, vizStyle, defaultAttrs);
		this.graph = graph;
	}

	/**
	 * Does nothing. A network view has no GraphViz attributes that correspond
	 * to a single Cytoscape VisualProperty
	 */
	@Override
	@SuppressWarnings("rawtypes")
	protected Pair<VisualProperty, Object> convertAttribute(String name, String val) {
		return null;
	}

	/**
	 * Overwrites the default VisualProperty values for the Cytoscape VisualStyle
	 * that came from converting the default attribute list with new values from
	 * converting the attribute list from the JPGD object that corresponds to
	 * the network view.
	 */
	@Override
	protected void setBypasses() {
		//Network doesn't set bypass value with the Graph object's attributes
		//overrides the defaults set in setDefault()
		LOGGER.info("Setting the Bypass values for Visual Style...");
		for (Entry<String, String> attrEntry : graph.getAttributes().entrySet()) {
			String attrKey = attrEntry.getKey();
			String attrVal = attrEntry.getValue();
			if (attrKey.equals("bgcolor")) {
				setColor(attrVal, vizStyle, ColorAttribute.BGCOLOR);
			}
		}
	}

	/**
	 * Does nothing. The GraphViz style attribute does not affect the
	 * network view
	 */
	@Override
	protected void setStyle(String attrVal, VisualStyle vizStyle) {
		//Network doesn't have properties set with style attribute
	}

	/**
	 * Does nothing. The GraphViz style attribute does not affect the
	 * network view
	 */
	@Override
	protected void setStyle(String attrVal,
			View<? extends CyIdentifiable> elementView) {
		//Network doesn't have properties set with style attribute
	}

	@Override
	protected void setColor(String attrVal, VisualStyle vizStyle,
			ColorAttribute attr) {
		Color color = convertColor(attrVal);
		switch (attr) {
			case BGCOLOR: {
				vizStyle.setDefaultValue(NETWORK_BACKGROUND_PAINT, color);
				break;
			}
			default: {
				break;
			}
		}
	}

	/**
	 * Does nothing. A network view does not set a color attribute with a
	 * bypass.
	 */
	@Override
	protected void setColor(String attrVal,
			View<? extends CyIdentifiable> elementView, ColorAttribute attr) {
		//Network doesn't set Background color with bypass
	}

}