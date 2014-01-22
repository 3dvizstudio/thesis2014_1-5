package util;

import main.XMLflowgraph;

import java.util.ArrayList;

public class RGen {
	private ArrayList<XMLflowgraph.Node> nodes = new ArrayList<>();
	private ArrayList<XMLflowgraph.Relation> relations = new ArrayList<>();
	private int count;
	private float size;
	private int index;
	private XMLflowgraph.Node parentNode;

	public RGen(XMLflowgraph.Node parentNode, int index, int count, float size, boolean isDivide) {
		this.parentNode = parentNode;
		this.index = index;
		this.count = count;
		if (isDivide) {
			this.size = parentNode.size / count;
			parentNode.size = this.size;
		} else { this.size = size; }
		build();
	}
	private void build() {
		for (int i = 0; i < count; i++) {
			XMLflowgraph.Node n = new XMLflowgraph.Node();
			n.id = index++;
			n.name = parentNode.name + "." + n.id;
			n.size = size;
			n.color = parentNode.color;
			n.x = parentNode.x;
			n.y = parentNode.y;
			n.build();
			nodes.add(n);

			XMLflowgraph.Relation r = new XMLflowgraph.Relation();
			r.setFrom(n.id);
			r.setTo(parentNode.id);
			relations.add(r);
		}
	}

	public ArrayList<XMLflowgraph.Node> getNodes() {return nodes;}
	public ArrayList<XMLflowgraph.Relation> getRelations() { return relations; }
}
