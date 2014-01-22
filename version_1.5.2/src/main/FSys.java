package main;

import processing.core.PGraphics;
import toxi.geom.Circle;
import toxi.geom.Rect;
import toxi.geom.Vec2D;
import toxi.processing.ToxiclibsSupport;
import util.RGen;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name = "flowgraph")
@XmlAccessorType(XmlAccessType.FIELD)
public class FSys extends XMLflowgraph {

	public FSys() { }
	public void createRelation() {
		if (getSelectedNodes().size() >= 2) {
			Node na = getSelectedNodes().get(0);
			Node nb = getSelectedNodes().get(1);
			Relation r = new Relation();
			r.setFrom(na.id);
			r.setTo(nb.id);
			addRelation(r);
			build();
			clearSelection();
			getSelectedNodes().add(na);
			getSelectedNodes().add(nb);
		}
	}
	public void createNode(Vec2D pos) {
		Node n = new Node();
		n.id = nodes.size();
		n.name = App.nameTextfield.getStringValue();
		n.size = App.radiusSlider.getValue();
		n.color = (int) App.colorSlider.getValue();
		n.capacity = (int) App.capacitySlider.getValue();
		n.x = pos.x;
		n.y = pos.y;
		addNode(n);
		build();
		selectNodeNearPosition(pos);
	}
	public void divideNode() {
		if (hasActiveNode()) {
			RGen rgen = new RGen(activeNode, nodes.size(), 3, 50, true);
			nodes.addAll(rgen.getNodes()); relations.addAll(rgen.getRelations());
		}
	}
	public void multiplyNode() {
		if (hasActiveNode()) {
			RGen rgen = new RGen(activeNode, nodes.size(), 3, 50, false);
			nodes.addAll(rgen.getNodes()); relations.addAll(rgen.getRelations());
		}
	}
	public void freezeNode() {
		for (XMLflowgraph.Node n : getSelectedNodes()) {
			if (n.isFrozen) {
				n.isFrozen = false; n.verlet.unlock();
			} else {
				n.isFrozen = true; n.verlet.lock();
			}
		}
	}
	public void selectNodeNearPosition(Vec2D mousePos) {
		deselectNode();
		for (Node n : nodes) {
			Circle c = new Circle(n.verlet, 20);
			if (c.containsPoint(mousePos)) {
				setActiveNode(n);
				break;
			} else if (!App.isShiftDown) {clearSelection();}
		}
	}

	public void moveActiveNode(Vec2D mousePos) {
		if (activeNode != null) {
			activeNode.verlet.set(mousePos);
			activeNode.synchronize();
		}
	}
	public void deselectNode() {
		if (hasActiveNode()) {
			if (!activeNode.isFrozen) activeNode.verlet.unlock();
			activeNode.isActive = false;
			activeNode = null;
		}
	}
	public void clearSelection() {
		for (Node n : nodes) { n.isSelected = false; }
		selectedNodes.clear();
	}
	public boolean hasActiveNode() { return activeNode != null; }
	public Node getActiveNode() { return activeNode; }
	public void setActiveNode(Node a) {
		activeNode = a;
		if (!App.isShiftDown) clearSelection();
		a.synchronize();
		a.verlet.lock();
		a.isActive = true;
		a.isSelected = true;
		selectedNodes.add(a);
	}
	public List<Node> getSelectedNodes() { return selectedNodes; }
}
