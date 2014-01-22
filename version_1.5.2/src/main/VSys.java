package main;

import processing.core.PApplet;
import processing.core.PGraphics;
import toxi.geom.ConvexPolygonClipper;
import toxi.geom.Circle;
import toxi.geom.Polygon2D;
import toxi.geom.PolygonClipper2D;
import toxi.geom.SutherlandHodgemanClipper;
import toxi.geom.Vec2D;
import toxi.geom.mesh2d.Voronoi;
import toxi.physics2d.VerletParticle2D;
import toxi.processing.ToxiclibsSupport;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static util.Color.*;

public class VSys {
	public boolean showVoronoi = true;
	public boolean showInfo;
	public boolean showBezier;
	public boolean showPolygons = true;
	public boolean showVerts;
	public boolean isUpdating;
	PolygonClipper2D clip, clip2;
	boolean doClip;
	private PolygonClipper2D clipper;
	private Voronoi voronoi;
	private ArrayList<VerletParticle2D> sites;
	private ArrayList<Polygon2D> cells;

	public VSys() {
		PSys psys = App.PSYS;
		this.sites = psys.getPhysics().particles;
		this.clipper = new SutherlandHodgemanClipper(psys.getBounds());
		this.voronoi = new Voronoi();
		this.cells = new ArrayList<>();
		clip = new ConvexPolygonClipper(new Circle(App.P5.height * 0.45f)
				                                .toPolygon2D(8).translate(new Vec2D(App.P5.width * 0.75f, App.P5.height / 2)));
		clip2 = new ConvexPolygonClipper(new Circle(App.P5.height * 0.45f)
				                                 .toPolygon2D(8).translate(new Vec2D(App.P5.width * 0.25f, App.P5.height / 2)));
	}
	public ArrayList<Polygon2D> getCells() { return cells; }
	public List<VerletParticle2D> getSites() { return sites; }
	public Voronoi getVoronoi() { return voronoi; }
	private void update() {
//		if (App.UPDATE_VORONOI) {
		voronoi = new Voronoi();
		voronoi.addPoints(sites);
//			voronoi.addPoints(App.PSYS.getPhysics().particles);
//		}
//		if (voronoi != null) {
		cells = new ArrayList<>();/* setCells(new ArrayList<Polygon2D>());*/
		HashMap<Polygon2D, Integer> cellmap = new HashMap<>();
		for (Polygon2D poly : voronoi.getRegions()) {
			poly = clipper.clipPolygon(poly);
			for (Vec2D v : this.sites) {
				if (poly.containsPoint(v)) { cells.add(poly); }
			}
		}
//		}
	}
	public void draw() {
		ToxiclibsSupport gfx = App.GFX;
		PGraphics pg = gfx.getGraphics();
		if (isUpdating) update();
		if (showVoronoi) {
			if (showBezier) {
				pg.noFill(); pg.stroke(VOR_CELLS);
				for (Polygon2D poly : cells) {
					List<Vec2D> vec = poly.vertices;
					int count = vec.size();
					pg.beginShape();
					pg.vertex((vec.get(count - 1).x + vec.get(0).x) / 2, (vec.get(count - 1).y + vec.get(0).y) / 2);
					for (int i = 0; i < count; i++) { pg.bezierVertex(vec.get(i).x, vec.get(i).y, vec.get(i).x, vec.get(i).y, (vec.get((i + 1) % count).x + vec.get(i).x) / 2, (vec.get((i + 1) % count).y + vec.get(i).y) / 2); }
					pg.endShape(PApplet.CLOSE);
				}
			}
			if (showPolygons) {
				pg.noFill(); pg.stroke(VOR_CELLS);
				for (Polygon2D poly : cells) { gfx.polygon2D(poly); }
			}
			if (showVerts) {
				pg.noFill(); pg.stroke(VOR_VERTS);
				for (Polygon2D poly : cells) {
					for (Vec2D vec : poly.vertices) { gfx.circle(vec, 2); }
				}
			}
			if (showInfo) {
				pg.fill(VOR_TXT);
				for (Polygon2D poly : cells) {
					pg.text(poly.getNumVertices() + "." + cells.indexOf(poly), poly.getCentroid().x, poly.getCentroid().y);
				}
			}
			pg.noStroke(); pg.noFill();

		}

	}

	void clipIt(ToxiclibsSupport gfx) {
		for (Polygon2D poly : voronoi.getRegions()) {
			if (doClip) {
				gfx.polygon2D(clip.clipPolygon(poly));
				gfx.polygon2D(clip2.clipPolygon(poly));
			} else {
				gfx.polygon2D(poly);
			}
		}
	}
}
//	HashMap<Vec2D, Integer> sitemap = new HashMap<>();
//	public void setVoronoi(Voronoi v) { voronoi = v; }
//	public void setCells(ArrayList<Polygon2D> cells) { this.cells = cells; }
//	public void setCellmap(HashMap<Polygon2D, Integer> cellmap) { this.cellmap = cellmap; }
//	public HashMap<Vec2D, Integer> getSitemap() { return sitemap; }
//	public PolygonClipper2D getClipper() { return clipper; }
//	public List<Vec2D> getCellSites() { return sites; }
//	public void addCell(Vec2D v) { getCellSites().add(v); }
//	public void addSite(Vec2D v, Integer i) {sitemap.put(v, i);}
//	public void setClipper(PolygonClipper2D clipper) { this.clipper = clipper; }
//	public void setCellSites(ArrayList<Vec2D> sites) { this.sites = sites; }
//	public void setSitemap(HashMap<Vec2D, Integer> sitemap) { this.sitemap = sitemap; }
//	public HashMap<Polygon2D, Integer> getCellmap() { return cellmap; }
