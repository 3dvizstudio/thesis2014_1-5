package main;

import processing.core.*;
import toxi.geom.Rect;
import toxi.geom.Vec2D;
import toxi.physics2d.VerletParticle2D;
import toxi.physics2d.VerletSpring2D;
import toxi.physics2d.behaviors.AttractionBehavior2D;
import toxi.processing.ToxiclibsSupport;
import util.Color;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.HashMap;

@XmlRootElement(name = "flowgraph")
@XmlAccessorType(XmlAccessType.FIELD)
public class XMLflowgraph {
	@XmlTransient
	protected static final ArrayList<Node> selectedNodes = new ArrayList<>();
	@XmlTransient
	public static boolean showNodes = true;
	@XmlTransient
	public static boolean showNodesCol;
	@XmlTransient
	public static boolean showRelations;
	@XmlTransient
	public static boolean showOutliner = true;
	public boolean isUpdating = true;
	@XmlTransient
	protected static HashMap<Integer, Node> nodeIndex = new HashMap<>();
	@XmlTransient
	protected static Node activeNode;
	@XmlElement(name = "node")
	protected ArrayList<Node> nodes = new ArrayList<>();
	@XmlElement(name = "rel")
	protected ArrayList<Relation> relations = new ArrayList<>();
	@XmlTransient
	protected HashMap<Integer, ArrayList<Node>> relationIndex = new HashMap<>();

	public static HashMap<Integer, Node> getNodeIndex() { return nodeIndex; }
	public void build() {
		App.PSYS.clearAll();
		for (Node n : nodes) {
			n.build();
			nodeIndex.put(n.id, n);
		} for (Relation r : relations) {
			ArrayList<Node> nlist = relationIndex.get(r.from);
			if (nlist == null) { nlist = new ArrayList<>(); relationIndex.put(r.from, nlist); }
			nlist.add(nodeIndex.get(r.to));
		} for (Node c : nodes) {
			App.PSYS.addParticle(c);
			if (c.isFrozen) {c.verlet.lock();}
		} for (Relation r : relations) {
			App.PSYS.addSpring(r);
		}
	}
	public void update() {
		for (Node n : nodes) {
			n.update();
		} for (Relation r : relations) {
			r.update();
		}
	}
	public void draw() {
		if (isUpdating) update();
		if (showRelations) { for (Relation r : relations) {r.draw();} }
		if (showNodes) { for (Node n : nodes) { n.draw(); } }
		if (showOutliner) { drawOutliner(); }
	}
	private void drawOutliner() {
		ToxiclibsSupport gfx = App.GFX;
		PGraphics pg = gfx.getGraphics();
		float totalSize = 0;
		pg.pushMatrix();
		pg.translate(App.P5.width - 120, 50);
		pg.fill(Color.BG_TEXT);
		pg.text("NAME", 10, -2);
		pg.textAlign(PApplet.RIGHT);
		pg.text("AREA", 100, -2);
		int stripe = 0;
		for (Node n : nodes) {
			totalSize += n.size;
			if (stripe % 2 == 0) { pg.fill(0xff383838); } else {pg.fill(0xff333333);}
			gfx.rect(Rect.fromCenterExtent(new Vec2D(53, 6), new Vec2D(50, 5)));
			pg.fill(Color.FACES);
			gfx.rect(Rect.fromCenterExtent(new Vec2D(0, 6), new Vec2D(3, 5)));
			pg.fill(n.color, 100, 100);
			gfx.rect(Rect.fromCenterExtent(new Vec2D(0, 5), new Vec2D(1, 4)));
			if (n == activeNode) pg.fill(Color.ACTIVE);
			else if (selectedNodes.contains(n)) pg.fill(Color.SELECTED);
			else pg.fill(Color.BG_TEXT);
			pg.translate(0, 10);
			pg.textAlign(PApplet.LEFT);
			pg.text(n.name, 10, 0);
			pg.textAlign(PApplet.RIGHT);
			pg.text(n.id, -10, 0);
			pg.text((int) n.size, 100, 0);
			stripe++;
		}
		pg.fill(Color.ACTIVE);
		pg.textAlign(PApplet.RIGHT);
		pg.text("Total Area", 100, 20);
		pg.text(App.DF3.format(totalSize) + " sq.m", 100, 30);
		pg.noFill();
		pg.popMatrix();
	}
	public void addNode(Node n) {nodes.add(n);}
	public void addRelation(Relation r) {relations.add(r);}
	public void clear() { nodes.clear(); relations.clear(); nodeIndex.clear(); relationIndex.clear(); }
	public final Node getNodeForID(int id) { return nodeIndex.get(id); }
	public final ArrayList<Node> getRelForID(int id) { return relationIndex.get(id); }
	public ArrayList<Node> getNodes() { return nodes; }
	public void setNodes(ArrayList<Node> nodes) {this.nodes = nodes;}
	public ArrayList<Relation> getRelations() { return relations; }
	public void setRelations(ArrayList<Relation> relations) {this.relations = relations;}

	@XmlRootElement(name = "node")
	@XmlAccessorType(XmlAccessType.FIELD)
	public static class Node {

		@XmlAttribute
		public String name;
		@XmlAttribute
		public int id;
		@XmlAttribute
		public float size;
		@XmlAttribute
		public float x;
		@XmlAttribute
		public float y;
		@XmlAttribute
		public int color;
		@XmlAttribute
		public int capacity;
		@XmlTransient
		public VerletParticle2D verlet;
		@XmlTransient
		public AttractionBehavior2D behavior;
		@XmlTransient
		boolean isActive;
		@XmlTransient
		boolean isFrozen;
		@XmlTransient
		boolean isSelected;
		@XmlAttribute
		public float radius = (float) Math.sqrt(size / Math.PI);
		@XmlTransient
		private Ntag tag = new Ntag(this);
		public void build() {
			verlet = new VerletParticle2D(x, y);
			radius = (float) ((Math.sqrt(size / Math.PI)) * App.PSYS.particleScale * App.world_scale) + App.PSYS.particlePadding;
			behavior = new AttractionBehavior2D(verlet, radius * App.PSYS.behaviorScale, -1.2f);
			update();
		}
		public void update() {
			radius = (float) ((Math.sqrt(size / Math.PI)) * App.PSYS.particleScale * App.world_scale) + App.PSYS.particlePadding;
			App.PSYS.updateParticle(this);
			synchronize();
		}
		public void synchronize() {
			this.x = verlet.x;
			this.y = verlet.y;
		}
		public void draw() {
			ToxiclibsSupport gfx = App.GFX;
			PGraphics pg = gfx.getGraphics();
			synchronize();
			if (showNodes) {
				if (isFrozen) {
					pg.stroke(Color.FROZEN); pg.fill(Color.FROZEN_FILL);
					pg.ellipse(x, y, (int) radius + 2, (int) radius + 2);
				} if (isActive) {
					tag.draw();
					pg.stroke(Color.ACTIVE); pg.fill(Color.ACTIVE_FILL);
				} else if (isSelected) {
					pg.stroke(Color.SELECTED); pg.fill(Color.SELECTED_FILL);
				} else { pg.stroke(Color.NORMAL); pg.fill(Color.NORMAL_FILL); }
				pg.ellipse(x, y, (int) radius, (int) radius);
				pg.stroke(Color.BLACK); pg.fill(Color.GREY);
				pg.ellipse(x, y, 3, 3);
			}
			if (showNodesCol) {
				pg.stroke(color, 100, 100); pg.fill(color, 100, 100);
				pg.ellipse(x, y, (int) radius, (int) radius);
			}
			pg.noFill();
			pg.noStroke();
		}
		public float getRadius() { return radius; }
		public void setName(String name) {this.name = name;update();}
		public void setSize(float size) { this.size = size; update(); }
		public void setColor(int color) {this.color = color;update();}
		public void setCapacity(int capacity) { this.capacity = capacity;update(); }
		public final String toString() {return Integer.toString(id);}
	}

	@XmlRootElement(name = "rel")
	@XmlAccessorType(XmlAccessType.FIELD)
	public static class Relation {
		@XmlAttribute
		public int from;
		@XmlAttribute
		public int to;

		public void setTo(int to) {this.to = to;}
		public void setFrom(int from) {this.from = from;}
		public void draw() {
			ToxiclibsSupport gfx = App.GFX;
			PGraphics pg = gfx.getGraphics();
			if (showRelations) {
				pg.noFill(); pg.stroke(0xff2b2b2b);
				gfx.line(getNodeIndex().get(from).verlet, getNodeIndex().get(to).verlet);
			}
		}
		public void update() {
			App.PSYS.updateSpring(this);
		}
		public VerletParticle2D a() {
			return nodeIndex.get(from).verlet;
		}
		public VerletParticle2D b() {
			return nodeIndex.get(to).verlet;
		}
		public float length() {
			float r1 = FSys.nodeIndex.get(from).radius;
			float r2 = FSys.nodeIndex.get(to).radius;
			return r1 + r2;
		}
	}
}
