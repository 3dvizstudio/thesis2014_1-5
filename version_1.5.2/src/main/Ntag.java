package main;

import processing.core.PApplet;
import processing.core.PGraphics;
import toxi.geom.Circle;
import toxi.geom.Rect;
import toxi.geom.Vec2D;
import toxi.processing.ToxiclibsSupport;

public class Ntag {
	private XMLflowgraph.Node node;
	private Vec2D tagExtent = new Vec2D(55, 8);
	private Vec2D idExtent = new Vec2D(8, 8);
	public Ntag(XMLflowgraph.Node node){
		this.node = node;


	}
	public void draw() {
		Vec2D tagCenter = new Vec2D(285,node.y);
		Vec2D idCenter = new Vec2D(350, node.y);
		ToxiclibsSupport gfx = App.GFX;
		PGraphics pg = gfx.getGraphics();
		pg.stroke(0xff444444);
		pg.line(350, (int) node.y, (int) node.x, (int) node.y);
		pg.stroke(0, 0, 50);
		pg.fill(node.color, 100, 100);
		gfx.rect(Rect.fromCenterExtent(idCenter, idExtent));
		pg.fill(0, 0, 30);
		gfx.rect(Rect.fromCenterExtent(tagCenter,tagExtent));
		pg.fill(0, 0, 90);
		pg.textAlign(PApplet.CENTER);
		pg.text(node.id, 350, (int) node.y + 5);
		pg.text(node.name, 285, (int) node.y + 5);
		pg.noFill();
	}

}
