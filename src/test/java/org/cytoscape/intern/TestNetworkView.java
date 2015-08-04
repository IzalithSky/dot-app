package org.cytoscape.intern;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;

import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualProperty;

import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.presentation.property.NullVisualProperty;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

public class TestNetworkView implements CyNetworkView {

	private HashMap<VisualProperty<Object>, Object> visualProperties;
	private ArrayList <View<CyEdge>> edgeViews;
	private ArrayList <View<CyNode>> nodeViews;
	private Long SUID;
	
	CyNetwork model;
	@SuppressWarnings("unchecked")
	public TestNetworkView(CyNetwork network) {
		edgeViews = new ArrayList<View<CyEdge>>();
		nodeViews = new ArrayList<View<CyNode>>();
		model = network;
		for (CyNode n : network.getNodeList()) {
			nodeViews.add(new TestNodeView(n));
		}
		for (CyEdge e : network.getEdgeList()) {
			edgeViews.add(new TestEdgeView(e));
		}
		SUID = network.getSUID();
		visualProperties = new HashMap<VisualProperty<Object>, Object>();
		BasicVisualLexicon bvl = new BasicVisualLexicon(new NullVisualProperty("ROOT", "Root"));
		for (VisualProperty<?> prop: bvl.getAllDescendants(BasicVisualLexicon.NETWORK)) {
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
	public CyNetwork getModel() {
		return model;
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

	@Override
	public Long getSUID() {
		return SUID;
	}

	@Override
	public void dispose() {
		
	}

	@Override
	public void fitContent() {
		
	}

	@Override
	public void fitSelected() {
		
	}

	@Override
	public Collection<View<? extends CyIdentifiable>> getAllViews() {
		int totalElements = nodeViews.size() + edgeViews.size();
		ArrayList<View<? extends CyIdentifiable>> allViews = new ArrayList<View<? extends CyIdentifiable>>(totalElements);
		allViews.addAll(nodeViews);
		allViews.addAll(edgeViews);
		return allViews;
	}

	@Override
	public View<CyEdge> getEdgeView(CyEdge arg0) {
		for (View<CyEdge> edgeView : edgeViews) {
			if (edgeView.getModel().equals(arg0)) {
				return edgeView;
			}
		}
		return null;
	}

	@Override
	public Collection<View<CyEdge>> getEdgeViews() {
		return edgeViews;
	}

	@Override
	public View<CyNode> getNodeView(CyNode arg0) {
		for (View<CyNode> nodeView : nodeViews) {
			if (nodeView.getModel().equals(arg0)) {
				return nodeView;
			}
		}
		return null;
	}

	@Override
	public Collection<View<CyNode>> getNodeViews() {
		return nodeViews;
	}

	@Override
	public <T, V extends T> void setViewDefault(
			VisualProperty<? extends T> arg0, V arg1) {
		
	}

	@Override
	public void updateView() {
		
	}
}