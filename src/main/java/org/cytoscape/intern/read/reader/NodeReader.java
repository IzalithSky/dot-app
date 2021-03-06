package org.cytoscape.intern.read.reader;

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
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_X_LOCATION;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_Y_LOCATION;

import java.awt.Color;
import java.awt.Font;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.tuple.Pair;
import org.cytoscape.intern.GradientListener;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.RenderingEngineManager;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics2Factory;
import org.cytoscape.view.presentation.property.NodeShapeVisualProperty;
import org.cytoscape.view.presentation.property.values.LineType;
import org.cytoscape.view.presentation.property.values.NodeShape;
import org.cytoscape.view.vizmap.VisualStyle;

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

	
	// maps GraphViz node shapes with corresponding Cytoscape node shapes
	private static final Map<String, NodeShape> NODE_SHAPE_MAP = new HashMap<String, NodeShape>();
	static {
		NODE_SHAPE_MAP.put("triangle", NodeShapeVisualProperty.TRIANGLE);
		NODE_SHAPE_MAP.put("diamond", NodeShapeVisualProperty.DIAMOND);
		NODE_SHAPE_MAP.put("ellipse", NodeShapeVisualProperty.ELLIPSE);
		NODE_SHAPE_MAP.put("hexagon", NodeShapeVisualProperty.HEXAGON);
		NODE_SHAPE_MAP.put("octagon", NodeShapeVisualProperty.OCTAGON);
		NODE_SHAPE_MAP.put("parallelogram", NodeShapeVisualProperty.PARALLELOGRAM);
		NODE_SHAPE_MAP.put("rectangle", NodeShapeVisualProperty.RECTANGLE);     
	}
	
	/*
	 * maps GraphViz attributes with a single Cytoscape VisualProperty
	 * equivalent. Other GraphViz attributes are handled separately
	 */
	private static final Map<String, VisualProperty<?>> DOT_TO_CYTOSCAPE = new HashMap<String, VisualProperty<?>>(9);
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
	
	// true if "fillcolor" attribute has already been consumed for a node
	private boolean usedFillColor = false;
	
	//Used to retrieve CyCustomGraphics2Factories used for gradients;
	private GradientListener gradientListener;
	
	/**
	 * Constructs an object of type Reader.
	 * 
	 * 
	 * @param networkView view of network we are creating/modifying
	 * @param vizStyle VisualStyle that we are applying to the network
	 * @param defaultAttrs Map that contains default attributes for Reader of this type
	 * eg. for NodeReader will be a list of default
	 * @param rendEngMgr RenderingEngineManager that contains the default
	 * VisualLexicon needed for gradient support
	 * @param elementMap Map where keys are JPGD node objects and Values are corresponding Cytoscape CyNodes
	 * @param gradientListener ServiceListener used to get Gradient Factories
	 */
	public NodeReader(CyNetworkView networkView, VisualStyle vizStyle, Map<String, String> defaultAttrs, RenderingEngineManager rendEngMgr, Map<Node, CyNode> elementMap, GradientListener gradientListener) {
		super(networkView, vizStyle, defaultAttrs, rendEngMgr);
		this.elementMap = elementMap;
		this.gradientListener = gradientListener;
	}
	
	/**
	 * Converts an angle into a coordinate to be used by the Cytoscape Radial
	 * gradient factory
	 * @param angle the angle to convert
	 * @return a coordinate representing the angle
	 */
	private Point2D convertAngleToPoint(double angle) {
		double center = 0.5;
		if (angle == 0.0) {
			return new Point2D.Double(center, center);
		}
		Point2D.Double doublePoint;
		double x = (.5 * Math.cos(Math.toRadians(angle))) + center;
		double y = (-.5 * Math.sin(Math.toRadians(angle))) + center;
		doublePoint = new Point2D.Double(x, y);
		return doublePoint;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void createGradient(List<Pair<Color, Float>> colorListValues,
			View<CyNode> elementView, String styleAttribute, String gradientAngle) {
		LOGGER.trace("Creating gradient...");

		LOGGER.debug("Retrieving VisualProperty NODE_CUSTOMGRAPHICS_1");
		VisualProperty<CyCustomGraphics> nodeGradientProp = 
				(VisualProperty<CyCustomGraphics>) vizLexicon.lookup(CyNode.class, "NODE_CUSTOMGRAPHICS_1");

		if (nodeGradientProp == null) {
			LOGGER.warn("Current Renderer doesn't support CustomGraphics");
			return;
		}

		float start = 0;
		float remain = 1;
		boolean adjustStart = false;
		boolean usingLinearFactory = true;
		/*
		 * Determine which Gradient graphic factory to get based on style attribute
		 * if it contains "radial" get the radial factory
		 * otherwise get the linear
		 */

		LOGGER.trace("Retrieving Gradient factory...");
		CyCustomGraphics2Factory<?> factory = gradientListener.getLinearFactory();
		if (styleAttribute.contains("radial")) {
			factory = gradientListener.getRadialFactory();
			usingLinearFactory = false;
			LOGGER.trace("Retrieved Radial Gradient factory.");
		}
		List<Color> colors = new ArrayList<Color>(colorListValues.size());
		List<Float> weights = new ArrayList<Float>(colorListValues.size());

		for (Pair<Color, Float> colorAndWeightPair : colorListValues) {
			Color retrievedColor = colorAndWeightPair.getLeft();
			Float retrievedWeight = colorAndWeightPair.getRight();
			LOGGER.debug(
				String.format("Retrieved color %s with weight %f",
					retrievedColor, retrievedWeight)
			);
			colors.add(retrievedColor);
			if (retrievedWeight == null) {
				adjustStart = true;
				weights.add(new Float(start));
				continue;
			}
			if (adjustStart) {
				start = remain - retrievedWeight.floatValue();
				adjustStart = false;
			}
			weights.add(new Float(start));
			start = start + retrievedWeight.floatValue();
		}
		if (start == 0 && remain == 1) {
			weights = new ArrayList<Float>(colorListValues.size());
			LOGGER.debug(
				String.format("Each color will now take up %f of gradient", 
					1f/colorListValues.size())
			);
			for (; start < remain; start += (1f/colorListValues.size())) {

				weights.add(start);
			}
		}
		LOGGER.debug("Number of colors in gradient: " + colors.size());
		HashMap<String, Object> gradientProps = new HashMap();
		gradientProps.put("cy_gradientFractions", weights);
		gradientProps.put("cy_gradientColors", colors);
		if (usingLinearFactory) {
			gradientProps.put("cy_angle", Double.parseDouble(gradientAngle));
		}
		else {
			Point2D point = convertAngleToPoint(Double.parseDouble(gradientAngle));
			gradientProps.put("cy_center", point);
		}

		elementView.setLockedValue(nodeGradientProp, factory.getInstance(gradientProps));
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void createGradient(List<Pair<Color, Float>> colorListValues,
			VisualStyle vizStyle, String styleAttribute, String gradientAngle) {
		LOGGER.trace("Creating gradient...");

		LOGGER.debug("Retrieving VisualProperty NODE_CUSTOMGRAPHICS_1");
		VisualProperty<CyCustomGraphics> nodeGradientProp = 
				(VisualProperty<CyCustomGraphics>) vizLexicon.lookup(CyNode.class, "NODE_CUSTOMGRAPHICS_1");

		if (nodeGradientProp == null) {
			LOGGER.warn("Current Renderer doesn't support CustomGraphics");
			return;
		}

		float start = 0;
		float remain = 1;
		boolean adjustStart = false;
		boolean usingLinearFactory = true;
		/*
		 * Determine which Gradient graphic factory to get based on style attribute
		 * if it contains "radial" get the radial factory
		 * otherwise get the linear
		 */

		LOGGER.trace("Retrieving Gradient factory...");
		CyCustomGraphics2Factory<?> factory = gradientListener.getLinearFactory();
		if (styleAttribute.contains("radial")) {
			factory = gradientListener.getRadialFactory();
			usingLinearFactory = false;
			LOGGER.trace("Retrieved Radial Gradient factory.");
		}
		List<Color> colors = new ArrayList<Color>(colorListValues.size());
		List<Float> weights = new ArrayList<Float>(colorListValues.size());

		for (Pair<Color, Float> colorAndWeightPair : colorListValues) {
			Color retrievedColor = colorAndWeightPair.getLeft();
			Float retrievedWeight = colorAndWeightPair.getRight();
			LOGGER.debug(
				String.format("Retrieved color %s with weight %f",
					retrievedColor, retrievedWeight)
			);
			colors.add(retrievedColor);
			if (retrievedWeight == null) {
				adjustStart = true;
				weights.add(new Float(start));
				continue;
			}
			if (adjustStart) {
				start = remain - retrievedWeight.floatValue();
				adjustStart = false;
			}
			weights.add(new Float(start));
			start = start + retrievedWeight.floatValue();
		}
		if (start == 0 && remain == 1) {
			weights = new ArrayList<Float>(colorListValues.size());
			LOGGER.debug(
				String.format("Each color will now take up %f of gradient", 
					1f/colorListValues.size())
			);
			for (; start < remain; start += (1f/colorListValues.size())) {

				weights.add(start);
			}
		}
		LOGGER.debug("Number of colors in gradient: " + colors.size());
		HashMap<String, Object> gradientProps = new HashMap();
		gradientProps.put("cy_gradientFractions", weights);
		gradientProps.put("cy_gradientColors", colors);
		if (usingLinearFactory) {
			gradientProps.put("cy_angle", Double.parseDouble(gradientAngle));
		}
		else {
			Point2D point = convertAngleToPoint(Double.parseDouble(gradientAngle));
			gradientProps.put("cy_center", point);
		}

		vizStyle.setDefaultValue(nodeGradientProp, factory.getInstance(gradientProps));
	}


	/**
	 * Sets VisualProperties for each node related to location of node.
	 * Here because cannot return 2 VisualProperties from convertAttribute
	 * and want to make exception clear
	 * @param attrVal 
	 * @param elementView 
	 */
	private void setPositions(String attrVal, View<CyNode> elementView) {
		String[] coords = attrVal.split(",");
		Double x = Double.parseDouble(coords[0]);
		
		//Y coordinate is different between GraphViz and Java.
		Double y = -1 * Double.parseDouble(coords[1]);

		//Position attributes are not set with bypasses.
		elementView.setVisualProperty(NODE_X_LOCATION, x);
		elementView.setVisualProperty(NODE_Y_LOCATION, y);
	}

	/**
	 * Converts the specified GraphViz attribute and value to its Cytoscape 
	 * equivalent VisualProperty and VisualPropertyValue. If an equivalent value
	 * is not found, then a default Cytoscape VisualPropertyValue is used.
	 * This method only handles GraphViz attributes that do not correspond to
	 * more than one Cytoscape VisualProperty.
	 * 
	 * @param name the name of the attribute
	 * @param val the value of the attribute
	 * 
	 * @return Pair object of which the left value is the VisualProperty and the right value
	 * is the VisualPropertyValue.
	 */
	@Override
	@SuppressWarnings({ "rawtypes" })
	protected Pair<VisualProperty, Object> convertAttribute(String name, String val) {
		LOGGER.debug(
			String.format("Converting GraphViz attribute %s with value %s", name, val)
		);
		VisualProperty retrievedProp = DOT_TO_CYTOSCAPE.get(name);
		Object retrievedVal = null;
		switch(name) {
			case "xlabel": {
				// Fall through to label case
			}
			case "label" : {
				retrievedVal = val;
				break;
			}
			case "penwidth": {
				retrievedVal = Double.parseDouble(val);
				break;
			}
			case "width": {
				//Fall through to height case
			}
			case "height": {
				retrievedVal = Double.parseDouble(val) * 72.0;
				break;
			}
			case "shape": {
				/* 
				 * Loop through and use contains because
				 * Graphviz has things like Mdiamond which is a diamond
				 * with lines through it, this just becomes diamond
				 */
				for (String key: NODE_SHAPE_MAP.keySet()) {
					if(val.contains(key)) {
						retrievedVal = NODE_SHAPE_MAP.get(key);
					}
				}
				break;
			}
			case "fontname": {
				retrievedVal = Font.decode(val);
				break;
			}
			case "fontsize": {
				retrievedVal = Integer.parseInt(val);
				break;
			}
		}
		return Pair.of(retrievedProp, retrievedVal);

	}
	
	/**
	 * Sets all the bypass Visual Properties values for Cytoscape View objects
	 * corresponding to CyNode objects in the elementMap
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	protected void setBypasses() {
		LOGGER.info("Setting the Bypass values for node views...");

		// for each element, get bypass attributes
		for (Entry<? extends Object, ? extends CyIdentifiable> entry : elementMap.entrySet()) {
			Map<String, String> bypassAttrs = getAttrMap(entry.getKey()); 
			String colorScheme = bypassAttrs.get("colorscheme");
			
			//Get the node view
			CyNode element = (CyNode)entry.getValue();
			View<CyNode> elementView = networkView.getNodeView(element);

			//reset the usedFillColor boolean for each node
			usedFillColor = false;

			//reference variables for attribute handling
			String styleAttribute = null;
			String colorAttribute = null;
			String fillAttribute = null;
			String gradientAngle = null;

			for (Entry<String, String> attrEntry : bypassAttrs.entrySet()) {
				String attrKey = attrEntry.getKey();
				String attrVal = attrEntry.getValue();
				LOGGER.debug(
					String.format("Converting GraphViz attribute: %s", attrKey)
				);

				
				//These attributes require special handling
				if (attrKey.equals("style")) {
					styleAttribute = attrVal;
					continue;
				}
				if (attrKey.equals("pos")) {
					setPositions(attrVal, elementView);
					continue;
				}
				if (attrKey.equals("color")) {
					colorAttribute = attrVal;
					continue;
				}
				if (attrKey.equals("fillcolor")) {
					fillAttribute = attrVal;
					continue;
				}
				if (attrKey.equals("fontcolor")) {
					setColor(attrVal, elementView, ColorAttribute.FONTCOLOR, colorScheme);
					continue;
				}
				if (attrKey.equals("gradientangle")) {
					gradientAngle = attrVal;
					continue;
				}

				// handle simple attributes
				Pair<VisualProperty, Object> p = convertAttribute(attrEntry.getKey(), attrEntry.getValue());
				if (p == null) {
					continue;
				}

				VisualProperty vizProp = p.getLeft();
				Object val = p.getRight();
				if (vizProp == null || val == null) {
					continue;
				}
				LOGGER.trace("Updating Visual Style...");
				LOGGER.debug(String.format("Setting Visual Property %s...", vizProp));
				elementView.setLockedValue(vizProp, val);
			}
			
			//Handle gradient creation and color setting now

			LOGGER.trace("Handle style and node color attributes");
			//Attempt to get the default gradient angle
			String defaultGradientAngle = defaultAttrs.get("gradientangle");
			if (defaultGradientAngle == null) {
				defaultGradientAngle = "0";
			}
			
			//Assume that the "color" and "fillcolor" attributes are from
			//the element
			boolean isBypassColorAttr = true;
			boolean isBypassFillAttr = true;

			//If a value was not found for "color" use the value from default list
			if (colorAttribute == null) {
				isBypassColorAttr = false;
				colorAttribute = defaultAttrs.get("color");
			}
			
			//If a value was not found for "fillcolor" use default list value
			if (fillAttribute == null) {
				isBypassFillAttr = false;
				fillAttribute = defaultAttrs.get("fillcolor");
			}
			
			if (styleAttribute != null) {
				setStyle(styleAttribute, elementView);
			}

			//handle "fillcolor" attribute first since "color" can replace it
			//if not found
			if (fillAttribute != null) {
				usedFillColor = true;
				List<Pair<Color, Float>> colorListValues = convertColorList(fillAttribute, colorScheme);
				if (colorListValues != null) {
					if (gradientAngle == null) {
						//default gradient angle is 0
						gradientAngle = "0";
					}
					/* A gradient needs to be applied if the color is from the
					 * default list but the gradient style is different or angle
					 * is different
					 */
					if (styleAttribute != null && 
						(!styleAttribute.equals(defaultAttrs.get("style")) ||
							!gradientAngle.equals(defaultGradientAngle))) {
						createGradient(colorListValues, elementView, styleAttribute, gradientAngle);
					}
				}
				else {
					if (isBypassFillAttr) {
						setColor(fillAttribute, elementView, ColorAttribute.FILLCOLOR, colorScheme);
					}
				}
			}
			if (colorAttribute != null) {
				List<Pair<Color, Float>> colorListValues = convertColorList(colorAttribute, colorScheme);
				if (colorListValues != null) {
					Color color = colorListValues.get(0).getLeft();
					colorAttribute = String.format("#%2x%2x%2x%2x", color.getRed(), color.getGreen(),
						color.getBlue(), color.getAlpha());
					if (gradientAngle == null) {
						gradientAngle = "0";
					}
					/* A gradient needs to be applied if the color is from the
					 * default list but the gradient style is different or angle
					 * is different
					 */
					if (styleAttribute != null && 
						(!styleAttribute.equals(defaultAttrs.get("style")) ||
							!gradientAngle.equals(defaultGradientAngle))) {
						createGradient(colorListValues, elementView, styleAttribute, gradientAngle);
					}
				}
				if (isBypassColorAttr) {
					setColor(colorAttribute, elementView, ColorAttribute.COLOR, colorScheme);
				}
			}
		}
	}

	/**
	 * Converts a GraphViz color attribute into corresponding VisualProperty
	 * bypass values for a Cytoscape View object
	 * 
	 * @param attrVal GraphViz color string
	 * @param elementView View of Cytoscape element to which a color 
	 * VisualProperty is being set
	 * @param attr enum for type of color: COLOR, FILLCOLOR, FONTCOLOR, BGCOLOR
	 * @param colorScheme Scheme from dot. Either "x11" or "svg"
	 */
	@Override
	protected void setColor(String attrVal,
			View<? extends CyIdentifiable> elementView, ColorAttribute attr, String colorScheme) {

		Color color = convertColor(attrVal, colorScheme);
		List<Pair<Color, Float>> colorListValues = convertColorList(attrVal, colorScheme);
		if (colorListValues != null) {
			color = colorListValues.get(0).getLeft();
		}
		Integer transparency = color.getAlpha();
		

		switch (attr) {
			case COLOR: {
				elementView.setLockedValue(NODE_BORDER_PAINT, color);
				elementView.setLockedValue(NODE_BORDER_TRANSPARENCY, transparency);

				//fillcolor has already been applied, should not redo
				//with color attribute
				if (usedFillColor) {
					break;
				}

			/*
			 * color attribute used for NODE_FILL_COLOR if
			 * fillcolor attribute not present, thus fall through
			 * to fillcolor case
			 */
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
			default: {
				break;
			}
		}
	}

	/**
	 * Converts a GraphViz color attribute into the corresponding default
	 * VisualProperty values for a Cytoscape VisualStyle
	 * 
	 * @param attrVal GraphViz color string
	 * @param vizStyle VisualStyle that this color is being used in
	 * @param attr enum for type of color: COLOR, FILLCOLOR or FONTCOLOR 
	 * @param colorScheme Scheme from dot. Either "x11" or "svg"
	 */
	@Override
	protected void setColor(String attrVal, VisualStyle vizStyle,
			ColorAttribute attr, String colorScheme) {

		Color color = convertColor(attrVal, colorScheme);
		List<Pair<Color, Float>> colorListValues = convertColorList(attrVal, colorScheme);
		if (colorListValues != null) {
			color = colorListValues.get(0).getLeft();
		}
		Integer transparency = color.getAlpha();

		switch (attr) {
			case COLOR: {
				LOGGER.trace("Setting default values for NODE_BORDER_PAINT and"
						+ " NODE_BORDER_TRANSPARENCY");
				vizStyle.setDefaultValue(NODE_BORDER_PAINT, color);
				vizStyle.setDefaultValue(NODE_BORDER_TRANSPARENCY, transparency);
				if (usedDefaultFillColor) {
					LOGGER.trace("Setting only NODE_BORDER_PAINT and"
							+ " NODE_BORDER_TRANSPARENCY");
					//default fillcolor has already been applied, should not redo
					//with color attribute
					break;
				}
				//color attribute used for NODE_FILL_COLOR if
				//fillcolor not present
			}
			case FILLCOLOR: {
				LOGGER.trace("Setting default values for NODE_FILL_COLOR and"
						+ " NODE_TRANSPARENCY");
				vizStyle.setDefaultValue(NODE_FILL_COLOR, color);
				vizStyle.setDefaultValue(NODE_TRANSPARENCY, transparency);
				break;
			}
			case FONTCOLOR: {
				LOGGER.trace("Setting default values for NODE_LABEL_FONT_COLOR and"
						+ " NODE_LABEL_TRANSPARENCY");
				vizStyle.setDefaultValue(NODE_LABEL_COLOR, color);
				vizStyle.setDefaultValue(NODE_LABEL_TRANSPARENCY, transparency);
				break;
			}
			default: {
				break;
			}
		}
		
	}

	@Override
	protected void setColorDefaults(VisualStyle vizStyle, String colorScheme) {
		LOGGER.info("Setting node property default values for color attributes");
		String fillAttribute = defaultAttrs.get("fillcolor");
		String colorAttribute = defaultAttrs.get("color");
		String gradientAngle = defaultAttrs.get("gradientangle");
		String styleAttribute = defaultAttrs.get("style");
		if (fillAttribute != null) {
			usedDefaultFillColor = true;
			List<Pair<Color, Float>> colorListValues = convertColorList(fillAttribute, colorScheme);
			if (colorListValues != null) {
				if (gradientAngle == null) {
					gradientAngle = "0";
				}
				createGradient(colorListValues, vizStyle, styleAttribute, gradientAngle);
			}
			else {
				setColor(fillAttribute, vizStyle, ColorAttribute.FILLCOLOR, colorScheme);
			}
		}
		if (colorAttribute != null) {
			List<Pair<Color, Float>> colorListValues = convertColorList(colorAttribute, colorScheme);
			if (colorListValues != null) {
				Color color = colorListValues.get(0).getLeft();
				colorAttribute = String.format("#%2x%2x%2x%2x", color.getRed(), color.getGreen(),
						color.getBlue(), color.getAlpha());
				if (gradientAngle == null) {
					gradientAngle = "0";
				}
				if (!usedDefaultFillColor) {
					createGradient(colorListValues, vizStyle, styleAttribute, gradientAngle);
				}
			}
			setColor(colorAttribute, vizStyle, ColorAttribute.COLOR, colorScheme);
		}
	}


	/**
	 * Converts the GraphViz "style" attribute into VisualProperty bypass values
	 * for a Cytoscape View object
	 * 
	 * @param attrVal String that is the value of "style" (eg. "dashed, round")
	 * @param elementView view to which "style" is being applied
	 */
	@Override
	protected void setStyle(String attrVal,
			View<? extends CyIdentifiable> elementView) {

		String[] styleAttrs = attrVal.split(",");

		// Get default node visibility
		boolean isVisibleDefault = vizStyle.getDefaultValue(NODE_VISIBLE);
		// Get default node border line type
		LineType defaultLineType = vizStyle.getDefaultValue(NODE_BORDER_LINE_TYPE);

		for (String styleAttr : styleAttrs) {
			styleAttr = styleAttr.trim();

			LineType lineType = LINE_TYPE_MAP.get(styleAttr);
			if (lineType != null && !lineType.equals(defaultLineType)) {
				elementView.setLockedValue(NODE_BORDER_LINE_TYPE, lineType);
			}
		}
		
		// check if rounded rectangle and set
		NodeShape elementShape = elementView.getVisualProperty(NODE_SHAPE);
		NodeShape defaultShape = vizStyle.getDefaultValue(NODE_SHAPE);
		if (attrVal.contains("rounded") && 
				elementShape.equals(NodeShapeVisualProperty.RECTANGLE)) {
			if (!elementShape.equals(defaultShape)) {
				elementView.setLockedValue(NODE_SHAPE, NodeShapeVisualProperty.ROUND_RECTANGLE);
			}
		}
		// check if invisible is enabled
		if( attrVal.contains("invis") ) {
			if (isVisibleDefault) {
				elementView.setLockedValue(NODE_VISIBLE, false);
			}
		}
		else {
			if (!isVisibleDefault) {
				elementView.setLockedValue(NODE_VISIBLE, true);
			}
		}
		// if node is not filled
		if(!attrVal.contains("filled")) {
			elementView.setLockedValue(NODE_TRANSPARENCY, 0);
		}
	}

	/**
	 * Converts the GraphViz "style" attribute into default VisualProperty
	 * values for a Cytoscape VisualStyle
	 * 
	 * @param attrVal String that is the value of "style" 
	 * eg. "dashed, rounded"
	 * @param vizStyle VisualStyle that "style" is being applied to
	 */
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
		
		// check if rounded rectangle and set
		if( attrVal.contains("rounded") && 
				(vizStyle.getDefaultValue(NODE_SHAPE)).equals(NodeShapeVisualProperty.RECTANGLE) ) {
				
			vizStyle.setDefaultValue(NODE_SHAPE, NodeShapeVisualProperty.ROUND_RECTANGLE);
			
		}
		// check if invisible is enabled
		if( attrVal.contains("invis") ) {
			vizStyle.setDefaultValue(NODE_VISIBLE, false);
		}
		// if node is not filled
		if(!attrVal.contains("filled")) {
			vizStyle.setDefaultValue(NODE_TRANSPARENCY, 0);
		}
	}
}

