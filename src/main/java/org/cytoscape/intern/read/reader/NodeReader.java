package org.cytoscape.intern.read.reader;

import java.awt.Color;
import java.awt.Font;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.tuple.Pair;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.presentation.property.values.LineType;
import org.cytoscape.view.presentation.property.values.NodeShape;
import org.cytoscape.view.presentation.property.NodeShapeVisualProperty;

import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_BORDER_LINE_TYPE;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_BORDER_PAINT;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_BORDER_TRANSPARENCY;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_BORDER_WIDTH;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_FILL_COLOR;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_HEIGHT;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_LABEL;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_LABEL_COLOR;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_LABEL_FONT_FACE;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_LABEL_FONT_SIZE;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_LABEL_TRANSPARENCY;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_SHAPE;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_TOOLTIP;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_TRANSPARENCY;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_VISIBLE;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_WIDTH;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_SIZE;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_X_LOCATION;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_Y_LOCATION;

import com.alexmerz.graphviz.objects.Node;

/**
 * Class that contains definitions and some implementation for converting a
 * dot graph to a CyNetwork. Data is passed in as a list of JPGD Node objects
 * This subclass handles importing of node properties
 * 
 * @author Massoud Maher
 * @author Braxton Fitts
 * @author Ziran Zhang
 */
public class NodeReader extends Reader{

	
	// Map to convert from .dot node shape to Cytoscape
	private static final Map<String, NodeShape> NODE_SHAPE_MAP = new HashMap<String, NodeShape>();
	static {
		NODE_SHAPE_MAP.put("triangle", NodeShapeVisualProperty.TRIANGLE);
		NODE_SHAPE_MAP.put("diamond", NodeShapeVisualProperty.DIAMOND);
		NODE_SHAPE_MAP.put("ellipse", NodeShapeVisualProperty.ELLIPSE);
		NODE_SHAPE_MAP.put("hexagon", NodeShapeVisualProperty.HEXAGON);
		NODE_SHAPE_MAP.put("octagon", NodeShapeVisualProperty.OCTAGON);
		NODE_SHAPE_MAP.put("parallelogram", NodeShapeVisualProperty.PARALLELOGRAM);
		NODE_SHAPE_MAP.put("rectangle", NodeShapeVisualProperty.ROUND_RECTANGLE);
		NODE_SHAPE_MAP.put("rectangle", NodeShapeVisualProperty.RECTANGLE);     
	}
	private static final Map<String, VisualProperty<?>> DOT_TO_CYTOSCAPE = new HashMap<String, VisualProperty<?>>();
	static {
		DOT_TO_CYTOSCAPE.put("label", NODE_LABEL);
		DOT_TO_CYTOSCAPE.put("xlabel", NODE_LABEL);
		DOT_TO_CYTOSCAPE.put("penwidth", NODE_BORDER_WIDTH);
		DOT_TO_CYTOSCAPE.put("height", NODE_HEIGHT);
		DOT_TO_CYTOSCAPE.put("width", NODE_WIDTH);
		DOT_TO_CYTOSCAPE.put("tooltip", NODE_TOOLTIP);
		DOT_TO_CYTOSCAPE.put("shape", NODE_SHAPE);
		DOT_TO_CYTOSCAPE.put("fontname", NODE_LABEL_FONT_FACE);
		DOT_TO_CYTOSCAPE.put("fontsize", NODE_LABEL_FONT_SIZE);
	}
	

	/**
	 * Constructs an object of type Reader.
	 * 
	 * 
	 * @param networkView view of network we are creating/modifying
	 * @param vizStyle VisualStyle that we are applying to the network
	 * @param defaultAttrs Map that contains default attributes for Reader of this type
	 * eg. for NodeReader will be a list of default
	 * @param elementMap Map where keys are JPGD node objects and Values are corresponding Cytoscape CyNodes
	 */
	public NodeReader(CyNetworkView networkView, VisualStyle vizStyle, Map<String, String> defaultAttrs, Map<Node, CyNode> elementMap) {
		super(networkView, vizStyle, defaultAttrs);
		this.elementMap = elementMap;
		LOGGER.info(String.valueOf(defaultAttrs.size()));

	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	protected void setBypasses() {
		LOGGER.info("Setting the Bypass values for Visual Style...");
		/*
		 * for each entry in elementMap
		 * 		bypassMap = getAttrMap(elementMap.getKey())
		 * 		for each entry in bypassMap
		 * 			Pair p = convertAttribute(name, val);
		 * 			VP = p.left()
		 * 			val = p.right()
		 * 			getValue().setLockedValue( VP, val);	
		 */
		for (Entry<? extends Object, ? extends CyIdentifiable> entry : elementMap.entrySet()) {
			bypassAttrs = getAttrMap(entry.getKey()); 
			CyNode element = (CyNode)entry.getValue();
			View<CyNode> elementView = networkView.getNodeView(element);

			for (Entry<String, String> attrEntry : bypassAttrs.entrySet()) {
				String attrKey = attrEntry.getKey();
				String attrVal = attrEntry.getValue();
				LOGGER.info(
					String.format("Converting DOT attribute: %s", attrKey)
				);
				if (attrKey.equals("pos")) {
					setPositions(attrVal, elementView);
					continue;
				}
				if (attrKey.equals("style")) {
					setStyle(attrVal, elementView);
					continue;
				}
				if (attrKey.equals("color") || attrKey.equals("fillcolor")
						|| attrKey.equals("fontcolor")) {
					switch (attrKey) {
						case "color": {
							setColor(attrVal, elementView, ColorAttribute.COLOR);
							break;
						}
						case "fillcolor": {
							setColor(attrVal, elementView, ColorAttribute.FILLCOLOR);
							break;
						}
						case "fontcolor": {
							setColor(attrVal, elementView, ColorAttribute.FONTCOLOR);
							break;
						}
					}
					continue;
				}
				Pair<VisualProperty, Object> p = convertAttribute(attrEntry.getKey(), attrEntry.getValue());
				if (p == null) {
					continue;
				}
				VisualProperty vizProp = p.getLeft();
				Object val = p.getRight();
				LOGGER.info("Updating Visual Style...");
				LOGGER.info(String.format("Setting Visual Property %S...", vizProp));
				elementView.setLockedValue(vizProp, val);
			}
		}
	}
	/**
	 * Sets defaults and bypass attributes for each node and sets positions
	 */
	public VisualStyle setProperties() {
		LOGGER.info("NodeReader: Setting properties for VisualStyle...");
		super.setProperties();
		return vizStyle;
	}
	
	/**
	 * Sets VisualProperties for each node related to location of node.
	 * Here because cannot return 2 VisualProperties from convertAttribute
	 * and want to make exception clear
	 * @param attrVal 
	 * @param elementView 
	 */
	private void setPositions(String attrVal, View<CyNode> elementView) {
		/*
		 * Get pos attribute
		 * Split string by ","
		 * Convert parts to Doubles
		 * Multiple Y coordinate by -1
		 * Set NODE_X_POSITION and NODE_Y_POSITION
		 */
		String[] coords = attrVal.split(",");
		Double x = Double.parseDouble(coords[0]);
		Double y = -1 * Double.parseDouble(coords[1]);
		elementView.setVisualProperty(NODE_X_LOCATION, x);
		elementView.setVisualProperty(NODE_Y_LOCATION, y);
	}
	
	/**
	 * Converts the specified .dot attribute to Cytoscape equivalent
	 * and returns the corresponding VisualProperty and its value
	 * Must be overidden and defined in each sub-class
	 * 
	 * @param name Name of attribute
	 * @param val Value of attribute
	 * 
	 * @return Pair where left value is VisualProperty and right value
	 * is the value of that VisualProperty. VisualProperty corresponds to graphviz
	 * attribute
	 */
	@SuppressWarnings({ "rawtypes" })
	protected Pair<VisualProperty, Object> convertAttribute(String name, String val) {
		/**
		 * properties to Map:
		 * 
		 * shape
		 * fill color
		 * border color/transparency
		 * border line type
		 * border width
		 * size
		 * label
		 * label position
		 * tooltip
		 * label font/size/color
		 * 
		 * 
		 * Pair output = null;
		 * switch(name) {
		 *		"shape":
		 *			
		 * 
		 * }
		 * 
		 */
		
		VisualProperty retrievedProp = DOT_TO_CYTOSCAPE.get(name);
		Object retrievedVal = null;
		switch(name) {
			case "xlabel": {
				//Fall through to label case
			}
			case "label" : {
				retrievedVal = val;
				break;
			}
			case "penwidth": {
				retrievedVal = (Object)Double.parseDouble(val);
				break;
			}
			case "width": {
				//Fall through to height case
			}
			case "height": {
				retrievedVal = (Object)(Double.parseDouble(val) * 72.0);
				break;
			}
			case "shape": {
				retrievedVal = NODE_SHAPE_MAP.get(val);
				break;
			}
			case "fontname": {
				retrievedVal = (Object)Font.decode(val);
				break;
			}
			case "fontsize": {
				retrievedVal = (Object)Integer.parseInt(val);
				break;
			}
		}
		return Pair.of(retrievedProp, retrievedVal);

	}

	@Override
	protected void setStyle(String attrVal, VisualStyle vizStyle) {
		String[] styleAttrs = attrVal.split(",");
		for (String styleAttr : styleAttrs) {
			styleAttr = styleAttr.trim();
			LineType lineType = LINE_TYPE_MAP.get(styleAttr);
			if (lineType != null) {
				vizStyle.setDefaultValue(NODE_BORDER_LINE_TYPE, lineType);
			}
		}
	}

	@Override
	protected void setStyle(String attrVal,
			View<? extends CyIdentifiable> elementView) {
		String[] styleAttrs = attrVal.split(",");
		for (String styleAttr : styleAttrs) {
			styleAttr = styleAttr.trim();
			LineType lineType = LINE_TYPE_MAP.get(styleAttr);
			if (lineType != null) {
				elementView.setLockedValue(NODE_BORDER_LINE_TYPE, lineType);
			}
		}
		
	}

	@Override
	protected void setColor(String attrVal, VisualStyle vizStyle,
			ColorAttribute attr) {
		// TODO Auto-generated method stub
		Color color = convertColor(attrVal);
		Integer transparency = color.getAlpha();
		switch (attr) {
			case COLOR: {
				vizStyle.setDefaultValue(NODE_BORDER_PAINT, color);
				vizStyle.setDefaultValue(NODE_BORDER_TRANSPARENCY, transparency);
				break;
			}
			case FILLCOLOR: {
				vizStyle.setDefaultValue(NODE_FILL_COLOR, color);
				vizStyle.setDefaultValue(NODE_TRANSPARENCY, transparency);
				break;
			}
			case FONTCOLOR: {
				vizStyle.setDefaultValue(NODE_LABEL_COLOR, color);
				vizStyle.setDefaultValue(NODE_LABEL_TRANSPARENCY, transparency);
				break;
			}
		}
		
	}

	@Override
	protected void setColor(String attrVal,
			View<? extends CyIdentifiable> elementView, ColorAttribute attr) {
		Color color = convertColor(attrVal);
		Integer transparency = color.getAlpha();
		switch (attr) {
			case COLOR: {
				elementView.setLockedValue(NODE_BORDER_PAINT, color);
				elementView.setLockedValue(NODE_BORDER_TRANSPARENCY, transparency);
				break;
			}
			case FILLCOLOR: {
				elementView.setLockedValue(NODE_FILL_COLOR, color);
				elementView.setLockedValue(NODE_TRANSPARENCY, transparency);
				break;
			}
			case FONTCOLOR: {
				elementView.setLockedValue(NODE_LABEL_COLOR, color);
				elementView.setLockedValue(NODE_LABEL_TRANSPARENCY, transparency);
				break;
			}
		}
		
	}

}

