package org.cytoscape.intern;

import java.util.HashMap;

import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.presentation.property.NullVisualProperty;

public class TestNodeView implements View<CyNode> {
	private HashMap<VisualProperty<Object>, Object> visualProperties;
	private CyNode model;
	private Long SUID;
	
	@SuppressWarnings("unchecked")
	public TestNodeView(CyNode node) {
		model = node;
		SUID = model.getSUID();
		visualProperties = new HashMap<VisualProperty<Object>, Object>();
		BasicVisualLexicon bvl = new BasicVisualLexicon(new NullVisualProperty("ROOT", "Root"));
		for (VisualProperty<?> prop: bvl.getAllDescendants(BasicVisualLexicon.NODE)) {
			visualProperties.put((VisualProperty<Object>) prop, prop.getDefault());
		}
	}
	@Override
	public void clearValueLock(VisualProperty<?> arg0) {
	}

	@Override
	public void clearVisualProperties() {
	}

	@Override
	public CyNode getModel() {
		return model;
	}

	@Override
	public Long getSUID() {
		return SUID;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getVisualProperty(VisualProperty<T> arg0) {
		T returnVal = (T)visualProperties.get(arg0);
		return returnVal;
	}

	@Override
	public boolean isDirectlyLocked(VisualProperty<?> arg0) {
		return false;
	}

	@Override
	public boolean isSet(VisualProperty<?> arg0) {
		return false;
	}

	@Override
	public boolean isValueLocked(VisualProperty<?> arg0) {
		return false;
	}

	@Override
	public <T, V extends T> void setLockedValue(
			VisualProperty<? extends T> arg0, V arg1) {
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T, V extends T> void setVisualProperty(
			VisualProperty<? extends T> arg0, V arg1) {
		visualProperties.put((VisualProperty<Object>) arg0, arg1);
	}

}