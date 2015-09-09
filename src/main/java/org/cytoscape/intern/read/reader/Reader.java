package org.cytoscape.intern.read.reader;

import static org.cytoscape.view.presentation.property.LineTypeVisualProperty.DOT;
import static org.cytoscape.view.presentation.property.LineTypeVisualProperty.EQUAL_DASH;
import static org.cytoscape.view.presentation.property.LineTypeVisualProperty.SOLID;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.tuple.Pair;
import org.cytoscape.intern.FileHandlerManager;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.RenderingEngineManager;
import org.cytoscape.view.presentation.property.values.LineType;
import org.cytoscape.view.vizmap.VisualStyle;

import com.alexmerz.graphviz.objects.Edge;
import com.alexmerz.graphviz.objects.Node;

/**
 * Abstract class that contains definitions and some implementation for converting a
 * dot graph to a CyNetwork. Data is passed in as JPGD objects
 * 
 * @author Massoud Maher
 * @author Braxton Fitts
 * @author Ziran Zhang
 */
public abstract class Reader {
	
	//debug logger declaration 
	protected static final Logger LOGGER = Logger.getLogger("org.cytoscape.intern.read.Reader");
	protected static final FileHandlerManager FILE_HANDLER_MGR = FileHandlerManager.getManager();
		//Regex patterns for DOT color strings
	private static final String RGB_REGEX = "^#[0-9A-Fa-f]{6}$";
	private static final String RGBA_REGEX = "^#(?<RED>[0-9A-Fa-f]{2})"
						   + "(?<GREEN>[0-9A-Fa-f]{2})"
						   + "(?<BLUE>[0-9A-Fa-f]{2})"
						   + "(?<ALPHA>[0-9A-Fa-f]{2})$";
	private static final String HSB_REGEX = "^(?<HUE>1(?:\\.0+)?|0*(?:\\.[0-9]+))(?:,|\\s)+"
						 + "(?<SAT>1(?:\\.0+)?|0*(?:\\.[0-9]+))(?:,|\\s)+"
						 + "(?<VAL>1(?:\\.0+)?|0*(?:\\.[0-9]+))$";
	static {
		FileHandler handler = null;
		try {
			handler = new FileHandler("log_Reader.txt");
			handler.setLevel(Level.ALL);
			handler.setFormatter(new SimpleFormatter());
		}
		catch(IOException e) {
			// to prevent compiler error
		}
		LOGGER.addHandler(handler);
		FILE_HANDLER_MGR.registerFileHandler(handler);
	}

	// view of network being created/modified
	protected CyNetworkView networkView;

	// visualStyle being applied to network, used to set default values
	protected VisualStyle vizStyle;

	//True if "fillcolor" attribute has already been consumed for VisualStyle
	protected boolean usedDefaultFillColor = false;
	/*
	 * Map of explicitly defined default attributes
	 * key is attribute name, value is value
	 */
	protected Map<String, String> defaultAttrs;
	
	// VisualLexicon containing definitions of all VisualProperties
	// Used for compatibility with "Ding" specific VisualProperties
	protected VisualLexicon vizLexicon;
	
	// Maps lineStyle attribute values to Cytoscape values
	protected static final Map<String, LineType> LINE_TYPE_MAP = new HashMap<String, LineType>();
	static {
		LINE_TYPE_MAP.put("dotted", DOT);
		LINE_TYPE_MAP.put("dashed", EQUAL_DASH);
		LINE_TYPE_MAP.put("solid", SOLID);
	}
	
	protected static enum ColorAttribute {
		COLOR, FILLCOLOR, FONTCOLOR, BGCOLOR
	}	

	/*
	 * Contains elements of Cytoscape graph and their corresponding JPGD elements
	 * is null for NetworkReader. Is initialized on Node, Edge Reader
	 */
	protected Map<? extends Object, ? extends CyIdentifiable> elementMap;
	

	/**
	 * Constructs an object of type Reader. Sets up Logger.
	 * 
	 * @param networkView view of network we are creating/modifying
	 * @param vizStyle VisualStyle that we are applying to the network
	 * @param defaultAttrs Map that contains default attributes for Reader of this type
	 * eg. for NodeReader will be a list of default
	 * @param rendEngMgr TODO
	 */
	public Reader(CyNetworkView networkView, VisualStyle vizStyle, Map<String, String> defaultAttrs, RenderingEngineManager rendEngMgr) {

		// Make logger write to file

		this.networkView = networkView;
		this.vizStyle = vizStyle;
		this.defaultAttrs = defaultAttrs;
		this.vizLexicon = rendEngMgr.getDefaultVisualLexicon();
	}
	

	/**
	 * Sets all the default Visual Properties in Cytoscape for this type of reader
	 * eg. NetworkReader sets all network props, same for nodes
	 * Modifies CyNetworkView networkView, VisualStyle vizStyle etc. 
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void setDefaults() {
		LOGGER.info("Setting the Default values for Visual Style...");
		/*
		 * for each entry in defaultAttrs
		 * 		Pair p = convertAttribute(getKey(), getValue());
		 * 		VP = p.left()
		 * 		val = p.right()
		 * 		vizStyle.setDefaultValue( VP, val);
		 */
		for (Entry<String, String> attrEntry : defaultAttrs.entrySet()) {
			String attrKey = attrEntry.getKey();
			String attrVal = attrEntry.getValue();
			LOGGER.info(
				String.format("Converting DOT attribute: %s", attrKey)
			);

			/*
			 * label attribute may be a special case if label="\N".
			 * In dot, \N is an escape sequence that maps the node name
			 * to the node label. So setDefaults needs to run additional code
			 * which should be added to NodeReader to handle the creation of
			 * the mapping
			 */
			/*
			 * if attrKey is "label"

			 *   call handleLabelDefault()
			 *   Method will written differently for each subclass
			 *   NodeReader will create a passthrough mapping if label="\N"
			 *   Every other subclass will return immediately
			 */
			/*if (attrKey.equals("style")) {
				setStyle(attrVal, vizStyle);
				// this attribute has been handled, move on to next one
				continue;
			}
			if (attrKey.equals("color") || attrKey.equals("fillcolor")
					|| attrKey.equals("fontcolor") || attrKey.equals("bgcolor")) {
				switch (attrKey) {
					case "color": {
						setColor(attrVal, vizStyle, ColorAttribute.COLOR);
						break;
					}
					case "fillcolor": {
						setColor(attrVal, vizStyle, ColorAttribute.FILLCOLOR);
						usedDefaultFillColor = true;
						break;
					}
					case "fontcolor": {
						setColor(attrVal, vizStyle, ColorAttribute.FONTCOLOR);
						break;
					}
					case "bgcolor": {
						setColor(attrVal, vizStyle, ColorAttribute.BGCOLOR);
						break;
					}
				}
				// this attribute has been handled, move on to next one
				continue;
			}*/

			Pair<VisualProperty, Object> p = convertAttribute(attrKey, attrVal);
			// if attribute cannot be converted, move on to next one
			if (p == null) {
				continue;
			}

			// set in vizStyle
			VisualProperty vizProp = p.getLeft();
			Object val = p.getRight();
			if (vizProp == null || val == null) {
				continue;
			}
			LOGGER.info("Updating Visual Style...");
			LOGGER.info(String.format("Setting Visual Property %S...", vizProp));
			vizStyle.setDefaultValue(vizProp, val);
		}
		String styleAttribute = defaultAttrs.get("style");
		if (styleAttribute != null) {
			setStyle(styleAttribute, vizStyle);
		}
		setColorDefaults(vizStyle);
	}


	/**
	 * Sets all the default values of Color VisualProperties for the VisualStyle.
	 * Subclasses implement this method to handle the different CyIdentifiables
	 */
	abstract protected void setColorDefaults(VisualStyle vizStyle); 


	/**
	 * Sets all the bypass Visual Properties in Cytoscape for this type of reader
	 * eg. NetworkReader sets all network props, same for nodes
	 * Modifies CyNetworkView networkView, VisualStyle vizStyle etc. 
	 */
	abstract protected void setBypasses();
	
	/**
	 * Sets default VisualProperties and bypasses for each element in list.
	 * Children classes may override this method, with a super call, to handle
	 * exception properties such as location and edge weights
	 */
	public void setProperties() {
		LOGGER.info("Setting the properties for Visual Style...");
		setDefaults();
		setBypasses();
	}

	/**
	 * Returns the Map of bypass attributes for a given JPGD object.
	 * We define bypass attributes as any attributes declared at the individual
	 * GraphViz element declaration.
	 * 
	 * @param element Graph element that we are getting list of attributes for. Should be
	 * either a Node or an Edge, inputs of any other type will throw an IllegalArgumentException
	 * @return Map<String, String> Where key is attribute name and value is attribute value. Map
	 * contains all attributes in node declaration
	 */
	protected Map<String, String> getAttrMap(Object element) {
	
		 /*
		  * if element instance of Node
		  *		return (Map)((Node)element.getAttributes() )	
		  * if element instanceof Edge
		  * 	sameThing
		  * else
		  * 	throw IllegalArgException
		  */
		if (element instanceof Node) {
			return ((Node) element).getAttributes();
		}
		if (element instanceof Edge) {
			return ((Edge) element).getAttributes();
		}
		throw new IllegalArgumentException();
	}
	
	/**
	 * Takes in a dot color string and returns the equivalent Color object
	 * Color formats are:
	 * #RRGGBB (in hex)
	 * #RRGGBBAA (in hex)
	 * H S V (0 <= Hue, saturation, value <= 1.0)
	 * String that is name of color
	 *  
	 * @param color Color from dot file-- takes all color formats
	 */
	protected Color convertColor(String color) {
		/*
		 * Match string to #RRGGBB
		 * if (matched) then return new Color(RR, GG, BB)
		 * OR
		 * Match string to #RRGGBBAA
		 * if (matched) then return new Color(RR, GG, BB, AA)
		 * OR
		 * Split string by "," or " "
		 * Convert sub strings to Floats
		 * return Color.getHSBColor()
		 * OR
		 * return Color.getColor(String)
		 */
		LOGGER.info("Converting DOT color string to Java Color...");
		//Remove trailing/leading whitespace
		color = color.trim();

		//Regex patterns for DOT color strings
		// Test color string against RGB regex
		LOGGER.info(String.format("Color string: %s", color));
		LOGGER.info("Comparing DOT color string to #FFFFFF format");
		Matcher matcher = Pattern.compile(RGB_REGEX).matcher(color);
		if (matcher.matches()) {
			return Color.decode(color);
		}
		// Test color string against RGBA regex
		LOGGER.info("Comparing DOT color string to #FFFFFFFF format");
		matcher.usePattern(Pattern.compile(RGBA_REGEX));
		if (matcher.matches()) {
			Integer red = Integer.valueOf(matcher.group("RED"), 16);
			Integer green = Integer.valueOf(matcher.group("GREEN"), 16);
			Integer blue = Integer.valueOf(matcher.group("BLUE"), 16);
			Integer alpha = Integer.valueOf(matcher.group("ALPHA"), 16);
			return new Color(red, green, blue, alpha);
		}
		// Test color string against HSB regex
		LOGGER.info("Comparing DOT color string to H S V format");
		matcher.usePattern(Pattern.compile(HSB_REGEX));
		if (matcher.matches()) {
			Float hue = Float.valueOf(matcher.group("HUE"));
			Float saturation = Float.valueOf(matcher.group("SAT"));
			Float value = Float.valueOf(matcher.group("VAL"));
			return Color.getHSBColor(hue, saturation, value);
		}
		// Don't handle the different color schemes, just the default (X11)
		// Only handle a few colors from the color scheme
		// TODO
		/*
		 * Map color string to a Java Color
		 * return Java Color
		 */
		// If not in map then return a default color
		LOGGER.info("Color string isn't handled. Returning default color...");
		return Color.BLUE;
	}
	
	protected List<Pair<Color, Float>> convertColorList(String colorList) {
		LOGGER.info("Converting DOT color list to Java aray...");
		//Split color list into weighted colors
		if (!colorList.contains(":")) {
			return null;
		}
		String[] weightedColors = colorList.split(":");
		int numColors = 0;
		ArrayList<Pair<Color, Float>> colorAndWeightPairs = new ArrayList<Pair<Color,Float>>(weightedColors.length);
		for (String weightedColor : weightedColors) {
			if (numColors == 2) {
				break;
			}
			if (weightedColor.contains(";")) {
				String[] colorAndWeight = weightedColor.split(";", 2);
				Color color = convertColor(colorAndWeight[0]);
				Float weight = null;
				try {
					weight = Float.parseFloat(colorAndWeight[1]);
				}
				catch (NumberFormatException exception) {
					LOGGER.severe("Error: Color list contains invalid weight");
				}
				LOGGER.fine(String.format("Retrieved weighted color from color list. Result: %s;%f", color.toString(), weight));
				colorAndWeightPairs.add(Pair.of(color, weight));
			}
			else {
				Color color = convertColor(weightedColor);
				LOGGER.fine(String.format("Retrieved color with no weight from color list. Result: %s", color.toString()));
				colorAndWeightPairs.add(Pair.of(color, (Float)null));
			}
			numColors++;
		}
		return colorAndWeightPairs;
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
	@SuppressWarnings("rawtypes")
	protected abstract Pair<VisualProperty, Object> convertAttribute(String name, String val);

	/**
	 * Converts the "style" attribute from graphviz for default value of Cytoscape
	 * 
	 * @param attrVal String that is the value of "style" 
	 * eg. "dashed, rounded"
	 * @param vizStyle VisualStyle that "style" is being applied to
	 */
	abstract protected void setStyle(String attrVal, VisualStyle vizStyle);

	/**
	 * Converts the "style" attribute from graphviz for bypass value of Cytoscape
	 * 
	 * @param attrVal String that is the value of "style" 
	 * eg. "dashed, rounded"
	 * @param elementView View of element that "style" is being applied to eg. View<CyNode> 
	 */
	abstract protected void setStyle(String attrVal, View<? extends CyIdentifiable> elementView);

	/**
	 * Converts .dot color to Cytoscape default value
	 * 
	 * @param attrVal String that is value of color from dot file
	 * @param vizStyle VisualStyle that this color is being used in
	 * @param attr enum for type of color: COLOR, FILLCOLOR or FONTCOLOR 
	 */
	abstract protected void setColor(String attrVal, VisualStyle vizStyle, ColorAttribute attr);

	/**
	 * Converts .dot color to Cytoscape bypass value
	 * 
	 * @param attrVal String that is value of color from dot file
	 * @param elementView View of element that color is being applied to
	 * @param attr enum for type of color: COLOR, FILLCOLOR or FONTCOLOR 
	 */
	abstract protected void setColor(String attrVal, View<? extends CyIdentifiable> elementView, ColorAttribute attr);
}

















